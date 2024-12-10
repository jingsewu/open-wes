package org.openwes.wes.api.algo.dto;

import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class PickingOrderReallocateContext {

    @NotEmpty
    private String warehouseCode;

    @NotEmpty
    private PickingOrderDTO pickingOrder;

    private List<PickingOrderReallocateDetail> pickingOrderReallocateDetails;

    private WarehouseAreaWorkTypeEnum warehouseAreaWorkType;

    public void addPickingOrderReallocateDetail(Long detailId, List<ContainerStockDTO> containerStocks, List<SkuBatchStockDTO> skuBatchStockDTOS) {
        this.pickingOrderReallocateDetails.stream().filter(v -> Objects.equals(v.getPickingOrderDetailId(), detailId))
                .findFirst().orElseThrow().addReallocateDetail(containerStocks, skuBatchStockDTOS);
    }

    public PickingOrderReallocateDetail getPickingOrderReallocateDetail(Long detailId) {
        return this.pickingOrderReallocateDetails.stream().filter(v -> Objects.equals(v.getPickingOrderDetailId(), detailId)).findFirst().orElseThrow();

    }

    public Long getWarehouseAreaId(Long skuBatchStockId) {
        SkuBatchStockDTO skuBatchStockDTO = this.pickingOrderReallocateDetails
                .stream().flatMap(v -> v.getSkuBatchStocks().stream())
                .filter(v -> v.getId().equals(skuBatchStockId))
                .findFirst().orElse(null);
        return skuBatchStockDTO == null ? -1L : skuBatchStockDTO.getWarehouseAreaId();
    }

    @Data
    @Accessors(chain = true)
    public static class PickingOrderReallocateDetail {

        private Long pickingOrderDetailId;
        private Long skuBatchStockId;
        private List<ContainerStockDTO> containerStocks;
        private List<SkuBatchStockDTO> skuBatchStocks;

        public List<ContainerStockDTO> getSortedContainerStocks() {
            if (this.containerStocks == null) {
                return Collections.emptyList();
            }
            // implement sorted , sorted by sku batch id and then by warehouse area id
            Map<Long, SkuBatchStockDTO> skuBatchStockDTOMap = this.skuBatchStocks.stream()
                    .collect(Collectors.toMap(SkuBatchStockDTO::getId, v -> v));
            return this.containerStocks.stream()
                    .sorted(Comparator.comparing((ContainerStockDTO c) -> !c.getSkuBatchStockId().equals(this.skuBatchStockId))
                            .thenComparing((ContainerStockDTO c) -> !getWarehouseAreaId(skuBatchStockDTOMap, c.getSkuBatchStockId())
                                    .equals(getWarehouseAreaId(skuBatchStockDTOMap, this.skuBatchStockId)))
                            .thenComparing(c -> getWarehouseAreaId(skuBatchStockDTOMap, c.getSkuBatchStockId())))
                    .toList();
        }

        public Long getWarehouseAreaId(Map<Long, SkuBatchStockDTO> skuBatchStockDTOMap, Long skuBatchStockId) {
            if (skuBatchStockDTOMap == null) {
                return -1L;
            }
            if (skuBatchStockDTOMap.get(skuBatchStockId) == null) {
                return -1L;
            }
            return skuBatchStockDTOMap.get(skuBatchStockId).getWarehouseAreaId();
        }

        public void addReallocateDetail(List<ContainerStockDTO> containerStocks, List<SkuBatchStockDTO> skuBatchStockDTOS) {
            if (this.containerStocks == null) {
                this.containerStocks = containerStocks;
            } else {
                this.containerStocks.addAll(containerStocks);
            }
            if (this.skuBatchStocks == null) {
                this.skuBatchStocks = skuBatchStockDTOS;
            } else {
                this.skuBatchStocks.addAll(skuBatchStockDTOS);
            }
        }
    }

}
