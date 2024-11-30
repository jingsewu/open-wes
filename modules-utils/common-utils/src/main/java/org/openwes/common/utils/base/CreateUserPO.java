package org.openwes.common.utils.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

@Data
@MappedSuperclass
public class CreateUserPO {
    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'Creation time'", updatable = false)
    @CreatedDate
    private Long createTime;

    @CreatedBy
    @Column(nullable = false, columnDefinition = "varchar(60) default '' comment 'Create user'", updatable = false)
    private String createUser;
}
