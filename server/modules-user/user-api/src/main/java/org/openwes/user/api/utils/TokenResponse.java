package org.openwes.user.api.utils;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TokenResponse {
    private String token;
    private long expiresIn;
}
