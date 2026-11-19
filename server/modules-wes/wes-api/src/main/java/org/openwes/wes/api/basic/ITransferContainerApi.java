package org.openwes.wes.api.basic;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.TransferContainerRecordDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.task.dto.*;

import java.util.List;

public interface ITransferContainerApi {

    void containerArrive(@Valid ContainerArrivedEvent containerArrivedEvent);

    void transferContainerRelease(@Valid List<TransferContainerReleaseDTO> releaseDTOS);

    void release(Long id);

    TransferContainerDTO findByContainerCodeAndWarehouseCode(@NotEmpty String containerCode, @NotEmpty String warehouseCode);

    List<TransferContainerDTO> findAllByWarehouseCodeAndContainerCodeIn(String warehouseCode, List<String> transferContainerCodes);

    PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO);

    PutWallSlotDTO unBindContainer(UnBindContainerDTO unBindContainerDTO, Long transferContainerRecordId);

    PutWallSlotDTO sealContainer(SealContainerDTO sealContainerDTO);

    void sealContainer(Long pickingOrderId);
}
