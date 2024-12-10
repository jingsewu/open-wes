package org.openwes.api.platform.utils.method;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.openwes.common.utils.constants.MarkConstant;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GroupByMethod implements TemplateMethodModelEx {

    @Override
    public List<Object> exec(List args) throws TemplateModelException {
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
            List<String> groupByList = Arrays.asList(args.get(1).toString().split(MarkConstant.COMMA_MARK));
            //第三个参数为合并汇总字段，以,分隔，如num1,num2，要求必须是数字类型
            List<String> sumList = args.size() > 2 ? Arrays.asList(args.get(2).toString().split(MarkConstant.COMMA_MARK)) : null;
            //第四个参数是数字类型，Integer、BigDecimal
            String numberType = args.size() > 3 ? args.get(3).toString() : "Integer";

            JSONArray jsonArray = JSON.parseArray(param);

            Map<String, Object> resultMap = jsonArray.stream().collect(Collectors.toMap(i -> {
                                //按字段groupBy
                                JSONObject obj = (JSONObject) i;
                                String key = "";
                                for (String groupBy : groupByList) {
                                    String val = val(groupBy, obj);
                                    key = key + val + MarkConstant.HYPHEN_CHARACTER;
                                }
                                return key;
                            },
                            (v) -> v,
                            //按字段汇总
                            (o1, o2) -> {
                                if (CollectionUtils.isEmpty(sumList)) {
                                    return o2;
                                }
                                JSONObject obj1 = (JSONObject) o1;
                                JSONObject obj2 = (JSONObject) o2;
                                for (String sumCol : sumList) {
                                    if ("BigDecimal".equals(numberType)) {
                                        BigDecimal val1 = obj1.get(sumCol) != null ? new BigDecimal(obj1.get(sumCol).toString()) : BigDecimal.ZERO;
                                        BigDecimal val2 = obj2.get(sumCol) != null ? new BigDecimal(obj2.get(sumCol).toString()) : BigDecimal.ZERO;
                                        obj2.put(sumCol, val1.add(val2));
                                    } else {
                                        Integer val1 = obj1.get(sumCol) != null ? Integer.parseInt(obj1.get(sumCol).toString()) : 0;
                                        Integer val2 = obj2.get(sumCol) != null ? Integer.parseInt(obj2.get(sumCol).toString()) : 0;
                                        obj2.put(sumCol, val1 + val2);
                                    }
                                }
                                return obj2;
                            })
            );

            return new ArrayList(resultMap.values());
        } catch (Exception e) {
            log.error("GroupByMethod error", e);
            throw new TemplateModelException("数据分组汇总异常");
        }
    }

    /**
     * 解决第二个参数group字段需要取有层级的字段值，
     * 如：skuCode,outboundOrderDetailDTO.reservedField3 中的 outboundOrderDetailDTO.reservedField3
     */
    private String val(String groupBy, JSONObject obj) {
        String[] split = groupBy.split("\\.");
        if (split.length < 2) {
            return obj.get(groupBy) != null ? obj.get(groupBy).toString() : "";
        }
        JSONObject curObj = obj;
        for (int i = 0; i < split.length - 1; i++) {
            // curObj只取到倒数第二个，最终要取的值类型是String，String强转为JSONObject会报错
            curObj = (JSONObject) curObj.get(split[i]);
        }
        return curObj != null ? curObj.get(split[split.length - 1]).toString() : "";    // 取到最终需要的key值
    }
}
