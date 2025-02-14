package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.wes.api.main.data.constants.OwnerTypeEnum;
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
@Comment("Owner Main Data Management Table - Stores detailed information about warehouse owners, including contact details and address information.")
public class OwnerMainDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the owner main data record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse associated with this owner")
    private String warehouseCode;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the owner")
    private String ownerCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the owner")
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'SELF'")
    @Comment("Type of the owner. Possible values are: " +
            "SELF (SELF - 自营), " +
            "THIRD_PARTY (THIRD_PARTY - 第三方)")
    private OwnerTypeEnum ownerType;

    @Column(length = 64)
    @Comment("Contact person name")
    private String name;

    @Column(length = 64)
    @Comment("Contact person telephone number")
    private String tel;

    @Column(length = 64)
    @Comment("Contact person email address")
    private String mail;

    @Column(length = 64)
    @Comment("Contact person fax number")
    private String fax;

    @Column(length = 64)
    @Comment("Country part of the owner address")
    private String country;

    @Column(length = 64)
    @Comment("Province part of the owner address")
    private String province;

    @Column(length = 64)
    @Comment("City part of the owner address")
    private String city;

    @Column(length = 64)
    @Comment("District part of the owner address")
    private String district;

    @Column()
    @Comment("Detailed address of the owner")
    private String address;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
