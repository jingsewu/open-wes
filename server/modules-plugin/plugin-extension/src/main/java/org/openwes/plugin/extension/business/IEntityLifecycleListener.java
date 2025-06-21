package org.openwes.plugin.extension.business;

public interface IEntityLifecycleListener<U, V> {

    /**
     * you can extends function after status change.
     * for example: you can call upstream api and notice it when order created
     * <p>
     * attention: sometimes the U will be null. but the v will not be null, so you can get order through the V value
     *
     * @param u         the entityId
     * @param v         the entity order no
     * @param newStatus the new status of the entity
     */
    default void afterStatusChange(U u, V v, String newStatus) {

    }

    String getEntityName();

}
