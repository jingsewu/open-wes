package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class GenerateHashCodeMethod implements TemplateMethodModelEx {

    @Override
    public Integer exec(List arguments) throws TemplateModelException {
        if (CollectionUtils.isEmpty(arguments)) {
            return null;
        }
        try {
            //第一个参数为原始列表数据，格式为List<Object>
            String param = JSON.toJSONString(((DefaultListAdapter) arguments.get(0)).getWrappedObject());
            if (StringUtils.isBlank(param)) {
                return null;
            }
            return param.hashCode();
        } catch (Exception e) {
            throw new TemplateModelException("GenerateHashCode exception");
        }
    }
}
