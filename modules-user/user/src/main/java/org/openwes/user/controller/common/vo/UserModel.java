package org.openwes.user.controller.common.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserModel {
    private long id;
    private String username;

    private String icon;
}
