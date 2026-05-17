package org.openwes.api.platform.utils;

import org.junit.jupiter.api.Test;
import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import org.openwes.api.platform.domain.entity.ApiConfigPO;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterHelperTest {

    @Test
    void convertParam_withNullConfig_returnsInputUnchanged() {
        Map<String, Object> input = Map.of("key", "value");
        Object result = ConverterHelper.convertParam(null, input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void convertParam_withNoneType_returnsInputUnchanged() {
        Map<String, Object> input = Map.of("key", "value");
        ApiConfigPO config = new ApiConfigPO();
        config.setParamConverterType(ConverterTypeEnum.NONE);
        Object result = ConverterHelper.convertParam(config, input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void convertParam_withJavaType_executesGroovyScript() {
        Map<String, Object> input = new HashMap<>();
        input.put("name", "World");

        ApiConfigPO config = new ApiConfigPO();
        config.setParamConverterType(ConverterTypeEnum.JAVA);
        config.setParamConverterScript("""
                //java:convert
                public class TestConverter {
                    public Object convert(Object param) {
                        Map<String, Object> map = (Map<String, Object>) param;
                        return "Hello, " + map.get("name");
                    }
                }
                """);

        Object result = ConverterHelper.convertParam(config, input);
        assertThat(result).isEqualTo("Hello, World");
    }

    @Test
    void convertResponse_withJavaType_executesGroovyScript() {
        Map<String, Object> input = new HashMap<>();
        input.put("code", "200");

        ApiConfigPO config = new ApiConfigPO();
        config.setResponseConverterType(ConverterTypeEnum.JAVA);
        config.setResponseConverterScript("""
                //java:convert
                public class ResponseConverter {
                    public Object convert(Object param) {
                        Map<String, Object> map = (Map<String, Object>) param;
                        return map.get("code");
                    }
                }
                """);

        Object result = ConverterHelper.convertResponse(config, input);
        assertThat(result).isEqualTo("200");
    }

    @Test
    void convertParam_withNullConverterType_returnsInputUnchanged() {
        Map<String, Object> input = Map.of("key", "value");
        ApiConfigPO config = new ApiConfigPO();
        // paramConverterType is null by default
        Object result = ConverterHelper.convertParam(config, input);
        assertThat(result).isEqualTo(input);
    }
}
