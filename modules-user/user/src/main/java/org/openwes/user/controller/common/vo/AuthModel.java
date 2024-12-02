package org.openwes.user.controller.common.vo;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class AuthModel {
    private String token;
    private long expiresIn;
    private String refreshToken;
    private UserModel user;
}
