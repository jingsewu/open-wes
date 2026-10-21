package org.openwes.script.plugin.runtime.materialize;

import lombok.Getter;
import lombok.Setter;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.host.ScriptContext;

@Getter
@Setter
public class MaterializedExtension {

    private ExtensionDeclarationDTO declaration;
    /** Host interface the proxy implements; null for dispatcher-backed archetypes (HTTP_ENDPOINT, CRON). */
    private Class<?> extensionPointClass;
    private Object scriptInstance;
    private Object proxy;
    private ScriptContext scriptContext;
}
