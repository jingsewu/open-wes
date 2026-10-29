---
name: capturing-browser-screenshots
description: Use when a task needs screenshots of a running web app or local dev UI for docs, bug reports, visual verification, or README images — especially login-protected pages, AntD/AMIS UIs, real-data screens, or when Chrome is already open with a CDP port.
---

# Capturing Browser Screenshots

## Overview

Drive the **real Chrome** over the DevTools Protocol with one declarative steps file. No npm install, no Playwright/Puppeteer, no new capture script per task. The helper opens its own tab, clicks real UI, waits for real data, and writes JPEG/PNG files.

Script: `scripts/cdp-capture.mjs` (Node 22+; uses global `fetch` and `WebSocket`).
Examples: `examples/openwes-plugin-list.json`.

## Quick Start

1. Check the app and Chrome:

```bash
curl -s http://127.0.0.1:9223/json/version
```

If CDP is down, either pass `--launch` (starts headless Chrome, leaves it running; add `--headed` to show the window), or start Chrome yourself:

```bash
"/c/Program Files/Google/Chrome/Application/chrome.exe" \
  --remote-debugging-port=9223 \
  --user-data-dir="C:/Temp/cdp-shots" \
  --no-first-run --disable-extensions --window-size=1440,900 about:blank &
```

2. Write a steps file (JSON). Start from an example if the target is OpenWES.

3. Run:

```bash
node .claude/skills/capturing-browser-screenshots/scripts/cdp-capture.mjs \
  --steps steps.json --out shots
```

4. **Read every screenshot with the Read tool.** An unread screenshot is unverified.

## Steps Reference

| Key | Value | Notes |
|---|---|---|
| `navigate` | URL | Waits for `document.readyState === "complete"` plus 400 ms |
| `wait` | ms | Fixed pause; prefer content waits |
| `waitForText` | `"text"` or `{text, scope, timeout}` | Polls visible text every 250 ms |
| `waitForSelector` | `"css"` or `{selector, timeout}` | Polls for a DOM node |
| `waitForGone` | `"css"` or `{selector, timeout}` | Waits until a node is absent or invisible (spinners, overlays) |
| `clickText` | `"text"` or `{text, exact, scope, index}` | **Whitespace-insensitive**; `exact` defaults true; clicks deepest match |
| `click` | `"css"` or `{selector, index}` | DOM `.click()` |
| `fill` | `{selector, value}` | Sets value + fires `input`/`change` (AMIS-safe) |
| `eval` | `"js"` or `{expression, awaitPromise}` | Runs in the page; promises awaited by default |
| `scrollTo` | `"css"` or `{selector, block}` | `block` defaults `start` |
| `setStorage` | `{key: value, ...}` | Run after the origin is loaded |
| `removeSelector` | `"css"` or `["css", ...]` | Removes overlays/banners |
| `screenshot` | `"name"` or `{name, selector, fullPage, padding, format, quality, maxHeight}` | JPEG q85 by default |
| `viewport` | `{width, height, deviceScaleFactor, mobile}` or `{clear:true}` | Device emulation |
| `press` | `"Escape"`, `"Enter"`, `"Tab"`, `"ArrowDown"`, `"ArrowUp"` | Keyboard events |

Top-level `viewport` and `timeout` apply globally. `out` can also live in the steps file. `{{ENV:NAME}}` is expanded in every string; a missing variable is an error — keep credentials in env vars, never in the JSON.

## OpenWES Recipe

Copy `examples/openwes-plugin-list.json`. Key points:

- **Login**: POST `${API}/user/api/auth/signin`, then set `ws_token` (from `token`), `Authorization: admin`, `i18nextLng: zh-CN` in localStorage. Credentials come from `OPENWES_USER` / `OPENWES_PASS` env vars. If the Chrome profile is already authenticated, drop the login steps.
- **Deep links redirect**: full-page loads of `/plugin-platform/*` land on `/wms/dashboard` because TabsLayout selects the first app child. Navigate to `/wms/dashboard`, wait for `插件平台`, then `clickText` it inside `.ant-menu-horizontal`.
- **AMIS table loading**: `插件标识` (a filter label) is visible while the table still shows `.cxd-Table-placeholder`; a screenshot then catches an empty table. Add `{"waitForGone": ".cxd-Table-placeholder"}`, or wait for a row key you know exists.
- **AntD spacing**: buttons render `校 验`, `取 消`, `刷 新` with a real space between Chinese characters. `clickText` normalizes whitespace, so pass the normal text.
- **Dev overlay**: webpack runtime errors inject `#webpack-dev-server-client-overlay` and cover the capture. Add `{"removeSelector": "#webpack-dev-server-client-overlay"}` immediately before each screenshot; it can be re-injected after an AMIS error.
- **Wide tables**: a 1440 px viewport hides the action column. Use `{"eval":"document.body.style.zoom='0.78'"}` for list shots, then reset to `'1'`.
- **DPI**: without a `viewport` override, this Windows machine renders 1440 CSS px as a 1800 px JPEG. With a `viewport` step, `deviceScaleFactor` controls output size (`1` = exact CSS pixels).
- **Memory pressure**: if `Emulation.setDeviceMetricsOverride` hangs, drop the `viewport` step and size the real Chrome window instead.
- **Do not** enable demo plugins, submit destructive actions, or use the user's normal Chrome profile just to take a picture.

## Verify Before Reporting

- Read the JPEG and confirm: not blank, correct page, target text visible, no overlay, nothing important clipped.
- If a step fails, the tab is left open for inspection; the step and reason are printed. Fix the step, not the page.
- For documentation, one capture per figure and make the caption match what the image actually shows.

## Embedding in a Single-File HTML Doc

Base64 embedding keeps the document portable and offline; add a click-to-zoom lightbox so detail survives downscaling (the plugin workbench guide at `docs/plugin-workbench-guide.html` is the reference implementation). Replace placeholders like `__IMG_01__` with:

```js
`data:image/jpeg;base64,${fs.readFileSync(file).toString("base64")}`
```

Then verify with a browser: all images `complete && naturalWidth > 0`, lightbox opens/closes, mobile width does not clip captions.

## Common Mistakes

| Mistake | Fix |
|---|---|
| Screenshot right after a click | `waitForText` / `waitForSelector` on the next screen |
| `innerText === "校验"` | AntD inserts spaces; use `clickText`, which normalizes |
| Deep-linking an app route | Enter through the dashboard and click the app menu |
| Overlay iframe in every shot | `removeSelector` before `screenshot` |
| Reporting a capture never viewed | Read it first; blank frames count as failures |
| Credentials in the steps file | `{{ENV:OPENWES_USER}}` / `{{ENV:OPENWES_PASS}}` only |
