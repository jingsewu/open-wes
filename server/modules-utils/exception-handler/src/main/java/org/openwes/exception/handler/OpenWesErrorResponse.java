package org.openwes.exception.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenWesErrorResponse {
    private String status;
    private String msg;
    private String description;
}
