package org.openwes.wes.api.config.dto;

import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BarcodeParseRequestDTO implements Serializable {

    @NotEmpty
    private String barcode;

    private String ownerCode;

    private List<KnownSku> knownSkus;

    @NotNull
    private BusinessFlowEnum businessFlow;
    @NotNull
    private ExecuteTimeEnum executeTime;

    @Data
    public static class KnownSku implements Serializable {

        @NotEmpty
        private String ownerCode;
        @NotEmpty
        private String skuCode;
        @NotEmpty
        private String brand;
    }
}
