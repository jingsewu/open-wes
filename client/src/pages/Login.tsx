import * as React from "react"
import {RouteComponentProps} from "react-router-dom"
import {IMainStore} from "@/stores"
import {inject, observer} from "mobx-react"
import {withRouter} from "react-router"
import "@/scss/style.scss"
import LoginForm from "./components/LoginForm"
import {withTranslation} from "react-i18next"
import Language from "./components/Language"
import {BarChart3, Zap, Shield} from 'lucide-react'

interface LoginProps extends RouteComponentProps<any> {
    store: IMainStore
    t: any
}

const BRAND_LOGO_GRAD_ID = "login-brand-logo-grad"

const BrandLogo = () => (
    <svg width="72" height="72" viewBox="0 0 34 34" fill="none" xmlns="http://www.w3.org/2000/svg">
        <defs>
            <linearGradient id={BRAND_LOGO_GRAD_ID} x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stopColor="#3b82f6" />
                <stop offset="100%" stopColor="#1d4ed8" />
            </linearGradient>
        </defs>
        <rect width="34" height="34" rx="9" fill={`url(#${BRAND_LOGO_GRAD_ID})`} />
        <text x="17" y="23.5" textAnchor="middle" fill="white" fontSize="17" fontWeight="900" fontFamily="Plus Jakarta Sans, Arial, sans-serif">W</text>
    </svg>
)

const features = [
    {icon: BarChart3, labelKey: "login.featureAnalytics"},
    {icon: Zap, labelKey: "login.featureRealtime"},
    {icon: Shield, labelKey: "login.featureSecurity"},
]

const featureFallbacks: Record<string, string> = {
    "login.featureAnalytics": "Real-time warehouse analytics",
    "login.featureRealtime": "High-performance task execution",
    "login.featureSecurity": "Enterprise-grade security",
}

@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginRoute extends React.Component<LoginProps, any> {
    render() {
        const {t} = this.props
        return (
            <div className="login-page-container d-flex">
                {/* Brand panel */}
                <div className="w-1/2 login-brand-panel">
                    <div style={{position: "relative", zIndex: 1, textAlign: "center"}}>
                        <BrandLogo />
                        <h1 style={{
                            fontSize: 36,
                            fontWeight: 800,
                            marginTop: 20,
                            marginBottom: 8,
                            lineHeight: 1.2,
                        }}>
                            <span style={{color: "#fff"}}>Open</span>
                            <span style={{color: "#60a5fa"}}>WES</span>
                        </h1>
                        <p style={{
                            color: "#94a3b8",
                            fontSize: 16,
                            fontWeight: 500,
                            marginBottom: 48,
                        }}>
                            {t("login.brandTagline")}
                        </p>

                        <div style={{
                            display: "flex",
                            flexDirection: "column",
                            gap: 20,
                            alignItems: "flex-start",
                            maxWidth: 300,
                            margin: "0 auto",
                        }}>
                            {features.map(({icon: Icon, labelKey}) => (
                                <div
                                    key={labelKey}
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        gap: 14,
                                    }}
                                >
                                    <div style={{
                                        width: 40,
                                        height: 40,
                                        borderRadius: 10,
                                        background: "rgba(59, 130, 246, 0.12)",
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        flexShrink: 0,
                                    }}>
                                        <Icon size={20} color="#60a5fa" />
                                    </div>
                                    <span style={{
                                        color: "#cbd5e1",
                                        fontSize: 14,
                                        fontWeight: 500,
                                    }}>
                                        {t(labelKey, featureFallbacks[labelKey])}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Form panel */}
                <div className="w-1/2 login-form-panel">
                    <div style={{position: "absolute", top: 24, right: 24}}>
                        <Language />
                    </div>
                    <LoginForm />
                </div>
            </div>
        )
    }
}

export default withTranslation()(LoginRoute)
