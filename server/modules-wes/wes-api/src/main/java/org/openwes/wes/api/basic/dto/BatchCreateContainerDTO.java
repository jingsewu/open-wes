package org.openwes.wes.api.basic.dto;

import org.openwes.common.utils.validate.IValidate;
import org.openwes.wes.api.basic.constants.ContainerTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchCreateContainerDTO implements IValidate {

    @NotEmpty
    private String warehouseCode;

    @NotNull
    private ContainerTypeEnum containerType;

    @NotEmpty
    private String containerSpecCode;

    @NotEmpty
    private String containerCodePrefix;

    @NotNull
    @Min(1)
    private Integer startIndex;

    @NotNull
    @Min(1)
    private Integer indexNumber;

    @NotNull
    @Min(1)
    private Integer createNumber;

    @Override
    public boolean validate() {
        return startIndex + createNumber <= getEndIndex();
    }

    public Integer getEndIndex() {
        return (int) Math.pow(10d, indexNumber - 1d);
    }
}
