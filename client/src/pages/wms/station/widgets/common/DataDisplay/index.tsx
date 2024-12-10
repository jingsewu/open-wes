import React from "react"
import { Col } from "antd"

const DataDisplay = (props: any) => {
    const { key, span, title, render, value } = props
    return (
        <Col key={key} span={span}>
            <div
                className="py-3.5 px-6 rounded-md"
                style={{
                    background: "#F8F8F8"
                }}
            >
                <header className="text-sm" style={{ color: "#696B7D" }}>
                    {title}
                </header>
                <div style={{ color: "#292B3E", fontSize: 24 }}>
                    {render ? (
                        render()
                    ) : (
                        <span className="text-ellipsis">{value ?? "-"}</span>
                    )}
                </div>
            </div>
        </Col>
    )
}

export default DataDisplay
