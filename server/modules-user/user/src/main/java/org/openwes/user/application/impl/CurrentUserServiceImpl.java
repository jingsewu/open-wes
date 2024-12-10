package org.openwes.user.application.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.user.application.CurrentUserService;
import org.openwes.user.controller.param.user.UserInfoUpdatedParam;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.transfer.UserTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserTransfer userTransfer;

    @Override
    public void updateCurrentUserPassword(String currentUsername, String oldPassword, String newPassword) {
        if (newPassword.length() < 6) {
            throw new WmsException(UserErrorDescEnum.ERR_CRED_TOO_SHORT);
        }
        if (StringUtils.isEmpty(currentUsername)) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        User user = userMapper.findByAccount(currentUsername);
        if (user == null) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new WmsException(UserErrorDescEnum.ERROR_WRONG_OLD_CRED);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.save(user);
    }

    @Override
    public void updateInfo(String currentUsername, UserInfoUpdatedParam param) {
        if (StringUtils.isEmpty(currentUsername)) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        User user = userMapper.findByAccount(currentUsername);
        userTransfer.updateDO(user, param);
        userMapper.save(user);
    }

}
