package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.openwes.common.utils.constants.MarkConstant;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupByToMapMethod implements TemplateMethodModelEx {

    @Override
    public Map<String, List<Object>> exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args)) {
            return null;
        }
        try {
            //第一个参数为原始列表数据，格式为List<Object>
            String param = JSON.toJSONString(((DefaultListAdapter) args.get(0)).getWrappedObject());
            if (StringUtils.isBlank(param)) {
                return null;
            }
            //第二个参数为group字段，以,分隔，如col1,col2
            String[] groupByList = args.get(1).toString().split(MarkConstant.COMMA_MARK);

            JSONArray jsonArray = JSON.parseArray(param);

            return jsonArray.stream().collect(Collectors.groupingBy(i -> {
                JSONObject obj = (JSONObject) i;
                String key = "";
                for (String groupBy : groupByList) {
                    String val = obj.get(groupBy) != null ? obj.get(groupBy).toString() : "";
                    key = key + val + MarkConstant.HYPHEN_CHARACTER;
                }
                return key;
            }));

        } catch (Exception e) {
            throw new TemplateModelException("GroupByToMap exception");
        }
    }
}
