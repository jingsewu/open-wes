package org.openwes.gateway.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.gateway.config.AuthProperties;
import org.openwes.gateway.constant.SystemConstant;
import org.openwes.gateway.util.ResponseUtil;
import org.openwes.user.api.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * authorization and filter request
 */
@Slf4j
public class GlobalGatewayFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    public GlobalGatewayFilter(AuthProperties authProperties, JwtUtils jwtUtils, RedisUtils redisUtils) {
        this.authProperties = authProperties;
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
    }

    /**
     * Token过滤器
     *
     * @param exchange exchange
     * @param chain    chain
     * @return Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String requestUrl = request.getURI().getRawPath();
        boolean authorizeNotRequired = ignore(requestUrl);
        if (authorizeNotRequired) {
            return chain.filter(exchange);
        }

        String token = getToken(request);

        if (token == null) {
            return unauthorized(requestUrl, exchange.getResponse(), "token not passed, please login.");
        }

        if (token.startsWith(SystemConstant.BEARER_TYPE)) {
            token = token.replace(SystemConstant.BEARER_TYPE, "");
        }
        DecodedJWT jwt;
        try {
            jwt = jwtUtils.verifyJwt(token);
        } catch (TokenExpiredException e) {
            return unauthorized(requestUrl, exchange.getResponse(), "token is expired.");
        } catch (JWTVerificationException e) {
            return unauthorized(requestUrl, exchange.getResponse(), "token verification failed.");
        }

        if (jwt == null) {
            return unauthorized(requestUrl, exchange.getResponse(), "token is not illegal.");
        }

        String username = jwt.getClaim(AuthConstants.USERNAME).asString();
        String tokenCache = redisUtils.get(RedisConstants.USER_TOKEN_CACHE + username);
        if (StringUtils.isEmpty(tokenCache) || !StringUtils.equals(tokenCache, token)) {
            return unauthorized(requestUrl, exchange.getResponse(), "user is already logged in on another device. Please log in again.");
        }

        if (!verifyAuthorization(jwt, requestUrl, request.getMethod())) {
            return unauthorized(requestUrl, exchange.getResponse(), "request access denied, may be unauthorized.");
        }

        //set username in request header
        ServerHttpRequest newRequest = request.mutate()
                .header(SystemConstant.HEADER_AUTHORIZATION, "")
                .header(AuthConstants.USERNAME, username)
                .header(AuthConstants.AUTH_WAREHOUSE, "").build();
        return chain.filter(exchange.mutate().request(newRequest).build());

    }

    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SystemConstant.HEADER_AUTHORIZATION);
        if (StringUtils.isEmpty(token)) {
            token = request.getQueryParams().getFirst(SystemConstant.HEADER_AUTHORIZATION);
        }
        return token;
    }

    private List<String> getAuthWarehouse(DecodedJWT jwt) {
        Claim claim = jwt.getClaim(AuthConstants.AUTH_WAREHOUSE);
        if (null == claim) {
            return Collections.emptyList();
        }
        return claim.asList(String.class);
    }

    private boolean checkWarehouse(ServerHttpRequest request, List<String> authWarehouseCodes) {
        List<String> warehouseCodes = request.getHeaders().get(SystemConstant.HEADER_WAREHOUSE);
        if (CollectionUtils.isEmpty(warehouseCodes)) {
            return true;
        }
        String warehouseCode = warehouseCodes.iterator().next();
        return !StringUtils.isNotEmpty(warehouseCode) || "null".equals(warehouseCode.trim()) || authWarehouseCodes.contains(warehouseCode);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * 检查是否忽略url
     *
     * @param path 路径
     * @return boolean
     */
    private boolean ignore(String path) {
        if (path == null) {
            return true;
        }
        List<String> ignoreUrls = authProperties.getAuthenticateUrlWhiteList();
        if (CollectionUtils.isEmpty(ignoreUrls)) {
            return false;
        }
        for (String url : ignoreUrls) {
            if (StringUtils.isBlank(url)) {
                continue;
            }
            if (path.toLowerCase().startsWith(url.replace("/**", "").toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(String path, ServerHttpResponse resp, String msg) {
        log.warn("request access denied, may be unauthorized. path:{}, msg:{}", path, msg);
        return ResponseUtil.webFluxResponseWriter(resp, SystemConstant.APPLICATION_JSON_UTF8, HttpStatus.UNAUTHORIZED, msg);
    }

    public boolean verifyAuthorization(DecodedJWT jwt, String requestUrl, HttpMethod httpMethod) throws JWTVerificationException {

        if (!authProperties.isCheckAuthorize()) {
            log.warn("Authorization check is disabled.");
            return true;
        }

        if (authProperties.getAuthorizeUrlWhiteList().contains(requestUrl)) {
            return true;
        }

        Claim authorities = jwt.getClaim(AuthConstants.AUTH_MENUS);
        if (null == authorities) {
            return false;
        }
        List<String> authoritySet = authorities.asList(String.class);
        if (CollectionUtils.isEmpty(authoritySet)) {
            return false;
        }
        if (authoritySet.contains(AuthConstants.SUPPER_PERMISSION)) {
            return true;
        }
        String url = httpMethod.name().toLowerCase() + ":" + requestUrl;
        return authoritySet.stream().anyMatch(url::startsWith);
    }

}
