package org.openwes.user.domain.model;

import org.openwes.user.domain.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserHasRole extends User {

    @ApiModelProperty("角色")
    private String roleNames;

    private String roleIds;

}
