package org.openwes.wes.config.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "m_address",
    indexes = {
        @Index(unique = true, name = "uk_c_p_c_d", columnList = "country,province,city,district")
    }
)
public class AddressPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '国家'")
    private String country;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '省份'")
    private String province;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '市'")
    private String city;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '区/县'")
    private String district;
}
