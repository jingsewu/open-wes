package org.openwes.wes.api.main.data.dto;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品条码信息")
public class BarcodeDTO implements Serializable {

    @Schema(description = "商品条码列表")
    private List<String> barcodes;

    public BarcodeDTO(String... barcodes) {
        this.barcodes = Lists.newArrayList(barcodes);
    }
}
