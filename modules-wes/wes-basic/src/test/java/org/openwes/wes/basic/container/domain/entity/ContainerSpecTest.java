package org.openwes.wes.basic.container.domain.entity;

import org.openwes.wes.api.basic.constants.ContainerTypeEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ContainerSpecTest {

    private ContainerSpec containerSpec;

    @BeforeEach
    void setUp() {
        containerSpec = new ContainerSpec();
        containerSpec.setId(1L);
        containerSpec.setWarehouseCode("WH001");
        containerSpec.setContainerSpecCode("CSC001");
        containerSpec.setContainerSpecName("Container Spec Name");
        containerSpec.setLength(100);
        containerSpec.setWidth(50);
        containerSpec.setHeight(30);
        containerSpec.setVolume(15000L);
        containerSpec.setLocation("Location");
        containerSpec.setContainerSlotNum(10);
        containerSpec.setDescription("Description");
        containerSpec.setContainerType(ContainerTypeEnum.CONTAINER);

        ContainerSpecDTO.ContainerSlotSpec containerSlotSpec1 = new ContainerSpecDTO.ContainerSlotSpec();
        containerSlotSpec1.setFace("FACE1");
        containerSlotSpec1.setLevel(1);
        containerSlotSpec1.setBay(1);

        ContainerSpecDTO.ContainerSlotSpec containerSlotSpec2 = new ContainerSpecDTO.ContainerSlotSpec();
        containerSlotSpec2.setFace("FACE2");
        containerSlotSpec2.setLevel(2);
        containerSlotSpec2.setBay(2);

        containerSpec.setContainerSlotSpecs(Lists.newArrayList(containerSlotSpec1, containerSlotSpec2));
        containerSpec.setVersion(1L);
    }

    @Test
    void getContainerSlotSpecsByFace_ValidFace_ReturnsCorrectList() {
        List<ContainerSpecDTO.ContainerSlotSpec> result = containerSpec.getContainerSlotSpecsByFace("FACE1");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("FACE1", result.get(0).getFace());
    }

    @Test
    void getContainerSlotSpecsByFace_InvalidFace_ReturnsEmptyList() {
        List<ContainerSpecDTO.ContainerSlotSpec> result = containerSpec.getContainerSlotSpecsByFace("FACE3");
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}
