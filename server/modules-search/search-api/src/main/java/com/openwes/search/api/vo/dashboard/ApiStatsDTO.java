package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@Data
@SearchBean(tables = "a_api_log")
public class ApiStatsDTO {
    @DbField(value = "COUNT(*)", alias = "totalRequests")
    private Long totalRequests;

    @DbField(value = "AVG(cost_time)", alias = "avgResponseTime")
    private Double avgResponseTime;

    @DbField(value = "(SUM(CASE WHEN status LIKE 'ERROR%' OR status LIKE '4%' OR status LIKE '5%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*))",
             alias = "errorRate")
    private Double errorRate;
}
