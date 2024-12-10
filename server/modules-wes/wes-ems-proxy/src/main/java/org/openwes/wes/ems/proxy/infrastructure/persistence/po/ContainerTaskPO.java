package org.openwes.wes.ems.proxy.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskStatusEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "e_container_task",
        indexes = {
                @Index(unique = true, name = "uk_task_code", columnList = "taskCode")
        })
public class ContainerTaskPO extends UpdateUserPO {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(64) comment '业务任务 type'")
    private BusinessTaskTypeEnum businessTaskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '容器任务 type'")
    private ContainerTaskTypeEnum containerTaskType;

    @Column(nullable = false, columnDefinition = "varchar(64) comment 'taskCode'")
    private String taskCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment 'taskGroupCode'")
    private String taskGroupCode;

    @Column(nullable = false, columnDefinition = "int(11) not null default 0 comment '任务优先级' ")
    private Integer taskPriority = 0;
    @Column(nullable = false, columnDefinition = "int(11) not null default 0 comment '组任务优先级' ")
    private Integer taskGroupPriority;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器号'")
    private String containerCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器面'")
    private String containerFace = "";

    @Column(nullable = false, columnDefinition = "bigint not null default 0 comment '父容器任务ID'")
    private Long parentContainerTaskId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '起点'")
    private String startLocation = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器规格'")
    private String containerSpecCode = "";

    @Column(columnDefinition = "json comment '目的地'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> destinations;

    @Column(columnDefinition = "json comment '客户任务ID'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Long> customerTaskIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '任务 status'")
    private ContainerTaskStatusEnum taskStatus;

}
