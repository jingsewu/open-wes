import React from "react"
import GrafanaDashboard from "./GrafanaDashboard"

const MonitoringBusiness: React.FC = () => {
    return <GrafanaDashboard dashboardUid="openwes-business" dashboardSlug="business" />
}

export default MonitoringBusiness
