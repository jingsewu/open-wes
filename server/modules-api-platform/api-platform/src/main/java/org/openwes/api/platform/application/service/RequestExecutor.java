package org.openwes.api.platform.application.service;

public interface RequestExecutor {

    Object executeRequest(String apiType, String body);

}
