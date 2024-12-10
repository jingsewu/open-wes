package org.openwes.wes.api.ems.proxy.dto;

import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class ContainerOperation implements Serializable {

    @NotEmpty
    private List<ContainerOperationDetail> containerOperationDetails;

    private Long workStationId;
    private String groupCode;

    @Data
    @Accessors(chain = true)
    public static class ContainerOperationDetail implements Serializable {

        private String containerCode;
        private String containerFace;
        private String locationCode;
        private String taskCode;

        private ContainerOperationTypeEnum operationType;

    }
}
