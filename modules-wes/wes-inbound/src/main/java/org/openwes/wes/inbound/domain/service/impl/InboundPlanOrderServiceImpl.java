package org.openwes.wes.inbound.domain.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.config.IBatchAttributeConfigApi;
import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.common.validator.IValidator;
import org.openwes.wes.common.validator.ValidateResult;
import org.openwes.wes.common.validator.ValidatorFactory;
import org.openwes.wes.common.validator.impl.ContainerValidator;
import org.openwes.wes.common.validator.impl.OwnerValidator;
import org.openwes.wes.common.validator.impl.SkuValidator;
import org.openwes.wes.common.validator.impl.WarehouseValidator;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrderDetail;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;
import org.openwes.wes.inbound.domain.service.InboundPlanOrderService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.InboundErrorDescEnum.*;

@Service
@RequiredArgsConstructor
public class InboundPlanOrderServiceImpl implements InboundPlanOrderService {

    private final InboundPlanOrderRepository inboundPlanOrderRepository;
    private final IBatchAttributeConfigApi batchAttributeConfigApi;
    private final ISkuMainDataApi skyMainDataApi;
    private final ContainerValidator containerValidator;
    private final AcceptOrderRepository acceptOrderRepository;

    @Override
    public ValidateResult<Set<SkuMainDataDTO>> validateCreation(List<InboundPlanOrder> inboundPlanOrders) {
        ValidateResult<Set<SkuMainDataDTO>> result = new ValidateResult<>();
        Set<SkuMainDataDTO> skuMainDataDTOS = Sets.newHashSet();

        inboundPlanOrders.forEach(inboundPlanOrder -> {
            WarehouseValidator warehouseValidator = (WarehouseValidator) ValidatorFactory.getValidator(IValidator.ValidatorName.WAREHOUSE);
            warehouseValidator.validate(Lists.newArrayList(inboundPlanOrder.getWarehouseCode()));

            OwnerValidator ownerValidator = (OwnerValidator) ValidatorFactory.getValidator(IValidator.ValidatorName.OWNER);
            SkuValidator skuValidator = (SkuValidator) ValidatorFactory.getValidator(IValidator.ValidatorName.SKU);

            List<InboundPlanOrderDetail> details = inboundPlanOrder.getDetails();
            Set<String> detailOwnerCodes = details.stream().map(InboundPlanOrderDetail::getOwnerCode).collect(Collectors.toSet());
            ownerValidator.validate(new OwnerValidator.OwnerValidatorObject(detailOwnerCodes, inboundPlanOrder.getWarehouseCode()));
            skuMainDataDTOS.addAll(details.stream()
                    .collect(Collectors.groupingBy(InboundPlanOrderDetail::getOwnerCode, Collectors.mapping(InboundPlanOrderDetail::getSkuCode, Collectors.toSet())))
                    .entrySet()
                    .stream()
                    .map(entry -> skuValidator.validate(new SkuValidator.SkuValidatorObject(entry.getValue(), entry.getKey())))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()));

        });
        result.setResult(IValidator.ValidatorName.SKU, skuMainDataDTOS);
        return result;
    }

    @Override
    public void beforeDoCreation(List<InboundPlanOrderDTO> inboundPlanOrderDTO) {

    }

    @Override
    public void afterDoCreation(List<InboundPlanOrder> inboundPlanOrder) {

    }

    @Override
    public void syncValidate(List<InboundPlanOrder> inboundPlanOrders) {

        inboundPlanOrders.stream().collect(Collectors.groupingBy(InboundPlanOrder::getWarehouseCode))
                .forEach((warehouseCode, subInboundPlanOrders) -> {
                    Set<String> customerOrderNos = subInboundPlanOrders.stream().map(InboundPlanOrder::getCustomerOrderNo).collect(Collectors.toSet());
                    Set<String> lpnCodes = subInboundPlanOrders.stream().map(InboundPlanOrder::getLpnCode).collect(Collectors.toSet());
                    Set<String> boxNos = subInboundPlanOrders.stream().flatMap(v -> v.getDetails().stream())
                            .map(InboundPlanOrderDetail::getBoxNo).filter(ObjectUtils::isNotEmpty).collect(Collectors.toSet());
                    checkRepeatCustomerOrderNo(customerOrderNos, warehouseCode);
                    checkRepeatLpnCode(lpnCodes, warehouseCode);
                    checkRepeatBoxNo(boxNos, warehouseCode);
                });

    }

    @Override
    public void validateAccept(AcceptRecordDTO acceptRecord, Long skuId) {

        containerValidator.validate(new ContainerValidator.ContainerValidatorObject()
                .setContainerCode(acceptRecord.getTargetContainerCode())
                .setContainerSlotCode(acceptRecord.getTargetContainerSlotCode())
                .setWarehouseCode(acceptRecord.getWarehouseCode()));

        SkuMainDataDTO skuMainDataDTO = skyMainDataApi.getById(skuId);
        BatchAttributeConfigDTO batchAttributeConfigDTO = batchAttributeConfigApi.getByOwnerAndSkuFirstCategory(skuMainDataDTO.getOwnerCode(),
                Optional.ofNullable(skuMainDataDTO.getSkuAttribute().getSkuFirstCategory()).orElse(StringUtils.EMPTY)
        );

        if (batchAttributeConfigDTO != null) {
            batchAttributeConfigDTO.validateBatchAttribute(acceptRecord.getBatchAttributes());
        }

    }

    @Override
    public void validateClose(List<InboundPlanOrder> inboundPlanOrders) {
        List<Long> inboundPlanOrderIds = inboundPlanOrders.stream().map(InboundPlanOrder::getId).toList();
        List<AcceptOrder> acceptOrders = acceptOrderRepository.findAllByInboundPlanOrderIds(inboundPlanOrderIds);
        if (CollectionUtils.isNotEmpty(acceptOrders)
                && acceptOrders.stream().anyMatch(v -> v.getAcceptOrderStatus() == AcceptOrderStatusEnum.NEW)) {
            throw WmsException.throwWmsException(INBOUND_CLOSE_ACCEPT_ORDER_NOT_COMPLETED);
        }
    }

    @Override
    public void validateForceCompleteAccept(InboundPlanOrder inboundPlanOrder) {

    }

    private void checkRepeatLpnCode(Set<String> lpnCodes, String warehouseCode) {
        boolean exist = inboundPlanOrderRepository.existByLpnCodeAndStatus(lpnCodes,
                List.of(InboundPlanOrderStatusEnum.NEW, InboundPlanOrderStatusEnum.ACCEPTING), warehouseCode);
        if (exist) {
            throw WmsException.throwWmsException(INBOUND_LPN_CODE_REPEATED, lpnCodes);
        }
    }

    private void checkRepeatCustomerOrderNo(Set<String> customerOrderNos, String warehouseCode) {
        boolean exist = inboundPlanOrderRepository.existByCustomerOrderNo(customerOrderNos, warehouseCode);
        if (exist) {
            throw WmsException.throwWmsException(INBOUND_CST_ORDER_NO_REPEATED, customerOrderNos);
        }
    }

    private void checkRepeatBoxNo(Set<String> boxNos, String warehouseCode) {
        if (CollectionUtils.isEmpty(boxNos)) {
            return;
        }
        boolean boxExists = inboundPlanOrderRepository.existByBoxNos(boxNos, warehouseCode);
        if (boxExists) {
            throw WmsException.throwWmsException(INBOUND_BOX_NO_EXIST);
        }
    }
}
