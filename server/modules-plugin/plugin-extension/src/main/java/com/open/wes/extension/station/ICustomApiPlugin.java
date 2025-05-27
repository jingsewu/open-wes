package com.open.wes.extension.station;

import com.open.wes.extension.IPlugin;

public interface ICustomApiPlugin extends IPlugin {

    public String customApiCode();

    void execute(CustomApiParameter customApiParameter);
}
