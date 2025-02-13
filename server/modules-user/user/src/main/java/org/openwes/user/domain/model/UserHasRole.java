package org.openwes.user.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.user.domain.entity.User;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserHasRole extends User {

    @Schema(title = "角色")
    private String roleNames;

    private String roleIds;

}
