package org.openwes.common.utils.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openwes.common.utils.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @author sws
 */
@Slf4j
public class ObjectUtils {

    private ObjectUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Integer LIST_OBJECT_NUMBER = 1;

    private static final Integer INTEGER_LIMIT = 100;

    private static final Long LONG_LIMIT = 100L;

    private static final Float FLOAT_LIMIT = 2f;

    private static final Double DOUBLE_LIMIT = 2d;

    private static final Integer STRING_LENGTH_LIMIT = 5;

    public static <T> T getRandomObject(Class<T> tClass) {

        try {
            return getRandomObjectIgnoreFields(tClass);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public static <T> T getRandomObjectIgnoreFields(Class<T> tClass, String... ignoreFields) {

        List<String> ignoreFieldList = Lists.newArrayList();
        ignoreFieldList.add("log");
        if (ignoreFields != null) {
            ignoreFieldList.addAll(Arrays.asList(ignoreFields));
        }

        try {

            T t = tClass.getDeclaredConstructor().newInstance();

            List<Field> fieldList = Lists.newArrayList();
            if (tClass.getSuperclass() != null && !StringUtils.equals(tClass.getSuperclass().getName(), Object.class.getName())) {
                fieldList.addAll(Lists.newArrayList(Objects.requireNonNull(tClass.getSuperclass()).getDeclaredFields()));
            }

            fieldList.addAll(Lists.newArrayList(tClass.getDeclaredFields()));
            if (fieldList.isEmpty()) {
                return t;
            }

            ignoreFieldList.forEach(ignoreField -> fieldList.removeIf(v -> v.getName().equals(ignoreField)));

            fieldList.forEach(v -> setFieldValue(v, t, ignoreFieldList));

            return t;

        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private static final String STRING_TYPE = "java.lang.String";

    private static Object getFieldValue(Class<?> tClass, Field field, Class fieldTypeClass, List<String> ignoreFields) {

        Object value = null;

        if (fieldTypeClass.getName().contains("Enum")) {
            return EnumUtils.getEnumMap(fieldTypeClass).values().iterator().next();
        }

        switch (fieldTypeClass.getName()) {
            case "java.lang.Integer", "int":
                value = RandomUtils.nextInt(1, INTEGER_LIMIT);
                break;
            case "java.lang.Long", "long":
                value = RandomUtils.nextLong(1, LONG_LIMIT);
                break;
            case "java.lang.Byte", "byte":
                value = RandomUtils.nextBytes(1)[0];
                break;
            case "java.lang.Float", "float":
                value = RandomUtils.nextFloat(0, FLOAT_LIMIT);
                break;
            case "java.lang.Double", "double":
                value = RandomUtils.nextDouble(0, DOUBLE_LIMIT);
                break;
            case "java.math.BigDecimal":
                value = new BigDecimal(String.valueOf(RandomUtils.nextDouble(0, DOUBLE_LIMIT)), MathContext.DECIMAL32);
                break;
            case STRING_TYPE:
                value = RandomStringUtils.randomAlphanumeric(13);
                break;
            case "java.util.Date":
                value = new Date();
                break;
            case "java.lang.Boolean", "boolean":
                value = RandomUtils.nextBoolean();
                break;
            case "[B":
                value = UUID.randomUUID().toString().substring(0, STRING_LENGTH_LIMIT).getBytes();
                break;
            case "java.util.Map":
                value = Maps.newHashMap();
                break;
            case "java.util.TreeMap":
                value = Maps.newTreeMap();
                break;
            case "java.util.Set":
                ParameterizedType fieldGenericType2 = (ParameterizedType) field.getGenericType();
                Class<?> fieldClass2 = (Class<?>) fieldGenericType2.getActualTypeArguments()[0];
                if ((fieldClass2.getSuperclass() != null && StringUtils.equals(fieldClass2.getSuperclass().getName(), "java.lang.Number"))
                    || (fieldClass2.getSuperclass() != null && StringUtils.equals(fieldClass2.getName(), STRING_TYPE))) {
                    Set<Object> objects = Sets.newHashSet();
                    for (int i = 0; i < LIST_OBJECT_NUMBER; i++) {
                        Object fieldObject = getFieldValue(null, null, fieldClass2, ignoreFields);
                        objects.add(fieldObject);
                    }
                    value = objects;
                } else {
                    Set<Object> objects = Sets.newHashSet();
                    for (int i = 0; i < LIST_OBJECT_NUMBER; i++) {
                        Object fieldObject = getRandomObjectIgnoreFields(fieldClass2, ignoreFields.toArray(new String[]{}));
                        objects.add(fieldObject);
                    }
                    value = objects;
                }
                break;
            case "java.util.List":
                ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
                Class<?> fieldClass = (Class<?>) fieldGenericType.getActualTypeArguments()[0];

                if (StringUtils.equals(fieldClass.getName(), tClass.getName())) {
                    break;
                } else if ((fieldClass.getSuperclass() != null && StringUtils.equals(fieldClass.getSuperclass().getName(), "java.lang.Number"))
                    || (fieldClass.getSuperclass() != null && StringUtils.equals(fieldClass.getName(), STRING_TYPE))) {
                    ArrayList<Object> objects = Lists.newArrayList();
                    for (int i = 0; i < LIST_OBJECT_NUMBER; i++) {
                        Object fieldObject = getFieldValue(null, null, fieldClass, ignoreFields);
                        objects.add(fieldObject);
                    }
                    value = objects;
                } else {
                    ArrayList<Object> objects = Lists.newArrayList();
                    for (int i = 0; i < LIST_OBJECT_NUMBER; i++) {
                        Object fieldObject = getRandomObjectIgnoreFields(fieldClass, ignoreFields.toArray(new String[]{}));
                        objects.add(fieldObject);
                    }
                    value = objects;
                }
                break;
            default:
                value = getRandomObjectIgnoreFields(fieldTypeClass, ignoreFields.toArray(new String[]{}));
                break;
        }

        return value;
    }

    private static <T> void setFieldValue(Field field, T t, List<String> ignoreFields) {

        try {
            if (field.getName().equalsIgnoreCase("$jacocoData")) {
                return;
            }
            field.setAccessible(true);
            Object fieldValue = getFieldValue(t.getClass(), field, field.getType(), ignoreFields);
            field.set(t, fieldValue);

            field.setAccessible(false);

        } catch (IllegalAccessException e) {
            log.error("ObjectUtils#setFieldValue error", e);
        }
    }

    public static boolean checkNull(Object object) {
        for (Field f : object.getClass().getDeclaredFields()) {
            try {
                if (f.get(object) != null) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                log.error("ObjectUtils#checkNull error", e);
                return true;
            }
        }
        return true;
    }

}
