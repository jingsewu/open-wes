package org.openwes.api.platform.utils.method;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class TimestampMethod implements TemplateMethodModelEx {

    @Override
    public String exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args)) {
            return null;
        }
        try {
            //第一个参数为日期时间
            String time = args.get(0).toString();
            //第二个参数为输入的日期格式，如yyyy-MM-dd HH:mm:ss
            String pattern = args.get(1).toString();

            //判断是否是数字,已经是时间戳
            String reg = "[0-9]+";
            if (Pattern.matches(reg, time)) {
                return time;
            }
            Date date = new SimpleDateFormat(pattern).parse(time);
            Long timestamp = date.getTime() / 1000;
            return timestamp.toString();
        } catch (Exception e) {
            throw new TemplateModelException("Date format error");
        }
    }
}
