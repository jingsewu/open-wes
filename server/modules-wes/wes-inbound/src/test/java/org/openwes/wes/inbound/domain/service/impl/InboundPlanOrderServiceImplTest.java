package org.openwes.wes.inbound.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.config.IBatchAttributeConfigApi;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuAttributeDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.common.validator.impl.ContainerValidator;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InboundPlanOrderServiceImplTest {

    private static final String WAREHOUSE = "WH001";
    private static final String CONTAINER = "RCV1";
    private static final String SLOT = "SLOT1";

    @Mock
    private InboundPlanOrderRepository inboundPlanOrderRepository;
    @Mock
    private IBatchAttributeConfigApi batchAttributeConfigApi;
    @Mock
    private ISkuMainDataApi skuMainDataApi;
    @Mock
    private ContainerValidator containerValidator;
    @Mock
    private AcceptOrderRepository acceptOrderRepository;
    @Mock
    private ISystemConfigApi systemConfigApi;
    @Mock
    private IContainerApi containerApi;

    private InboundPlanOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InboundPlanOrderServiceImpl(inboundPlanOrderRepository, batchAttributeConfigApi,
                skuMainDataApi, containerValidator, acceptOrderRepository, systemConfigApi, containerApi);

        SkuMainDataDTO sku = mock(SkuMainDataDTO.class);
        when(sku.getOwnerCode()).thenReturn("OWNER001");
        when(sku.getSkuAttribute()).thenReturn(mock(SkuAttributeDTO.class));
        when(skuMainDataApi.getById(1L)).thenReturn(sku);
    }

    @Test
    void validateAcceptDerivesFaceFromTargetContainerSlotWhenBlank() {
        AcceptRecordDTO acceptRecord = acceptRecord("");
        when(containerApi.queryContainer(CONTAINER, WAREHOUSE)).thenReturn(containerWithSlotFace("FACE1"));

        service.validateAccept(acceptRecord, 1L);

        assertEquals("FACE1", acceptRecord.getTargetContainerFace());
    }

    @Test
    void validateAcceptKeepsCallerProvidedFace() {
        AcceptRecordDTO acceptRecord = acceptRecord("FACE2");

        service.validateAccept(acceptRecord, 1L);

        assertEquals("FACE2", acceptRecord.getTargetContainerFace());
        verifyNoInteractions(containerApi);
    }

    @Test
    void validateAcceptLeavesFaceBlankWhenSlotIsUnknown() {
        AcceptRecordDTO acceptRecord = acceptRecord("");
        ContainerDTO container = containerWithSlotFace("FACE1");
        container.getContainerSlots().get(0).setContainerSlotCode("OTHER_SLOT");
        when(containerApi.queryContainer(CONTAINER, WAREHOUSE)).thenReturn(container);

        service.validateAccept(acceptRecord, 1L);

        assertEquals("", acceptRecord.getTargetContainerFace());
    }

    private static AcceptRecordDTO acceptRecord(String face) {
        AcceptRecordDTO acceptRecord = new AcceptRecordDTO();
        acceptRecord.setWarehouseCode(WAREHOUSE);
        acceptRecord.setSkuId(1L);
        acceptRecord.setQtyAccepted(1);
        acceptRecord.setTargetContainerId(1L);
        acceptRecord.setTargetContainerCode(CONTAINER);
        acceptRecord.setTargetContainerSpecCode("RCV_SPEC");
        acceptRecord.setTargetContainerSlotCode(SLOT);
        acceptRecord.setTargetContainerFace(face);
        acceptRecord.setWorkStationId(1L);
        return acceptRecord;
    }

    private static ContainerDTO containerWithSlotFace(String face) {
        ContainerDTO.ContainerSlot slot = new ContainerDTO.ContainerSlot();
        slot.setContainerSlotCode(SLOT);
        slot.setFace(face);
        ContainerDTO container = new ContainerDTO();
        container.setContainerCode(CONTAINER);
        container.setWarehouseCode(WAREHOUSE);
        container.setContainerSlots(List.of(slot));
        return container;
    }
}
