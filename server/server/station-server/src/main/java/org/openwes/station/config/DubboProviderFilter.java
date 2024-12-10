package org.openwes.station.config;

import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext rpcContext = RpcContext.getServerAttachment();
        String currentUser = rpcContext.getAttachment(AuthConstants.USERNAME);
        if (StringUtils.isNotEmpty(currentUser)) {
            UserContext.setAccount(currentUser);
        }
        return invoker.invoke(invocation);
    }
}
