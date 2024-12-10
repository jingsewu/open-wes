package org.openwes.wes.printer.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "p_print_config",
        indexes = {
                @Index(unique = true, name = "idx_print_config_code", columnList = "configCode,deleteTime")
        })
@Where(clause = "deleted = false")
public class PrintConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(255) default '' comment '配置编码'")
    private String configCode;

    @Column(nullable = false, columnDefinition = "bigint comment '工作站ID'")
    private Long workStationId;

    @Column(columnDefinition = "json comment '打印配置详情'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<PrintConfigDetail> printConfigDetails;

    private boolean enabled;
    private boolean deleted;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '删除时间'")
    private Long deleteTime = 0L;

    @Data
    public static class PrintConfigDetail {

        @NotNull
        private String ruleCode;

        @NotNull
        private String printer;
    }

}
