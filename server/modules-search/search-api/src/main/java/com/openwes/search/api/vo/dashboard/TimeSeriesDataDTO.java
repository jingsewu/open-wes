package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@Data
@SearchBean(tables = "a_api_log", groupBy = "timeSlot", orderBy = "timeSlot asc")
public class TimeSeriesDataDTO {

    @DbField(value = "CONCAT(LPAD(HOUR(FROM_UNIXTIME(create_time)), 2, '0'), ':00')", alias = "timeSlot")
    private String timeSlot;

    @DbField(value = "AVG(cost_time)", alias = "avgResponseTime")
    private Double avgResponseTime;

    @DbField(value = "COUNT(*)", alias = "requestCount")
    private Long requestCount;
}
