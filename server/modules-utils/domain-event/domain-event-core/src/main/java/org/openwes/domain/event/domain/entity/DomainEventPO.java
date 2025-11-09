package org.openwes.domain.event.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.openwes.domain.event.constants.DomainEventStatusEnum;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "d_domain_event",
        indexes = {
                @Index(name = "idx_create_time", columnList = "createTime"),
                @Index(name = "idx_aggregator_id", columnList = "aggregatorId"),
                @Index(name = "idx_status", columnList = "status")
        }
)
public class DomainEventPO {

    @Id
    private Long id;

    @Column(nullable = false, length = 5000)
    @Comment("事件信息")
    private String event;

    @Column(nullable = false, updatable = false)
    private Long aggregatorId = 0L;

    @Column(nullable = false)
    @Comment("事件类型")
    private String eventType;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Long createTime;

    @Column(nullable = false)
    @LastModifiedDate
    private Long updateTime;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    @Comment("状态")
    private DomainEventStatusEnum status = DomainEventStatusEnum.NEW;

    public void succeed() {
        if (this.status == DomainEventStatusEnum.SUCCESS) {
            throw new IllegalStateException("domain event status is SUCCESS,can't succeed.");
        }
        this.status = DomainEventStatusEnum.SUCCESS;
    }
}
