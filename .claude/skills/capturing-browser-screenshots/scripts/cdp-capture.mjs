#!/usr/bin/env node
/**
 * Declarative screenshot driver for a real Chrome over the DevTools Protocol.
 *
 *   node cdp-capture.mjs --steps steps.json --out ./shots [--port 9223] [--launch]
 *
 * No npm dependencies: uses Node's global fetch and WebSocket (Node >= 22).
 * See SKILL.md for the steps reference.
 */
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawn } from "node:child_process";

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const log = (...parts) => process.stdout.write(parts.join(" ") + "\n");

function parseArgs(argv) {
  const out = {};
  for (let i = 0; i < argv.length; i += 1) {
    const token = argv[i];
    if (!token.startsWith("--")) continue;
    const key = token.slice(2);
    const next = argv[i + 1];
    if (next === undefined || next.startsWith("--")) {
      out[key] = true;
    } else {
      out[key] = next;
      i += 1;
    }
  }
  return out;
}

const args = parseArgs(process.argv.slice(2));
const port = Number(args.port || process.env.CDP_PORT || 9223);
const stepsFile = args.steps;
if (!stepsFile || stepsFile === true) {
  console.error("usage: node cdp-capture.mjs --steps <steps.json> --out <dir> [--port 9223] [--launch] [--headed] [--keep-open]");
  process.exit(2);
}

/** Replace {{ENV:NAME}} with environment variables; missing vars are an error. */
function expandEnv(value) {
  if (typeof value === "string") {
    return value.replace(/\{\{ENV:([A-Za-z_][A-Za-z0-9_]*)\}\}/g, (_, name) => {
      const v = process.env[name];
      if (v === undefined) throw new Error(`Environment variable ${name} is not set`);
      return v;
    });
  }
  if (Array.isArray(value)) return value.map(expandEnv);
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, expandEnv(v)]));
  }
  return value;
}

let config;
try {
  config = expandEnv(JSON.parse(fs.readFileSync(stepsFile, "utf8")));
} catch (error) {
  console.error(`error: could not load steps file ${stepsFile}: ${error.message}`);
  process.exit(2);
}
const steps = config.steps || [];
const outDir = path.resolve(expandEnv(args.out || config.out || "shots"));
fs.mkdirSync(outDir, { recursive: true });

async function cdpReachable() {
  try {
    const res = await fetch(`http://127.0.0.1:${port}/json/version`, { signal: AbortSignal.timeout(1500) });
    return res.ok;
  } catch {
    return false;
  }
}

function findChrome() {
  const candidates = [
    process.env.CHROME_PATH,
    "C:/Program Files/Google/Chrome/Application/chrome.exe",
    "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe",
    path.join(process.env.LOCALAPPDATA || "", "Google/Chrome/Application/chrome.exe"),
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    "/usr/bin/google-chrome",
    "/usr/bin/chromium",
    "/usr/bin/chromium-browser"
  ].filter(Boolean);
  return candidates.find((p) => fs.existsSync(p));
}

async function ensureChrome() {
  if (await cdpReachable()) return;
  if (!args.launch) {
    throw new Error(
      `Chrome CDP is not reachable on port ${port}. Start Chrome with --remote-debugging-port=${port} (and a separate --user-data-dir), or rerun with --launch.`
    );
  }
  const exe = typeof args["chrome-path"] === "string" ? args["chrome-path"] : findChrome();
  if (!exe) throw new Error("Chrome executable not found; pass --chrome-path <path>");
  const profile = typeof args.profile === "string" ? args.profile : path.join(os.tmpdir(), "cdp-capture-profile");
  const width = Number(config.viewport?.width || 1440);
  const height = Number(config.viewport?.height || 900);
  const flags = [
    `--remote-debugging-port=${port}`,
    `--user-data-dir=${profile}`,
    "--no-first-run",
    "--no-default-browser-check",
    "--disable-extensions",
    "--disable-background-timer-throttling",
    "--disable-renderer-backgrounding",
    `--window-size=${width},${height}`,
    "about:blank"
  ];
  if (!args.headed) flags.unshift("--headless=new");
  const child = spawn(exe, flags, { detached: true, stdio: "ignore" });
  child.unref();
  for (let i = 0; i < 60; i += 1) {
    await sleep(250);
    if (await cdpReachable()) return;
  }
  throw new Error("Chrome was launched but did not expose CDP; check the executable and profile path.");
}

