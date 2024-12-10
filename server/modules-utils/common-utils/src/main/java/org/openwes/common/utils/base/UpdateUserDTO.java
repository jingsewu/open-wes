package org.openwes.common.utils.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateUserDTO extends CreateUserDTO {
    private String updateUser;
    private Long updateTime;
}
