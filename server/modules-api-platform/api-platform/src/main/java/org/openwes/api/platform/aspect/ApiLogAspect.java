package org.openwes.api.platform.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openwes.api.platform.api.constants.ApiLogStatusEnum;
import org.openwes.api.platform.domain.entity.ApiLogPO;
import org.openwes.api.platform.domain.service.ApiLogService;
import org.openwes.api.platform.utils.SpringExpressionUtils;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.common.utils.utils.JsonUtils;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    private final ApiLogService apiLogService;
    private static final int MAX_STRING_LENGTH = 65535;

    @Around("@annotation(apiLog)")
    public Object around(ProceedingJoinPoint joinPoint, org.openwes.api.platform.aspect.ApiLog apiLog) throws Throwable {
        // Pre-process: Prepare basic log info
        ApiLogPO apiLogPO = prepareApiLog(joinPoint, apiLog);
        long startTime = System.currentTimeMillis();

        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            // Complete log info in finally block to ensure it's always executed
            completeApiLogInfo(apiLogPO, result, exception, startTime, endTime);
            // Save log asynchronously
            apiLogService.saveApiLogAsync(apiLogPO);
        }
    }

    private ApiLogPO prepareApiLog(ProceedingJoinPoint joinPoint, org.openwes.api.platform.aspect.ApiLog apiLog) {
        Object[] args = joinPoint.getArgs();

        ApiLogPO apiLogPO = new ApiLogPO();

        // Set API code
        String apiCode = SpringExpressionUtils.generateKeyBySpEL(apiLog.apiCode(), joinPoint);
        apiLogPO.setApiCode(apiCode);

        // Set message ID
        String messageId = SpringExpressionUtils.generateKeyBySpEL(apiLog.messageId(), joinPoint);
        if (StringUtils.isEmpty(messageId) || StringUtils.equals("null", messageId)) {
            apiLogPO.setMessageId(SnowflakeUtils.generateId());
        } else {
            try {
                apiLogPO.setMessageId(Long.parseLong(messageId));
            } catch (NumberFormatException e) {
                log.warn("Invalid messageId format: {}, using snowflake ID instead", messageId);
                apiLogPO.setMessageId(SnowflakeUtils.generateId());
            }
        }

        // Set request data
        if (args.length > 0) {
            String requestData = JsonUtils.obj2String(args[args.length - 1]);
            apiLogPO.setRequestData(requestData);
        }

        return apiLogPO;
    }

    private void completeApiLogInfo(ApiLogPO apiLogPO, Object result, Throwable exception,
                                    long startTime, long endTime) {
        apiLogPO.setCostTime(endTime - startTime);

        // Set response data and status
        if (exception != null) {
            String errorMsg = exception.getMessage();
            apiLogPO.setResponseData(truncateString(errorMsg, MAX_STRING_LENGTH));
            apiLogPO.setStatus(ApiLogStatusEnum.FAIL);
        } else {
            String responseData = JsonUtils.obj2String(result);
            apiLogPO.setResponseData(truncateString(responseData, MAX_STRING_LENGTH));

            if (result instanceof Response response) {
                apiLogPO.setStatus(Response.SUCCESS_CODE.equals(response.getCode())
                        ? ApiLogStatusEnum.SUCCESS
                        : ApiLogStatusEnum.FAIL);
            } else {
                apiLogPO.setStatus(ApiLogStatusEnum.SUCCESS);
            }
        }
    }

    private String truncateString(String str, int maxLength) {
        if (StringUtils.isBlank(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
}
