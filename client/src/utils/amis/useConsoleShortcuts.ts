import { useEffect } from "react"

/**
 * Console keyboard shortcuts for operators working with a barcode scanner.
 *
 * `/` focuses the current page's search input and `Alt+N` triggers the page's
 * primary create action. A scanner types a whole code in well under 50 ms per
 * character; any keydown that lands inside such a burst is left to the focused
 * control (or the station scanner hook) instead of being treated as a shortcut.
 */
const SCAN_KEY_GAP_MS = 50

function isEditable(target: EventTarget | null): boolean {
    if (!(target instanceof HTMLElement)) {
        return false
    }
    const tag = target.tagName
    return (
        tag === "INPUT" ||
        tag === "TEXTAREA" ||
        tag === "SELECT" ||
        target.isContentEditable
    )
}

function visibleElement<T extends HTMLElement>(selector: string): T | null {
    const elements = Array.from(document.querySelectorAll<T>(selector))
    return elements.find((element) => element.offsetParent !== null) ?? null
}

function focusPageSearch(): void {
    const input = visibleElement<HTMLInputElement>(
        ".cxd-Crud-filter input:not([type='hidden']):not([disabled]), " +
            ".cxd-Crud-filter textarea:not([disabled])"
    )
    if (!input) {
        return
    }
    input.focus()
    input.select()
}

function clickPrimaryPageAction(): void {
    const button = visibleElement<HTMLButtonElement>(
        ".cxd-Page-toolbar .cxd-Button--primary:not([disabled])"
    )
    button?.click()
}

/** AMIS unmounts drawers and modals on close, so presence means open. */
function hasOpenOverlay(): boolean {
    return document.querySelector(".cxd-Drawer, .cxd-Modal") !== null
}

export function useConsoleShortcuts(): void {
    useEffect(() => {
        let lastKeyDownAt = 0

        const handleKeyDown = (event: KeyboardEvent) => {
            const now = Date.now()
            const isScanBurst = now - lastKeyDownAt < SCAN_KEY_GAP_MS
            lastKeyDownAt = now

            if (
                isScanBurst ||
                event.defaultPrevented ||
                isEditable(event.target) ||
                hasOpenOverlay()
            ) {
                return
            }

            const hasCommandModifier =
                event.ctrlKey || event.metaKey || event.altKey

            if (event.key === "/" && !hasCommandModifier) {
                event.preventDefault()
                focusPageSearch()
                return
            }

            if (
                event.altKey &&
                !event.ctrlKey &&
                !event.metaKey &&
                event.key.toLowerCase() === "n"
            ) {
                event.preventDefault()
                clickPrimaryPageAction()
            }
        }

        window.addEventListener("keydown", handleKeyDown)
        return () => window.removeEventListener("keydown", handleKeyDown)
    }, [])
}
