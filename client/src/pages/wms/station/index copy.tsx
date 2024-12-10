// import React, { useEffect, useState } from "react"
import schema2component from "@/utils/schema2component"
const schema = {
    type: "page",
    data: {
        items: [
            {
                apiCode: "SELECT_CONTAINER_PUT_AWAY",
                browser: "选择容器上架"
            },
            {
                apiCode: "PICKING",
                browser: "出库"
            },
            {
                apiCode: "COUNTING",
                browser: "盘点"
            },
            {
                apiCode: "ONE_STEP_RELOCATION",
                browser: "理货"
            }
        ]
    },
    body: {
        type: "cards",
        source: "$items",
        card: {
            header: {
                avatarText: "${browser|substring:0:2}",
                avatarTextBackground: [
                    "#FFB900",
                    "#D83B01",
                    "#B50E0E",
                    "#E81123",
                    "#B4009E",
                    "#5C2D91",
                    "#0078D7",
                    "#00B4FF",
                    "#008272"
                ],
                title: "${browser}",
                titleClassName: "text-xl h-full	d-flex items-center	"
                // highlight: true
            },
            itemAction: {
                type: "button",
                actionType: "ajax",
                // api: "put:/station/api?apiCode=ONLINE",
                api: {
                    method: "put",
                    url: "/station/api?apiCode=ONLINE",
                    headers: { "Content-Type": "text/plain" },
                    requestAdaptor: function (api: any, context: any) {
                        return {
                            ...api,
                            data: context.apiCode
                        }
                    }
                }
            },
            style: {
                display: "flex"
                // height: "150px"
            }
        }
    }
}

export default schema2component(schema)
