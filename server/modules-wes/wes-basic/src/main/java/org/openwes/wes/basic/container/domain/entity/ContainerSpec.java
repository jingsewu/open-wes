package org.openwes.wes.basic.container.domain.entity;

import org.openwes.wes.api.basic.constants.ContainerTypeEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class ContainerSpec {

    private Long id;

    private String warehouseCode;

    // unique identifier
    private String containerSpecCode;
    private String containerSpecName;

    private Integer length;
    private Integer width;
    private Integer height;
    private Long volume;
    private String location;

    private Integer containerSlotNum;

    private String description;

    private ContainerTypeEnum containerType;

    private List<ContainerSpecDTO.ContainerSlotSpec> containerSlotSpecs;

    private Long version;

    public List<ContainerSpecDTO.ContainerSlotSpec> getContainerSlotSpecsByFace(String face) {
        return containerSlotSpecs.stream().filter(v -> StringUtils.equals(v.getFace(), face)).toList();
    }
}
