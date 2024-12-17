import React, { useState, useEffect, memo } from "react"
import { Select, Typography, Button } from "antd"
import store from "@/stores"
import request from "@/utils/requestInterceptor"

const { Title } = Typography

const SelectStation = ({ isConfigSationId, setIsConfigStationId }: any) => {
    const [stationId, setStationId] = useState("")
    const [options, setOptions] = useState<any[]>([])

    useEffect(() => {
        getStationId()
    }, [])

    useEffect(() => {
        if (isConfigSationId) return
        getStationOptions()
    }, [store.warehouse.code, isConfigSationId])

    const getStationId = async () => {
        const res: any = await request({
            method: "get",
            url: "/station/api"
        })
        if (res?.data?.errorCode === "SAT010001") {
            setIsConfigStationId(false)
        } else {
            setIsConfigStationId(true)
        }
    }

    const getStationOptions = () => {
        request({
            method: "post",
            url:
                "/search/search?page=1&perPage=1000&warehouseCode-op=eq&warehouseCode=" +
                store.warehouse.code,
            data: {
                searchIdentity: "WWorkStation",
                searchObject: {
                    orderBy: "update_time desc"
                },
                showColumns: [
                    {
                        name: "id",
                        label: "ID",
                        hidden: true
                    },
                    {
                        name: "version",
                        label: "Version",
                        hidden: true
                    },
                    {
                        name: "warehouseCode",
                        label: "table.warehouseCode",
                        hidden: true
                    },
                    {
                        name: "stationCode",
                        label: "table.workstationCoding",
                        searchable: true
                    },
                    {
                        name: "stationName",
                        label: "table.workstationName"
                    }
                ]
            }
        }).then((res: any) => {
            setOptions(res.data.items)
        })
    }

    const handleChange = (val: string) => {
        setStationId(val)
    }

    const handleClick = () => {
        setIsConfigStationId(true)
        localStorage.setItem("stationId", stationId)
    }

    return (
        <div className="w-full h-full d-flex flex-col justify-center items-center">
            <Title level={4} className="mb-3">
                请选择工作站
            </Title>
            <Select
                // defaultValue="lucy"
                style={{ width: 300 }}
                value={stationId}
                onChange={handleChange}
                options={options}
                fieldNames={{ label: "stationName", value: "id" }}
            />
            <Button
                type="primary"
                style={{
                    width: 300,
                    backgroundColor: "#23c560",
                    borderColor: "#23c560",
                    marginTop: 10
                }}
                onClick={handleClick}
            >
                确定
            </Button>
        </div>
    )
}

export default memo(SelectStation)
