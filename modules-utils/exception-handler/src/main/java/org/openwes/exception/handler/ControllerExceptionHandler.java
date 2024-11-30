package org.openwes.exception.handler;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;
import org.openwes.common.utils.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public ResponseEntity<SwmsErrorResponse> httpRequestMethodNotSupportedHandler() {
        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("Method Not Allow")
                .errorCode(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .description(CommonErrorDescEnum.METHOD_NOT_ALLOWED.getDesc())
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<SwmsErrorResponse> noHandlerFoundHandler() {
        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("Method Not Found")
                .errorCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .description(CommonErrorDescEnum.NOT_FOUND.getDesc())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(WmsException.class)
    public ResponseEntity<SwmsErrorResponse> bizExceptionHandler(WmsException wmsException) {

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

        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("Business Error")
                .errorCode(wmsException.getCode())
                .description(msg)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<SwmsErrorResponse> duplicateKeyExceptionHandler() {
        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("Database Error")
                .errorCode(CommonErrorDescEnum.DATABASE_UNIQUE_ERROR.getCode())
                .description(CommonErrorDescEnum.DATABASE_UNIQUE_ERROR.getDesc())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SwmsErrorResponse> exceptionHandler(Exception e) {
        String description = CommonErrorDescEnum.SYSTEM_EXEC_ERROR.getDesc();
        if (StringUtils.isEmpty(description)) {
            description = e.getMessage();
        }

        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message(description)
                .errorCode(CommonErrorDescEnum.SYSTEM_EXEC_ERROR.getCode())
                .description(e.getMessage())
                .build();
        log.error("business catch exception error:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalStateException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<SwmsErrorResponse> httpRequestExceptionHandler(Exception exception) {
        log.error("http request error: ", exception);
        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("Bad Request")
                .errorCode(CommonErrorDescEnum.HTTP_REQUEST_ERROR.getCode())
                .description(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理接口的数据检查异常
     */
    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<SwmsErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
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
        SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("Param Error")
                .errorCode(CommonErrorDescEnum.PARAMETER_ERROR.getCode())
                .description(JsonUtils.obj2String(validResultMap))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
