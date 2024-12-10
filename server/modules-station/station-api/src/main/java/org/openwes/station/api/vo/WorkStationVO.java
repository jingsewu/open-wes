package org.openwes.station.api.vo;

import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationProcessingStatusEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallTagConfigDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeCreateMethodEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeMethodEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkStationVO {

    private Long workStationId;
    private String warehouseCode;
    private String warehouseAreaId;
    private String stationCode;
    private String stationName;
    private WorkStationStatusEnum workStationStatus;
    private WorkStationModeEnum workStationMode;
    private ChooseAreaEnum chooseArea;
    private String scanCode;
    private String processingType;
    private String processingInboundOrderDetailId;
    private Integer callContainerCount;

    private List<Tip> tips;

    private WorkLocationArea workLocationArea;
    private SkuArea skuArea;
    private PutWallArea putWallArea;
    private Toolbar toolbar;
    private OrderArea operationOrderArea;

    private WorkStationProcessingStatusEnum stationProcessingStatus;


    @Getter
    @AllArgsConstructor
    public enum ChooseAreaEnum {
        SKU_AREA("skuArea"),
        CONTAINER_AREA("containerArea"),
        PUT_WALL_AREA("putWallArea"),
        SCAN_AREA("scanArea"), // 主要用于非缓存货架的空箱出库
        ORDER_AREA("orderArea"),
        TIPS("tips");
        private final String value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tip {
        private TipTypeEnum tipType;
        private String type;
        private Object data;
        private Long duration;
        private String tipCode;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Tip tip = (Tip) o;
            return Objects.equals(tipType, tip.tipType) && Objects.equals(tipCode, tip.tipCode);
        }

        @Override
        public int hashCode() {
            return tipType.hashCode() + tipCode.hashCode();
        }

        @Getter
        public enum TipTypeEnum {
            // 空箱处理提示
            EMPTY_CONTAINER_HANDLE_TIP,
            // 选择拣选任务提示
            CHOOSE_PICKING_TASK_TIP,
            // 封箱提示
            SEAL_CONTAINER_TIP,
            // 异常登记
            REPORT_ABNORMAL_TIP,
            // 扫描错误提示
            SCAN_ERROR_REMIND_TIP,
            // 整箱出库不拣选提示
            FULL_CONTAINER_AUTO_OUTBOUND_TIP,
            // 语音提醒
            PICKING_VOICE_TIP,
            /*入站异常提示*/
            INBOUND_ABNORMAL_TIP,
            /*一码多品提示*/
            BARCODE_2_MANY_SKU_CODE_TIP,
            /*skuCode对应多条订单或者多个货主提示*/
            SKU_ORDERS_OR_OWNER_CODES_TIP,

        }

        @Getter
        @AllArgsConstructor
        public enum TipShowTypeEnum {
            TIP("tip", "提示框"),
            CONFIRM("confirm", "弹框"),
            VOICE("voice", "语音播报"),
            ;

            private final String value;
            private final String name;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Toolbar {
        private boolean enableReportAbnormal;
        private boolean enableSplitContainer;
        private boolean enableReleaseSlot;
    }

    @Data
    public static class WorkLocationArea {
        private List<WorkLocationExtend> workLocationViews;

        public WorkLocationArea(List<WorkStationDTO.WorkLocation<? extends WorkStationDTO.WorkLocationSlot>> workLocations) {
            if (CollectionUtils.isEmpty(workLocations)) {
                return;
            }

            workLocationViews = workLocations.stream().map(v -> {
                WorkLocationExtend workLocationExtend = new WorkLocationExtend();
                BeanUtils.copyProperties(v, workLocationExtend);
                if (ObjectUtils.isNotEmpty(v.getWorkLocationSlots())) {
                    List<WorkLocationExtend.WorkLocationSlotExtend> workLocationSlotExtends = v.getWorkLocationSlots().stream().map(slot -> {
                        WorkLocationExtend.WorkLocationSlotExtend workLocationSlotExtend = new WorkLocationExtend.WorkLocationSlotExtend();
                        workLocationSlotExtend.setWorkLocationCode(slot.getWorkLocationCode());
                        workLocationSlotExtend.setSlotCode(slot.getSlotCode());
                        workLocationSlotExtend.setLevel(slot.getLevel());
                        workLocationSlotExtend.setBay(slot.getBay());
                        workLocationSlotExtend.setEnable(slot.isEnable());
                        workLocationSlotExtend.setGroupCode(slot.getGroupCode());
                        return workLocationSlotExtend;
                    }).toList();
                    workLocationExtend.setWorkLocationSlots(workLocationSlotExtends);
                }
                return workLocationExtend;
            }).toList();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuArea {
        private String pickType;
        private List<SkuTaskInfo> pickingViews;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SkuTaskInfo {
            private SkuMainDataDTO skuMainDataDTO;
            private SkuBatchAttributeDTO skuBatchAttributeDTO;
            private List<OperationTaskDTO> operationTaskDTOS;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PutWallArea {
        private String putWallDisplayStyle;
        private PutWallTagConfigDTO putWallTagConfigDTO;
        private List<PutWallDTO> putWallViews;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class OrderArea {
        private StocktakeOrderVO currentStocktakeOrder;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class StocktakeOrderVO {
        //common

        //stocktake
        private String taskNo;
        private StocktakeCreateMethodEnum stocktakeCreateMethod;
        private StocktakeMethodEnum stocktakeMethod;
        private StocktakeTypeEnum stocktakeType;
    }


}
