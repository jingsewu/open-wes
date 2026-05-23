import React from "react"

interface GrafanaDashboardProps {
    dashboardUid: string
    dashboardSlug: string
}

const GrafanaDashboard: React.FC<GrafanaDashboardProps> = ({
    dashboardUid,
    dashboardSlug
}) => {
    const grafanaBaseUrl = window.location.protocol + "//" + window.location.hostname + ":3000"
    const src = `${grafanaBaseUrl}/d/${dashboardUid}/${dashboardSlug}?kiosk`

    return (
        <iframe
            src={src}
            style={{
                width: "100%",
                height: "calc(100vh - 100px)",
                border: "none"
            }}
            title={dashboardSlug}
        />
    )
}

export default GrafanaDashboard
