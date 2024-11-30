package org.openwes.station.api.dto;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.wes.api.basic.constants.WorkStationProcessingStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;

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
@Slf4j
public class WorkStationCacheDTO {

    @Id
    protected Long id;

    protected String warehouseCode;
    protected Long warehouseAreaId;
    protected String stationCode;
    protected OperationTaskTypeEnum operationType;

    protected List<OperationTaskVO> operateTasks;


    protected List<ArrivedContainerCacheDTO> arrivedContainers;

    //just use it as the cache of putWallSlots. its status is not right.
    protected List<PutWallSlotDTO> putWallSlots;

    protected WorkStationConfigDTO workStationConfig;
    protected WorkStationVO.ChooseAreaEnum chooseArea;
    protected List<WorkStationVO.Tip> tips;

    /**
     * 事件代码
     */
    protected ApiCodeEnum eventCode;

    /**
     * 工作站任务状态 /NOT_TASK/CALL_ROBOT/
     */
    private WorkStationProcessingStatusEnum stationProcessingStatus;

}
