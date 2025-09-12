package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@Data
@SearchBean(tables = "a_api_log",
        where = "create_time >= :todayStart AND create_time < :tomorrowStart",
        groupBy = "api_code",
        orderBy = "requestCount asc")
public class EndpointStatsDTO {
    @DbField("api_code")
    private String apiCode;

    @DbField(value = "COUNT(*)", alias = "requestCount")
    private Long requestCount;
}
