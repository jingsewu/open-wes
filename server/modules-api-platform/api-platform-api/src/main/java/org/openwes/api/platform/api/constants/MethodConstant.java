package org.openwes.api.platform.api.constants;

public class MethodConstant {

    /**
     * 自定义函数：时间戳
     */
    public static final String METHOD_TIMESTAMP = "timestamp";

    /**
     * 自定义函数：字段校验
     */
    public static final String METHOD_VALIDATE = "validate";

    /**
     * 自定义函数：抛出异常
     */
    public static final String METHOD_THROWEXCEPTION = "throwException";

    /**
     * 自定义函数：输出整个json内容
     */
    public static final String METHOD_TO_JSON = "toJson";

    /**
     * 自定义函数：json字符串转对象
     */
    public static final String METHOD_TO_JSONOBJECT = "toJsonObject";

    /**
     * 自定义函数：列表分组汇总部分字段，返回List
     */
    public static final String METHOD_GROUP_BY = "groupBy";

    /**
     * 自定义函数：列表分组汇总部分字段，返回List
     */
    public static final String METHOD_GROUP_BY_TO_MAP = "groupByToMap";

    public static final String METHOD_JSONKEYS_TO_LOWERCASE = "jsonKeysToLowerCase";

    /**
     * 自定义函数：列表分组统计记录数，返回int
     */
    public static final String METHOD_GROUP_COUNT = "groupCount";

    /**
     * 自定义函数：生成请求唯一号，返回long
     */
    public static final String METHOD_SEQ = "seq";

    /**
     * 自定义函数：列表转成Map，返回Map
     */
    public static final String METHOD_TO_MAP = "toMap";

    /**
     * 自定义函数：列表转成Map，返回Map
     */
    public static final String HASH_CODE = "hashCode";

    /**
     * 对参数进行累计求hash
     */
    public static final String HASH_CODE2 = "hashCode2";

    /**
     * 自定义函数：检查数据格式，返回string
     */
    public static final String DATA_TYPE = "dataType";

    /**
     * 自定义函数：object转String，返回string
     */
    public static final String TO_STRING = "toString";

    // 自定义函数：去重并分组统计数量，返回Map<String, Long>
    public static final String DISTINCT_GROUP_BY_COUNT = "distinctGroupByCount";

    /**
     * debug，用于断点调试flt模板，返回空字符串
     */
    public static final String DEBUG = "debug";

    /**
     * 自定义常量：非空表达式
     */
    public static final String CONSTANT_PATTERN_NOT_EMPTY = "PATTERN_NOT_EMPTY";

    /**
     * 自定义常量：数字表达式
     */
    public static final String CONSTANT_PATTERN_NUMBER = "PATTERN_NUMBER";

    /**
     * 自定义常量：日期表达式
     */
    public static final String CONSTANT_PATTERN_DATE = "PATTERN_DATE";

    /**
     * 自定义常量：正则表达式
     */
    public static final String CONSTANT_PATTERN_REG = "PATTERN_REG";

}
