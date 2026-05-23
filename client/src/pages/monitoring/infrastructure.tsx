import React from "react"
import GrafanaDashboard from "./GrafanaDashboard"

const MonitoringInfrastructure: React.FC = () => {
    return <GrafanaDashboard dashboardUid="openwes-infra" dashboardSlug="infrastructure" />
}

export default MonitoringInfrastructure
