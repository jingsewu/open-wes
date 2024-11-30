package org.openwes.wes.basic.container.domain.entity;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.basic.constants.ContainerStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.CONTAINER_SPECIFIC_CANNOT_CHANGE;

@Data
@Slf4j
public class Container {

    private Long id;

    //unique identifier = containerCode + warehouseCode
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

    private ContainerStatusEnum containerStatus;

    private List<ContainerDTO.ContainerSlot> containerSlots;

    private Long version;

    public Container(String warehouseCode, String containerCode, String containerSpecCode, @NotNull List<ContainerDTO.ContainerSlot> containerSlots) {
        this.warehouseCode = warehouseCode;
        this.containerSlots = containerSlots;
        this.containerCode = containerCode;
        this.containerSpecCode = containerSpecCode;
        this.containerStatus = ContainerStatusEnum.OUT_SIDE;
        this.emptyContainer = true;
        this.emptySlotNum = containerSlots.size();
        this.containerSlotNum = containerSlots.size();
        this.occupationRatio = BigDecimal.ZERO;

        this.containerSlots.forEach(ContainerDTO.ContainerSlot::setContainerSlotCode);

        checkContainerSlotCodeUnique();
    }

    private void checkContainerSlotCodeUnique() {
        //todo implement it
    }

    public void changeContainerSpec(String containerSpecCode, List<ContainerDTO.ContainerSlot> containerSlots) {
        log.info("container id: {} code: {} change container spec to: {}", this.id, this.containerCode, containerSpecCode);

        if (this.containerStatus != ContainerStatusEnum.OUT_SIDE) {
            throw new WmsException(CONTAINER_SPECIFIC_CANNOT_CHANGE);
        }

        this.containerSpecCode = containerSpecCode;
        this.containerSlots = containerSlots;
        this.containerSlots.forEach(ContainerDTO.ContainerSlot::setContainerSlotCode);
    }

    public void lock() {
        log.info("container id: {} code: {} lock", this.id, this.containerCode);

        if (this.locked) {
            throw new IllegalStateException("container is already locked");
        }
        this.locked = true;
    }

    public void unlock() {
        log.info("container id: {} code: {} unlock", this.id, this.containerCode);
        this.locked = false;
    }

    public void changeStocks(List<ContainerStockDTO> containerStocks) {
        log.info("container id: {} code: {} change stocks", this.id, this.containerCode);

        containerStocks = containerStocks.stream().filter(v -> v.getTotalQty() > 0).toList();

        if (CollectionUtils.isEmpty(containerStocks)) {
            this.emptyContainer = true;
            this.emptySlotNum = containerSlotNum;
            this.occupationRatio = BigDecimal.ZERO;
            this.containerSlots.forEach(containerSlot -> {
                containerSlot.setEmptySlot(true);
                containerSlot.setOccupationRatio(BigDecimal.ZERO);
            });
            return;
        }

        this.emptyContainer = false;
        Set<String> notEmptySlots = containerStocks.stream().filter(v -> v.getTotalQty() > 0)
                .map(ContainerStockDTO::getContainerSlotCode).collect(Collectors.toSet());
        this.emptySlotNum = this.containerSlotNum - notEmptySlots.size();
        this.occupationRatio = BigDecimal.valueOf(this.containerSlotNum - (long) this.emptySlotNum)
                .divide(BigDecimal.valueOf(this.containerSlotNum), 2, RoundingMode.HALF_EVEN);
        containerSlots.forEach(containerSlot -> {
            if (notEmptySlots.contains(containerSlot.getContainerSlotCode())) {
                containerSlot.setEmptySlot(false);
                containerSlot.setOccupationRatio(BigDecimal.ONE);
            }
        });
    }

    public void changeLocation(String warehouseCode, Long warehouseAreaId, String locationCode, String locationType) {
        this.warehouseCode = warehouseCode;
        this.warehouseAreaId = warehouseAreaId;
        this.locationCode = locationCode;
        this.locationType = locationType;
    }

    public void moveOutside() {
        this.containerStatus = ContainerStatusEnum.OUT_SIDE;
    }
}
