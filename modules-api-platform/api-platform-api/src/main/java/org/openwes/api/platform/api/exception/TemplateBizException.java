package org.openwes.api.platform.api.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateBizException extends RuntimeException {

    public TemplateBizException(String var1) {
        super(var1);
    }

}
