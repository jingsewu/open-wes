package org.openwes.user.controller.param.role;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Schema(title = "添加角色权限参数")
public class RoleMenuUpdateParam {

    @Schema(name = "menus", title = "选中菜单id")
    private Set<Long> menuSet;

    @Schema(name = "roleId", title = "角色id")
    private Long roleId;

    private String menus;

    public Set<Long> getMenuSet() {
        if (StringUtils.isEmpty(menus)) {
            return Collections.emptySet();
        }
        return Arrays.stream(menus.split(",")).map(Long::parseLong).collect(Collectors.toSet());
    }
}
