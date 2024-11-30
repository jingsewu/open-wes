package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.dto.AssignOrdersDTO;
import org.openwes.wes.api.basic.dto.CreatePutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.task.dto.BindContainerDTO;
import org.openwes.wes.api.task.dto.UnBindContainerDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IPutWallApi {

    void create(@Valid CreatePutWallDTO createPutWallDTO);

    void update(@Valid CreatePutWallDTO putWallDTO);

    void enable(@NotNull Long putWallId);

    void disable(@NotNull Long putWallId);

    void delete(@NotNull Long putWallId);

    void assignOrders(@Valid AssignOrdersDTO assignOrdersDTO);

    void bindContainer(@Valid BindContainerDTO bindContainerDTO, Long id);

    void unBindContainer(@Valid UnBindContainerDTO unBindContainerDTO);

    void sealContainer(String putWallSlotCode, Long workStationId);

    void remindToSealContainer(Long pickingOrderId, Map<Long, String> assignWorkStation);

    PutWallSlotDTO getPutWallSlot(String putWallSlotCode, Long workStationId);

    List<PutWallSlotDTO> getPutWallSlots(Collection<String> putWallSlotCodes, Long workStationId);
}
