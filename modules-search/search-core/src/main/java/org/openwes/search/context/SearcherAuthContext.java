package org.openwes.search.context;

public class SearcherAuthContext {
    private static final ThreadLocal<String> WAREHOUSE_AUTH = new ThreadLocal<>();

    public static String getWarehouseAuth() {
        return WAREHOUSE_AUTH.get();
    }

    public static void setWarehouseAuth(String warehouseAuth) {
        WAREHOUSE_AUTH.set(warehouseAuth);
    }

    public static void removeWarehouseAuth() {
        WAREHOUSE_AUTH.remove();
    }

}
