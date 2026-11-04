package org.openwes.script.plugin.runtime.config;

import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.host.JdkScriptHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RuntimeConfiguration {

    @Bean
    public GroovyScriptEngine groovyScriptEngine() {
        return new GroovyScriptEngine();
    }

    @Bean(destroyMethod = "close")
    public ScriptExecutionRuntime scriptExecutionRuntime(
            @Value("${script-plugin.execution.timeout-ms:2000}") long timeoutMillis,
            @Value("${script-plugin.execution.pool-size:4}") int poolSize) {
        return new ScriptExecutionRuntime(timeoutMillis, poolSize);
    }

    @Bean
    public ScriptHttpClient scriptHttpClient() {
        return new JdkScriptHttpClient();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
