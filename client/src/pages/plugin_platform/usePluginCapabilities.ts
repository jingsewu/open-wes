import { useEffect, useState } from "react"
import { getCapabilities } from "./api"

export interface PluginCapabilitiesState {
    canDevelop: boolean
    canAdminister: boolean
    roles: string[]
    loading: boolean
}

const INITIAL: PluginCapabilitiesState = {
    canDevelop: false,
    canAdminister: false,
    roles: [],
    loading: true
}

/**
 * UI gating only. Every mutation is authorized again by the server, which audits denials.
 */
export default function usePluginCapabilities(): PluginCapabilitiesState {
    const [state, setState] = useState<PluginCapabilitiesState>(INITIAL)

    useEffect(() => {
        let active = true
        getCapabilities()
            .then((capabilities) => {
                if (!active) {
                    return
                }
                setState({
                    canDevelop: capabilities.canDevelop,
                    canAdminister: capabilities.canAdminister,
                    roles: capabilities.roles || [],
                    loading: false
                })
            })
            .catch(() => {
                if (active) {
                    setState({ canDevelop: false, canAdminister: false, roles: [], loading: false })
                }
            })
        return () => {
            active = false
        }
    }, [])

    return state
}
