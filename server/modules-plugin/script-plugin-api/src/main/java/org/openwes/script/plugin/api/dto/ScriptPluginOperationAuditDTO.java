package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptPluginOperationAuditDTO {
    private Long id;
    private String pluginKey;
    private ScriptPluginAuditActionEnum action;
    private Long revisionId;
    private String operator;
    private boolean success;
    private String detail;
    private Long createdAt;
}
