package org.openwes.station.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

/**
 * definition：a place that operators working, only support one station one Operation Type at a time.
 * <p>
 * a base work station Entity , only contains the basic information of work station. if you need to add more information,
 * please add it to the subclasses. such as InboundWorkStationCache and OutboundWorkStationCache.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("WorkStation")
@Slf4j
public class WorkStationCachePO {

    @Id
    protected Long id;

    protected String warehouseCode;
    protected Long warehouseAreaId;
    protected String stationCode;
    protected WorkStationModeEnum workStationMode;

    protected List<OperationTaskVO> operateTasks;

    protected List<ArrivedContainerCache> arrivedContainers;

    //just use it as the cache of putWallSlots. its status is not right.
    protected List<PutWallSlotDTO> putWallSlots;

    protected WorkStationConfigDTO workStationConfig;
    protected WorkStationVO.ChooseAreaEnum chooseArea;
    protected List<WorkStationVO.Tip> tips;

    protected ApiCodeEnum eventCode;

    protected String scannedBarcode;
}
