package org.openwes.search.utils;

import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.SearchBean;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.search.parameter.SearchParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SearchUtils {

    private SearchUtils() {

    }

    private static final String PACKAGE_NAME = "org.openwes.search.parameter.";

    // Thread-safe cache for class instances
    private static final Map<String, SearchParameterClassCache> classCache = new ConcurrentHashMap<>();

    public static Class<?> createClass(SearchParam searchParam) throws CannotCompileException {
        String className = PACKAGE_NAME + searchParam.getSearchIdentity();

        // Check if the class is already in the cache
        SearchParameterClassCache cachedClass = classCache.get(className);
        ClassPool classPool = ClassPool.getDefault();
        if (cachedClass == null) {
            Class<?> newClass = createClass(searchParam, classPool);
            classCache.put(className, new SearchParameterClassCache(className, newClass));
            return newClass;
        }

        if (!cachedClass.isDetached()) {
            return cachedClass.getClassObject();
        }

        //replace class object
        int version = cachedClass.getClassVersion() + 1;
        searchParam.setSearchIdentity(searchParam.getSearchIdentity() + "_" + version);
        Class<?> newClass = createClass(searchParam, classPool);
        cachedClass.setClassObject(newClass);
        cachedClass.setClassVersion(version);
        return newClass;
    }

    private static Class<?> createClass(SearchParam searchParam, ClassPool classPool) throws CannotCompileException {
        CtClass ct;
        try {
            ct = createClass(classPool, searchParam);
        } catch (Exception e) {
            log.error("create class: {} error: ", searchParam.getSearchIdentity(), e);
            throw new WmsException("search server create class error:" + e.getMessage());
        }
        return ct.toClass(SearchParam.class);
    }

    private static CtClass createClass(ClassPool classPool, SearchParam searchParam) throws NotFoundException, CannotCompileException {

        handleTables(searchParam);

        CtClass dynamicClass = classPool.makeClass(PACKAGE_NAME + searchParam.getSearchIdentity());

        for (SearchParam.Column column : searchParam.getShowColumns()) {
            CtField field = new CtField(classPool.get(column.getJavaType()), column.getName(), dynamicClass);
            dynamicClass.addField(field);

            //add dbField mapping
            if (StringUtils.isNotBlank(column.getDbField())) {
                Annotation annotation = new Annotation(DbField.class.getName(), dynamicClass.getClassFile().getConstPool());
                annotation.addMemberValue("value", new StringMemberValue(column.getDbField(), dynamicClass.getClassFile().getConstPool()));
                AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(dynamicClass.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
                annotationsAttribute.addAnnotation(annotation);
                field.getFieldInfo().addAttribute(annotationsAttribute);
            }
        }

        SearchParam.SearchObject searchObject = searchParam.getSearchObject();
        if (searchObject != null) {
            AnnotationsAttribute classAnnotations = new AnnotationsAttribute(dynamicClass.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
            Annotation annotation = new Annotation(SearchBean.class.getName(), dynamicClass.getClassFile().getConstPool());

            if (StringUtils.isNotEmpty(searchObject.getDataSource())) {
                annotation.addMemberValue("dataSource", new StringMemberValue(searchObject.getDataSource(), dynamicClass.getClassFile().getConstPool()));
            }
            if (StringUtils.isNotEmpty(searchObject.getTables())) {
                annotation.addMemberValue("tables", new StringMemberValue(searchObject.getTables(), dynamicClass.getClassFile().getConstPool()));
            }
                if (StringUtils.isNotEmpty(searchObject.getWhere())) {
                annotation.addMemberValue("where", new StringMemberValue(searchObject.getWhere(), dynamicClass.getClassFile().getConstPool()));
            }
            if (StringUtils.isNotEmpty(searchObject.getGroupBy())) {
                annotation.addMemberValue("groupBy", new StringMemberValue(searchObject.getGroupBy(), dynamicClass.getClassFile().getConstPool()));
            }
            if (StringUtils.isNotEmpty(searchObject.getOrderBy())) {
                annotation.addMemberValue("orderBy", new StringMemberValue(searchObject.getOrderBy(), dynamicClass.getClassFile().getConstPool()));
            }
            classAnnotations.addAnnotation(annotation);
            dynamicClass.getClassFile().addAttribute(classAnnotations);
        }

        return dynamicClass;
    }

    private static void handleTables(SearchParam searchParam) {
        if (searchParam.getSearchObject() != null && StringUtils.isNotEmpty(searchParam.getSearchObject().getTables())) {
            return;
        }

        SearchParam.SearchObject searchObject = searchParam.getSearchObject();
        if (searchObject == null) {
            searchObject = new SearchParam.SearchObject();
            searchParam.setSearchObject(searchObject);
        }

        searchObject.setTables(toTableName(searchParam.getSearchIdentity()));

    }

    public static String toTableName(String identity) {

        if (identity.contains("_")) {
            identity = identity.substring(0, identity.lastIndexOf("_"));
        }

        return cn.zhxu.bs.util.StringUtils.toUnderline(identity);
    }

    private static final Set<String> arrayOps = Sets.newHashSet("bt", "il");

    /**
     * amis array parameters is String , we need to convert it to arrays to adapter Bean Searcher framework
     *
     * @param paramMap
     * @return
     */
    public static Map<String, Object> handleSpecialParams(Map<String, Object> paramMap) {

        Map<String, Object> newParamMap = Maps.newHashMap(paramMap);

        for (Map.Entry<String, Object> next : paramMap.entrySet()) {
            String key = next.getKey();
            String value = String.valueOf(next.getValue());
            if (arrayOps.contains(value)) {
                String fieldName = key.trim().split("-")[0];
                String fieldValue = paramMap.getOrDefault(fieldName, "").toString();

                if (StringUtils.isNotEmpty(fieldValue)) {
                    int index = 0;
                    for (String v : fieldValue.split(",")) {
                        newParamMap.put(fieldName + "-" + (index++), v);
                    }
                    newParamMap.remove(fieldName);
                }
            }
        }

        return newParamMap;
    }

    public static void clearSearchMetaData() {
        if (ObjectUtils.isEmpty(classCache)) {
            return;
        }

        classCache.forEach((k, v) -> v.detach());
    }

}
