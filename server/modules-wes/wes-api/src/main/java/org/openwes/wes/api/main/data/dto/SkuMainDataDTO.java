package org.openwes.wes.api.main.data.dto;

import com.google.common.collect.Sets;
import org.openwes.common.utils.validate.IValidate;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;

@Data
@Schema(description = "商品信息")
@NoArgsConstructor
@AllArgsConstructor
public class SkuMainDataDTO implements Serializable, IValidate {

    @Hidden
    private Long id;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "货主编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerCode;

    @NotEmpty
    @Size(max = 128)
    @Schema(title = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuName;

    @Schema(title = "商品样式")
    private String style;

    @Schema(title = "商品颜色")
    private String color;

    @Schema(title = "商品尺寸")
    private String size;

    @Schema(title = "商品品牌")
    private String brand;

    @Schema(description = "重量信息")
    private WeightDTO weight;

    @Schema(description = "体积信息")
    private VolumeDTO volumeDTO;

    @Schema(description = "商品属性信息")
    private SkuAttributeDTO skuAttribute;

    @Schema(description = "商品配置信息")
    private SkuConfigDTO skuConfig;

    @Schema(description = "商品包装信息")
    private SkuPackageDTO skuPackage;

    @Schema(description = "商品条码信息")
    private BarcodeDTO skuBarcode;

    @Hidden
    private Long version;

    @Override
    public boolean validate() {
        if (this.skuBarcode == null || ObjectUtils.isEmpty(this.skuBarcode.getBarcodes())) {
            return true;
        }

        return this.skuBarcode.getBarcodes().size() == Sets.newHashSet(this.skuBarcode.getBarcodes()).size();
    }

    /**
     * rewrite the equals and hash function to solve the problems that
     * for example , sku1: aab, owner1: baa; sku1: aabb, owner1: aa
     * if we only use string join , then they are the same.
     */
    public static class Key {
        private final String skuCode;
        private final String ownerCode;
        private final String warehouseCode;

        public Key(String skuCode, String ownerCode, String warehouseCode) {
            this.skuCode = skuCode;
            this.ownerCode = ownerCode;
            this.warehouseCode = warehouseCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(this.skuCode, key.skuCode) &&
                    Objects.equals(this.ownerCode, key.ownerCode) &&
                    Objects.equals(this.warehouseCode, key.warehouseCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.skuCode, this.ownerCode, this.warehouseCode);
        }
    }
}
