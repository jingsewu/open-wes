package org.openwes.search.op;

import cn.zhxu.bs.FieldOp;
import cn.zhxu.bs.SqlWrapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonContainsKey implements FieldOp {

    public static final String JCK = "jck";

    @Override
    public String name() {
        return JCK;
    }

    @Override
    public boolean isNamed(String name) {
        return JCK.equals(name);
    }

    @Override
    public List<Object> operate(StringBuilder sqlBuilder, OpPara opPara) {
        SqlWrapper<Object> fieldSql = opPara.getFieldSql();
        Object[] values = opPara.getValues();
        sqlBuilder.append("JSON_CONTAINS(JSON_KEYS(").append(fieldSql.getSql()).append("), '\"").append(values[0]).append("\"')");
        return fieldSql.getParas();
    }
}
