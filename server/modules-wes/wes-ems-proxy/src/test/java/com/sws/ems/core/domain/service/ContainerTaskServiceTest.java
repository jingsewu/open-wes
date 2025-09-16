package com.sws.ems.core.domain.service;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.id.Snowflake;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.common.utils.utils.ObjectUtils;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.ems.proxy.domain.service.ContainerTaskService;
import org.openwes.wes.ems.proxy.domain.service.impl.ContainerTaskServiceImpl;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ContainerTaskServiceTest {

    @InjectMocks
    private ContainerTaskServiceImpl containerTaskService;

    @Mock
    private Snowflake snowflake;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        new SnowflakeUtils(snowflake);
        // 当调用 snowflake.nextId() 时返回一个固定值
        when(snowflake.nextId()).thenReturn(1L);
    }

    @Test
    void testGroupContainerTasks() {

        RedisUtils mockRedisUtils = Mockito.mock(RedisUtils.class);
        when(mockRedisUtils.getAndIncrement(anyString(), anyInt())).thenReturn(10L);

        new OrderNoGenerator(mockRedisUtils);
        new IdGenerator(new Snowflake(1L, 1L));

        CreateContainerTaskDTO createContainerTaskDTO1 = ObjectUtils.getRandomObjectIgnoreFields(CreateContainerTaskDTO.class, "destinations");
        CreateContainerTaskDTO createContainerTaskDTO2 = ObjectUtils.getRandomObjectIgnoreFields(CreateContainerTaskDTO.class, "destinations");

        createContainerTaskDTO1.setContainerCode("CTN-001");
        createContainerTaskDTO1.setContainerFace("FRONT");
        createContainerTaskDTO1.setTaskPriority(10);
        createContainerTaskDTO1.setDestinations(Lists.newArrayList("652070221623988224"));

        createContainerTaskDTO2.setContainerCode("CTN-001");
        createContainerTaskDTO2.setContainerFace("FRONT");
        createContainerTaskDTO2.setTaskPriority(9);
        createContainerTaskDTO2.setDestinations(Lists.newArrayList("652070221623988224"));

        List<ContainerTask> containerTasks = containerTaskService.groupContainerTasks(Lists.newArrayList(createContainerTaskDTO1, createContainerTaskDTO2));
        Assertions.assertEquals(1, containerTasks.size());
        Assertions.assertEquals(10, containerTasks.iterator().next().getTaskPriority());

        List<ContainerTask> flatContainerTasks = containerTaskService.flatContainerTasks(containerTasks);
        Assertions.assertEquals(1, flatContainerTasks.size());
        Assertions.assertEquals("CTN-001", flatContainerTasks.get(0).getContainerCode());

        createContainerTaskDTO2.setContainerCode("BBB");

        containerTasks = containerTaskService.groupContainerTasks(Lists.newArrayList(createContainerTaskDTO1, createContainerTaskDTO2));
        Assertions.assertEquals(2, containerTasks.size());

        createContainerTaskDTO2.setContainerCode("AAA");
        createContainerTaskDTO2.setDestinations(Lists.newArrayList("1"));

        containerTasks = containerTaskService.groupContainerTasks(Lists.newArrayList(createContainerTaskDTO1, createContainerTaskDTO2));
        Assertions.assertEquals(2, containerTasks.size());
    }

}
