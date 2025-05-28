package com.open.wes.extension.business;

public interface IEntityLifecycleListener<C, D, U> {

    default void beforeCreate(C c) {
    }

    default void afterCreate(D d) {
    }

    default void afterStatusChange(U u, String newStatus) {
    }

    default void beforeCancel(U u) {
    }

    default void afterCancel(U u) {
    }

    String getEntityName();
}
