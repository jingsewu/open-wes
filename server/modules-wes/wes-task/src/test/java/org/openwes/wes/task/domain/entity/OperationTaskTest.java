package org.openwes.wes.task.domain.entity;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class OperationTaskTest {

    private OperationTask operationTask;
    private MockedStatic<DomainEventPublisher> publisherMock;
    private MockedStatic<SnowflakeUtils> snowflakeUtilsMock;

    @BeforeEach
    public void setUp() {
        operationTask = new OperationTask();
        operationTask.setId(1L);
        operationTask.setWarehouseCode("WH001");
        operationTask.setTaskType(OperationTaskTypeEnum.PICKING);
        operationTask.setWorkStationId(1L);
        operationTask.setPriority(1);
        operationTask.setSkuId(1L);
        operationTask.setRequiredQty(10);
        operationTask.setOperatedQty(0);
        operationTask.setAbnormalQty(0);
        operationTask.setTaskStatus(OperationTaskStatusEnum.NEW);

        publisherMock = mockStatic(DomainEventPublisher.class);
        publisherMock.when(() -> DomainEventPublisher.sendAsyncDomainEvent(any())).thenAnswer(invocation -> null);

        snowflakeUtilsMock = mockStatic(SnowflakeUtils.class);
        snowflakeUtilsMock.when(SnowflakeUtils::generateId).thenReturn(1L);
    }

    @AfterEach
    public void tearDown() {
        // Close the static mock after each test to avoid interference between tests
        publisherMock.close();
        snowflakeUtilsMock.close();
    }

    @Test
    void testValidateQty_ValidQty_ShouldPass() {
        operationTask.validateQty(); // Should not throw any exception
    }

    @Test
    void testValidateQty_InvalidQtyRelation_ShouldThrowException() {
        operationTask.setOperatedQty(5);
        operationTask.setAbnormalQty(6);
        assertThrows(IllegalStateException.class, () -> operationTask.validateQty());
    }

    @Test
    void testOperate_TaskProcessed_ShouldThrowException() {
        operationTask.setTaskStatus(OperationTaskStatusEnum.PROCESSED);
        assertThrows(WmsException.class, () -> operationTask.operate(5, HandleTaskDTO.HandleTaskTypeEnum.COMPLETE, "TC001", 1L));
    }

    @Test
    void testOperate_CompleteTask_ShouldUpdateStatus() {
        operationTask.operate(10, HandleTaskDTO.HandleTaskTypeEnum.COMPLETE, "TC001", 1L);
        assertEquals(OperationTaskStatusEnum.PROCESSED, operationTask.getTaskStatus());
    }

    @Test
    void testOperate_SplitTask_ShouldUpdateRequiredQty() {
        operationTask.operate(5, HandleTaskDTO.HandleTaskTypeEnum.SPLIT, "TC001", 1L);
        assertEquals(5, operationTask.getRequiredQty());
    }

    @Test
    void testReportAbnormal_ShouldThrowExceptionWhenRepeating() {
        operationTask.setAbnormalQty(1);
        assertThrows(WmsException.class, () -> operationTask.reportAbnormal(1));
    }

    @Test
    void testReportAbnormal_ShouldUpdateAbnormalQty() {
        operationTask.reportAbnormal(2);
        assertEquals(2, operationTask.getAbnormalQty());
    }

    @Test
    void testSetActualWorkStation_ShouldUpdateWorkStationId() {
        operationTask.setActualWorkStation(2L);
        assertEquals(2L, operationTask.getWorkStationId());
    }
}
