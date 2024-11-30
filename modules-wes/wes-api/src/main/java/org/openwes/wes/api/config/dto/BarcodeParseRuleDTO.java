package org.openwes.wes.api.config.dto;

import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.api.config.constants.UnionLocationEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BarcodeParseRuleDTO {

    private Long id;

    @NotEmpty
    private String code;
    @NotEmpty
    private String name;
    @NotNull
    private ExecuteTimeEnum executeTime;
    @NotNull
    private BusinessFlowEnum businessFlow;

    private String ownerCode;
    private String brand;

    private boolean enable;

    private UnionLocationEnum unionLocation;
    private String unionStr;

    private String regularExpression;

    @NotEmpty
    private List<String> resultFields;

    private Long version;
}
