package org.openwes.user.controller.param.auth;

import lombok.Data;

@Data
public class AuthTokenDTO {

    private String grantType;
    private String username;
    private String password;
}
