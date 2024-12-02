package org.openwes.wes.basic.work_station.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.dto.CreatePutWallDTO;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.domain.transfer.PutWallTransfer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("basic/putWall")
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class PutWallController {

    private final IPutWallApi putWallApi;
    private final PutWallRepository putWallRepository;
    private final PutWallTransfer putWallTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid CreatePutWallDTO createPutWallDTO) {
        if (createPutWallDTO.getId() != null && createPutWallDTO.getId() > 0) {
            putWallApi.update(createPutWallDTO);
            return Response.success();
        }
        putWallApi.create(createPutWallDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object get(@PathVariable Long id) {
        PutWall putWall = putWallRepository.findById(id);
        return putWallTransfer.toDTO(putWall);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable Long id) {
        putWallApi.delete(id);
    }

}
