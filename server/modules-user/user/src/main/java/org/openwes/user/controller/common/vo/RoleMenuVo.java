package org.openwes.user.controller.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openwes.user.domain.entity.Menu;

import java.util.List;
import java.util.Set;

@Schema(title = "角色权限模型")
@Data
public class RoleMenuVo {

    @Schema(title = "当前角色已经分配的菜单id")
    private Set<Long> menuIds;

    @Schema(title = "当前登录的用户所拥有的菜单树")
    private List<Menu> menuTree;

}
