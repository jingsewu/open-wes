package org.openwes.common.utils.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuditUserDTO extends UpdateUserDTO {

    private String auditUser;

    private Long auditTime;
}
