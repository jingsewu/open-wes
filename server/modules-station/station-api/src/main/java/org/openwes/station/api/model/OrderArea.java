package org.openwes.station.api.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderArea {
    private OrderVO currentOrder;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderVO {
        private String orderNo;
        private String orderType;
        private String stocktakeCreateMethod;
        private String stocktakeMethod;
        private String stocktakeType;
    }
}
