import React, {memo, useEffect, useState} from "react"
import {Button, message, Select, Typography, Spin, Alert} from "antd"
import store from "@/stores"
import {useTranslation} from "react-i18next"
import {request_work_station} from "@/pages/wms/station/constants/constant"

const {Title} = Typography

interface SelectStationProps {
    isConfigStationId: boolean
    setIsConfigStationId: (value: boolean) => void
}

const SelectStation = ({isConfigStationId, setIsConfigStationId}: SelectStationProps) => {
    const {t} = useTranslation()
    const [stationId, setStationId] = useState("")
    const [options, setOptions] = useState<any[]>([])
    const [loading, setLoading] = useState(false)
    const [fetchError, setFetchError] = useState("")

    useEffect(() => {
        if (isConfigStationId) return

        let isMounted = true

        const fetchStations = async () => {
            try {
                setLoading(true)
                setFetchError("")
                const res: any = await request_work_station(store.warehouse.code)
                
                if (!isMounted) return
                
                setOptions(res?.data?.items || [])
            } catch (error: any) {
                if (!isMounted) return
                
                console.error("获取工作站列表失败:", error)
                const errorMsg = error?.message || "获取工作站列表失败"
                setFetchError(errorMsg)
                message.error("获取工作站列表失败，请刷新重试")
            } finally {
                if (isMounted) {
                    setLoading(false)
                }
            }
        }

        fetchStations()

        return () => {
            isMounted = false
        }
    }, [store.warehouse.code, isConfigStationId])

    const handleChange = (val: string) => {
        setStationId(val)
    }

    const handleConfirm = () => {
        if (!stationId) {
            message.error(t("station.home.div.selectStation"))
            return
        }
        localStorage.setItem("stationId", stationId)
        setIsConfigStationId(true)
    }

    const handleRetry = () => {
        window.location.reload()
    }

    const isDisabled = loading || !!fetchError

    return (
        <div className="w-full h-full d-flex flex-col justify-center items-center">
            <Title level={4} className="mb-3">
                {t("station.home.div.selectStation")}
            </Title>

            {fetchError && (
                <Alert
                    message="获取工作站列表失败"
                    description={fetchError}
                    type="error"
                    showIcon
                    style={{width: 300, marginBottom: 16}}
                    action={
                        <Button size="small" onClick={handleRetry}>
                            重试
                        </Button>
                    }
                />
            )}

            <Spin spinning={loading}>
                <Select
                    style={{width: 300}}
                    value={stationId}
                    onChange={handleChange}
                    options={options}
                    fieldNames={{label: "stationName", value: "id"}}
                    disabled={isDisabled}
                    placeholder="请选择工作站"
                    notFoundContent="暂无工作站"
                />
            </Spin>

            <Button
                type="primary"
                style={{
                    width: 300,
                    backgroundColor: "#23c560",
                    borderColor: "#23c560",
                    marginTop: 10
                }}
                onClick={handleConfirm}
                disabled={isDisabled}
            >
                {t("station.home.button.confirm")}
            </Button>
        </div>
    )
}

export default memo(SelectStation)
