package com.openwes.search.api.vo;

import cn.zhxu.bs.bean.SearchBean;
import lombok.Data;

import java.math.BigDecimal;

@Data
@SearchBean(tables = "w_container")
public class ContainerSearchVO {

    private Long id;

    private String containerCode;
    private String warehouseCode;

    private String containerSpecCode;

    private Long warehouseAreaId;
    private String warehouseAreaCode;
    private String warehouseLogicCode;
    private Long warehouseLogicId;
    private String locationCode;
    private String locationType;

    private BigDecimal occupationRatio;

    private boolean emptyContainer;
    private boolean locked;
    private boolean opened;

    private Integer containerSlotNum;
    private Integer emptySlotNum;

    private String containerStatus;
}
