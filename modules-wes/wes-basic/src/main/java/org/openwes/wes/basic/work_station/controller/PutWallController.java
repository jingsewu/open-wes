package org.openwes.wes.basic.work_station.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.dto.CreatePutWallDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("basic/putWall")
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class PutWallController {

    private final IPutWallApi iPutWallApi;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid CreatePutWallDTO createPutWallDTO) {
        if (createPutWallDTO.getId() != null && createPutWallDTO.getId() > 0) {
            iPutWallApi.update(createPutWallDTO);
            return Response.success();
        }
        iPutWallApi.create(createPutWallDTO);
        return Response.success();
    }

}
