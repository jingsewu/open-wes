package org.openwes.api.platform.utils;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class AuthUtils {

    private AuthUtils() {
        throw new IllegalStateException("auth url is not initialized");
    }

    public static String getAccessToken(Authentication authentication) throws Exception {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(authentication.getAuthUrl());

            // Set the request body
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("grant_type", authentication.getGrantType()));
            nvps.add(new BasicNameValuePair("username", authentication.getUsername()));
            nvps.add(new BasicNameValuePair("password", authentication.getPassword()));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName(authentication.getEncoding())));

            // Set Basic Authentication header
            String credentials = authentication.getSecretId() + ":" + authentication.getSecretKey();
            byte[] credentialsBytes = credentials.getBytes();
            String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentialsBytes);
            httpPost.setHeader("Authorization", "Basic " + base64Credentials);

            // Execute the request
            CloseableHttpResponse response = httpClient.execute(httpPost);

            // Check for a successful response
            if (response.getCode() != 200) {
                throw new AuthException("Failed to obtain access token. HTTP error code: " + response.getCode());
            }

            // Extract and return the access token from the response
            String responseBody = EntityUtils.toString(response.getEntity());
            return JSON.parseObject(responseBody).getString(authentication.getTokenName());
        }

    }

    @Data
    @Accessors(chain = true)
    public static class Authentication {
        private String authUrl;
        private String grantType;
        private String username;
        private String password;
        private String secretId;
        private String secretKey;
        private String encoding;
        private String tokenName;
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
