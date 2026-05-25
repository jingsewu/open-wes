package org.openwes.station.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SkuAreaTest {

    private SkuArea skuArea;

    @BeforeEach
    void setUp() {
        skuArea = new SkuArea();
    }

    private OperationTaskDTO createTask(Long id, String skuCode, String containerCode, String face, int requiredQty, int operatedQty, int abnormalQty) {
        OperationTaskDTO task = new OperationTaskDTO();
        task.setId(id);
        task.setSourceContainerCode(containerCode);
        task.setSourceContainerFace(face);
        task.setRequiredQty(requiredQty);
        task.setOperatedQty(operatedQty);
        task.setAbnormalQty(abnormalQty);
        task.setTaskStatus(OperationTaskStatusEnum.NEW);
        return task;
    }

    private SkuArea.SkuTaskInfo createSkuTaskInfo(String skuCode, OperationTaskDTO... tasks) {
        SkuMainDataDTO sku = new SkuMainDataDTO();
        sku.setSkuCode(skuCode);
        SkuArea.SkuTaskInfo info = new SkuArea.SkuTaskInfo();
        info.setSkuMainDataDTO(sku);
        info.setOperationTaskDTOs(new ArrayList<>(Arrays.asList(tasks)));
        return info;
    }

    @Test
    void markTasksProcessing_resetsAllToNewThenSetsMatching() {
        OperationTaskDTO task1 = createTask(1L, "SKU-1", "C-1", "A", 10, 0, 0);
        OperationTaskDTO task2 = createTask(2L, "SKU-1", "C-2", "A", 5, 0, 0);
        task1.setTaskStatus(OperationTaskStatusEnum.PROCESSING);

        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task1, task2))));
        skuArea.markTasksProcessing("SKU-1", "C-1", "A");

        assertEquals(OperationTaskStatusEnum.PROCESSING, task1.getTaskStatus());
        assertEquals(OperationTaskStatusEnum.NEW, task2.getTaskStatus());
    }

    @Test
    void markTasksProcessing_filtersByContainerAndFace() {
        OperationTaskDTO task1 = createTask(1L, "SKU-1", "C-1", "A", 10, 0, 0);
        OperationTaskDTO task2 = createTask(2L, "SKU-1", "C-1", "B", 5, 0, 0);

        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task1, task2))));
        skuArea.markTasksProcessing("SKU-1", "C-1", "A");

        assertEquals(OperationTaskStatusEnum.PROCESSING, task1.getTaskStatus());
        assertEquals(OperationTaskStatusEnum.NEW, task2.getTaskStatus());
    }

    @Test
    void removeCompletedTasks_removesFullyOperatedTasks() {
        OperationTaskDTO task1 = createTask(1L, "SKU-1", "C-1", "A", 10, 10, 0);
        OperationTaskDTO task2 = createTask(2L, "SKU-1", "C-1", "A", 5, 2, 0);

        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task1, task2))));
        skuArea.removeCompletedTasks();

        assertEquals(1, skuArea.getOperationViews().size());
        assertEquals(1, skuArea.getOperationViews().get(0).getOperationTaskDTOs().size());
        assertEquals(2L, skuArea.getOperationViews().get(0).getOperationTaskDTOs().get(0).getId());
    }

    @Test
    void removeCompletedTasks_removesEmptyViews() {
        OperationTaskDTO task1 = createTask(1L, "SKU-1", "C-1", "A", 10, 10, 0);

        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task1))));
        skuArea.removeCompletedTasks();

        assertTrue(skuArea.getOperationViews().isEmpty());
    }

    @Test
    void reportAbnormal_updatesAbnormalQtyAndRemovesZeroPick() {
        OperationTaskDTO task1 = createTask(1L, "SKU-1", "C-1", "A", 10, 0, 0);
        task1.setTaskStatus(OperationTaskStatusEnum.PROCESSING);
        OperationTaskDTO task2 = createTask(2L, "SKU-1", "C-1", "A", 5, 0, 0);
        task2.setTaskStatus(OperationTaskStatusEnum.PROCESSING);

        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task1, task2))));

        Map<Long, Integer> abnormalMap = new HashMap<>();
        abnormalMap.put(1L, 10); // fully abnormal
        abnormalMap.put(2L, 2);  // partially abnormal

        skuArea.reportAbnormal(abnormalMap);

        // task1 (requiredQty == abnormalQty) should be removed
        assertEquals(1, skuArea.getOperationViews().get(0).getOperationTaskDTOs().size());
        assertEquals(2L, skuArea.getOperationViews().get(0).getOperationTaskDTOs().get(0).getId());
        assertEquals(2, skuArea.getOperationViews().get(0).getOperationTaskDTOs().get(0).getAbnormalQty());
    }

    @Test
    void hasProcessingTasks_returnsTrueWhenExists() {
        OperationTaskDTO task = createTask(1L, "SKU-1", "C-1", "A", 10, 0, 0);
        task.setTaskStatus(OperationTaskStatusEnum.PROCESSING);

        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task))));

        assertTrue(skuArea.hasProcessingTasks());
    }

    @Test
    void getFirstTask_returnsNullWhenEmpty() {
        assertNull(skuArea.getFirstTask());
    }

    @Test
    void clear_resetsAll() {
        OperationTaskDTO task = createTask(1L, "SKU-1", "C-1", "A", 10, 0, 0);
        skuArea.setScanCode("test");
        skuArea.setOperationViews(new ArrayList<>(List.of(createSkuTaskInfo("SKU-1", task))));

        skuArea.clear();

        assertNull(skuArea.getScanCode());
        assertTrue(skuArea.getOperationViews().isEmpty());
    }
}
