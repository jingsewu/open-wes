package org.openwes.common.utils.exception.code_enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * InboundErrorDescEnum
 *
 * @author sws
 * @date 2023/02/24
 */
@Getter
@AllArgsConstructor
public enum CommonErrorDescEnum implements IBaseError {


    METHOD_NOT_ALLOWED("CM000001", "method not allowed", COMMON_APP_CODE),
    NOT_FOUND("CM000002", "method not found", COMMON_APP_CODE),

    SYSTEM_EXEC_ERROR("CM010001", "system error.", COMMON_APP_CODE),
    DATABASE_UNIQUE_ERROR("CM010002", "database unique key repeated.", COMMON_APP_CODE),
    JSON_PARSER_ERROR("CM010003", "json {0} parse error {1}.", COMMON_APP_CODE),
    REPEAT_REQUEST("CM010004", "repeat request, please try again later.", COMMON_APP_CODE),
    PARAMETER_ERROR("CM010005", "parameter error.", COMMON_APP_CODE),
    HTTP_REQUEST_ERROR("CM010006", "http request error.", COMMON_APP_CODE),

    FILE_CREATE_ERROR("CM010007", "file {0} create error.", COMMON_APP_CODE),
    DIR_CREATE_ERROR("CM010008", "directory {0} create error.", COMMON_APP_CODE),

    UNKNOWN_SYMBOL("CM010009", "unknown symbol {0}", COMMON_APP_CODE),
    GENERATE_TEMPLATE_FAILED("CM010010", "generate template file failed, reason {0}", COMMON_APP_CODE),
    ;

    private final String code;
    private final String desc;
    private final String appCode;

}
