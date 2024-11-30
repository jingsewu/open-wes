package org.openwes.wes.basic.warehouse.controller.parameter;

import org.openwes.wes.api.basic.constants.LocationTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class AisleLocationVO {

    @NotEmpty
    private String warehouseCode;
    @NotNull
    private Long warehouseAreaId;
    @NotNull
    private LocationTypeEnum locationType;
    @NotEmpty
    private List<LocationDesc> locationDescList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationDesc {

        private String warehouseAreaGroupCode;
        private String warehouseAreaCode;

        @NotEmpty
        private String aisleCode;
        @NotEmpty
        private String shelfLine;
        @NotNull
        private Long warehouseLogicId;
        @NotNull
        private String containerSpecCode;
        @NotNull
        @Min(1)
        private Integer shelfNumber;
        private boolean singleEntrance;

        private String addZero(int number, int size) {
            String format = "%0" + size + "d";
            return String.format(format, number);
        }

        public String generateShelfCode(int shelfNo) {
            return this.shelfLine + addZero(shelfNo, 2);
        }

        public String generateLocationCode(int shelfNo, int bay, int line) {
            return this.warehouseAreaGroupCode + this.warehouseAreaCode +
                this.aisleCode + "-" + generateShelfCode(shelfNo) + "-" + addZero(bay, 2) + addZero(line, 2);
        }
    }

}
