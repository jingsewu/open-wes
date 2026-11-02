#!/usr/bin/env node
/**
 * AMIS schema lint.
 *
 * Scans TypeScript schema files under src/pages for the mistakes documented in
 * docs/standards/amis.md. No dependencies; run with:
 *
 *   npm run lint:amis
 *   npm run lint:amis -- --strict   # warnings also fail
 */

import fs from "node:fs"
import path from "node:path"
import { fileURLToPath } from "node:url"

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..")
const PAGES = path.join(ROOT, "src", "pages")
const LOCALES = ["zh-cn", "en-us"].map((name) => ({
    name,
    messages: JSON.parse(
        fs.readFileSync(
            path.join(ROOT, "src", "locales", `${name}.json`),
            "utf8"
        )
    )
}))

const strict = process.argv.includes("--strict")
const findings = []

/**
 * List pages that still compose raw page + CRUD schemas. The ratchet fails on
 * any candidate that is neither converted to `crudPage()` nor explicitly
 * allowlisted; each wave removes its migrated entries. See docs/standards/amis.md.
 */
const LIST_PAGE_EXCEPTIONS = new Set(
    JSON.parse(
        fs.readFileSync(
            path.join(ROOT, "scripts", "amis-list-page-exceptions.json"),
            "utf8"
        )
    ).entries.map((entry) => entry.file)
)

const LABEL_PROPS = [
    "label",
    "title",
    "confirmText",
    "confirmTitle",
    "placeholder",
    "description",
    "submitText",
    "resetText",
    "headerToolbar",
    "toolbar"
]

const KEY_PATTERN = /^[a-zA-Z][\w-]*(\.[\w-]+)+$/
// Conventional tokens and coordinate axes need no translation.
const IGNORED_LITERALS = new Set([
    "ID",
    "URL",
    "API",
    "URI",
    "SKU",
    "LPN",
    "x",
    "y",
    "z"
])

function walk(dir) {
    return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
        const full = path.join(dir, entry.name)
        if (entry.isDirectory()) return walk(full)
        return entry.name.endsWith(".tsx") || entry.name.endsWith(".ts")
            ? [full]
            : []
    })
}

function add(file, line, severity, rule, message) {
    findings.push({
        file: path.relative(ROOT, file).replaceAll("\\", "/"),
        line,
        severity,
        rule,
        message
    })
}

function isDisabled(lines, index) {
    return lines
        .slice(Math.max(0, index - 4), index + 1)
        .some((line) => line.includes("amis-lint-disable"))
}

/**
 * Editor placeholders hold code samples (Groovy or Java) and stay in one
 * language on purpose.
 */
function isCodeSample(prop, value) {
    return (
        prop === "placeholder" &&
        (value.includes("\\n") ||
            value.startsWith("//") ||
            /\b(function|class|public)\b/.test(value))
    )
}

/**
 * Labels on hidden fields and columns are never rendered, so they do not need
 * locale keys. Heuristic: look for hidden/visible flags in the surrounding
 * object literal.
 */