class Cdp {
  constructor(ws) {
    this.ws = ws;
    this.nextId = 0;
    this.pending = new Map();
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (message.method === "Runtime.exceptionThrown") {
        const details = message.params?.exceptionDetails;
        const text = details?.exception?.description || details?.text || "unknown page exception";
        log("[page exception]", String(text).split("\n")[0]);
      }
      if (message.id && this.pending.has(message.id)) {
        const { resolve, reject } = this.pending.get(message.id);
        this.pending.delete(message.id);
        if (message.error) reject(new Error(JSON.stringify(message.error)));
        else resolve(message.result);
      }
    };
  }

  send(method, params = {}, timeout = 30000) {
    return Promise.race([
      new Promise((resolve, reject) => {
        const id = ++this.nextId;
        this.pending.set(id, { resolve, reject });
        this.ws.send(JSON.stringify({ id, method, params }));
      }),
      new Promise((_, reject) => setTimeout(() => reject(new Error(`${method} timed out after ${timeout}ms`)), timeout))
    ]);
  }

  async evaluate(expression, awaitPromise = true) {
    const result = await this.send("Runtime.evaluate", {
      expression,
      awaitPromise,
      returnByValue: true,
      userGesture: true
    });
    if (result.exceptionDetails) {
      const details = result.exceptionDetails;
      throw new Error(details.exception?.description || details.text || "evaluate failed");
    }
    return result.result.value;
  }
}

async function openTarget() {
  let res = await fetch(`http://127.0.0.1:${port}/json/new?about:blank`, { method: "PUT" });
  if (!res.ok) res = await fetch(`http://127.0.0.1:${port}/json/new?about:blank`);
  if (!res.ok) throw new Error(`Could not open a new Chrome tab (HTTP ${res.status})`);
  return res.json();
}

async function closeTarget(id) {
  try {
    await fetch(`http://127.0.0.1:${port}/json/close/${id}`);
  } catch {
    // already gone
  }
}

async function waitUntil(cdp, predicate, timeout, label) {
  const start = Date.now();
  let lastError = null;
  while (Date.now() - start < timeout) {
    try {
      if (await predicate()) return;
    } catch (error) {
      lastError = error;
    }
    await sleep(250);
  }
  throw new Error(`Timed out after ${timeout}ms waiting for ${label}${lastError ? ` (${lastError.message})` : ""}`);
}

function timeoutOf(spec, value) {
  return Number((spec && typeof spec === "object" && spec.timeout) || config.timeout || value);
}

async function clickByText(cdp, spec) {
  const options = typeof spec === "string" ? { text: spec } : spec;
  const result = await cdp.evaluate(`(() => {
    const target = ${JSON.stringify(options.text)};
    const scopeSelector = ${JSON.stringify(options.scope || null)};
    const exact = ${options.exact !== false};
    const index = ${Number.isInteger(options.index) ? options.index : -1};
    const normalize = (value) => (value || "").replace(/[\\s\\u00a0\\u2000-\\u200f\\u2028\\u2029\\u202f\\u205f\\u3000]/g, "");
    const root = scopeSelector ? document.querySelector(scopeSelector) : document;
    if (!root) return { ok: false, reason: "scope not found: " + scopeSelector };
    const candidates = Array.from(root.querySelectorAll("button, a, [role=button], [role=menuitem], [role=tab], li, td, th, span, div, label"));
    const visible = (el) => {
      const rect = el.getBoundingClientRect();
      const style = getComputedStyle(el);
      return rect.width > 0 && rect.height > 0 && style.display !== "none" && style.visibility !== "hidden" && Number(style.opacity) > 0;
    };
    const wanted = normalize(target);
    let matches = candidates.filter((el) => visible(el) && (exact ? normalize(el.innerText) === wanted : normalize(el.innerText).includes(wanted)));
    if (!matches.length) {
      matches = candidates.filter((el) => {
        if (!visible(el)) return false;
        const label = normalize(el.getAttribute("aria-label") || el.getAttribute("title") || "");
        return label && (exact ? label === wanted : label.includes(wanted));
      });
    }
    if (!matches.length) {
      return { ok: false, reason: "no visible element matches", visibleSample: candidates.filter(visible).map((el) => normalize(el.innerText)).filter(Boolean).slice(0, 25) };
    }
    const depth = (el) => { let d = 0; while (el) { d += 1; el = el.parentElement; } return d; };
    const chosen = index >= 0
      ? matches[index]
      : matches.slice().sort((a, b) => depth(a) - depth(b)).pop();
    if (!chosen) return { ok: false, reason: "index " + index + " out of range (" + matches.length + " matches)" };
    chosen.scrollIntoView({ block: "center", inline: "center" });
    chosen.click();
    return { ok: true, tag: chosen.tagName, text: normalize(chosen.innerText).slice(0, 80), matches: matches.length };
  })()`);
  if (!result?.ok) throw new Error(`clickText "${options.text}" failed: ${result?.reason || "unknown"} ${JSON.stringify(result?.visibleSample || [])}`);
  log(`clickText "${options.text}" -> ${result.tag} "${result.text}"`);
}

