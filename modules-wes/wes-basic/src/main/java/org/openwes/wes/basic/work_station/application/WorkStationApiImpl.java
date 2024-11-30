package org.openwes.wes.basic.work_station.application;

import org.openwes.wes.api.basic.IWorkStationApi;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.basic.work_station.domain.aggregate.WorkStationPutWallAggregate;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.openwes.wes.basic.work_station.domain.entity.WorkStationConfig;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationConfigRepository;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationRepository;
import org.openwes.wes.basic.work_station.domain.service.WorkStationService;
import org.openwes.wes.basic.work_station.domain.transfer.PutWallTransfer;
import org.openwes.wes.basic.work_station.domain.transfer.WorkStationConfigTransfer;
import org.openwes.wes.basic.work_station.domain.transfer.WorkStationTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
@DubboService
@RequiredArgsConstructor
public class WorkStationApiImpl implements IWorkStationApi {

    private final WorkStationService workStationService;
    private final WorkStationRepository workStationRepository;
    private final WorkStationTransfer workStationTransfer;
    private final PutWallRepository putWallRepository;
    private final PutWallTransfer putWallTransfer;
    private final WorkStationConfigRepository workStationConfigRepository;
    private final WorkStationConfigTransfer workStationConfigTransfer;
    private final WorkStationPutWallAggregate workStationPutWallAggregate;

    @Override
    public void save(WorkStationDTO workStationDTO) {
        workStationRepository.save(workStationTransfer.toDO(workStationDTO));
    }

    @Override
    public void enable(Long id) {
        WorkStation workStation = workStationRepository.findById(id);
        workStation.enable();
        workStationRepository.save(workStation);
    }

    @Override
    public void disable(Long id) {
        WorkStation workStation = workStationRepository.findById(id);
        workStation.disable();
        workStationRepository.save(workStation);
    }

    @Override
    public void delete(Long id) {
        WorkStation workStation = workStationRepository.findById(id);
        workStation.delete();
        workStationRepository.save(workStation);
    }

    @Override
    public void online(Long id, WorkStationModeEnum workStationMode) {
        WorkStation workStation = workStationRepository.findById(id);
        workStation.online(workStationMode);

        List<PutWall> putWalls = putWallRepository.findAllByWorkStationId(workStation.getId());
        workStationPutWallAggregate.online(putWalls, workStation);
    }

    @Override
    public void offline(Long id) {
        WorkStation workStation = workStationRepository.findById(id);
        List<PutWall> putWalls = putWallRepository.findAllByWorkStationId(workStation.getId());
        workStationService.validateOffline(workStation, putWalls);

        workStation.offline();
        workStationPutWallAggregate.offline(putWalls, workStation);
    }

    @Override
    public void pause(Long id) {
        WorkStation workStation = workStationRepository.findById(id);
        workStation.pause();
        workStationRepository.save(workStation);
    }

    @Override
    public void resume(Long id) {
        WorkStation workStation = workStationRepository.findById(id);
        workStation.resume();
        workStationRepository.save(workStation);
    }

    @Override
    public WorkStationDTO getById(Long id) {
        WorkStationDTO workStationDTO = workStationTransfer.toDTO(workStationRepository.findById(id));

        WorkStationConfig workStationConfig = workStationConfigRepository.findByWorkStationId(id);
        workStationDTO.setWorkStationConfig(workStationConfigTransfer.toDTO(workStationConfig));

        List<PutWall> putWalls = putWallRepository.findAllByWorkStationId(id);
        workStationDTO.setPutWalls(putWallTransfer.toDTOs(putWalls));

        return workStationDTO;
    }

    @Override
    public List<WorkStationDTO> getByWarehouseCode(String warehouseCode) {
        List<WorkStation> workStations = workStationRepository.findAllByWarehouseCode(warehouseCode);

        List<PutWall> putWalls = putWallRepository.findAllByWorkStationIds(workStations.stream().map(WorkStation::getId).toList())
                .stream().filter(PutWall::isEnable).toList();

        Map<Long, List<PutWall>> putWallMap = putWalls.stream().collect(Collectors.groupingBy(PutWall::getWorkStationId));
        return workStations.stream().map(workStation ->
                workStationTransfer.toDTO(workStation, putWallMap.get(workStation.getId()))).toList();
    }
}
