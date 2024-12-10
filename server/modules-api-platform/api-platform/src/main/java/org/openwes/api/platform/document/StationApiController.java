package org.openwes.api.platform.document;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("api/station")
@RestController
@Tag(name = "Upstream Api")
public class StationApiController {

    @PostMapping("containerArrive")
    @Operation(summary = "容器抵达工作站时的通知接口")
    public Response containerArrive(@RequestBody List<ContainerArrivedEvent> events) {
        return Response.success();
    }
}
