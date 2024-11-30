package org.openwes.wes.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ArrivedContainerDTO implements Serializable {

    @NotEmpty
    private String containerCode;

    private String face;
}
