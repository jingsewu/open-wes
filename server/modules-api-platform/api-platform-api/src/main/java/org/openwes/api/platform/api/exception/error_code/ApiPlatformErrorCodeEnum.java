package org.openwes.api.platform.api.exception.error_code;

import org.openwes.common.utils.constants.AppCodeEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiPlatformErrorCodeEnum implements IBaseError {

    API_API_TYPE_ERROR("API001001", "Can not found handler apiType {0}", AppCodeEnum.API_PLATFORM.name()),
    API_TEMPLATE_PARSE_ERROR("API001002", "Can not parse template {0}", AppCodeEnum.API_PLATFORM.name()),
    API_DATA_PARSE_ERROR("API001003", "Can not parse data {0}", AppCodeEnum.API_PLATFORM.name()),
    API_API_TYPE_NOT_EXIST("API001004", "Api code {0} is not exist", AppCodeEnum.API_PLATFORM.name()),
    API_API_IS_NOT_ENABLE("API001005", "Api code {0} is not enable", AppCodeEnum.API_PLATFORM.name());

    private final String code;
    private final String desc;
    private final String appCode;
}
