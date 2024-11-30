package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class JsonKeysToLowerCaseMethod implements TemplateMethodModelEx {

    @Override
    public JSONObject exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args)) {
            return null;
        }
        try {
            String param = JSON.toJSONString(((DefaultMapAdapter) args.get(0)).getWrappedObject());

            if (StringUtils.isBlank(param)) {
                return null;
            }
            JSONObject obj = JSON.parseObject(param);
            JSONObject resultObj = new JSONObject();
            obj.entrySet().forEach(entry -> {
                resultObj.put(entry.getKey().toLowerCase(), entry.getValue());
            });
            return resultObj;
        } catch (Exception e) {
            throw new TemplateModelException("数据转换异常");
        }
    }
}
