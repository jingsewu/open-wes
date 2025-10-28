package org.openwes.wes.printer.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.CreateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.wes.api.print.constants.ModuleEnum;
import org.openwes.wes.api.print.constants.PrintNodeEnum;
import org.openwes.wes.api.print.constants.PrintStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "p_print_record",
        indexes = {
                @Index(name = "idx_create_time", columnList = "createTime")
        })
public class PrintRecord extends CreateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(64) comment '模块'")
    private ModuleEnum module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(64) comment '打印节点'")
    private PrintNodeEnum printNode;
    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '模板编码'")
    private String templateCode = "";

    @Column(nullable = false, columnDefinition = "varchar(128) default '' comment '模板名称'")
    private String templateName = "";

    private Long workStationId;

    private Long printTime;

    private String printer;

    @Column(length = 50000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private PrintStatusEnum printStatus;

    @Column(columnDefinition = "varchar(500) comment '错误信息'")
    private String errorMessage;

    public static PrintRecord create(ModuleEnum module, PrintNodeEnum printNode,
                                     String templateCode, String templateName, Long workStationId,
                                     String message, String printer) {
        PrintRecord printRecord = new PrintRecord();
        printRecord.setModule(module);
        printRecord.setPrintNode(printNode);
        printRecord.setTemplateCode(templateCode);
        printRecord.setTemplateName(templateName);
        printRecord.setWorkStationId(workStationId);
        printRecord.setMessage(message);
        printRecord.setPrinter(printer);
        return printRecord;
    }

    public void updateStatus(PrintStatusEnum status, String errorMessage) {
        this.printStatus = status;
        this.errorMessage = StringUtils.isNotEmpty(errorMessage) ? errorMessage.substring(0, 255) : errorMessage;
    }
}
