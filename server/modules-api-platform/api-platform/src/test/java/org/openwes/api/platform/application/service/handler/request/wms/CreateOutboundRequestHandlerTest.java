package org.openwes.api.platform.application.service.handler.request.wms;

import org.junit.jupiter.api.Test;
import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOutboundRequestHandlerTest {

    private final CreateOutboundRequestHandler handler = new CreateOutboundRequestHandler();

    private void withPlugin(ICustomRequestPlugin plugin) {
        PluginSdkUtils pluginUtils = mock(PluginSdkUtils.class);
        when(pluginUtils.getExtractObject(ICustomRequestPlugin.class)).thenReturn(List.of(plugin));
        ReflectionTestUtils.setField(handler, "pluginUtils", pluginUtils);
    }

    @Test
    void invoke_runsCustomRequestPluginsOfTheSameApiType() {
        ICustomRequestPlugin plugin = mock(ICustomRequestPlugin.class);
        when(plugin.getApiType()).thenReturn(ApiTypeEnum.ORDER_OUTBOUND_CREATE.name());
        withPlugin(plugin);
        RequestHandleContext context = new RequestHandleContext();

        handler.invoke(context);

        verify(plugin).invoke(context);
    }

    @Test
    void invoke_ignoresPluginsOfOtherApiTypes() {
        ICustomRequestPlugin plugin = mock(ICustomRequestPlugin.class);
        when(plugin.getApiType()).thenReturn("SOMETHING_ELSE");
        withPlugin(plugin);
        RequestHandleContext context = new RequestHandleContext();

        handler.invoke(context);

        verify(plugin, org.mockito.Mockito.never()).invoke(context);
    }

    @Test
    void invoke_pluginRejects_propagatesTheException() {
        ICustomRequestPlugin plugin = mock(ICustomRequestPlugin.class);
        when(plugin.getApiType()).thenReturn(ApiTypeEnum.ORDER_OUTBOUND_CREATE.name());
        org.mockito.Mockito.doThrow(new IllegalStateException("rejected by script plugin"))
                .when(plugin).invoke(org.mockito.ArgumentMatchers.any());
        withPlugin(plugin);

        assertThrows(IllegalStateException.class, () -> handler.invoke(new RequestHandleContext()));
    }
}
