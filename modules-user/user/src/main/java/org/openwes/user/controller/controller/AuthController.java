package org.openwes.user.controller.controller;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.user.UserContext;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.user.api.utils.JwtUtils;
import org.openwes.user.api.utils.TokenResponse;
import org.openwes.user.application.UserRoleService;
import org.openwes.user.application.model.UserDetailsModel;
import org.openwes.user.controller.common.vo.AuthModel;
import org.openwes.user.controller.common.vo.UserModel;
import org.openwes.user.controller.param.auth.AuthTokenDTO;
import org.openwes.user.controller.param.auth.AuthTokenResult;
import org.openwes.user.controller.param.login.LoginRequest;
import org.openwes.user.domain.entity.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "User Module Api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRoleService userRoleService;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    @PostMapping("/signin")
    public AuthModel authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        UserDetailsModel userDetails = (UserDetailsModel) authentication.getPrincipal();
        List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        List<Role> roles = userRoleService.getByUserName(loginRequest.getUsername());
        Set<String> authWarehouseCodes = roles.stream().filter(v -> v.getWarehouseCodes() != null)
                .flatMap(v -> v.getWarehouseCodes().stream())
                .collect(Collectors.toSet());

        TokenResponse tokenResponse = jwtUtils.generateJwtCookie(authorities, userDetails.getUsername(), authWarehouseCodes,
                userDetails.getUser().getTenantName());

        redisUtils.set(RedisConstants.USER_TOKEN_CACHE + userDetails.getUsername(), tokenResponse.getToken(), tokenResponse.getExpiresIn());

        UserModel userModel = UserModel.builder().id(userDetails.getUser().getId())
                .username(userDetails.getUsername()).icon(userDetails.getUser().getAvatar()).build();

        return AuthModel.builder().token(tokenResponse.getToken()).user(userModel).expiresIn(tokenResponse.getExpiresIn()).build();
    }

    @GetMapping("/signout")
    public Response<Object> logoutUser() {
        String username = UserContext.getCurrentUser();
        redisUtils.delete(RedisConstants.USER_TOKEN_CACHE + username);
        return Response.success();
    }

    @PostMapping("token")
    public Object getToken(@RequestBody AuthTokenDTO authTokenDTO) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authTokenDTO.getUsername(), authTokenDTO.getPassword()));

        UserDetailsModel userDetails = (UserDetailsModel) authentication.getPrincipal();
        List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        TokenResponse tokenResponse = jwtUtils.generateJwtCookie(authorities, userDetails.getUsername(),
                Collections.emptySet(), userDetails.getUser().getTenantName());

        return new AuthTokenResult().setAccessToken(tokenResponse.getToken()).setExpireIn(tokenResponse.getExpiresIn());
    }
}
