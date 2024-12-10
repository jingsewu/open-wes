package org.openwes.common.utils.user;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String currentUser = UserContext.getCurrentUser();
        if (StringUtils.isEmpty(currentUser)) {
            RpcContextAttachment attachment = RpcContext.getServerAttachment();
            currentUser = attachment.getAttachment(AuthConstants.USERNAME);
            if (StringUtils.isEmpty(currentUser)) {
                return Optional.of(AuthConstants.USERNAME_SYSTEM);
            }
        }
        return Optional.of(currentUser);
    }
}
