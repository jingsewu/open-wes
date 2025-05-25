package org.openwes.wes.printer.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "p_print_template",
        indexes = {
                @Index(unique = true, name = "uk_template_code", columnList = "templateCode")
        })
public class PrintTemplate extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '模板编码'")
    private String templateCode;

    @Column(nullable = false, columnDefinition = "varchar(128) default '' comment '模板名称'")
    private String templateName;

    @Column(length = 50000)
    private String templateContent ;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '启用状态'")
    private boolean enabled;

}
