package org.openwes.exception.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwmsErrorResponse {
    private String errorCode;
    private String message;
    private String description;
}
