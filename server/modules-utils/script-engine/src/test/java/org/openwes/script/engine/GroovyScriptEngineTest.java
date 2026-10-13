package org.openwes.script.engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroovyScriptEngineTest {

    private final GroovyScriptEngine engine = new GroovyScriptEngine();

    @Test
    void compile_isCachedByContent() {
        String source = "class Same { Object run(Object p) { return p } }";

        assertSame(engine.compile(source), engine.compile(source));
    }

    @Test
    void invoke_callsNamedMethodWithUntypedParameter() {
        Class<?> clazz = engine.compile("class Wave { Object wave(Object p) { return [1, 2] } }");
        Object instance = engine.newInstance(clazz);

        assertEquals(List.of(1, 2), engine.invoke(instance, "wave", null));
    }

    @Test
    void invoke_mutatesTypedMapParameter() {
        Map<String, Object> input = new HashMap<>();
        input.put("name", "World");
        Class<?> clazz = engine.compile(
                "class Greeter { Object greet(java.util.Map input) { return \"Hello, \" + input.get('name') } }");
        Object instance = engine.newInstance(clazz);

        assertEquals("Hello, World", engine.invoke(instance, "greet", input));
    }

    @Test
    void compile_invalidSource_throwsScriptCompileException() {
        assertThrows(ScriptCompileException.class, () -> engine.compile("class Broken {"));
    }

    @Test
    void compile_blankSource_throwsScriptCompileException() {
        assertThrows(ScriptCompileException.class, () -> engine.compile("   "));
    }

    @Test
    void invoke_missingMethod_throwsScriptExecutionException() {
        Class<?> clazz = engine.compile("class Only { Object x() { return 1 } }");
        Object instance = engine.newInstance(clazz);

        assertThrows(ScriptExecutionException.class, () -> engine.invoke(instance, "nope"));
    }

    @Test
    void invoke_methodThrows_propagatesAsScriptExecutionException() {
        Class<?> clazz = engine.compile("class Boom { Object run(Object p) { throw new IllegalStateException('boom') } }");
        Object instance = engine.newInstance(clazz);

        assertThrows(ScriptExecutionException.class, () -> engine.invoke(instance, "run", null));
    }
}
