package org.openwes.api.platform.utils;

import com.google.common.collect.Maps;
import groovy.lang.GroovyClassLoader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.openwes.script.engine.ScriptExecutionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class JavaScriptUtils {

    private static final Pattern GROOVY_PATTERN = Pattern.compile("//java:(\\w+)");

    private static final GroovyScriptEngine GROOVY_SCRIPT_ENGINE = new GroovyScriptEngine();

    private JavaScriptUtils() {
    }

    public static synchronized Object executeJs(Context context, String jsCode, Object param) {

        // Define your JavaScript code here, including the 'convert' function
        String extJsCode = "var obj = {}; var param = " + JsonUtils.obj2String(param) + "; obj.convert = " + jsCode;

        // Evaluate the JavaScript code
        context.eval(Source.create("js", extJsCode));

        // Create a JavaScript object
        Value jsObject = context.eval("js", "obj");

        // Call the 'convert' function on the object
        Value result = jsObject.getMember("convert").execute(jsObject);

        return result.as(new TypeLiteral<Object>() {
        });

    }

    public static Object executeJava(@NonNull String script, Object param) {

        String methodName = "convert";

        Matcher matcher = GROOVY_PATTERN.matcher(script);
        if (matcher.find()) {
            methodName = matcher.group(1);
            script = script.substring(0, matcher.start()) + script.substring(matcher.end()); // More efficient string removal
        }

        try {
            Class<?> clazz = GROOVY_SCRIPT_ENGINE.compile(script);
            return GROOVY_SCRIPT_ENGINE.invoke(GROOVY_SCRIPT_ENGINE.newInstance(clazz), methodName, param);
        } catch (ScriptCompileException | ScriptExecutionException e) {
            log.error("Error executing Java script: {}", script, e);
            throw new CommonException("script execution failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("skuCode", "123");
        params.put("abc", "222");
        Object result = executeJs(Context.create(), " function convert(p) { return p; }", params);
        System.out.println(result);

        String script = """
                //java:myMethod
                public class MyClass {
                    public Object myMethod(Object param) {
                        Map<String, Object> input = (Map<String, Object>) param;
                        return "Hello,  "+ input.get("name");
                    }
                }
                """;

        Map<String, Object> input = new HashMap<>();
        input.put("name", "World");

        System.out.println(executeJava(script, input));
    }
}
