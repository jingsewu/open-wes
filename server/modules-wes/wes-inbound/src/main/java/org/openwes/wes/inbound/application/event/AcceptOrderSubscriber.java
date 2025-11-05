package org.openwes.wes.inbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.inbound.event.InboundPlanOrderAcceptedEvent;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.transfer.AcceptOrderTransfer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptOrderSubscriber {

    private final AcceptOrderRepository acceptOrderRepository;
    private final AcceptOrderTransfer acceptOrderTransfer;
    private final ISkuBatchAttributeApi skuBatchAttributeApi;
    private final ISkuMainDataApi skuMainDataApi;

    @Subscribe
    public void onAccept(@Valid InboundPlanOrderAcceptedEvent event) {

        SkuMainDataDTO skuMainDataDTO = skuMainDataApi.getById(event.getSkuId());
        SkuBatchAttributeDTO skuBatchAttribute = skuBatchAttributeApi
                .getOrCreateSkuBatchAttribute(event.getSkuId(), event.getBatchAttributes());

        AcceptOrder acceptOrder = acceptOrderRepository.findNewStatusAcceptOrder(event.getTargetContainer().getTargetContainerCode());

        AcceptOrderDetail acceptOrderDetail = acceptOrderTransfer.toDetailDO(skuMainDataDTO, event, event.getTargetContainer());
        acceptOrderDetail.setSkuBatchAttributeId(skuBatchAttribute.getId());

        if (acceptOrder == null) {
            acceptOrder = acceptOrderTransfer.toDO(event);
            acceptOrder.initialize();
        }
        acceptOrder.accept(acceptOrderDetail);

        acceptOrderRepository.saveOrderAndDetail(acceptOrder);

    }
}
