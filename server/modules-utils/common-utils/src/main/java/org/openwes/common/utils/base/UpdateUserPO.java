package org.openwes.common.utils.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
public class UpdateUserPO extends CreateUserPO {

    @LastModifiedBy
    @Column(nullable = false, columnDefinition = "varchar(60) default '' comment 'Update user'")
    private String updateUser;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'Update time'")
    @LastModifiedDate
    private Long updateTime;
}
