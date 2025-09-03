package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@SearchBean(
        tables = "w_work_station ws LEFT JOIN w_operation_task t ON ws.id = t.work_station_id",
        where = "t.create_time >= :todayStart AND t.create_time < :tomorrowStart",
        groupBy = "ws.station_code, t.task_type "
)
@Data
public class WorkstationTask {
    @DbField("ws.station_code")
    private String stationCode;

    @DbField("t.task_type")
    private String taskType;

    @DbField(value = "COUNT(t.id)")
    private Integer taskCount;

    @DbField(value = "COUNT(t.required_qty)")
    private Integer requiredQty;

    @DbField(value = "COUNT(t.operated_qty)")
    private Integer operatedQty;
}
