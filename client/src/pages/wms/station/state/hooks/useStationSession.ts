import { useState } from "react"
import { workStationEventLoop } from "../../event-loop/eventLoopInstance"

/**
 * Owns the station-session layer:
 * "Which workstation am I at?"
 *
 * Persists stationId to localStorage. Never clears it on task exit —
 * only clearStation() (explicit station change) does that.
 *
 * Extension point: add autoSelectByIp() here for Phase 2 IP binding.
 */
export function useStationSession() {
    const [isStationSelected, setIsStationSelected] = useState(
        () => !!localStorage.getItem("stationId")
    )

    const selectStation = (id: string) => {
        localStorage.setItem("stationId", id)
        setIsStationSelected(true)
    }

    /**
     * Full teardown: closes WebSocket, resets store, clears stationId.
     * Call this when the user explicitly changes workstation or logs out.
     */
    const clearStation = () => {
        void workStationEventLoop.destroy()
        localStorage.removeItem("stationId")
        setIsStationSelected(false)
    }

    return { isStationSelected, selectStation, clearStation }
}
