package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class ToStringMethod implements TemplateMethodModelEx {

    @Override
    public String exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args) || args.get(0) == null) {
            return "";
        }
        String param = "";
        if (args.get(0) instanceof DefaultMapAdapter defaultMapAdapter) {
            param = JSON.toJSONString(defaultMapAdapter.getWrappedObject());
        }
        return param;
    }
}
