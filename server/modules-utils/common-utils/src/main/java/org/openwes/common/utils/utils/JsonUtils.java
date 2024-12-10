package org.openwes.common.utils.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Lists;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

@Slf4j
public class JsonUtils {

    private JsonUtils() {

    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectMapper OBJECT_MAPPER_FOR_WEB = JsonMapper.builder().enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS).build();

    static {
        config(OBJECT_MAPPER);
        config(OBJECT_MAPPER_FOR_WEB);
    }

    private static void config(ObjectMapper objectMapper) {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //取消默认转换timestamps形式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //所有的日期格式都统一为以下的样式
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //忽略空Bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许接受空字符串
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    public static String obj2StringPretty(Object object) {
        return obj2String(OBJECT_MAPPER, object, true);
    }

    public static String obj2StringForWeb(Object object) {
        return obj2String(OBJECT_MAPPER_FOR_WEB, object, false);
    }


    public static String obj2String(Object object) {
        return obj2String(OBJECT_MAPPER, object, false);
    }

    private static String obj2String(ObjectMapper objectMapper, Object object, boolean isPretty) {

        if (object == null) {
            return null;
        }

        try {
            if (object instanceof String) {
                return object.toString();
            }

            if (isPretty) {
                return System.lineSeparator()
                        + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object)
                        + System.lineSeparator();
            } else {
                return objectMapper.writeValueAsString(object);
            }

        } catch (JsonProcessingException e) {
            log.error("obj2String json processing error: ", e);
            return object.toString();
        }
    }

    public static <T> T string2Object(String str, Class<T> elementClasses) {
        if (StringUtils.isEmpty(str) || elementClasses == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(str, elementClasses);
        } catch (JsonProcessingException e) {
            log.error("string2Object json: {} processing error: ", str, e);
            throw new CommonException(CommonErrorDescEnum.JSON_PARSER_ERROR.getDesc(), str, e.getMessage());
        }
    }

    public static <T> List<T> string2List(String str, Class<T> elementClasses) {
        return string2Collection(str, List.class, elementClasses);
    }

    public static <T> Set<T> string2Set(String str, Class<T> elementClasses) {
        return string2Collection(str, Set.class, elementClasses);
    }

    public static Map string2Map(String str) {
        try {
            return OBJECT_MAPPER.readValue(str, Map.class);
        } catch (JsonProcessingException e) {
            log.error("string2Map json: {} processing error: ", str, e);
            throw new CommonException(CommonErrorDescEnum.JSON_PARSER_ERROR.getDesc(), str);
        }
    }

    public static Map<String, Object> string2MapObject(String str) {
        try {
            return OBJECT_MAPPER.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("string2MapObject json: {} processing error: ", str, e);
            throw new CommonException(CommonErrorDescEnum.JSON_PARSER_ERROR.getDesc(), str);
        }
    }

    private static <T> T string2Collection(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        if (StringUtils.isEmpty(str) || elementClasses == null) {
            return null;
        }
        JavaType javaType = getCollectionType(collectionClass, elementClasses);

        try {
            return OBJECT_MAPPER.readValue(str, javaType);
        } catch (JsonProcessingException e) {
            log.error("string2Collection json: {} processing error: ", str, e);
            throw new CommonException("json parse error", str);
        }
    }

    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static JsonNode objectToJsonNode(Object object) {
        try {
            return OBJECT_MAPPER.readTree(obj2String(object));
        } catch (JsonProcessingException e) {
            log.error("json processing error: ", e);
            throw new CommonException("json parse error", object);
        }
    }

    public static void main(String[] args) throws JsonProcessingException {

        List<Integer> list = Lists.newArrayList(1, 2, 4).stream().toList();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(list));
    }

}
