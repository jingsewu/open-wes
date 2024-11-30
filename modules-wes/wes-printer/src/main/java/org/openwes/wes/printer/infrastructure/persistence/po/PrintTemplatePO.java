package org.openwes.wes.printer.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "p_print_template",
        indexes = {
                @Index(unique = true, name = "idx_template_code", columnList = "templateCode")
        })
public class PrintTemplatePO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '模板编码'")
    private String templateCode = "";

    @Column(nullable = false, columnDefinition = "varchar(128) default '' comment '模板名称'")
    private String templateName = "";

    @Column(columnDefinition = "varchar(255) default '' comment '模板文件名'")
    private String templateFileName = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '启用状态'")
    private boolean enabled;

}
