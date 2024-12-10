package org.openwes.user.api.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.user.AuthConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.openwes.common.utils.user.AuthConstants.*;

@Component
@Slf4j
public class JwtUtils {

    @Value("${Jwt.token.secret: sanandxuan}")
    private String tokenSecret;

    @Value("${Jwt.token.expiration.seconds:3600}")
    private int tokenExpiration;

    public TokenResponse generateJwtCookie(List<String> authorities, String userName, Set<String> authWarehouseCodes, String tenantName) {
        String token = generateToken(authorities, userName, authWarehouseCodes, tenantName);
        return new TokenResponse().setToken(token).setExpiresIn(tokenExpiration);

    }

    public String generateToken(List<String> authorityList, String userName, Set<String> authWarehouseCodes, String tenantName) {
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        return JWT.create()
                .withClaim(AUTH_TENANT, tenantName)
                .withClaim(AuthConstants.USERNAME, userName)
                .withClaim(AUTH_MENUS, authorityList)
                .withClaim(AUTH_WAREHOUSE, Lists.newArrayList(authWarehouseCodes))
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration * 1000L))
                .sign(algorithm);
    }

    public DecodedJWT verifyJwt(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(tokenSecret)).build();
        return verifier.verify(token);
    }

}
