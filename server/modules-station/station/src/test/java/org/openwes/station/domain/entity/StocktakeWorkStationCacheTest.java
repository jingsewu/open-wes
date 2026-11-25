package org.openwes.station.domain.entity;

import org.junit.jupiter.api.Test;
import org.openwes.station.api.model.SkuArea;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StocktakeWorkStationCacheTest {

    /** The station exposes a stocktake record as an OperationTaskDTO: id=record id, detailId=task detail id. */
    private static OperationTaskDTO recordTask(long recordId, long taskDetailId) {
        OperationTaskDTO task = new OperationTaskDTO();
        task.setId(recordId);
        task.setDetailId(taskDetailId);
        task.setTaskStatus(OperationTaskStatusEnum.PROCESSING);
        task.setRequiredQty(10);
        task.setOperatedQty(0);
        task.setAbnormalQty(0);
        return task;
    }

    private static StocktakeWorkStationCache cacheWith(OperationTaskDTO... tasks) {
        SkuArea skuArea = new SkuArea();
        SkuArea.SkuTaskInfo info = new SkuArea.SkuTaskInfo();
        info.setOperationTaskDTOs(new ArrayList<>(List.of(tasks)));
        skuArea.updateOperationViews(List.of(info));

        StocktakeWorkStationCache cache = new StocktakeWorkStationCache();
        cache.setSkuArea(skuArea);
        return cache;
    }

    /** STOCKTAKE_SUBMIT carries the stocktake record id, which is the operation task id. */
    @Test
    void removesTheSubmittedRecordByItsRecordId() {
        StocktakeWorkStationCache cache = cacheWith(recordTask(1001L, 2002L));

        cache.removeOperationTask(1001L);

        assertFalse(cache.getSkuArea().hasTasks(), "submitted record must leave the sku area");
        assertTrue(cache.getSkuArea().getOperationViews().isEmpty());
    }

    /** Several records can share one stocktake task detail; submitting one must not remove the rest. */
    @Test
    void keepsOtherRecordsThatShareTheSameTaskDetail() {
        StocktakeWorkStationCache cache = cacheWith(
                recordTask(1001L, 2002L),
                recordTask(1003L, 2002L));

        cache.removeOperationTask(1001L);

        assertTrue(cache.getSkuArea().hasTasks());
        assertEquals(1, cache.getSkuArea().getOperationViews().size());
        assertEquals(1003L,
                cache.getSkuArea().getOperationViews().get(0).getOperationTaskDTOs().get(0).getId());
    }
}
