package org.openwes.station.application.business.handler.event.stocktake;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StocktakeSubmitEvent implements Serializable {

    @NotNull
    private Long detailId;

    @NotNull
    @Min(0)
    private Integer stocktakeQty;
}
