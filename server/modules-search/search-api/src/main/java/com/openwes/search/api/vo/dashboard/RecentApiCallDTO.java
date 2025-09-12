package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@Data
@SearchBean(tables = "a_api_log",
        where = "create_time >= :todayStart AND create_time < :tomorrowStart")
public class RecentApiCallDTO {
    @DbField("api_code")
    private String endpoint;

    @DbField(value = "'GET'", alias = "method") // You can extract method from api_code if needed
    private String method;

    @DbField("status")
    private String status;

    @DbField("cost_time")
    private Integer responseTime;

    @DbField("create_time")
    private Long timestamp;
}
