package org.openwes.user.application.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.repository.UserRoleMapper;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleApiImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleMapper roleMapper;

    private UserRoleApiImpl api;

    @BeforeEach
    void setUp() {
        api = new UserRoleApiImpl(userMapper, userRoleMapper, roleMapper);
    }

    @Test
    void blankUsername_returnsEmptySetWithoutQuerying() {
        assertTrue(api.findRoleCodes("   ").isEmpty());
        assertTrue(api.findRoleCodes(null).isEmpty());
        verifyNoInteractions(userMapper, userRoleMapper, roleMapper);
    }

    @Test
    void unknownUser_returnsEmptySet() {
        when(userMapper.findByAccount("ghost")).thenReturn(null);

        assertTrue(api.findRoleCodes("ghost").isEmpty());
        verifyNoInteractions(userRoleMapper, roleMapper);
    }

    @Test
    void userWithoutRoles_returnsEmptySet() {
        User user = new User();
        user.setId(7L);
        when(userMapper.findByAccount("alice")).thenReturn(user);
        when(userRoleMapper.findByUserId(7L)).thenReturn(List.of());

        assertTrue(api.findRoleCodes("alice").isEmpty());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void mapsUserRoleIdsToSortedRoleCodes() {
        User user = new User();
        user.setId(7L);
        when(userMapper.findByAccount("alice")).thenReturn(user);
        when(userRoleMapper.findByUserId(7L)).thenReturn(List.of(
                new UserRole().setUserId(7L).setRoleId(1L),
                new UserRole().setUserId(7L).setRoleId(2L),
                new UserRole().setUserId(7L).setRoleId(1L)));
        when(roleMapper.findAllById(anyList())).thenReturn(List.of(
                new Role().setId(1L).setCode("pluginDeveloper"),
                new Role().setId(2L).setCode("pluginAdmin")));

        assertEquals(Set.of("pluginAdmin", "pluginDeveloper"), api.findRoleCodes("alice"));
    }
}
