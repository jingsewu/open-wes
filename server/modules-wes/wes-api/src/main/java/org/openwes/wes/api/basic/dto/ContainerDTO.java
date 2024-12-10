package org.openwes.wes.api.basic.dto;

import org.openwes.wes.api.basic.constants.ContainerStatusEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5116833747755076925L;

    private Long id;

    @NotEmpty
    private String containerCode;

    @NotEmpty
    private String containerSpecCode;

    @NotEmpty
    private String warehouseCode;
    private String warehouseAreaCode;
    private String warehouseLogicCode;
    private Long warehouseAreaId;
    private Long warehouseLogicId;
    private String locationCode;
    private String locationType;

    private BigDecimal occupationRatio;

    private boolean emptyContainer;
    private boolean locked;
    private boolean opened;

    @NotNull
    @Min(1)
    private Integer containerSlotNum;

    @NotNull
    @Min(0)
    private Integer emptySlotNum;

    private ContainerStatusEnum containerStatus;

    private Long version;

    private List<ContainerSlot> containerSlots;

    @Data
    public static class ContainerSlot implements Serializable {

        @Serial
        private static final long serialVersionUID = -5101149868271542343L;

        // unique in a container
        private String containerSlotCode;
        private String containerSlotSpecCode;

        private BigDecimal occupationRatio;
        /**
         * 是否空格口
         */
        private boolean emptySlot;

        /**
         * 面
         */
        private String face;

        // every container slot has a unique location code.
        private String locationCode;

        private List<ContainerSlot> children;

        public void setContainerSlotCode() {
            //use container slot spec code as the container slot code because container slot spec code is unique in a container spec
            this.containerSlotCode = this.containerSlotSpecCode;
            if (CollectionUtils.isNotEmpty(this.children)) {
                this.children.forEach(ContainerSlot::setContainerSlotCode);
            }
        }
    }
}