async function clickSelector(cdp, spec) {
  const options = typeof spec === "string" ? { selector: spec } : spec;
  const result = await cdp.evaluate(`(() => {
    const nodes = Array.from(document.querySelectorAll(${JSON.stringify(options.selector)}));
    const index = ${Number.isInteger(options.index) ? options.index : 0};
    const el = nodes[index];
    if (!el) return { ok: false, count: nodes.length };
    el.scrollIntoView({ block: "center", inline: "center" });
    el.click();
    return { ok: true, tag: el.tagName, count: nodes.length };
  })()`);
  if (!result?.ok) throw new Error(`click "${options.selector}" failed: ${result?.count || 0} node(s) found`);
  log(`click ${options.selector} -> ${result.tag}`);
}

async function fillField(cdp, spec) {
  const result = await cdp.evaluate(`(() => {
    const el = document.querySelector(${JSON.stringify(spec.selector)});
    if (!el) return { ok: false };
    const proto = el instanceof HTMLTextAreaElement ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype;
    const setter = Object.getOwnPropertyDescriptor(proto, "value")?.set;
    el.focus();
    if (setter) setter.call(el, ${JSON.stringify(spec.value ?? "")});
    else el.value = ${JSON.stringify(spec.value ?? "")};
    el.dispatchEvent(new Event("input", { bubbles: true }));
    el.dispatchEvent(new Event("change", { bubbles: true }));
    return { ok: true, tag: el.tagName };
  })()`);
  if (!result?.ok) throw new Error(`fill "${spec.selector}" failed: selector not found`);
  log(`fill ${spec.selector}`);
}

async function screenshot(cdp, spec, index) {
  const options = typeof spec === "string" ? { name: spec } : spec;
  const name = options.name || `shot-${String(index).padStart(2, "0")}`;
  const format = options.format === "png" ? "png" : "jpeg";
  const params = { format, fromSurface: true, captureBeyondViewport: false };
  if (format === "jpeg") params.quality = options.quality ?? 85;

  if (options.selector) {
    const rect = await cdp.evaluate(`(() => {
      const el = document.querySelector(${JSON.stringify(options.selector)});
      if (!el) return null;
      el.scrollIntoView({ block: "center", inline: "nearest" });
      const r = el.getBoundingClientRect();
      return { x: r.left + window.scrollX, y: r.top + window.scrollY, width: r.width, height: r.height };
    })()`);
    if (!rect) throw new Error(`screenshot selector not found: ${options.selector}`);
    const pad = Number(options.padding || 0);
    await sleep(250);
    params.captureBeyondViewport = true;
    params.clip = {
      x: Math.max(0, rect.x - pad),
      y: Math.max(0, rect.y - pad),
      width: rect.width + pad * 2,
      height: rect.height + pad * 2,
      scale: 1
    };
  } else if (options.fullPage) {
    const metrics = await cdp.send("Page.getLayoutMetrics");
    const size = metrics.cssContentSize || metrics.contentSize;
    const maxHeight = Number(options.maxHeight || 12000);
    params.captureBeyondViewport = true;
    params.clip = { x: 0, y: 0, width: size.width, height: Math.min(size.height, maxHeight), scale: 1 };
  }

  const { data } = await cdp.send("Page.captureScreenshot", params, 45000);
  const ext = format === "png" ? "png" : "jpg";
  const file = path.join(outDir, `${name}.${ext}`);
  fs.writeFileSync(file, Buffer.from(data, "base64"));
  const size = fs.statSync(file).size;
  log(`shot ${name}.${ext} ${Math.round(size / 1024)}KB`);
  return file;
}

