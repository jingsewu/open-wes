package com.sws.ems.core.domain.service;

import org.openwes.common.utils.id.IdGenerator;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.id.Snowflake;
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

class ContainerTaskServiceTest {

    @Test
    void testGroupContainerTasks() {

        RedisUtils mockRedisUtils = Mockito.mock(RedisUtils.class);
        Mockito.when(mockRedisUtils.getAndIncrement(anyString(), anyInt())).thenReturn(10L);
        new OrderNoGenerator(mockRedisUtils);
        new IdGenerator(new Snowflake(1L, 1L));

        ContainerTaskService containerTaskService = new ContainerTaskServiceImpl();

        CreateContainerTaskDTO createContainerTaskDTO1 = ObjectUtils.getRandomObjectIgnoreFields(CreateContainerTaskDTO.class, "destinations");
        CreateContainerTaskDTO createContainerTaskDTO2 = ObjectUtils.getRandomObjectIgnoreFields(CreateContainerTaskDTO.class, "destinations");

        createContainerTaskDTO1.setContainerCode("AAA");
        createContainerTaskDTO1.setContainerFace("A");
        createContainerTaskDTO1.setTaskPriority(10);
        createContainerTaskDTO1.setDestinations(Lists.newArrayList("1", "2"));

        createContainerTaskDTO2.setContainerCode("AAA");
        createContainerTaskDTO2.setContainerFace("A");
        createContainerTaskDTO2.setTaskPriority(9);
        createContainerTaskDTO2.setDestinations(Lists.newArrayList("2", "1"));

        List<ContainerTask> containerTasks = containerTaskService.groupContainerTasks(Lists.newArrayList(createContainerTaskDTO1, createContainerTaskDTO2));
        Assertions.assertEquals(1, containerTasks.size());
        Assertions.assertEquals(10, containerTasks.iterator().next().getTaskPriority());

        createContainerTaskDTO2.setContainerCode("BBB");

        containerTasks = containerTaskService.groupContainerTasks(Lists.newArrayList(createContainerTaskDTO1, createContainerTaskDTO2));
        Assertions.assertEquals(2, containerTasks.size());

        createContainerTaskDTO2.setContainerCode("AAA");
        createContainerTaskDTO2.setDestinations(Lists.newArrayList("1"));

        containerTasks = containerTaskService.groupContainerTasks(Lists.newArrayList(createContainerTaskDTO1, createContainerTaskDTO2));
        Assertions.assertEquals(2, containerTasks.size());
    }

}
