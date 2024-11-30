package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import freemarker.template.DefaultListAdapter;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class ToJsonMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args)) {
            return null;
        }
        try {
            Object param = null;
            Object o = args.get(0);
            if (o instanceof DefaultMapAdapter defaultMapAdapter) {
                // {}
                param = defaultMapAdapter.getWrappedObject();
            } else if (o instanceof DefaultListAdapter defaultListAdapter) {
                // []
                param = defaultListAdapter.getWrappedObject();
            }
            return JSON.toJSONString(param);
        } catch (Exception e) {
            throw new TemplateModelException("Convert json exception");
        }
    }
}
