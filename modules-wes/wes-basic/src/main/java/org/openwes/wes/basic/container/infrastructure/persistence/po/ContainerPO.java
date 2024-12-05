package org.openwes.wes.basic.container.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.ContainerStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_container",
        indexes = {
                @Index(unique = true, name = "uk_container_code_warehouse_code", columnList = "containerCode,warehouseCode"),
                @Index(name = "idx_container_spec_code", columnList = "containerSpecCode")
        }
)
@DynamicUpdate
public class ContainerPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器编码'")
    private String containerCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器规格编码'")
    private String containerSpecCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;
    @Column(columnDefinition = "varchar(64) comment '库区编码'")
    private String warehouseAreaCode;
    @Column(nullable = false, columnDefinition = "bigint comment '库区ID'")
    private Long warehouseAreaId = 0L;
    @Column(columnDefinition = "varchar(64) comment '逻辑区编码'")
    private String warehouseLogicCode;
    @Column(nullable = false, columnDefinition = "bigint comment '逻辑区ID'")
    private Long warehouseLogicId = 0L;
    @Column(columnDefinition = "varchar(64) comment '库位编码'")
    private String locationCode;
    @Column(columnDefinition = "varchar(64) comment '库位类型'")
    private String locationType;

    @Column(nullable = false, columnDefinition = "decimal(18,6) default '0.000000' comment 'SKU体积占用比例'")
    private BigDecimal occupationRatio = BigDecimal.ZERO;

    private boolean emptyContainer;
    private boolean locked;
    private boolean opened;

    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '格口数量'")
    private Integer containerSlotNum;
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '空格口数量'")
    private Integer emptySlotNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '容器状态'")
    private ContainerStatusEnum containerStatus;

    @Column(columnDefinition = "json comment '容器格口'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ContainerDTO.ContainerSlot> containerSlots;

    @Version
    private Long version;
}
