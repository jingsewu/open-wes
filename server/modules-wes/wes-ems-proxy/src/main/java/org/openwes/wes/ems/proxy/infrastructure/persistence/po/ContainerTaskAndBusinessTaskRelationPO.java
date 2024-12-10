package org.openwes.wes.ems.proxy.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskAndBusinessTaskRelationStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "e_container_task_and_business_task_relation",
        indexes = {
                @Index(name = "idx_task_id", columnList = "taskId"),
                @Index(name = "idx_task_code", columnList = "taskCode"),
                @Index(name = "idx_customer_task_id", columnList = "customerTaskId")
        })
public class ContainerTaskAndBusinessTaskRelationPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(columnDefinition = "bigint(11) comment '搬箱任务ID'")
    private Long taskId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '搬箱任务编号'")
    private String taskCode;

    @Column(columnDefinition = "bigint(11) comment '客户任务ID'")
    private Long customerTaskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '任务 status'")
    private ContainerTaskAndBusinessTaskRelationStatusEnum containerTaskAndBusinessTaskRelationStatus;
}
