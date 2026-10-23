package org.openwes.script.plugin.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.constants.AppCodeEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;

@Getter
@AllArgsConstructor
public enum ScriptPluginWorkbenchErrorDescEnum implements IBaseError {

    DEVELOPER_ROLE_REQUIRED("PL002001",
            "the plugin developer role is required for this operation",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    ADMIN_ROLE_REQUIRED("PL002002",
            "the plugin administrator role is required for this operation",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AUTHENTICATED_USER_REQUIRED("PL002003",
            "an authenticated user is required for this operation",
            AppCodeEnum.PLUGIN_PLATFORM.name());

    private final String code;
    private final String desc;
    private final String appCode;
}
