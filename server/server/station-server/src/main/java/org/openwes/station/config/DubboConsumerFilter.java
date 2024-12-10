package org.openwes.station.config;

import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Slf4j
@Activate(group = CommonConstants.CONSUMER)
public class DubboConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        RpcContext rpcContext = RpcContext.getClientAttachment();
        try {
            String currentUser = UserContext.getCurrentUser();
            rpcContext.setAttachment(AuthConstants.USERNAME, currentUser);
            return invoker.invoke(invocation);
        } finally {
            rpcContext.removeAttachment(AuthConstants.USERNAME);
        }
    }
}
