package org.openwes.api.platform.document;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/sku")
@Tag(name = "Upstream Api")
public class SkuApiController {

    @PostMapping("create")
    @Operation(summary = "导入商品信息", description = "会跟传入的数据创建新的商品信息或者更新已经存在的商品信息")
    public Response create(@RequestBody List<SkuMainDataDTO> body) {
        return Response.success();
    }
}
