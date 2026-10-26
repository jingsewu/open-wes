package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@SearchBean(
        tables = "w_operation_task o JOIN u_user u ON o.update_user = u.account",
        where = "o.update_time >= :todayStart AND o.update_time < :tomorrowStart",
        autoMapTo = "o",
        groupBy = "u.account",
        orderBy = "totalQty desc"
)
@Data
public class OperatorRanking {
    @DbField("u.account")
    private String account;

    @DbField("u.name")
    private String operator;

    @DbField(value = "SUM(o.operated_qty)", alias = "totalQty")
    private Integer totalQty;

    @DbField(value = "SUM(CASE WHEN o.task_type = 'PUT_AWAY' THEN o.operated_qty ELSE 0 END)")
    private Integer putAwayQty;

    @DbField(value = "SUM(CASE WHEN o.task_type = 'PICKING' THEN o.operated_qty ELSE 0 END)")
    private Integer pickingQty;
}
