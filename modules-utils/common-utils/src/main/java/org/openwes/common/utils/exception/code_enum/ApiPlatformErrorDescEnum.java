package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiPlatformErrorDescEnum implements IBaseError {

    ERR_API_CODE_EXISTS("AP001001", "ERR_API_CODE_EXISTS", AppCodeEnum.API_PLATFORM.name()),
    ERR_API_NOT_EXISTS("AP001002", "ERR_API_NOT_EXISTS", AppCodeEnum.API_PLATFORM.name()),

    // ems
    EMS_CONTAINER_TASKS_IS_EMPTY("AP002001", "container tasks is empty", AppCodeEnum.API_PLATFORM.name())
    ;

    private final String code;
    private final String desc;
    private final String appCode;

}


