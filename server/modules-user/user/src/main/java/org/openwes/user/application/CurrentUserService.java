package org.openwes.user.application;

import org.openwes.user.controller.param.user.UserInfoUpdatedParam;

public interface CurrentUserService {

    void updateCurrentUserPassword(String currentUsername, String oldPassword, String newPassword);

    void updateInfo(String currentUsername, UserInfoUpdatedParam param);

}
