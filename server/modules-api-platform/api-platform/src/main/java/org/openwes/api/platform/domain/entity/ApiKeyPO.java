package org.openwes.api.platform.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;


@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "a_api_key",
        indexes = {
                @Index(name = "uk_api_key", columnList = "apiKey", unique = true),
                @Index(name = "uk_api_key_name", columnList = "apiKeyName", unique = true),
        })
public class ApiKeyPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    private String apiKeyName;

    private String apiKey;

    public void generateApiKey() {
        this.apiKey = generateUniqueApiKey(this.apiKeyName);
    }

    public static String generateUniqueApiKey(String apiKeyName) {
        try {
            String combinedString = apiKeyName + UUID.randomUUID();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedString.getBytes(StandardCharsets.UTF_8));

            String apiKey = Base64.getEncoder().encodeToString(hashBytes);

            apiKey = apiKey.replaceAll("[^a-zA-Z0-9]", "").substring(0, 32); // 32-character key
            return apiKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate API key", e);
        }
    }
}
