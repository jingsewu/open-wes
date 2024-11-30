package org.openwes.wes.outbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.outbound.constants.OutboundWaveStatusEnum;
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
    name = "w_outbound_wave",
    indexes = {
        @Index(unique = true, name = "uk_wave_no", columnList = "waveNo")
    }
)
@DynamicUpdate
public class OutboundWavePO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;

    private int priority;

    private boolean shortOutbound;

    @Column(nullable = false, columnDefinition = "varchar(32) comment '波次号'")
    private String waveNo;

    @Column(columnDefinition = "json comment '出库计划单ID'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Long> outboundPlanOrderIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private OutboundWaveStatusEnum waveStatus;

    @Version
    private Long version;
}
