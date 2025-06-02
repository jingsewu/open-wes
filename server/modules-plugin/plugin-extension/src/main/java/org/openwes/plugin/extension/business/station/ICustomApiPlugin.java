package org.openwes.plugin.extension.business.station;

import org.openwes.plugin.extension.IPlugin;

public interface ICustomApiPlugin extends IPlugin {

    String customApiCode();

    void execute(CustomApiParameter customApiParameter);
}
