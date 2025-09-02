package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@SearchBean(
        tables = "w_inbound_plan_order",
        where = "create_time >= :todayStart AND create_time < :tomorrowStart",
        groupBy = "inbound_plan_order_status"
)
@Data
public class InboundProgress {
    @DbField("inbound_plan_order_status")
    private String name;

    @DbField(value = "COUNT(*)")
    private Integer value;
}
