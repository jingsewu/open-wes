package org.openwes.user.controller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.http.Response;
import org.openwes.user.application.MenuService;
import org.openwes.user.controller.common.BaseResource;
import org.openwes.user.controller.param.menu.MenuAddParam;
import org.openwes.user.controller.param.menu.MenuUpdateStatusParam;
import org.openwes.user.controller.param.menu.MenuUpdatedParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(BaseResource.API + "menu")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Module Api")
public class MenuController extends BaseResource {

    private final MenuService menuService;

    @PostMapping("/add")
    @Operation(summary = "添加菜单")
    public Object add(@RequestBody @Valid MenuAddParam param) {
        menuService.addMenu(param);
        return Response.success();
    }

    @PostMapping("/update")
    @Operation(summary = "修改菜单")
    public Object update(@RequestBody @Valid MenuUpdatedParam param) {
        menuService.updateMenu(param);
        return Response.success();
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "修改菜单状态")
    public Object updateStatus(@RequestBody @Valid MenuUpdateStatusParam param) {
        menuService.updateStatus(param.getMenuId(), param.getEnable());
        return Response.success();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public Object delete(@PathVariable @Valid Long id) {
        menuService.removeMenuById(id);
        return Response.success();
    }

    @PostMapping("/addList")
    @Operation(summary = "批量添加菜单")
    public Object addList(@RequestBody @Valid List<MenuAddParam> paramList) {
        int count = 0;
        for (MenuAddParam menuAddParam : paramList) {
            menuService.addMenu(menuAddParam);
            count++;
        }
        return count;
    }
}
