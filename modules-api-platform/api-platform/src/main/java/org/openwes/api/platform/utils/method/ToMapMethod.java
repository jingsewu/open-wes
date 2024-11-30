package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ToMapMethod implements TemplateMethodModelEx {

    @Override
    public Map<String, Object> exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args) || args.get(0) == null || args.get(1) == null) {
            return Collections.emptyMap();
        }
        try {
            String param = JSON.toJSONString(((DefaultListAdapter) args.get(0)).getWrappedObject());
            String groupBy = args.get(1).toString();
            JSONArray jsonArray = JSON.parseArray(param);
            return jsonArray.stream().collect(Collectors.toMap(i -> {
                //按字段groupBy
                JSONObject obj = (JSONObject) i;
                return obj.get(groupBy) != null ? obj.get(groupBy).toString() : "";
            }, Function.identity()));
        } catch (Exception e) {
            throw new TemplateModelException("Convert map exception");
        }
    }
}
