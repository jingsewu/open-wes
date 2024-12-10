package org.openwes.api.platform.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

    /**
     * 判断数据是否是json格式
     *
     * @param obj
     * @return
     */
    public static boolean isJson(Object obj) {
        try {
            if (obj == null) {
                return true;
            }
            JSONObject.parse(obj.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * body内容解析，兼容数组及单个对象模式
     *
     * @param body
     * @return
     */
    public static JSONArray parseBody(String body) {
        Object obj = JSON.parse(body);
        if (obj instanceof JSONObject) {
            JSONArray array = new JSONArray();
            array.add(obj);
            return array;
        } else if (obj instanceof JSONArray jsonArray) {
            return jsonArray;
        }
        AssertUtils.throwBizException(CommonErrorDescEnum.PARAMETER_ERROR, "Illegal data format");
        return null;
    }

    /**
     * 根据路径获取json节点内容
     *
     * @param content
     * @param path
     * @return
     */
    public static String extractByPath(String content, String path) {
        //路径为空，不需要转换
        if (StringUtils.isBlank(path)) {
            return content;
        }
        if (StringUtils.isBlank(content)) {
            AssertUtils.throwBizException(CommonErrorDescEnum.PARAMETER_ERROR, "Illegal parameter");
        }
        try {
            return JSON.toJSONString(JSONPath.extract(content, path));
        } catch (Exception e) {
            AssertUtils.throwBizException(CommonErrorDescEnum.PARAMETER_ERROR, "Extract body error.path=" + path);
        }
        return content;
    }
}
