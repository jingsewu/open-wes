package org.openwes.wes.api.main.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "重量信息")
public class WeightDTO implements Serializable {

    @Schema(title = "毛重", description = "毫克(mg)")
    private Long grossWeight;

    @Schema(title = "净重", description = "毫克(mg)")
    private Long netWeight;
}
