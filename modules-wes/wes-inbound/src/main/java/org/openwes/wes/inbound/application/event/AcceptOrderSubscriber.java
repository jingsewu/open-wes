package org.openwes.wes.inbound.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.inbound.event.AcceptEvent;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.transfer.AcceptOrderTransfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static org.openwes.common.utils.constants.RedisConstants.INBOUND_ACCEPT_ADD_LOCK;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptOrderSubscriber {

    private final AcceptOrderRepository acceptOrderRepository;
    private final AcceptOrderTransfer acceptOrderTransfer;
    private final ISkuBatchAttributeApi skuBatchAttributeApi;
    private final DistributeLock distributeLock;
    private final ISkuMainDataApi skuMainDataApi;

    @Subscribe
    public void onAccept(@Valid AcceptEvent event) {

        SkuMainDataDTO skuMainDataDTO = skuMainDataApi.getById(event.getSkuId());
        SkuBatchAttributeDTO skuBatchAttribute = skuBatchAttributeApi
                .getOrCreateSkuBatchAttribute(event.getSkuId(), event.getBatchAttributes());

        distributeLock.acquireLockIfThrows(INBOUND_ACCEPT_ADD_LOCK + event.getTargetContainerCode());
        try {
            AcceptOrder acceptOrder = acceptOrderRepository.findNewStatusAcceptOrder(event.getTargetContainerCode());

            AcceptOrderDetail acceptOrderDetail = acceptOrderTransfer.toDetailDO(skuMainDataDTO, event);
            acceptOrderDetail.setSkuBatchAttributeId(skuBatchAttribute.getId());

            if (acceptOrder == null) {
                acceptOrder = acceptOrderTransfer.toDO(event);
                acceptOrder.initialize();
            }
            acceptOrder.accept(acceptOrderDetail);

            acceptOrderRepository.saveOrderAndDetail(acceptOrder);

        } finally {
            distributeLock.releaseLock(INBOUND_ACCEPT_ADD_LOCK + event.getTargetContainerCode());
        }
    }
}
