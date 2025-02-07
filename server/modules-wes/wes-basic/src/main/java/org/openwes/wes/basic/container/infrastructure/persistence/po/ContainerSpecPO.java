package org.openwes.wes.basic.container.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.ContainerTypeEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_container_spec",
        indexes = {
                @Index(unique = true, name = "uk_container_spec_code_warehouse", columnList = "containerSpecCode,warehouseCode")
        }
)
@DynamicUpdate
@Comment("Container Specification Management Table - Stores detailed specifications of containers including dimensions," +
        " slot configurations, and types.")
public class ContainerSpecPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the container specification")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the container specification")
    private String containerSpecCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this container specification is used")
    private String warehouseCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the container specification")
    private String containerSpecName;

    @Column(nullable = false)
    @Comment("Volume of the container (in cubic units)")
    private Long volume = 0L;

    @Column(nullable = false)
    @Comment("Height of the container (in units)")
    private Long height = 0L;

    @Column(nullable = false)
    @Comment("Width of the container (in units)")
    private Long width = 0L;

    @Column(nullable = false)
    @Comment("Length of the container (in units)")
    private Long length = 0L;

    @Column(nullable = false)
    @Comment("Number of slots in the container")
    private Integer containerSlotNum = 0;

    @Column()
    @Comment("Description of the container specification")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Type of the container. Possible values are: " +
            "CONTAINER (CONTAINER - 料箱), " +
            "TRANSFER_CONTAINER (TRANSFER_CONTAINER - 周转箱), " +
            "SHELF (SHELF - 料架), " +
            "PUT_WALL (PUT_WALL - 播种墙)")
    private ContainerTypeEnum containerType;

    @Column(length = 20)
    @Comment("Location of the container in a sowing wall (LEFT, MIDDLE, RIGHT)")
    private String location;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("JSON object containing specifications for each slot in the container")
    private List<ContainerSpecDTO.ContainerSlotSpec> containerSlotSpecs;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
