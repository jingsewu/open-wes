package org.openwes.station.api.model;

import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tip {
    private TipTypeEnum tipType;
    private String type;
    private Object data;
    private Long duration;
    private String tipCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return Objects.equals(tipType, tip.tipType) && Objects.equals(tipCode, tip.tipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipType, tipCode);
    }

    @Getter
    public enum TipTypeEnum {
        EMPTY_CONTAINER_HANDLE_TIP,
        CHOOSE_PICKING_TASK_TIP,
        SEAL_CONTAINER_TIP,
        REPORT_ABNORMAL_TIP,
        SCAN_ERROR_REMIND_TIP,
        FULL_CONTAINER_AUTO_OUTBOUND_TIP,
        PICKING_VOICE_TIP,
        INBOUND_ABNORMAL_TIP,
        BARCODE_2_MANY_SKU_CODE_TIP,
        SKU_ORDERS_OR_OWNER_CODES_TIP,
    }

    @Getter
    @AllArgsConstructor
    public enum TipShowTypeEnum {
        TIP("tip", "tip"),
        CONFIRM("confirm", "confirm dialog"),
        VOICE("voice", "voice broadcast"),
        ;
        private final String value;
        private final String name;
    }
}
