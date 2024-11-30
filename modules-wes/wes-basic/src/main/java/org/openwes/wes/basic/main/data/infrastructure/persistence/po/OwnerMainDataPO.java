package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.main.data.constants.OwnerTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "m_owner_main_data",
        indexes = {
                @Index(unique = true, name = "uk_owner_warehouse", columnList = "ownerCode,warehouseCode")
        }
)
public class OwnerMainDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;
    @Column(nullable = false, columnDefinition = "varchar(128) comment '货主名称'")
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '货主类型'")
    private OwnerTypeEnum ownerType;

    @Column(columnDefinition = "varchar(64) comment '联系人'")
    private String name;
    @Column(columnDefinition = "varchar(64) comment '联系人电话'")
    private String tel;
    @Column(columnDefinition = "varchar(64) comment '联系邮箱'")
    private String mail;
    @Column(columnDefinition = "varchar(64) comment '传真'")
    private String fax;

    @Column(columnDefinition = "varchar(64) comment '国家'")
    private String country;
    @Column(columnDefinition = "varchar(64) comment '省'")
    private String province;
    @Column(columnDefinition = "varchar(64) comment '市'")
    private String city;
    @Column(columnDefinition = "varchar(64) comment '区'")
    private String district;
    @Column(columnDefinition = "varchar(255) comment '详细地址'")
    private String address;

    @Version
    private Long version;
}
