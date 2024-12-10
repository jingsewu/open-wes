package org.openwes.station.infrastructure.filters;

public class HttpStationContext {

    protected HttpStationContext() {
    }

    public static final String STATION_CODE = "stationCode";

    private static final ThreadLocal<Long> stationCodeContext = new ThreadLocal<>();

    public static void setWorkStationId(Long workStationId) {
        stationCodeContext.set(workStationId);
    }

    public static void removeStationCode() {
        stationCodeContext.remove();
    }

    public static Long getWorkStationId() {
        return stationCodeContext.get();
    }

}
