package com.open.wes.extension.business.station;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomApiParameter {

    private Long workStationId;
    private Object requestParameter;
}
