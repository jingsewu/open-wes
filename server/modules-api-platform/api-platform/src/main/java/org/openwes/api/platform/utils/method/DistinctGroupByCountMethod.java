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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DistinctGroupByCountMethod implements TemplateMethodModelEx {

    @Override
    public Map<String, Long> exec(List args) throws TemplateModelException {
        Map<String, Long> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(args)) {
            return null;
        }
        try {
            // 第一个参数为原始列表数据，格式为List<Object>
            String param = JSON.toJSONString(((DefaultListAdapter) args.get(0)).getWrappedObject());
            if (StringUtils.isBlank(param)) {
                return resultMap;
            }
            // 第二个参数为group字段，以,分隔，如col1,col2
            String[] groupByList = args.get(1).toString().trim().split("\\s?,\\s?");
            if (groupByList.length == 0) {
                return resultMap;
            }
            // 第三个参数为distinctList字段，以,分隔，如col1,col2
            String[] distinctList = args.get(2).toString().trim().split("\\s?,\\s?");
            if (distinctList.length == 0) {
                return resultMap;
            }

            JSONArray jsonArray = JSON.parseArray(param);

            resultMap = jsonArray.stream()
                    .map(v -> {
                        // 只保留groupBy字段和distinct字段
                        JSONObject obj = (JSONObject) v;
                        JSONObject jsonObject = new JSONObject();
                        for (String groupBy : groupByList) {
                            jsonObject.put(groupBy, obj.get(groupBy));
                        }
                        for (String distinct : distinctList) {
                            jsonObject.put(distinct, obj.get(distinct));
                        }
                        return jsonObject;
                    })
                    .distinct()     // 去重
                    .collect(Collectors.groupingBy(
                            i -> {
                                // 按字段groupBy
                                StringBuilder key = new StringBuilder();
                                for (String groupBy : groupByList) {
                                    String val = i.get(groupBy) != null ? i.get(groupBy).toString() : "";
                                    key.append(val).append(MarkConstant.HYPHEN_CHARACTER);
                                }
                                return key.toString();
                            },
                            Collectors.counting()   // 统计数量
                    ));

            return resultMap;
        } catch (Exception e) {
            throw new TemplateModelException("GroupByToMap exception");
        }
    }
}
