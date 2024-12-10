package org.openwes.user.config.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "system")
public class SystemProp {

    /**
     * 超级角色的编码
     */
    @Value("${superRoleCode:superAdmin}")
    private String superRoleCode;

    /**
     * 超级管理员id
     */
    @Value("${superAdminId: 1}")
    private Long superAdminId;
}
