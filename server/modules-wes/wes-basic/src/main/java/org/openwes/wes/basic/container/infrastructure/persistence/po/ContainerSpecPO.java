package org.openwes.wes.basic.container.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.ContainerTypeEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
public class ContainerSpecPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器规格编码'")
    private String containerSpecCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '容器规格名称'")
    private String containerSpecName;

    @Column(nullable = false, columnDefinition = "bigint comment '体积'")
    private Long volume = 0L;
    @Column(nullable = false, columnDefinition = "bigint comment '高度'")
    private Long height = 0L;
    @Column(nullable = false, columnDefinition = "bigint comment '宽度'")
    private Long width = 0L;
    @Column(nullable = false, columnDefinition = "bigint comment '长度'")
    private Long length = 0L;

    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '格子数量'")
    private Integer containerSlotNum = 0;

    @Column(columnDefinition = "varchar(255) comment '描述'")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '容器类型'")
    private ContainerTypeEnum containerType;

    @Column(columnDefinition = "varchar(20) comment '播种墙位置：LEFT/MIDDLE/RIGHT'")
    private String location;

    @Column(columnDefinition = "json comment '容器格子规格'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ContainerSpecDTO.ContainerSlotSpec> containerSlotSpecs;

    @Version
    private Long version;
}
