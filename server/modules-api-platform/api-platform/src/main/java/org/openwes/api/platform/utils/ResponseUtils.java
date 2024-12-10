package org.openwes.api.platform.utils;

import org.openwes.api.platform.api.exception.TemplateBizException;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;
import org.openwes.common.utils.http.Response;
import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;

public class ResponseUtils {

    public static Response buildResponse(Exception e) {
        String code = CommonErrorDescEnum.SYSTEM_EXEC_ERROR.getCode();
        String msg = e.getMessage();
        if (e instanceof WmsException wmsException) {
            code = wmsException.getCode();
            IBaseError baseError = wmsException.getBaseError();
            if (wmsException.getBaseError() != null) {
                msg = MessageFormat.format(baseError.getDesc(), wmsException.getArgs());
            } else {
                msg = MessageFormat.format(wmsException.getMessage(), wmsException.getArgs());
            }

        } else if (e instanceof TemplateBizException templateBizException) {
            msg = templateBizException.getMessage();
        }
        return Response.builder().code(code).msg(msg).build();
    }

    public static boolean isSuccess(Response response) {
        if (response == null) {
            return false;
        }
        return Response.SUCCESS_CODE.equals(response.getCode());
    }

    public static boolean isAllSuccess(List<Response> responseList) {
        if (CollectionUtils.isEmpty(responseList)) {
            return false;
        }
        return responseList.stream().allMatch(i -> Response.SUCCESS_CODE.equals(i.getCode()));
    }
}
