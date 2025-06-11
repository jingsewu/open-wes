package org.openwes.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;
import org.openwes.common.utils.utils.JsonUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<OpenWesErrorResponse> httpRequestMethodNotSupportedHandler() {
        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg("Method Not Allow")
                .status(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .description(CommonErrorDescEnum.METHOD_NOT_ALLOWED.getDesc())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<OpenWesErrorResponse> noHandlerFoundHandler() {
        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg("Method Not Found")
                .status(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .description(CommonErrorDescEnum.NOT_FOUND.getDesc())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(WmsException.class)
    public ResponseEntity<OpenWesErrorResponse> bizExceptionHandler(WmsException wmsException) {

        String msg = null;
        IBaseError baseError = wmsException.getBaseError();
        if (wmsException.getBaseError() != null) {
            msg = MessageFormat.format(baseError.getDesc(), wmsException.getArgs());
            if (StringUtils.isEmpty(msg)) {
                msg = MessageFormat.format(baseError.getDesc(), wmsException.getArgs());
            }
        }

        if (StringUtils.isEmpty(msg)) {
            msg = wmsException.getMessage();
        }

        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg("Business Error")
                .status(wmsException.getCode())
                .description(msg)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<OpenWesErrorResponse> duplicateKeyExceptionHandler() {
        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg("Database Error")
                .status(CommonErrorDescEnum.DATABASE_UNIQUE_ERROR.getCode())
                .description(CommonErrorDescEnum.DATABASE_UNIQUE_ERROR.getDesc())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<OpenWesErrorResponse> exceptionHandler(Exception e) {
        String description = CommonErrorDescEnum.SYSTEM_EXEC_ERROR.getDesc();
        if (StringUtils.isEmpty(description)) {
            description = e.getMessage();
        }

        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg(description)
                .status(CommonErrorDescEnum.SYSTEM_EXEC_ERROR.getCode())
                .description(e.getMessage())
                .build();
        log.error("business catch exception error:", e);
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalStateException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<OpenWesErrorResponse> httpRequestExceptionHandler(Exception exception) {
        log.error("http request error: ", exception);
        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg("Bad Request")
                .status(CommonErrorDescEnum.HTTP_REQUEST_ERROR.getCode())
                .description(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }

    /**
     * 处理接口的数据检查异常
     */
    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<OpenWesErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.warn("interface parameter error: ", exception);
        List<Map<String, Object>> validResultMap = exception.getBindingResult().getFieldErrors().stream()
                .map(v -> {
                            Map<String, Object> fieldResultMap = new HashMap<>();
                            fieldResultMap.put("Field", v.getField());
                            fieldResultMap.put("rejectedValue", v.getRejectedValue());
                            fieldResultMap.put("defaultMessage", v.getDefaultMessage());
                            return fieldResultMap;
                        }
                ).toList();
        OpenWesErrorResponse errorResponse = OpenWesErrorResponse.builder().msg("Param Error")
                .status(CommonErrorDescEnum.PARAMETER_ERROR.getCode())
                .description(JsonUtils.obj2String(validResultMap))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
    }
}
