package org.openwes.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private boolean checkAuthorize = true;

    @PostConstruct
    public void init() {
        Collections.addAll(authenticateUrlWhiteList, DEFAULT_AUTHENTICATE_WHITE_LIST);
        Collections.addAll(authorizeUrlWhiteList, DEFAULT_AUTHORIZE_WHITE_LIST);
    }

    /**
     * urls that do not verify authentication
     */
    private List<String> authenticateUrlWhiteList = new ArrayList<>();
    private static final String[] DEFAULT_AUTHENTICATE_WHITE_LIST = {
            "/oauth2/**",
            "/login/oauth2/**",
            "/actuator/**",
            "/v2/api-docs/**",
            "/v2/api-docs-ext/**",
            "/swagger/api-docs",
            "/swagger-ui.html",
            "/doc.html",
            "/swagger-resources/**",
            "/druid/**",
            "/user/api/auth/signin"
    };

    /**
     * urls that need verify authentication, but not verify authorization
     */
    private final List<String> authorizeUrlWhiteList = new ArrayList<>();
    private static final String[] DEFAULT_AUTHORIZE_WHITE_LIST = {
            "/user/api/currentUser/*",
            "/user/api/auth/signout",
            "/search/search",
            "/search/search/searchSelectResult",
            "/mdm/dictionary/getAll",
            "/api-platform/api/execute",
            "/station/api"
    };
}
