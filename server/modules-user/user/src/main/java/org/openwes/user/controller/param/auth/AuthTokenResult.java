package org.openwes.user.controller.param.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthTokenResult {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType = "bearer";
    @JsonProperty("expire_in")
    private long expireIn;
    private String scope = "universe";
}
