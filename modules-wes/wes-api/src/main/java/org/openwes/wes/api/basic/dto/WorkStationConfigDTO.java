package org.openwes.wes.api.basic.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.openwes.wes.api.basic.constants.PageFieldEnum;
import org.openwes.wes.api.basic.constants.PutWallDisplayOrderEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkStationConfigDTO implements Serializable {

    private Long id;
    private Long workStationId;

    private InboundStationConfigDTO inboundStationConfig;
    private PickingStationConfigDTO pickingStationConfig;
    private StocktakeStationConfigDTO stocktakeStationConfig;
    private RelocationStationConfigDTO relocationStationConfig;

    private Long version;

    public PickingStationConfigDTO getPickingStationConfig() {
        if (this.pickingStationConfig == null) {
            this.pickingStationConfig = new PickingStationConfigDTO();
            this.pickingStationConfig.initialize();
        }
        return this.pickingStationConfig;
    }

    @Data
    public static class InboundStationConfigDTO implements Serializable {
        public void initialize() {

        }
    }

    @Data
    @NoArgsConstructor
    public static class PickingStationConfigDTO implements Serializable {
        private boolean emptyToteRecycle;
        private PutWallDisplayOrderEnum leftPutWallDisplayOrder;
        private PutWallDisplayOrderEnum rightPutWallDisplayOrder;
        private PutWallTagConfigDTO putWallTagConfig;
        @JsonDeserialize(as = List.class)
        private List<PageFieldConfig> putWallSlotFields;

        public void initialize() {
            this.putWallTagConfig = new PutWallTagConfigDTO();
            this.putWallTagConfig.initialize();
            this.putWallSlotFields = Arrays.stream(PageFieldEnum.values())
                    .filter(e -> e.getSupportTaskTypes().contains(OperationTaskTypeEnum.PICKING))
                    .map(e -> new PageFieldConfig(e.getFieldName(), e.getFieldDesc(), "", e.isDisplay(), e.getColor(), e.isBold()))
                    .toList();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageFieldConfig implements Serializable {
        private String fieldName;
        private String fieldDesc;
        private String fieldValue;
        private boolean display;
        private String color;
        private boolean bold;
    }

    @Data
    public static class StocktakeStationConfigDTO implements Serializable {
        public void initialize() {

        }
    }

    @Data
    public static class RelocationStationConfigDTO implements Serializable {
        public void initialize() {

        }
    }
}
