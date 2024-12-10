package org.openwes.common.utils.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
public class AuditUserPO extends UpdateUserPO {

    @Column(nullable = false, columnDefinition = "varchar(60) default '' comment 'audit user'")
    private String auditUser = "";

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'audit time'")
    private Long auditTime = 0L;
}
