package org.openwes.user.config.security;

import org.openwes.user.application.LoginLogService;
import org.openwes.user.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEvents {

    private final LoginLogService loginLogService;

    @EventListener
    @Async
    public void onSuccess(AuthenticationSuccessEvent success) {
        Authentication authentication = success.getAuthentication();
        String ip = HttpUtils.getRemoteAddress();
        loginLogService.addSuccess(authentication.getName(), ip, success.getTimestamp());
    }

    @EventListener
    @Async
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        Authentication authentication = failures.getAuthentication();
        String ip = HttpUtils.getRemoteAddress();
        loginLogService.addFailure(authentication.getName(), ip, failures.getTimestamp(), failures.getException().getLocalizedMessage());
    }

}
