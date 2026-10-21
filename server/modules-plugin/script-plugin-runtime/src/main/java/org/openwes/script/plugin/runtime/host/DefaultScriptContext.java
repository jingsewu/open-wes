package org.openwes.script.plugin.runtime.host;

import org.openwes.script.plugin.api.host.ScriptContext;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Map;

public class DefaultScriptContext implements ScriptContext {

    private final String pluginKey;
    private final ConfigurableApplicationContext revisionContext;
    private final Map<String, Object> config;
    private final List<ConfigFieldDTO> configSchema;
    private final ScriptHttpClient httpClient;

    public DefaultScriptContext(String pluginKey, ConfigurableApplicationContext revisionContext,
                                Map<String, Object> config, List<ConfigFieldDTO> configSchema,
                                ScriptHttpClient httpClient) {
        this.pluginKey = pluginKey;
        this.revisionContext = revisionContext;
        this.config = config;
        this.configSchema = configSchema;
        this.httpClient = httpClient;
    }

    @Override
    public String pluginKey() {
        return pluginKey;
    }

    @Override
    public <T> T bean(Class<T> type) {
        return revisionContext.getBean(type);
    }

    @Override
    public Object bean(String name) {
        return revisionContext.getBean(name);
    }

    @Override
    public Object configObject(String field) {
        return config.get(field);
    }

    @Override
    public String configString(String field) {
        Object value = config.get(field);
        return value == null ? null : value.toString();
    }

    @Override
    public Integer configInt(String field) {
        Object value = config.get(field);
        return value == null ? null : Integer.valueOf(value.toString());
    }

    @Override
    public Boolean configBool(String field) {
        Object value = config.get(field);
        return value == null ? null : Boolean.valueOf(value.toString());
    }

    @Override
    public Map<String, Object> config() {
        return config;
    }

    @Override
    public List<ConfigFieldDTO> configSchema() {
        return configSchema;
    }

    @Override
    public ScriptHttpClient http() {
        return httpClient;
    }

    @Override
    public Logger log() {
        return LoggerFactory.getLogger("script-plugin." + pluginKey);
    }
}
