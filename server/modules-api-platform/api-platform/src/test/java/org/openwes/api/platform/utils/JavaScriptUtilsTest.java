package org.openwes.api.platform.utils;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.exception.CommonException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaScriptUtilsTest {

    @Test
    void executeJava_withMethodMarker_callsMarkedMethod() {
        String script = "//java:myMethod\n"
                + "class MyClass { Object myMethod(Object param) { return \"Hello, \" + param } }";

        assertEquals("Hello, World", JavaScriptUtils.executeJava(script, "World"));
    }

    @Test
    void executeJava_withoutMarker_defaultsToConvert() {
        String script = "class MyClass { Object convert(Object param) { return param } }";

        assertEquals("X", JavaScriptUtils.executeJava(script, "X"));
    }

    @Test
    void executeJava_mapParameter_works() {
        String script = "//java:convert\n"
                + "class MyClass { Object convert(Object param) { return param.get('skuCode') } }";
        Map<String, Object> params = new HashMap<>();
        params.put("skuCode", "SKU-1");

        assertEquals("SKU-1", JavaScriptUtils.executeJava(script, params));
    }

    @Test
    void executeJava_invalidScript_throwsCommonExceptionInsteadOfNpe() {
        CommonException e = assertThrows(CommonException.class, () -> JavaScriptUtils.executeJava("class Broken {", "x"));

        assertTrue(e.getMessage().contains("script execution failed"));
    }
}