function isHiddenAt(lines, index) {
    const window = lines
        .slice(Math.max(0, index - 5), index + 6)
        .join("\n")
    return (
        /\btype\s*:\s*["']hidden["']/.test(window) ||
        /\bhidden\s*:\s*true\b/.test(window) ||
        /\bvisible\s*:\s*false\b/.test(window)
    )
}

const schemaFiles = walk(PAGES)

for (const file of schemaFiles) {
    const text = fs.readFileSync(file, "utf8")
    const lines = text.split(/\r?\n/)
    const relativeFile = path.relative(ROOT, file).replaceAll("\\", "/")
    const isListPageCandidate =
        /\btype\s*:\s*["']page["']/.test(text) &&
        /\btype\s*:\s*["']crud["']/.test(text)

    if (isListPageCandidate) {
        const legacyAuditLine = lines.findIndex((line) =>
            line.includes("create_update_columns")
        )
        if (legacyAuditLine >= 0) {
            add(
                file,
                legacyAuditLine + 1,
                "warning",
                "audit-columns-preset",
                "create_update_columns is legacy; use auditColumns()"
            )
        }

        if (
            !/\bcrudPage\s*\(/.test(text) &&
            !LIST_PAGE_EXCEPTIONS.has(relativeFile)
        ) {
            const pageLine = lines.findIndex((line) =>
                /\btype\s*:\s*["']page["']/.test(line)
            )
            add(
                file,
                pageLine + 1,
                "error",
                "list-page-preset",
                "raw page + crud schema; use crudPage() from @/utils/amis/presets or add an allowlist entry in scripts/amis-list-page-exceptions.json"
            )
        }
    }

    lines.forEach((line, index) => {
        const lineNo = index + 1

        if (isDisabled(lines, index)) return

        const trimmed = line.trim()
        if (
            trimmed.startsWith("//") ||
            trimmed.startsWith("*") ||
            trimmed.startsWith("/*") ||
            trimmed.startsWith("{/*")
        ) {
            return
        }

        // 1. Boolean strings silently disable visibility logic.
        if (/\bhidden\s*:\s*["'](true|false)["']/.test(line)) {
            add(
                file,
                lineNo,
                "error",
                "boolean-string",
                'hidden must be a boolean, not "true"/"false"'
            )
        }

        // 2. `require` is not a Form item prop; validation never runs.
        if (/\brequire\s*:\s*(true|false)\b/.test(line)) {
            add(
                file,
                lineNo,
                "error",
                "required-typo",
                'use "required", not "require"'
            )
        }

        // 3. AMIS reads lower-case `filename` for export-excel.
        if (/\bfileName\s*:/.test(line)) {
            add(
                file,
                lineNo,
                "error",
                "export-filename",
                'export-excel reads "filename", not "fileName"'
            )
        }

        // 4. Stray `}` inside a literal field name or option source.
        //    Expression strings ("${dictionary.X}") legitimately end with }.
        const stringProp = line.match(
            /\b(name|source)\s*:\s*["']([^"']*)["']/
        )
        if (
            stringProp &&
            stringProp[2].includes("}") &&
            !stringProp[2].startsWith("${")
        ) {
            add(
                file,
                lineNo,
                "error",
                "stray-brace",
                `"${stringProp[1]}" contains a stray closing brace ("${stringProp[2]}")`
            )
        }

        // 5. Raw HTML bypasses React escaping.
        if (/\btype\s*:\s*["']html["']/.test(line)) {
            add(
                file,
                lineNo,
                "warning",
                "raw-html",
                'type: "html" renders unsanitised markup; use "tpl"'
            )
        }

        // 6. Filled danger buttons in rows read as alarms on every row.
        if (/\blevel\s*:\s*["']danger["']/.test(line)) {
            add(
                file,
                lineNo,
                "warning",
                "row-danger",
                'row-level destructive actions should be level: "link" with className "owes-action--danger"'
            )
        }

        // 7. Label-ish copy: must be a locale key or a data expression.
        //    Hidden fields are never rendered, so their labels are exempt.
        const hidden = isHiddenAt(lines, index)
        if (!hidden) {
            for (const prop of LABEL_PROPS) {
                const match = line.match(
                    new RegExp(`\\b${prop}\\s*:\\s*["']([^"']+)["']`)
                )
                if (!match) continue
                const value = match[1]
                if (!value || value.startsWith("${")) continue
                if (isCodeSample(prop, value)) continue
                if (/[一-鿿]/.test(value)) {
                    add(
                        file,
                        lineNo,
                        "error",
                        "raw-chinese",
                        `"${prop}" contains raw Chinese; use a locale key (${value})`
                    )
                    continue
                }
                if (KEY_PATTERN.test(value)) {
                    for (const locale of LOCALES) {
                        if (!(value in locale.messages)) {
                            add(
                                file,
                                lineNo,
                                "error",
                                "missing-locale-key",
                                `"${value}" is missing from ${locale.name}.json`
                            )
                        }
                    }
                } else if (!IGNORED_LITERALS.has(value)) {
                    add(
                        file,
                        lineNo,
                        "warning",
                        "hardcoded-copy",
                        `"${prop}" is hardcoded copy ("${value}"); use a locale key`
                    )
                }
            }
        }
    })

    // 8. Inline drawers should declare an explicit size; the default (400px)
    //    is too narrow for two-column forms.
    const drawerMatches = [...text.matchAll(/\bdrawer\s*:\s*\{/g)]
    for (const match of drawerMatches) {
        const startLine =
            text.slice(0, match.index).split(/\r?\n/).length - 1
        const window = lines.slice(startLine, startLine + 30).join("\n")
        if (!/\bsize\s*:/.test(window)) {
            add(
                file,
                startLine + 1,
                "warning",
                "drawer-size",
                'inline drawer has no "size"; use md (one column) or lg (two columns)'
            )
        }
    }

    // 9. Export action without a filename.
    const exportMatches = [...text.matchAll(/type\s*:\s*["']export-excel["']/g)]
    for (const match of exportMatches) {
        const startLine =
            text.slice(0, match.index).split(/\r?\n/).length - 1
        const window = lines
            .slice(startLine, startLine + 20)
            .join("\n")
        if (!/\bfilename\s*:/.test(window)) {
            add(
                file,
                startLine + 1,
                "warning",
                "export-filename-missing",
                "export-excel has no filename"
            )
        }
    }

    // 10. Status shown as 是/否 instead of enabled/disabled.
    const statusMatches = [...text.matchAll(/\bname\s*:\s*["']status["']/g)]
    for (const match of statusMatches) {
        const startLine =
            text.slice(0, match.index).split(/\r?\n/).length - 1
        const window = lines.slice(startLine, startLine + 8).join("\n")
        if (/yes_no_options/.test(window)) {
            add(
                file,
                startLine + 1,
                "warning",
                "status-semantics",
                "status uses yes_no_options; use common.enabled / common.disabled"
            )
        }
    }
}

findings.sort(
    (a, b) =>
        a.file.localeCompare(b.file) || a.line - b.line
)

const errors = findings.filter((finding) => finding.severity === "error")
const warnings = findings.filter((finding) => finding.severity === "warning")

let currentFile = ""
for (const finding of findings) {
    if (finding.file !== currentFile) {
        currentFile = finding.file
        console.log(`\n${currentFile}`)
    }
    console.log(
        `  ${String(finding.line).padStart(4)}  ${finding.severity.padEnd(
            7
        )}  ${finding.rule.padEnd(24)}  ${finding.message}`
    )
}

console.log(
    `\n${errors.length} error(s), ${warnings.length} warning(s) in ${schemaFiles.length} schema file(s).`
)
console.log("Standard: docs/standards/amis.md\n")

if (errors.length > 0 || (strict && warnings.length > 0)) {
    process.exit(1)
}
