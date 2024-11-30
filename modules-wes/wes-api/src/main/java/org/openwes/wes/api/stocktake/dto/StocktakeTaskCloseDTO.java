package org.openwes.wes.api.stocktake.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StocktakeTaskCloseDTO implements Serializable {
    private List<Long> taskIdList;
}
