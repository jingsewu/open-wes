package org.openwes.user.application.impl;

import org.openwes.user.api.dto.constants.YesOrNoEnum;
import org.openwes.user.application.LoginLogService;
import org.openwes.user.domain.entity.LoginLog;
import org.openwes.user.domain.repository.LoginLogMapper;
import org.openwes.user.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    private String getAddressByIp(String ip) {
        return ip;
    }

    @Override
    public void addSuccess(String account, String ip, Long loginTimestamp) {
        LoginLog loginLog = new LoginLog();
        loginLog.setAccount(account);
        loginLog.setLoginIp(ip);
        loginLog.setLoginResult(Integer.valueOf(YesOrNoEnum.YES.getValue()));
        loginLog.setLoginAddress(getAddressByIp(ip));
        setTime(loginLog, loginTimestamp);
        loginLogMapper.save(loginLog);
    }

    @Override
    public void addFailure(String account, String ip, Long loginTimestamp, String failureMsg) {
        LoginLog loginLog = new LoginLog();
        loginLog.setAccount(account);
        loginLog.setLoginIp(ip);
        loginLog.setLoginResult(Integer.valueOf(YesOrNoEnum.NO.getValue()));
        loginLog.setLoginFailureMsg(failureMsg);
        loginLog.setLoginAddress(getAddressByIp(ip));
        setTime(loginLog, loginTimestamp);
        loginLogMapper.save(loginLog);
    }


    private void setTime(LoginLog loginLog, Long loginTimestamp) {
        loginLog.setGmtLoginTimestamp(loginTimestamp);
        loginLog.setGmtLoginTime(TimeUtil.formatTime(loginTimestamp));
    }
}
