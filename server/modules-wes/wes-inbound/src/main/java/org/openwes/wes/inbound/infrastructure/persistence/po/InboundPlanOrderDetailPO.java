package org.openwes.wes.inbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;
import java.util.TreeMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_inbound_plan_order_detail",
        indexes = {
                @Index(name = "idx_inbound_plan_order_id", columnList = "inboundPlanOrderId"),
                @Index(name = "idx_box_no", columnList = "boxNo")
        }
)
@DynamicUpdate
@Comment("Inbound Plan Order Detail Management Table - " +
        "This table stores detailed information about inbound plan order items, " +
        "including box details, quantities, SKU information, and additional attributes. " +
        "It is used to manage and track the process of inbound operations.")
public class InboundPlanOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the inbound plan order detail record")
    private Long id;

    @Column(nullable = false)
    @Comment("ID of the inbound plan order (Reference to w_inbound_plan_order table id)")
    private Long inboundPlanOrderId;

    @Column(nullable = false, length = 64)
    @Comment("Box number for the inbound plan order detail")
    private String boxNo = "";

    @Column(nullable = false)
    @Comment("Planned quantity to be restocked")
    private Integer qtyRestocked = 0;

    @Column(nullable = false)
    @Comment("Accepted quantity after inspection")
    private Integer qtyAccepted = 0;

    @Column(nullable = false)
    @Comment("Quantity with abnormalities")
    private Integer qtyAbnormal = 0;

    @Column(length = 128)
    @Comment("Reason for the abnormality")
    private String abnormalReason;

    @Column(length = 128)
    @Comment("Party responsible for the abnormality")
    private String responsibleParty;

    @Column(nullable = false, length = 64)
    @Comment("Code of the owner of the goods")
    private String ownerCode;

    @Column(nullable = false)
    @Comment("ID of the SKU (Reference to m_sku_main_data table id)")
    private Long skuId;

    @Column(nullable = false, length = 64)
    @Comment("Code of the SKU")
    private String skuCode;

    @Column(nullable = false, length = 512)
    @Comment("Name of the SKU")
    private String skuName = "";

    @Column(length = 64)
    @Comment("Style of the SKU")
    private String style;

    @Column(length = 64)
    @Comment("Color of the SKU")
    private String color;

    @Column(length = 64)
    @Comment("Size of the SKU")
    private String size;

    @Column(length = 64)
    @Comment("Brand of the SKU")
    private String brand;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Batch attributes stored as JSON")
    private Map<String, Object> batchAttributes = new TreeMap<>();

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Extended fields stored as JSON")
    private Map<String, Object> extendFields = new TreeMap<>();
}
