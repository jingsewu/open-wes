package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorDescEnum implements IBaseError {

    USER_ERROR_DESC_ENUM("USE001001", "user base error", AppCodeEnum.USER.name()),
    ERR_EMPTY_OLD_CRED("USE001003", "ERR_EMPTY_OLD_PASSWORD", AppCodeEnum.USER.name()),
    ERR_EMPTY_NEW_CRED("USE001004", "ERR_EMPTY_NEW_PASSWORD", AppCodeEnum.USER.name()),
    ERR_CRED_TOO_SHORT("USE001005", "ERR_PASSWORD_TOO_SHORT", AppCodeEnum.USER.name()),
    NO_AUTHED_USER_FOUND("USE001006", "NO_AUTHED_USER_FOUND", AppCodeEnum.USER.name()),
    ERROR_WRONG_OLD_CRED("USE001007", "ERROR_WRONG_OLD_PASSWORD", AppCodeEnum.USER.name()),
    ERR_AUTHED_CLIENT_ALREADY_EXISTS("USE001008", "ERR_AUTHED_CLIENT_ALREADY_EXISTS", AppCodeEnum.USER.name()),
    ERR_EMPTY_AUTH_TYPE("USE001009", "ERR_EMPTY_AUTH_TYPE", AppCodeEnum.USER.name()),
    ERR_CLIENT_DOES_NOT_EXIST("USE001010", "ERR_CLIENT_DOES_NOT_EXIST", AppCodeEnum.USER.name()),
    ERR_INIT_ENUM_MAP("USE001011", "ERR_INIT_ENUM_MAP", AppCodeEnum.USER.name()),
    ERR_EMPTY_STATUS("USE001012", "ERR_EMPTY_STATUS", AppCodeEnum.USER.name()),
    ERR_INCORRECT_STATUS("USE001013", "ERR_INCORRECT_STATUS", AppCodeEnum.USER.name()),
    ERR_ROLE_NOT_FOUND("USE001014", "ERR_ROLE_NOT_FOUND", AppCodeEnum.USER.name()),
    ERR_ROLE_ADMIN_IS_IMMUTABLE("USE001015", "ERR_ROLE_ADMIN_IS_IMMUTABLE", AppCodeEnum.USER.name()),
    ERR_ROLE_CODE_EXISTS("USE001016", "ERR_ROLE_CODE_EXISTS", AppCodeEnum.USER.name()),
    ERR_WRONG_CREDENTIALS("USE001017", "ERR_WRONG_CREDENTIALS", AppCodeEnum.USER.name()),
    ERR_EMPTY_USER_NAME("USE001018", "ERR_EMPTY_USER_NAME", AppCodeEnum.USER.name()),
    ERR_EMPTY_ROLE_ID_SET("USE001019", "ERR_EMPTY_ROLE_ID_SET", AppCodeEnum.USER.name()),
    ERR_USER_NAME_EXISTS("USE001020", "ERR_USER_NAME_EXISTS", AppCodeEnum.USER.name()),
    ERR_EMPTY_USERADD_PARAM("USE001021", "ERR_EMPTY_USERADD_PARAM", AppCodeEnum.USER.name()),
    ERR_EMPTY_USER_ID("USE001022", "ERR_EMPTY_USER_ID", AppCodeEnum.USER.name()),
    ERR_EMPTY_ROLE_ID("USE001023", "ERR_EMPTY_ROLE_ID", AppCodeEnum.USER.name()),
    ERR_ROLE_IS_DISABLE("USE001024", "ERR_ROLE_IS_DISABLE", AppCodeEnum.USER.name()),
    ERR_CRED_NOT_MATCHED("USE001025", "ERR_PASSWORD_NOT_MATCHED", AppCodeEnum.USER.name()),
    ERR_USER_IS_NOT_DISABLED("USE001026", "ERR_USER_IS_NOT_DISABLED", AppCodeEnum.USER.name()),
    ERR_USER_CAN_NOT_DISABLE_OR_DELETE_SELF("USE001027", "ERR_USER_CAN_NOT_DISABLE_OR_DELETE_SELF", AppCodeEnum.USER.name()),
    ERR_ROLE_IS_ENABLE_AND_USED("USE001028", "ERR_ROLE_IS_ENABLE_AND_USED", AppCodeEnum.USER.name()),
    ERR_INVALID_EXTERNAL_LOGIN("USE001029", "ERR_INVALID_EXTERNAL_LOGIN", AppCodeEnum.USER.name()),
    ERR_INVALID_LOGOUT_REDIRECT_URL("USE001030", "ERR_INVALID_LOGOUT_REDIRECT_URL", AppCodeEnum.USER.name());

    private final String code;
    private final String desc;
    private final String appCode;

}
