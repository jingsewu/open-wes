package org.openwes.wes.stocktake.infrastructure.persistence.transfer;

import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeTaskDetailPO;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeTaskPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StocktakeTaskPOTransfer {

    StocktakeTaskPO toPO(StocktakeTask stocktakeTask);

    List<StocktakeTaskPO> toPOS(List<StocktakeTask> stocktakeTaskList);

    List<StocktakeTaskDetailPO> toDetailPOS(List<StocktakeTaskDetail> details);

    List<StocktakeTaskDetail> toDetailDOS(List<StocktakeTaskDetailPO> detailPOS);

    StocktakeTask toDO(StocktakeTaskPO stocktakeTaskPO, List<StocktakeTaskDetailPO> details);

    List<StocktakeTask> toDOS(List<StocktakeTaskPO> taskPOS);

    StocktakeTask toDO(StocktakeTaskPO stocktakeTaskPO);

    StocktakeTaskDetail toDetailDO(StocktakeTaskDetailPO stocktakeTaskDetailPO);

    StocktakeTaskDetailPO toDetailPO(StocktakeTaskDetail stocktakeTaskDetail);
}
