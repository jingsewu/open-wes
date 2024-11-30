package org.openwes.wes.api.basic;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.openwes.wes.api.basic.dto.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IContainerApi {

    void createContainer(@NotNull String warehouseCode, @NotNull String containerCode, @NotNull String containerSpecCode);

    void createContainer(@NotNull String warehouseCode, @Valid List<CreateEmptyContainerDTO> containerDTOS);

    ContainerSpecDTO queryContainerLayout(@NotNull String containerCode, @NotNull String warehouseCode, String face);

    void changeContainerSpec(@NotNull String warehouseCode, @NotNull String containerCode, @NotNull String containerSpecCode);

    void changeContainerSpec(@NotNull Long containerId, @NotNull String containerSpecCode);

    void batchCreateContainer(@Valid BatchCreateContainerDTO createContainerDTO);

    ContainerDTO queryContainer(@NotNull String containerCode, @NotNull String warehouseCode);

    Collection<ContainerDTO> queryContainer(@NotNull Collection<String> containerCodes, @NotNull String warehouseCode);

    void lockContainer(@NotEmpty String warehouseCode, @NotEmpty Set<String> containerCodes);

    void unLockContainer(@NotEmpty String warehouseCode, @NotEmpty Set<String> containerCodes);

    void updateContainerLocation(@Valid List<ContainerLocationReportDTO> reportDTOS);
}
