package org.openwes.api.platform.utils;

import com.google.common.collect.Maps;
import org.openwes.common.utils.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;

import java.util.Map;

@Slf4j
public class JavaScriptUtils {

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

    public static void main(String[] args) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("skuCode", "123");
        params.put("abc", "222");
        Object result = executeJs(Context.create(), " function convert(p) { return p; }", params);
        System.out.println(result);
    }
}
