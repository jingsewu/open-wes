package org.openwes.wes.common.validator;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

@Data
public class ValidateResult<R> {

    private Map<IValidator.ValidatorName, R> resultMap = Maps.newHashMap();

    public R getResult(IValidator.ValidatorName key) {
        return resultMap.get(key);
    }

    public void setResult(IValidator.ValidatorName key, R value) {
        resultMap.put(key, value);
    }
}
