package org.openwes.plugin.extension.business;

public interface IEntityLifecycleListener<U> {

    default void afterStatusChange(U u, String newStatus) {
    }

    String getEntityName();

}
