package org.openwes.wes.outbound.application;

import com.openwes.search.api.ISearchApi;
import com.openwes.search.api.vo.ContainerSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.wes.api.outbound.IEmptyContainerOutboundOrderApi;
import org.openwes.wes.api.outbound.dto.EmptyContainerOutboundOrderCreateDTO;
import org.openwes.wes.outbound.application.usecase.EmptyContainerOutboundUseCase;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrderDetail;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.openwes.wes.outbound.domain.transfer.EmptyContainerOutboundTransfer;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Validated
@Service
@Slf4j
public class EmptyContainerOutboundOrderApiImpl implements IEmptyContainerOutboundOrderApi {

    private final EmptyContainerOutboundUseCase emptyContainerOutboundUseCase;
    private final EmptyContainerOutboundOrderRepository emptyContainerOutboundOrderRepository;
    private final EmptyContainerOutboundTransfer emptyContainerOutboundTransfer;
    private final ISearchApi searchApi;

    @Override
    public void createEmptyContainerOutboundOrder(EmptyContainerOutboundOrderCreateDTO emptyContainerOutboundOrderCreateDTO) {

        Map<String, Object> requestMap = JsonUtils.string2MapObject(JsonUtils.obj2String(emptyContainerOutboundOrderCreateDTO));
        List<ContainerSearchVO> containerSearchVOs = searchApi.search(ContainerSearchVO.class, requestMap);

        if (ObjectUtils.isEmpty(containerSearchVOs)) {
            throw new IllegalArgumentException("can not found any empty containers");
        }

        List<EmptyContainerOutboundOrderDetail> details = containerSearchVOs.stream().map(v ->
            EmptyContainerOutboundOrderDetail.builder()
                .containerId(v.getId())
                .containerCode(v.getContainerCode())
                .build()
        ).toList();

        EmptyContainerOutboundOrder emptyContainerOutboundOrder = emptyContainerOutboundTransfer.toDO(emptyContainerOutboundOrderCreateDTO, details);
        emptyContainerOutboundOrder.initial();

        emptyContainerOutboundUseCase.create(emptyContainerOutboundOrder,
                containerSearchVOs.stream().map(ContainerSearchVO::getContainerCode).collect(Collectors.toSet()));

    }

    @Override
    public void execute(List<Long> orderIds) {
        List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders = emptyContainerOutboundOrderRepository.findAllByIds(orderIds);
        emptyContainerOutboundUseCase.execute(emptyContainerOutboundOrders);
    }

    @Override
    public void cancel(List<Long> orderIds) {
        List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders = emptyContainerOutboundOrderRepository.findAllByIds(orderIds);
        emptyContainerOutboundUseCase.cancel(emptyContainerOutboundOrders);
    }

    @Override
    public void completeDetails(List<Long> detailIds) {
        List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders = emptyContainerOutboundOrderRepository.findOrderByDetailIds(detailIds);

        emptyContainerOutboundOrders.forEach(emptyContainerOutboundOrder ->
                emptyContainerOutboundOrder.complete(detailIds));

        emptyContainerOutboundOrderRepository.saveAllOrderAndDetails(emptyContainerOutboundOrders);
    }

}
