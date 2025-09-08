package com.openwes.search.api.vo.dashboard;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

@Data
@SearchBean(tables = "a_api_log", groupBy = "status")
public class ErrorDistributionDTO {

    @DbField("status")
    private String errorType;

    @DbField(value = "COUNT(*)", alias = "count")
    private Long count;
}
