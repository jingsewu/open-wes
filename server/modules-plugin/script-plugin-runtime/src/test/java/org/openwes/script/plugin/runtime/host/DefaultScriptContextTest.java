package org.openwes.script.plugin.runtime.host;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultScriptContextTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private final AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
    private DefaultScriptContext context;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        child.setParent(parent);
        child.refresh();
        context = new DefaultScriptContext("packing-wave", child,
                Map.of("greeting", "hello", "maxPerWave", 5, "enabled", true), List.of(), new JdkScriptHttpClient());
    }

    @AfterEach
    void tearDown() {
        child.close();
        parent.close();
    }

    @Test
    void bean_resolvesThroughTheChildThenParentContext() {
        assertEquals("injected", context.bean(String.class));
        assertEquals("injected", context.bean("string"));
    }

    @Test
    void bean_missingType_throwsNoSuchBeanDefinition() {
        assertThrows(Exception.class, () -> context.bean(Integer.class));
    }

    @Test
    void typedConfigGetters_convertAndDefaultToNull() {
        assertEquals("hello", context.configString("greeting"));
        assertEquals(5, context.configInt("maxPerWave"));
        assertTrue(context.configBool("enabled"));
        assertNull(context.configString("missing"));
        assertNull(context.configInt("missing"));
    }

    @Test
    void logger_isNamespacedPerPlugin() {
        assertTrue(context.log().getName().contains("packing-wave"));
    }
}
