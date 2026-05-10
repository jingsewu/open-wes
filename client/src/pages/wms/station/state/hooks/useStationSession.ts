import { useState } from "react"

/**
 * Owns the station-session layer:
 * "Which workstation am I at?"
 *
 * Persists stationId to localStorage. Never clears it on task exit —
 * only a future "change station" action would do that.
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

    return { isStationSelected, selectStation }
}
