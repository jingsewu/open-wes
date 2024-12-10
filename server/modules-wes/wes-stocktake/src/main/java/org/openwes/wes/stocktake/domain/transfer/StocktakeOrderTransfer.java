package org.openwes.wes.stocktake.domain.transfer;

import org.openwes.wes.api.stocktake.dto.StocktakeOrderCreateDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeOrderDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrderDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StocktakeOrderTransfer {

    StocktakeOrder fromCreateDTO(StocktakeOrderCreateDTO stocktakeOrderCreateDTO);

    StocktakeOrderDetail toDetail(StocktakeOrderCreateDTO stocktakeOrderCreateDTO);

    StocktakeOrder toDO(StocktakeOrderDTO stocktakeOrderDTO);

    StocktakeOrderDTO toDTO(StocktakeOrder order);

    default List<StocktakeOrderDetail> toDetails(StocktakeOrderCreateDTO stocktakeOrderCreateDTO) {
        List<StocktakeOrderDetail> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(stocktakeOrderCreateDTO.getUnitIds())) {
            for (Long unitId : stocktakeOrderCreateDTO.getUnitIds()) {
                StocktakeOrderDetail detail = this.toDetail(stocktakeOrderCreateDTO);
                detail.setUnitId(unitId);
                list.add(detail);
            }
        }
        if (CollectionUtils.isNotEmpty(stocktakeOrderCreateDTO.getUnitCodes())) {
            for (String unitCode : stocktakeOrderCreateDTO.getUnitCodes()) {
                StocktakeOrderDetail detail = this.toDetail(stocktakeOrderCreateDTO);
                detail.setUnitCode(unitCode);
                list.add(detail);
            }
        }
        return list;
    }
}
