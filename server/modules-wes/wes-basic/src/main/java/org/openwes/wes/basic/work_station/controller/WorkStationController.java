package org.openwes.wes.basic.work_station.controller;

import org.openwes.wes.api.basic.IWorkStationApi;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.basic.work_station.domain.entity.WorkStationConfig;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationConfigRepository;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationRepository;
import org.openwes.wes.basic.work_station.domain.transfer.WorkStationConfigTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("basic/work/station")
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class WorkStationController {

    private final IWorkStationApi workStationApi;
    private final WorkStationRepository workStationRepository;
    private final WorkStationConfigRepository workStationConfigRepository;
    private final WorkStationConfigTransfer workStationConfigTransfer;

    @PostMapping("createOrUpdateWorkStation")
    public void createOrUpdateWorkStation(@RequestBody @Valid WorkStationDTO workStationDTO) {
        workStationApi.save(workStationDTO);
    }

    @PostMapping("get/{id}")
    public Object get(@PathVariable Long id) {
        return workStationRepository.findById(id);
    }

    @PostMapping("getStationConfig/{workStationId}")
    public Object getStationConfig(@PathVariable Long workStationId) {
        WorkStationConfig workStationConfig = workStationConfigRepository.findByWorkStationId(workStationId);
        if (workStationConfig == null) {
            workStationConfig = new WorkStationConfig();
            workStationConfig.initialize();
        }
        return workStationConfigTransfer.toDTO(workStationConfig);
    }

    @PostMapping("createOrUpdateStationConfig")
    public void createOrUpdateStationConfig(@RequestBody WorkStationConfigDTO workStationConfigDTO) {
        WorkStationConfig workStationConfig = workStationConfigTransfer.toDO(workStationConfigDTO);
        workStationConfigRepository.save(workStationConfig);
    }
}
