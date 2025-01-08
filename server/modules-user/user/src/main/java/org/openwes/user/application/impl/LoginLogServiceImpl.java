package org.openwes.user.application.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.user.api.dto.constants.YesOrNoEnum;
import org.openwes.user.application.LoginLogService;
import org.openwes.user.domain.entity.LoginLog;
import org.openwes.user.domain.repository.LoginLogMapper;
import org.openwes.user.utils.HttpUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    private String getAddressByIp(String ip) {
        return HttpUtils.getRemoteAddress();
    }

    @Override
    public void addSuccess(String account, String ip, Long loginTimestamp) {
        LoginLog loginLog = new LoginLog();
        loginLog.setAccount(account);
        loginLog.setLoginResult(Integer.valueOf(YesOrNoEnum.YES.getValue()));
        loginLog.setLoginAddress(getAddressByIp(ip));
        loginLog.setLoginTime(loginTimestamp);
        loginLogMapper.save(loginLog);
    }

    @Override
    public void addFailure(String account, String ip, Long loginTimestamp, String failureMsg) {
        LoginLog loginLog = new LoginLog();
        loginLog.setAccount(account);
        loginLog.setLoginResult(Integer.valueOf(YesOrNoEnum.NO.getValue()));
        loginLog.setLoginFailureMsg(failureMsg);
        loginLog.setLoginAddress(getAddressByIp(ip));
        loginLog.setLoginTime(loginTimestamp);
        loginLogMapper.save(loginLog);
    }

}
