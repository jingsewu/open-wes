package org.openwes.wes.config.domain.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.api.config.constants.ParserObjectEnum;
import org.openwes.wes.api.config.constants.UnionLocationEnum;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class BarcodeParseRule extends UpdateUserDTO {

    private Long id;

    private String code;
    private String name;

    private String ownerCode;

    private ExecuteTimeEnum executeTime;
    private BusinessFlowEnum businessFlow;

    private String brand;

    private boolean enable;

    private UnionLocationEnum unionLocation;
    private String unionStr;

    private String regularExpression;

    /**
     * these field values are from ParserObjectEnum
     *
     * @see ParserObjectEnum
     */
    private List<String> resultFields;

    private Long version;

    public Map<String, String> parse(String barcode) {
        String unionBarcode = union(barcode);
        List<String> compileResult = compile(unionBarcode);
        return buildResult(compileResult);
    }

    private String union(String barcode) {
        if (unionLocation == UnionLocationEnum.LEFT) {
            return StringUtils.join(unionStr, barcode);
        } else {
            return StringUtils.join(barcode, unionStr);
        }
    }

    private List<String> compile(String unionBarcode) {
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(unionBarcode);
        List<String> result = Lists.newArrayListWithCapacity(resultFields.size());
        if (matcher.find()) {
            try {
                for (int i = 1; i <= resultFields.size(); i++) {
                    result.add(matcher.group(i));
                }
            } catch (Exception e) {
                log.error("barcode rule parse error,regex: {},parameter: {}, size: {}", regularExpression, unionBarcode,
                        resultFields.size(), e);
            }
        }
        return result;
    }

    private Map<String, String> buildResult(List<String> compileResult) {
        if (CollectionUtils.isEmpty(compileResult)) {
            return Maps.newHashMap();
        }
        if (compileResult.size() != resultFields.size()) {
            return Maps.newHashMap();
        }

        Map<String, String> map = Maps.newHashMap();
        for (int i = 0; i < resultFields.size(); i++) {
            String field = resultFields.get(i);
            String result = compileResult.get(i);
            map.put(field, result);
        }
        return map;
    }

    public void enable() {
        this.enable = true;
    }

    public void disable() {
        this.enable = false;
    }

    public boolean match(String ownerCode, String brand) {
        return matchOwner(ownerCode) && matchBrand(brand);
    }

    /**
     * when choose * ,it means all
     *
     * @param ownerCode
     * @return
     */
    public boolean matchOwner(String ownerCode) {
        return StringUtils.equals(this.ownerCode, "*") || StringUtils.equals(ownerCode, "*")
                || StringUtils.equals(this.ownerCode, ownerCode);
    }

    public boolean matchBrand(String brand) {
        return StringUtils.equals(this.brand, "*") || StringUtils.equals(brand, "*") ||
                StringUtils.equals(this.brand, brand);
    }
}
