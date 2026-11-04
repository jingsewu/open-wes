package org.openwes.user.api;

import java.util.Set;

/**
 * Read-only role lookup for service-layer authorization. Implementations must not cache: role
 * changes take effect on the next call.
 */
public interface IUserRoleApi {

    /** Role codes assigned to the account; empty when the account is blank, unknown, or has no roles. */
    Set<String> findRoleCodes(String username);
}
