package org.openwes.script.plugin.core.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.exception.ScriptPluginWorkbenchErrorDescEnum;
import org.openwes.user.api.IUserRoleApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Single authorization point for the workbench. Every check reads the caller from UserContext and
 * resolves role codes through IUserRoleApi; no cache, so revocation is immediate.
 */
@Service
@Slf4j
public class PluginWorkbenchAccessService {

    private final IUserRoleApi userRoleApi;
    private final ScriptPluginOperationAuditService auditService;
    private final List<String> developerRoleCodes;
    private final List<String> adminRoleCodes;
    private final String superRoleCode;

    public PluginWorkbenchAccessService(
            IUserRoleApi userRoleApi,
            ScriptPluginOperationAuditService auditService,
            @Value("${plugin.security.developer-role-codes:pluginDeveloper}") String developerRoleCodes,
            @Value("${plugin.security.admin-role-codes:pluginAdmin}") String adminRoleCodes,
            @Value("${superRoleCode:superAdmin}") String superRoleCode) {
        this.userRoleApi = userRoleApi;
        this.auditService = auditService;
        this.developerRoleCodes = parseRoleCodes(developerRoleCodes, "pluginDeveloper");
        this.adminRoleCodes = parseRoleCodes(adminRoleCodes, "pluginAdmin");
        this.superRoleCode = StringUtils.defaultIfBlank(superRoleCode, "superAdmin");
    }

    public PluginCapabilitiesDTO capabilities() {
        String username = requireAuthenticated();
        Set<String> roles = rolesOf(username);
        boolean administer = isAdmin(roles);
        boolean develop = administer || hasAny(roles, developerRoleCodes);
        return new PluginCapabilitiesDTO(develop, administer,
                Collections.unmodifiableSet(new TreeSet<>(roles)));
    }

    public String requireAuthenticated() {
        String username = UserContext.getCurrentUser();
        if (StringUtils.isBlank(username)) {
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.AUTHENTICATED_USER_REQUIRED);
        }
        return username;
    }

    public String requireDeveloper() {
        String username = requireAuthenticated();
        if (!isDeveloper(rolesOf(username))) {
            log.warn("user {} is not a script plugin developer", username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.DEVELOPER_ROLE_REQUIRED);
        }
        return username;
    }

    public String requireDeveloper(String pluginKey, ScriptPluginAuditActionEnum action) {
        String username = requireAuthenticated();
        if (!isDeveloper(rolesOf(username))) {
            auditService.recordAs(pluginKey, action, null, false,
                    missingRole(developerRoleCodes), username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.DEVELOPER_ROLE_REQUIRED);
        }
        return username;
    }

    public String requireAdmin() {
        String username = requireAuthenticated();
        if (!isAdmin(rolesOf(username))) {
            log.warn("user {} is not a script plugin administrator", username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.ADMIN_ROLE_REQUIRED);
        }
        return username;
    }

    public String requireAdmin(String pluginKey, ScriptPluginAuditActionEnum action) {
        String username = requireAuthenticated();
        if (!isAdmin(rolesOf(username))) {
            auditService.recordAs(pluginKey, action, null, false,
                    missingRole(adminRoleCodes), username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.ADMIN_ROLE_REQUIRED);
        }
        return username;
    }

    private boolean isDeveloper(Set<String> roles) {
        return isAdmin(roles) || hasAny(roles, developerRoleCodes);
    }

    private boolean isAdmin(Set<String> roles) {
        return roles.contains(superRoleCode) || hasAny(roles, adminRoleCodes);
    }

    private static boolean hasAny(Set<String> roles, List<String> codes) {
        return codes.stream().anyMatch(roles::contains);
    }

    private Set<String> rolesOf(String username) {
        Set<String> roles = userRoleApi.findRoleCodes(username);
        return roles == null ? Set.of() : new TreeSet<>(roles);
    }

    private static String missingRole(List<String> codes) {
        return "missing role " + String.join("|", codes);
    }

    private static List<String> parseRoleCodes(String raw, String fallback) {
        if (StringUtils.isBlank(raw)) {
            return List.of(fallback);
        }
        List<String> codes = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        return codes.isEmpty() ? List.of(fallback) : codes;
    }
}
