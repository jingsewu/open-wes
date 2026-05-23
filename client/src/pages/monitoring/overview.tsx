import React from "react"
import GrafanaDashboard from "./GrafanaDashboard"

const MonitoringOverview: React.FC = () => {
    return <GrafanaDashboard dashboardUid="openwes-overview" dashboardSlug="overview" />
}

export default MonitoringOverview
