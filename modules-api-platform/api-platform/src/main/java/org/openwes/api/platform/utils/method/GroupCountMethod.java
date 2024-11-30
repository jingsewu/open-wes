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

public class GroupCountMethod implements TemplateMethodModelEx {

    @Override
    public Integer exec(List args) throws TemplateModelException {
        if (CollectionUtils.isEmpty(args)) {
            return 0;
        }
        try {
            //第一个参数为原始列表数据，格式为List<Object>
            String param = JSON.toJSONString(((DefaultListAdapter) args.get(0)).getWrappedObject());
            if (StringUtils.isBlank(param)) {
                return 0;
            }
            //第二个参数为group字段，以,分隔，如col1,col2
            String[] groupByList = args.get(1).toString().split(MarkConstant.COMMA_MARK);

            JSONArray jsonArray = JSON.parseArray(param);

            Map<String, Object> resultMap = jsonArray.stream().collect(Collectors.toMap(i -> {
                                //按字段groupBy
                                JSONObject obj = (JSONObject) i;
                                String key = "";
                                for (String groupBy : groupByList) {
                                    String val = obj.get(groupBy) != null ? obj.get(groupBy).toString() : "";
                                    key = key + val + MarkConstant.HYPHEN_CHARACTER;
                                }
                                return key;
                            },
                            (v) -> v,
                            //按字段汇总
                            (o1, o2) -> o2)
            );

            return resultMap.size();
        } catch (Exception e) {
            throw new TemplateModelException("GroupCount exception");
        }
    }
}
