package org.openwes.api.platform.aspect;

import org.openwes.api.platform.api.constants.ApiLogStatusEnum;
import org.openwes.api.platform.domain.entity.ApiLogPO;
import org.openwes.api.platform.domain.repository.ApiLogPORepository;
import org.openwes.api.platform.utils.SpringExpressionUtils;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.id.SnowflakeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    private final ApiLogPORepository apiLogPORepository;

    @Around("@annotation(apiLog)")
    public Object around(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {

        Object[] args = joinPoint.getArgs();

        ApiLogPO apiLogPO = new ApiLogPO();
        apiLogPO.setApiCode(SpringExpressionUtils.generateKeyBySpEL(apiLog.apiCode(), joinPoint));
        String messageId = SpringExpressionUtils.generateKeyBySpEL(apiLog.messageId(), joinPoint);
        apiLogPO.setMessageId(StringUtils.isEmpty(messageId) || StringUtils.equals("null", messageId)
                ? SnowflakeUtils.generateId() : Long.parseLong(messageId));

        if (args.length > 0) {
            apiLogPO.setRequestData(args[args.length - 1]);
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            String targetClass = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            log.error("api log Aspect happened error,targetClass: {},methodName: {},args: {}",
                    targetClass, methodName, args, e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            apiLogPO.setResponseData(result);
            apiLogPO.setCostTime(endTime - startTime);

            if (result instanceof Response response) {
                apiLogPO.setStatus(Response.SUCCESS_CODE.equals(response.getCode()) ? ApiLogStatusEnum.SUCCESS : ApiLogStatusEnum.FAIL);
            } else {
                apiLogPO.setStatus(ApiLogStatusEnum.SUCCESS);
            }
            try {
                apiLogPORepository.save(apiLogPO);
            } catch (Exception e) {
                log.error("Failed to save log: {}", apiLogPO, e);
            }
        }
        return result;
    }
}