async function setViewport(cdp, spec) {
  if (!spec || spec.clear) {
    await cdp.send("Emulation.clearDeviceMetricsOverride");
    log("viewport cleared");
    return;
  }
  await cdp.send("Emulation.setDeviceMetricsOverride", {
    width: Number(spec.width || 1440),
    height: Number(spec.height || 900),
    deviceScaleFactor: Number(spec.deviceScaleFactor || 1),
    mobile: Boolean(spec.mobile)
  });
  log(`viewport ${spec.width}x${spec.height} @${spec.deviceScaleFactor || 1}x${spec.mobile ? " mobile" : ""}`);
}

async function runStep(cdp, step, index) {
  if (step.navigate) {
    await cdp.send("Page.navigate", { url: step.navigate }, 30000);
    await waitUntil(cdp, async () => (await cdp.evaluate("document.readyState", false)) === "complete", timeoutOf(step, 30000), `page load of ${step.navigate}`);
    await sleep(400);
    log(`navigate ${step.navigate}`);
    return;
  }
  if (step.wait) {
    await sleep(Number(step.wait));
    log(`wait ${step.wait}ms`);
    return;
  }
  if (step.waitForText) {
    const spec = typeof step.waitForText === "string" ? { text: step.waitForText } : step.waitForText;
    const scope = spec.scope ? `document.querySelector(${JSON.stringify(spec.scope)})?.innerText || ""` : "document.body ? document.body.innerText : \"\"";
    await waitUntil(
      cdp,
      async () => (await cdp.evaluate(`(${scope}).includes(${JSON.stringify(spec.text)})`, false)) === true,
      timeoutOf(spec, 30000),
      `text ${JSON.stringify(spec.text)}`
    );
    log(`waitForText "${spec.text}" ok`);
    return;
  }
  if (step.waitForSelector) {
    const spec = typeof step.waitForSelector === "string" ? { selector: step.waitForSelector } : step.waitForSelector;
    await waitUntil(
      cdp,
      async () => (await cdp.evaluate(`!!document.querySelector(${JSON.stringify(spec.selector)})`, false)) === true,
      timeoutOf(spec, 30000),
      `selector ${spec.selector}`
    );
    log(`waitForSelector ${spec.selector} ok`);
    return;
  }
  if (step.waitForGone) {
    const spec = typeof step.waitForGone === "string" ? { selector: step.waitForGone } : step.waitForGone;
    await waitUntil(
      cdp,
      async () => (await cdp.evaluate(`(() => {
        return !Array.from(document.querySelectorAll(${JSON.stringify(spec.selector)})).some((el) => {
          const rect = el.getBoundingClientRect();
          const style = getComputedStyle(el);
          return rect.width > 0 && rect.height > 0 && style.display !== "none" && style.visibility !== "hidden";
        });
      })()`, false)) === true,
      timeoutOf(spec, 30000),
      `selector ${spec.selector} to disappear`
    );
    log(`waitForGone ${spec.selector} ok`);
    return;
  }
  if (step.clickText) {
    await clickByText(cdp, step.clickText);
    return;
  }
  if (step.click) {
    await clickSelector(cdp, step.click);
    return;
  }
  if (step.fill) {
    await fillField(cdp, step.fill);
    return;
  }
  if (step.eval) {
    const spec = typeof step.eval === "string" ? { expression: step.eval } : step.eval;
    const value = await cdp.evaluate(spec.expression, spec.awaitPromise !== false);
    if (spec.print !== false && value !== undefined) log(`eval -> ${typeof value === "string" ? value : JSON.stringify(value)}`);
    else log("eval ok");
    return;
  }
  if (step.scrollTo) {
    const spec = typeof step.scrollTo === "string" ? { selector: step.scrollTo } : step.scrollTo;
    const ok = await cdp.evaluate(`(() => {
      const el = document.querySelector(${JSON.stringify(spec.selector)});
      if (!el) return false;
      el.scrollIntoView({ block: ${JSON.stringify(spec.block || "start")}, inline: "nearest" });
      return true;
    })()`, false);
    if (!ok) throw new Error(`scrollTo selector not found: ${spec.selector}`);
    await sleep(250);
    log(`scrollTo ${spec.selector}`);
    return;
  }
  if (step.setStorage) {
    await cdp.evaluate(`(() => {
      const values = ${JSON.stringify(step.setStorage)};
      for (const [key, value] of Object.entries(values)) localStorage.setItem(key, String(value));
      return Object.keys(values);
    })()`, false);
    log(`setStorage ${Object.keys(step.setStorage).join(", ")}`);
    return;
  }
  if (step.removeSelector) {
    const selectors = Array.isArray(step.removeSelector) ? step.removeSelector : [step.removeSelector];
    const removed = await cdp.evaluate(`(() => {
      let count = 0;
      for (const selector of ${JSON.stringify(selectors)}) {
        document.querySelectorAll(selector).forEach((node) => { node.remove(); count += 1; });
      }
      return count;
    })()`, false);
    log(`removeSelector removed ${removed}`);
    return;
  }
  if (step.viewport !== undefined) {
    await setViewport(cdp, step.viewport);
    return;
  }
  if (step.press) {
    const keys = { Escape: 27, Enter: 13, Tab: 9, ArrowDown: 40, ArrowUp: 38 };
    const code = keys[step.press] || Number(step.press);
    if (!code) throw new Error(`Unsupported key: ${step.press}`);
    await cdp.send("Input.dispatchKeyEvent", { type: "keyDown", key: step.press, windowsVirtualKeyCode: code });
    await cdp.send("Input.dispatchKeyEvent", { type: "keyUp", key: step.press, windowsVirtualKeyCode: code });
    log(`press ${step.press}`);
    return;
  }
  if (step.screenshot) {
    const inlineKeys = ["selector", "fullPage", "padding", "format", "quality", "maxHeight"];
    const spec = typeof step.screenshot === "string"
      ? { name: step.screenshot, ...Object.fromEntries(inlineKeys.filter((k) => step[k] !== undefined).map((k) => [k, step[k]])) }
      : step.screenshot;
    await screenshot(cdp, spec, index);
    return;
  }
  throw new Error(`Unknown step: ${JSON.stringify(step)}`);
}

await ensureChrome();
if (config.viewport) {
  // Applied to the tab after it opens; kept for reference in launch sizing.
}

const target = await openTarget();
const ws = new WebSocket(target.webSocketDebuggerUrl);
await new Promise((resolve, reject) => {
  ws.onopen = resolve;
  ws.onerror = reject;
});
const cdp = new Cdp(ws);
await cdp.send("Page.enable");
await cdp.send("Runtime.enable");
if (config.viewport) await setViewport(cdp, config.viewport);

try {
  for (let i = 0; i < steps.length; i += 1) {
    const step = steps[i];
    try {
      await runStep(cdp, step, i + 1);
    } catch (error) {
      log(`STEP ${i + 1} FAILED: ${JSON.stringify(step)}`);
      throw error;
    }
  }
  if (!args["keep-open"]) await closeTarget(target.id);
  else log(`tab kept open: ${target.id}`);
  log(`done: ${outDir}`);
  ws.close();
  process.exit(0);
} catch (error) {
  log(`error: ${error.message}`);
  log(`tab left open for inspection: ${target.id} (${target.url})`);
  ws.close();
  process.exit(1);
}
