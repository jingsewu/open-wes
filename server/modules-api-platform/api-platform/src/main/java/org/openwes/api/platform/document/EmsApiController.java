package org.openwes.api.platform.document;


import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("api/ems")
@RestController
@Tag(name = "Upstream Api")
public class EmsApiController {

    @PostMapping("containerTaskStatusUpdate")
    @Operation(summary = "更新容器任务状态")
    public Response containerTaskStatusUpdate(@RequestBody List<UpdateContainerTaskDTO> tasks) {
        return Response.success();
    }
}
