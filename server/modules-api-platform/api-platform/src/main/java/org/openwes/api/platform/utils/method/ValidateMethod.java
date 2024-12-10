package org.openwes.api.platform.utils.method;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class ValidateMethod implements TemplateMethodModelEx {

    /**
     * 非空校验
     */
    public static final String PATTERN_NOT_EMPTY = "NotEmpty";
    /**
     * 日期校验
     */
    public static final String PATTERN_DATE = "Date";
    /**
     * 数字校验
     */
    public static final String PATTERN_NUMBER = "Number";
    /**
     * 正则校验
     */
    public static final String PATTERN_REG = "Regular";

    @Override
    public Object exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args)) {
            return null;
        }
        try {
            //第一个参数为输入内容
            String param = args.get(0).toString();
            //第二个参数提示内容
            String msg = args.get(1).toString();
            //第三个参数校验类型：非空NotEmpty,日期Date,数字Number,正则Regular
            String pattern = args.get(2).toString();
            //第四个参数为正则表达式或时间格式，与第三个参数对应
            String format = args.size() > 3 ? args.get(3).toString() : null;
            if (PATTERN_NOT_EMPTY.equalsIgnoreCase(pattern)) {
                checkNotEmpty(param, msg);
            } else if (PATTERN_DATE.equalsIgnoreCase(pattern)) {
                checkDate(param, msg, format);
            } else if (PATTERN_NUMBER.equalsIgnoreCase(pattern)) {
                checkNumber(param, msg);
            } else if (PATTERN_REG.equalsIgnoreCase(pattern)) {
                checkRegular(param, msg, format);
            }
            return "";
        } catch (TemplateModelException e) {
            throw e;
        } catch (Exception e) {
            throw new TemplateModelException("Validate exception");
        }
    }

    private void checkNotEmpty(String param, String msg) throws TemplateModelException {
        if (StringUtils.isEmpty(param)) {
            throw new TemplateModelException(msg);
        }
    }

    private void checkDate(String param, String msg, String format) throws TemplateModelException {
        try {
            new SimpleDateFormat(format).parse(param);
        } catch (Exception e) {
            throw new TemplateModelException(msg);
        }
    }

    private void checkNumber(String param, String msg) throws TemplateModelException {
        if (!NumberUtils.isCreatable(param)) {
            throw new TemplateModelException(msg);
        }
    }

    private void checkRegular(String param, String msg, String format) throws TemplateModelException {
        boolean result = param.matches(format);
        if (!result) {
            throw new TemplateModelException(msg);
        }
    }
}
