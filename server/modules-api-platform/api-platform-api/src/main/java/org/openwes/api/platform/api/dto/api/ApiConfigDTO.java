package org.openwes.api.platform.api.dto.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import org.openwes.common.utils.base.UpdateUserPO;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiConfigDTO extends UpdateUserPO {

    private Long id;

    private String code;

    private ConverterTypeEnum paramConverterType;

    private ConverterTypeEnum responseConverterType;

    private String jsParamConverter;
    private String jsResponseConverter;

    private String templateParamConverter;
    private String templateResponseConverter;

}
