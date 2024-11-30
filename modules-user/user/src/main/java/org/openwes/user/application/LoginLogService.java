package org.openwes.user.application;

public interface LoginLogService {

    void addSuccess(String username, String ip, Long loginTimestamp);

    void addFailure(String username, String ip, Long loginTimestamp, String failureMsg);

}
