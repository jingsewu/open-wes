package org.openwes.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.user.application.impl.CurrentUserServiceImpl;
import org.openwes.user.controller.param.user.UserInfoUpdatedParam;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.transfer.UserTransfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class CurrentUserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserTransfer userTransfer;

    @InjectMocks
    private CurrentUserServiceImpl currentUserService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setAccount("testUser");
        user.setPassword("encodedPassword");
    }

    @Test
    public void updateCurrentUserPassword_NewPasswordTooShort_ShouldThrowException() {
        WmsException exception = assertThrows(WmsException.class, () -> {
            currentUserService.updateCurrentUserPassword("testUser", "oldPassword", "short");
        });
        assertEquals(UserErrorDescEnum.ERR_CRED_TOO_SHORT.getCode(), exception.getCode());
    }

    @Test
    public void updateCurrentUserPassword_EmptyUsername_ShouldThrowException() {
        WmsException exception = assertThrows(WmsException.class, () -> {
            currentUserService.updateCurrentUserPassword("", "oldPassword", "newPassword");
        });
        assertEquals(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode(), exception.getCode());
    }

    @Test
    public void updateCurrentUserPassword_UserNotFound_ShouldThrowException() {
        when(userMapper.findByAccount("testUser")).thenReturn(null);

        WmsException exception = assertThrows(WmsException.class, () -> {
            currentUserService.updateCurrentUserPassword("testUser", "oldPassword", "newPassword");
        });
        assertEquals(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode(), exception.getCode());
    }

    @Test
    public void updateCurrentUserPassword_WrongOldPassword_ShouldThrowException() {
        when(userMapper.findByAccount("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(false);

        WmsException exception = assertThrows(WmsException.class, () -> {
            currentUserService.updateCurrentUserPassword("testUser", "oldPassword", "newPassword");
        });
        assertEquals(UserErrorDescEnum.ERROR_WRONG_OLD_CRED.getCode(), exception.getCode());
    }

    @Test
    public void updateCurrentUserPassword_ValidInput_ShouldUpdatePassword() {
        when(userMapper.findByAccount("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        currentUserService.updateCurrentUserPassword("testUser", "oldPassword", "newPassword");

        verify(userMapper).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
    }

    @Test
    public void updateInfo_EmptyUsername_ShouldThrowException() {
        WmsException exception = assertThrows(WmsException.class, () -> {
            currentUserService.updateInfo("", new UserInfoUpdatedParam());
        });
        assertEquals(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode(), exception.getCode());
    }

    @Test
    public void updateInfo_ValidInput_ShouldUpdateInfo() {
        when(userMapper.findByAccount("testUser")).thenReturn(user);
        UserInfoUpdatedParam param = new UserInfoUpdatedParam();
        param.setName("newNickname");

        doNothing().when(userTransfer).updateDO(any(User.class), any(UserInfoUpdatedParam.class));

        currentUserService.updateInfo("testUser", param);

        verify(userMapper).save(user);
        verify(userTransfer).updateDO(user, param);
    }
}
