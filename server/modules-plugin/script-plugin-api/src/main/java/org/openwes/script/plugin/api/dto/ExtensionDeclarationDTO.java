package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.ExtensionType;

/**
 * One declared extension point of a plugin. Field usage depends on `type`:
 * apiType (REQUEST_*), entityName (LIFECYCLE_LISTENER), customApiCode (CUSTOM_API),
 * httpMethod + path (HTTP_ENDPOINT), cron (CRON). `script` names a key in the revision's script map,
 * `method` is the script method to invoke.
 */
@Data
public class ExtensionDeclarationDTO {
    private ExtensionType type;
    private int priority;
    private String script;
    private String method;
    private String apiType;
    private String entityName;
    private String customApiCode;
    private String httpMethod;
    private String path;
    private String cron;
}
