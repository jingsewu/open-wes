package org.openwes.wes.api.ems.proxy.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmsLocationConfigDTO implements Serializable {

    private Long id;
    private String locationCode;
    private LocationType locationType;
    private Long warehouseAreaId;
    private String warehouseCode;

    /**
     * 暂时还不能很好的按照类型抽象，暂时先用已知的动作抽象
     */
    @Data
    public static class LocationType implements Serializable {
        private boolean notifyWorkStation;
        private boolean notifyTransferContainer;
        private boolean finishContainerTask;
    }
}
