package org.openwes.api.platform.utils;

import org.openwes.api.platform.api.exception.TemplateBizException;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.IBaseError;
import org.apache.commons.lang3.StringUtils;

public class AssertUtils {

    public static void notNull(Object obj, IBaseError errorCodeEnum) {
        if (null == obj) {
            throwBizException(errorCodeEnum);
        }
    }

    public static void notNull(Object obj, IBaseError errorCodeEnum, Object[] args) {
        if (null == obj) {
            throwBizException(errorCodeEnum, args);
        }
    }

    public static void notNull(Object obj, IBaseError errorCodeEnum, String message) {
        if (null == obj) {
            throwBizException(errorCodeEnum, message);
        }
    }

    public static void notBlank(String obj, IBaseError errorCodeEnum) {
        if (StringUtils.isBlank(obj)) {
            throwBizException(errorCodeEnum);
        }
    }

    public static void notBlank(String obj, IBaseError errorCodeEnum, String message) {
        if (StringUtils.isBlank(obj)) {
            throwBizException(errorCodeEnum, message);
        }
    }

    public static void checkExpression(boolean expression, IBaseError errorCodeEnum) {
        if (!expression) {
            throwBizException(errorCodeEnum);
        }
    }

    public static void checkExpression(boolean expression, IBaseError errorCodeEnum, Object[] args) {
        if (!expression) {
            throwBizException(errorCodeEnum, args);
        }
    }


    public static void checkExpression(boolean expression, IBaseError errorCodeEnum, String message) {
        if (!expression) {
            throwBizException(errorCodeEnum);
        }
    }

    public static void throwBizException(IBaseError errorCodeEnum) {
        throwBizException(errorCodeEnum, errorCodeEnum.getDesc());
    }

    public static void throwBizException(IBaseError errorCodeEnum, String message) {
        throw WmsException.throwWmsException(errorCodeEnum);
    }

    public static void throwBizException(IBaseError errorCodeEnum, Object[] args) {
        throw WmsException.throwWmsException(errorCodeEnum, args);
    }

    public static void throwTemplateBizException(TemplateBizException e) {
        throw e;
    }
}
