import React, {memo, useEffect, useState} from "react"
import {Button, message, Select, Typography} from "antd"
import store from "@/stores"
import {useTranslation} from "react-i18next";
import {request_work_station} from "@/pages/wms/station/constants/constant";

const {Title} = Typography

const SelectStation = ({isConfigSationId, setIsConfigStationId}: any) => {
    const [stationId, setStationId] = useState("")
    const [options, setOptions] = useState<any[]>([])
    const [error, setError] = useState("")

    useEffect(() => {
        if (isConfigSationId) return
        getStationOptions()
    }, [store.warehouse.code, isConfigSationId])

    const getStationOptions = () => {
        request_work_station(store.warehouse.code).then((res: any) => {
            setOptions(res.data.items)
        })
    }

    const handleChange = (val: string) => {
        setStationId(val)
        setError("")
    }

    const handleClick = () => {
        if (!stationId) {
            setError(t("station.home.div.selectStation"))
            message.error(t("station.home.div.selectStation"))
            return;
        }
        setIsConfigStationId(true)
        localStorage.setItem("stationId", stationId)
    }

    const {t} = useTranslation();

    return (
        <div className="w-full h-full d-flex flex-col justify-center items-center">
            <Title level={4} className="mb-3">
                {t("station.home.div.selectStation")}
            </Title>
            <Select
                style={{width: 300}}
                value={stationId}
                onChange={handleChange}
                options={options}
                fieldNames={{label: "stationName", value: "id"}}
                status={error ? "error" : ""}
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
                {t("station.home.button.confirm")}
            </Button>
        </div>
    )
}

export default memo(SelectStation)
