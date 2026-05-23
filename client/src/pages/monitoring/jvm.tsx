import React from "react"
import GrafanaDashboard from "./GrafanaDashboard"

const MonitoringJvm: React.FC = () => {
    return <GrafanaDashboard dashboardUid="openwes-jvm" dashboardSlug="jvm" />
}

export default MonitoringJvm
