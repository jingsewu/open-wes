/**
 * Design tokens for inline-styled React components.
 * Values mirror the SCSS variables in src/scss/default.scss — keep the two in sync.
 */

export const colors = {
    /* Brand */
    primary: "#3b82f6",
    primaryLight: "#60a5fa",
    primaryDark: "#1d4ed8",
    success: "#10b981",
    danger: "#ef4444",
    warning: "#f59e0b",
    info: "#0ea5e9",

    /* Text */
    textStrong: "#1e293b", // headings, KPI values
    text: "#334155", // body, table cells
    textSecondary: "#64748b", // labels, secondary
    textMuted: "#94a3b8", // placeholders, hints
    textInverse: "#ffffff",

    /* Borders & backgrounds */
    border: "#e2e8f0",
    borderSubtle: "#f1f5f9",
    bgSubtle: "#f8fafc",
    bgHover: "#f3f4f6",
    bgPage: "#f1f5f9",

    /* Sidebar (dark command-center) */
    sidebarBg: "#1e293b",
    sidebarBgHover: "#334155",
    sidebarBgActive: "#0f172a",
    sidebarText: "#cbd5e1",
    sidebarTextActive: "#ffffff",
    sidebarAccent: "#3b82f6",
}

export const radius = {
    sm: 8,
    md: 12,
    lg: 16,
}

export const spacing = {
    xs: 8,
    sm: 12,
    md: 16,
    lg: 24,
    xl: 32,
    xxl: 48,
}

export const shadows = {
    card: "0 1px 3px rgba(0, 0, 0, 0.06)",
    cardHover: "0 4px 12px rgba(0, 0, 0, 0.08)",
    drop: "0 8px 24px rgba(0, 0, 0, 0.12)",
}

export const fontSizes = {
    xs: 11,
    sm: 12,
    md: 13,
    lg: 14,
    xl: 16,
    heading: 20,
    kpi: 24,
}
