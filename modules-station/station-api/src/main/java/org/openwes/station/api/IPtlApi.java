package org.openwes.station.api;

public interface IPtlApi {

    void reminderBind(Long workStationId, String ptlTag);

    void reminderDispatch(Long workStationId, String ptlTag, int qty, String displayText);

    void reminderSeal(Long workStationId, String ptlTag);

    void off(Long workStationId, String ptlTag);

}
