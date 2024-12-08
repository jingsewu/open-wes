package org.openwes.wes.basic.work_station.application;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.validate.ValidationSequence;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.basic.IContainerSpecApi;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.dto.*;
import org.openwes.wes.api.basic.event.AssignOrderEvent;
import org.openwes.wes.api.basic.event.RemindSealContainerEvent;
import org.openwes.wes.api.task.dto.BindContainerDTO;
import org.openwes.wes.api.task.dto.UnBindContainerDTO;
import org.openwes.wes.basic.work_station.domain.aggregate.PutWallAggregate;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.domain.repository.PutWallSlotRepository;
import org.openwes.wes.basic.work_station.domain.service.PutWallService;
import org.openwes.wes.basic.work_station.domain.transfer.PutWallSlotTransfer;
import org.openwes.wes.basic.work_station.domain.transfer.PutWallTransfer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
@DubboService
@RequiredArgsConstructor
@Validated(value = ValidationSequence.class)
public class PutWallApiImpl implements IPutWallApi {

    private final IContainerSpecApi containerSpecApi;
    private final PutWallAggregate putWallAggregate;
    private final PutWallRepository putWallRepository;
    private final PutWallSlotRepository putWallSlotRepository;
    private final PutWallTransfer putWallTransfer;
    private final PutWallSlotTransfer putWallSlotTransfer;
    private final PutWallService putWallService;

    @Override
    public void create(PutWallDTO putWallDTO) {
        PutWall putWall = putWallTransfer.toDO(putWallDTO);
        putWall.initial();
        putWallRepository.save(putWall);
    }

    @Override
    public void update(PutWallDTO putWallDTO) {
        PutWall putWall = putWallTransfer.toDO(putWallDTO);

        List<PutWallSlot> exitSlots = putWallSlotRepository.findAllByPutWallId(putWall.getId());
        putWall.updateSlots(exitSlots);

        putWallAggregate.update(putWall, exitSlots);
    }

    @Override
    public void enable(Long putWallId) {
        PutWall putWall = putWallRepository.findById(putWallId);
        putWall.enable();
        putWallRepository.save(putWall);
    }

    @Override
    public void disable(Long putWallId) {
        PutWall putWall = putWallRepository.findById(putWallId);

        if (!putWallService.checkDisablePutWall(putWall)) {
            throw new WmsException("Could not disable");
        }

        putWall.disable();
        putWallRepository.save(putWall);
    }

    @Override
    public void delete(Long putWallId) {
        PutWall putWall = putWallRepository.findById(putWallId);
        putWall.delete();
        putWallRepository.save(putWall);
    }

    @Override
    public void assignOrders(AssignOrdersDTO assignOrdersDTO) {

        List<Long> workStationIds = assignOrdersDTO.getAssignDetails()
                .stream().map(AssignOrdersDTO.AssignDetail::getWorkStationId).toList();
        List<PutWallSlot> putWallSlots = putWallSlotRepository.findAllByWorkStationIds(workStationIds);

        List<PutWallSlot> updatePutWallSlots = Lists.newArrayList();
        putWallSlots.forEach(putWallSlot -> assignOrdersDTO.getAssignDetails().forEach(assignDetail -> {

            if (putWallSlot.getPutWallSlotCode().equals(assignDetail.getPutWallSlotCode())
                    && Objects.equals(putWallSlot.getWorkStationId(), assignDetail.getWorkStationId())) {
                putWallSlot.assignOrder(assignDetail.getOrderId());
                updatePutWallSlots.add(putWallSlot);
            }
        }));
        putWallSlotRepository.saveAll(updatePutWallSlots);

        List<PutWallSlotAssignedDTO> orderDetails = putWallSlots.stream().map(v ->
                new PutWallSlotAssignedDTO()
                        .setWorkStationId(v.getWorkStationId())
                        .setPutWallSlotCode(v.getPutWallSlotCode())
                        .setPtlTag(v.getPtlTag())).toList();

        DomainEventPublisher.sendAsyncDomainEvent(new AssignOrderEvent().setDetails(orderDetails));
    }

    @Override
    public void bindContainer(BindContainerDTO bindContainerDTO, Long transferContainerRecordId) {
        PutWallSlot putWallSlot = putWallSlotRepository.findBySlotCodeAndWorkStationId(bindContainerDTO.getPutWallSlotCode(), bindContainerDTO.getWorkStationId());
        putWallSlot.bindContainer(bindContainerDTO.getContainerCode(), transferContainerRecordId);
        putWallSlotRepository.save(putWallSlot);
    }

    @Override
    public void unBindContainer(UnBindContainerDTO unBindContainerDTO) {
        PutWallSlot putWallSlot = putWallSlotRepository.findBySlotCodeAndWorkStationId(unBindContainerDTO.getPutWallSlotCode(), unBindContainerDTO.getWorkStationId());
        putWallSlot.unBindContainer();
        putWallSlotRepository.save(putWallSlot);
    }

    @Override
    public void sealContainer(String putWallSlotCode, Long workStationId) {
        PutWallSlot putWallSlot = putWallSlotRepository.findBySlotCodeAndWorkStationId(putWallSlotCode, workStationId);
        putWallSlot.sealContainer();
        putWallSlotRepository.save(putWallSlot);
    }

    @Override
    public void remindToSealContainer(Long pickingOrderId, Map<Long, String> assignWorkStation) {
        List<PutWallSlot> putWallSlots = putWallSlotRepository.findAllByPickingOrderId(pickingOrderId)
                .stream()
                .filter(v -> assignWorkStation.containsKey(v.getWorkStationId())
                        && Objects.equals(assignWorkStation.get(v.getWorkStationId()), v.getPutWallSlotCode()))
                .toList();

        // when picking order completed after picking , system will remind to seal container.
        // but when full split container, the put wall slot status will change by the splitContainer function.
        // so there is no need to change the put wall slot status.
        if (CollectionUtils.isEmpty(putWallSlots)) {
            return;
        }

        putWallSlots.forEach(putWallSlot -> putWallSlot.remindToSealContainer(pickingOrderId));
        putWallSlotRepository.saveAll(putWallSlots);

        List<PutWallSlotRemindSealedDTO> sealContainerDetails = putWallSlots.stream()
                .map(k -> new PutWallSlotRemindSealedDTO()
                        .setPutWallSlotCode(k.getPutWallSlotCode())
                        .setWorkStationId(k.getWorkStationId())
                        .setPtlTag(k.getPtlTag())).toList();

        DomainEventPublisher.sendAsyncDomainEvent(new RemindSealContainerEvent().setDetails(sealContainerDetails));
    }

    @Override
    public PutWallSlotDTO getPutWallSlot(String putWallSlotCode, Long workStationId) {
        PutWallSlot putWallSlot = putWallSlotRepository.findBySlotCodeAndWorkStationId(putWallSlotCode, workStationId);
        return putWallSlotTransfer.toDTO(putWallSlot);
    }

    @Override
    public List<PutWallSlotDTO> getPutWallSlots(Collection<String> putWallSlotCodes, Long workStationId) {
        List<PutWallSlot> putWallSlots = putWallSlotRepository.findAllBySlotCodesAndWorkStationId(putWallSlotCodes, workStationId);
        return putWallSlotTransfer.toDTOs(putWallSlots);
    }
}
