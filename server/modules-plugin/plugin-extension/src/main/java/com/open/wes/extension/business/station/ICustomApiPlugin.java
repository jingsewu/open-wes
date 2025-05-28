package com.open.wes.extension.business.station;

import com.open.wes.extension.IPlugin;

public interface ICustomApiPlugin extends IPlugin {

    String customApiCode();

    void execute(CustomApiParameter customApiParameter);
}
