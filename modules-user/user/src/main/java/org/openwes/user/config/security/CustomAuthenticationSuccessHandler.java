package org.openwes.user.config.security;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.exception.handler.SwmsErrorResponse;
import org.openwes.user.api.utils.JwtUtils;
import org.openwes.user.api.utils.TokenResponse;
import org.openwes.user.application.UserService;
import org.openwes.user.controller.common.vo.AuthModel;
import org.openwes.user.controller.common.vo.UserModel;
import org.openwes.user.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collections;

@AllArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtils tokenUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        // Generate the JWT token
        DefaultOAuth2User userDetails = (DefaultOAuth2User) authentication.getPrincipal();
        String name = userDetails.getName();

        User user = userService.getByAccount(name);
        if (user == null) {
            SwmsErrorResponse errorResponse = SwmsErrorResponse.builder().message("User not found")
                    .errorCode(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode())
                    .description(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getDesc())
                    .build();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(JsonUtils.obj2String(errorResponse));
            return;
        }

        Object authModel = buildResponse(user);

        // Send JWT token as a response body (or set in headers/cookies)
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(JsonUtils.obj2String(authModel));
    }

    private Object buildResponse(User user) {
        UserModel userModel = UserModel.builder().id(user.getId())
                .username(user.getAccount()).icon(user.getAvatar()).build();

        TokenResponse tokenResponse = tokenUtils.generateJwtCookie(Lists.newArrayList(), user.getAccount(),
                Collections.emptySet(), user.getTenantName());
        return AuthModel.builder().token(tokenResponse.getToken()).expiresIn(tokenResponse.getExpiresIn())
                .user(userModel).build();
    }
}
