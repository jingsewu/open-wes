import * as React from "react"
import { colors } from "@/theme"

/**
 * Shared "W" brand mark used across login page and app header.
 * gradientId is caller-supplied so multiple instances on one screen
 * never share a duplicate SVG gradient ID (see SVG-ID convention).
 */
interface BrandLogoProps {
    size?: number
    gradientId?: string
}

const DEFAULT_LOGO_GRAD_ID = "openwes-brand-logo-grad"

const BrandLogo = ({ size = 72, gradientId = DEFAULT_LOGO_GRAD_ID }: BrandLogoProps) => (
    <svg
        width={size}
        height={size}
        viewBox="0 0 34 34"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
    >
        <defs>
            <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stopColor={colors.primary} />
                <stop offset="100%" stopColor={colors.primaryDark} />
            </linearGradient>
        </defs>
        <rect width="34" height="34" rx="9" fill={`url(#${gradientId})`} />
        <text
            x="17"
            y="23.5"
            textAnchor="middle"
            fill="#fff"
            fontSize="17"
            fontWeight="900"
            fontFamily="Plus Jakarta Sans, Arial, sans-serif"
        >
            W
        </text>
    </svg>
)

export default BrandLogo
