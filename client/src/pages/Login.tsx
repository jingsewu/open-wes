import * as React from "react"
import {useTranslation} from "react-i18next"
// Global app chrome (header, sidebar, AMIS overrides) ships from this file — keep the import.
import "@/scss/style.scss"
import "@/scss/login.scss"
import LoginForm from "./components/LoginForm"

// Brand mark — same artwork as @/icon/icon_logo/wes.svg, inlined so the page
// can tint it with currentColor and stay free of build-time SVG loaders.
const WES_MARK_PATH =
    "M110.28 764.028l404.348 236v-113.28L211.76 707.384v-67.652l302.868 179.356v-118.52l-303.128-179.36 0.524-65.552 302.604 178.832v-115.472L110.28 271.44zM529.572 10.404l-96.76 60.572 304.44 180.932-55.852 33.828-310.732-181.72-94.4 58.996L582.28 345.52l-57.428 35.4-309.944-185.656-94.4 57.428 405.92 246.228 409.848-245.44zM945.72 394.292V277.868L537.436 519.8l1.58 297.288 312.304-185.224v71.584l-311.164 181.724-0.784 116.424 406.348-237.568v-302.08l-309.16 184.08V579.16z"

/** Stages of the execution line, left to right. Positions are percentages of the belt. */
const flowStages = [
    {key: "login.flowReceiving", at: "12%"},
    {key: "login.flowStorage", at: "37.3%"},
    {key: "login.flowPicking", at: "62.6%"},
    {key: "login.flowShipping", at: "88%"},
]

const toteTracks = ["a", "b", "c"]

const BrandMark = () => (
    <svg className="login-brand__mark" viewBox="0 0 1024 1024" aria-hidden="true" focusable="false">
        <path d={WES_MARK_PATH} fill="currentColor" fillRule="evenodd" clipRule="evenodd"/>
    </svg>
)

/**
 * Sign-in page: the machine hall with the execution line running across the
 * floor, and the sign-in console docked on the right edge. The line is
 * decorative here — its stages are described in the hero copy for screen readers.
 */
const Login = () => {
    const {t} = useTranslation()

    return (
        <div className="login-page">
            <div className="login-hall">
                <div className="login-brand">
                    <BrandMark/>
                    <span className="login-brand__word">
                        Open<b>WES</b>
                    </span>
                </div>

                <div className="login-hero">
                    <h1 className="login-hero__title">{t("login.heroTitle")}</h1>
                    <p className="login-hero__sub">{t("login.heroSubtitle")}</p>
                </div>

                <div className="login-line" aria-hidden="true">
                    <div className="login-line__belt">
                        <span className="login-line__rollers"/>
                        <span className="login-line__rail login-line__rail--top"/>
                        <span className="login-line__rail login-line__rail--bottom"/>
                        {toteTracks.map((track) => (
                            <span
                                key={track}
                                className={`login-line__track login-line__track--${track}`}
                            >
                                <span className="login-line__tote"/>
                            </span>
                        ))}
                    </div>

                    {flowStages.map((stage, index) => (
                        <div
                            key={stage.key}
                            className={`login-line__station login-line__station--${index + 1}`}
                            style={{left: stage.at}}
                        >
                            <span className="login-line__label">{t(stage.key)}</span>
                            <span className="login-line__post"/>
                        </div>
                    ))}

                    <span className="login-line__floor"/>
                </div>
            </div>

            <LoginForm/>
        </div>
    )
}

export default Login
