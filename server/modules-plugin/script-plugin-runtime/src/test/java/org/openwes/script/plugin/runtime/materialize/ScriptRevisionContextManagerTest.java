package org.openwes.script.plugin.runtime.materialize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptRevisionContextManagerTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private ScriptRevisionContextManager manager;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        manager = new ScriptRevisionContextManager(parent, new GroovyScriptEngine());
    }

    @AfterEach
    void tearDown() {
        manager.closeAll();
        parent.close();
    }

    private Map<String, String> greeter() {
        return Map.of("Greeter.groovy", """
                import org.springframework.beans.factory.annotation.Autowired
                class Greeter {
                    @Autowired String greeting
                    String greet() { greeting }
                }
                """);
    }

    @Test
    void acquire_injectsBeansFromTheParentContext() throws Exception {
        ScriptRevisionContextManager.RevisionContext revisionContext =
                manager.acquire("packing-wave", 1L, greeter());

        Object greeter = revisionContext.instances().get("Greeter.groovy");
        Method greet = greeter.getClass().getMethod("greet");

        assertEquals("injected", greet.invoke(greeter));
    }

    @Test
    void acquire_sameRevision_reusesTheContext() {
        ScriptRevisionContextManager.RevisionContext first = manager.acquire("packing-wave", 1L, greeter());
        ScriptRevisionContextManager.RevisionContext second = manager.acquire("packing-wave", 1L, greeter());

        assertSame(first, second);
    }

    @Test
    void closeRevisionsExcept_closesOnlyTheSupersededContext() {
        ScriptRevisionContextManager.RevisionContext old = manager.acquire("packing-wave", 1L, greeter());
        ScriptRevisionContextManager.RevisionContext current = manager.acquire("packing-wave", 2L, greeter());

        manager.closeRevisionsExcept("packing-wave", 2L);

        assertFalse(old.context().isActive());
        assertTrue(current.context().isActive());
    }

    @Test
    void closePlugin_closesAllRevisionsOfThatPluginOnly() {
        ScriptRevisionContextManager.RevisionContext wave = manager.acquire("packing-wave", 1L, greeter());
        ScriptRevisionContextManager.RevisionContext other = manager.acquire("other-plugin", 1L, greeter());

        manager.closePlugin("packing-wave");

        assertFalse(wave.context().isActive());
        assertTrue(other.context().isActive());
    }

    @Test
    void openValidationScope_closesWithoutTouchingTheRevisionCache() {
        try (ScriptRevisionContextManager.ValidationScope scope = manager.openValidationScope("packing-wave", greeter())) {
            assertEquals(1, scope.instances().size());
            assertTrue(scope.context().isActive());
        }
        // the cache is untouched, so an acquire afterwards builds a fresh context
        assertTrue(manager.acquire("packing-wave", 1L, greeter()).context().isActive());
    }

    @Test
    void failedAcquire_isNotCachedAndTheSameKeyCanBeRetried() throws Exception {
        Map<String, String> broken = Map.of("Greeter.groovy", "class Greeter { def oops( { }");

        assertThrows(ScriptCompileException.class, () -> manager.acquire("packing-wave", 1L, broken));

        ScriptRevisionContextManager.RevisionContext retried = manager.acquire("packing-wave", 1L, greeter());
        Object greeter = retried.instances().get("Greeter.groovy");
        Method greet = greeter.getClass().getMethod("greet");

        assertTrue(retried.context().isActive());
        assertEquals("injected", greet.invoke(greeter));
    }
}
