import React from "react"

const Warehouse3D: React.FC = () => {
    // Construct URL from current hostname, matching the pattern used by GrafanaDashboard
    const viewerUrl = window.location.protocol + "//" + window.location.hostname + ":8092"

    return (
        <iframe
            src={viewerUrl}
            style={{
                width: "100%",
                height: "calc(100vh - 100px)",
                border: "none"
            }}
            title="3D Warehouse Viewer"
        />
    )
}

export default Warehouse3D
