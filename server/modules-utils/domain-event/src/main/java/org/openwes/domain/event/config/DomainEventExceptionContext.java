package org.openwes.domain.event.config;

public class DomainEventExceptionContext {

    private static final ThreadLocal<Throwable> threadLocalException = new ThreadLocal<>();

    public static void setException(Throwable throwable) {
        threadLocalException.set(throwable);
    }

    public static Throwable getException() {
        return threadLocalException.get();
    }

    public static void clearException() {
        threadLocalException.remove();
    }

    public static boolean hasException() {
        return getException() != null;
    }
}
