import React, { memo } from "react"
import { Card, Avatar, Col, Row, Image } from "antd"
import { useDebounceFn } from "ahooks"
import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
const { Meta } = Card

interface Iprops {
    title: any
    avatar: string
    value: string
    description: any
    rightIcon: any
    backgroundColor?: string
    handleCardClick: () => void
}

const StationCard = (props: Iprops) => {
    const { handleCardClick, rightIcon, value, avatar, title, description } =
        props
    return (
        <Card
            // onClick={() => handleCardClick(item)}
            onClick={
                useDebounceFn(handleCardClick, {
                    wait: DEBOUNCE_TIME,
                    leading: true
                }).run
            }
            style={{
                background: `#fff url(${rightIcon}) right top/contain no-repeat `,
                cursor: "pointer",
                borderRadius: 10
            }}
            id={value}
        >
            <Meta
                avatar={
                    <Avatar
                        src={avatar}
                        style={{
                            width: 160,
                            height: 130,
                            borderRadius: 5,
                            marginRight: 32
                        }}
                    />
                }
                title={
                    <div
                        style={{
                            fontSize: 30,
                            paddingTop: 20
                        }}
                    >
                        {title}
                    </div>
                }
                description={
                    <div
                        style={{
                            fontSize: 16
                        }}
                    >
                        {description}
                    </div>
                }
            />
        </Card>
    )
}

export default memo(StationCard)
