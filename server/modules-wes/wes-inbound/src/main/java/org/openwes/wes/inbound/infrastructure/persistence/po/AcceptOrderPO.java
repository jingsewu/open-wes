package org.openwes.wes.inbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.AuditUserPO;
import org.openwes.wes.api.inbound.constants.AcceptMethodEnum;
import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.AcceptTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_accept_order",
        indexes = {
                @Index(unique = true, name = "uk_order_no", columnList = "orderNo"),
                @Index(name = "idx_identify_no", columnList = "identifyNo")
        }
)
@DynamicUpdate
public class AcceptOrderPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单编号'")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '验收单创建维度标识'")
    private String identifyNo;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '收货方式'")
    private AcceptMethodEnum acceptMethod = AcceptMethodEnum.LOOSE_INVENTORY;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '收货类型'")
    private AcceptTypeEnum acceptType = AcceptTypeEnum.RECEIVE;

    private boolean putAway;

    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '验收总数量'")
    private Integer totalQty = 0;
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '验收总箱数'")
    private Integer totalBox = 0;

    @Column(nullable = false, columnDefinition = "varchar(500) comment '备注'")
    private String remark = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '验收单状态'")
    private AcceptOrderStatusEnum acceptOrderStatus = AcceptOrderStatusEnum.NEW;

    @Version
    private Long version;
}
