package org.openwes.common.utils.user;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContext {

    private UserContext() {
    }

    public static final ThreadLocal<String> USER = new TransmittableThreadLocal<>();

    public static String getCurrentUser() {
        return USER.get() == null ? "" : USER.get();
    }

    public static void setAccount(String account) {
        USER.set(account);
    }

    public static void removeUser() {
        USER.remove();
    }
}
