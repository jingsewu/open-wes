package org.openwes.wes.api.main.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品属性信息")
public class SkuAttributeDTO implements Serializable {

    @Schema(title = "商品图片地址")
    private String imageUrl;

    @Schema(title = "商品单位")
    private String unit;

    @Schema(title = "一级分类")
    private String skuFirstCategory;

    @Schema(title = "二级分类")
    private String skuSecondCategory;

    @Schema(title = "三级分类")
    private String skuThirdCategory;

    @Schema(title = "商品属性类别")
    private String skuAttributeCategory;

    @Schema(title = "商品属性子类别")
    private String skuAttributeSubCategory;
}
