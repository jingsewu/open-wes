package org.openwes.wes.api.print.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PrintTemplateDTO {

    private Long id;

    @NotEmpty
    private String templateCode;

    @NotEmpty
    private String templateName;

    @NotEmpty
    private String templateContent;

    private boolean enabled;
}
