package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@SearchBean(
        tables = "w_outbound_plan_order",
        where = "create_time >= :todayStart AND create_time < :tomorrowStart",
        groupBy = "outbound_plan_order_status"
)
@Data
public class OutboundProgress {
    @DbField("outbound_plan_order_status")
    private String name;

    @DbField(value = "COUNT(*)")
    private Integer value;
}
