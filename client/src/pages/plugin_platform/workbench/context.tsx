import React, { createContext, useCallback, useContext, useEffect, useState } from "react"
import {
    errorMessage,
    getContract,
    getPlugin,
    listRevisions
} from "../api"
import {
    PluginContract,
    ScriptPlugin,
    ScriptPluginRevision
} from "../types"
import usePluginCapabilities, { PluginCapabilitiesState } from "../usePluginCapabilities"

export interface WorkbenchContextValue {
    pluginKey: string
    plugin: ScriptPlugin | null
    revisions: ScriptPluginRevision[]
    currentRevision: ScriptPluginRevision | null
    contract: PluginContract | null
    capabilities: PluginCapabilitiesState
    loading: boolean
    error: string | null
    reload: () => Promise<void>
}

const WorkbenchContext = createContext<WorkbenchContextValue | null>(null)

interface WorkbenchProviderProps {
    pluginKey: string
    children: React.ReactNode
}

export const WorkbenchProvider: React.FC<WorkbenchProviderProps> = ({ pluginKey, children }) => {
    const [plugin, setPlugin] = useState<ScriptPlugin | null>(null)
    const [revisions, setRevisions] = useState<ScriptPluginRevision[]>([])
    const [contract, setContract] = useState<PluginContract | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const capabilities = usePluginCapabilities()

    useEffect(() => {
        let active = true
        setLoading(true)
        setError(null)
        getPlugin(pluginKey)
            .then((detail) => {
                if (!active) {
                    return
                }
                setPlugin(detail)
                listRevisions(pluginKey)
                    .then((list) => {
                        if (active) {
                            setRevisions(list)
                        }
                    })
                    .catch((revisionError) => {
                        if (active) {
                            setError(errorMessage(revisionError, "The plugin revisions could not be loaded."))
                        }
                    })
                    .then(() => {
                        if (active) {
                            setLoading(false)
                        }
                    })
                getContract(detail.applySystem)
                    .then((loaded) => {
                        if (active) {
                            setContract(loaded)
                        }
                    })
                    .catch(() => {
                        if (active) {
                            setContract(null)
                        }
                    })
            })
            .catch((detailError) => {
                if (active) {
                    setError(errorMessage(detailError, "The plugin could not be loaded."))
                    setLoading(false)
                }
            })
        return () => {
            active = false
        }
    }, [pluginKey])

    const reload = useCallback(() => {
        return Promise.all([getPlugin(pluginKey), listRevisions(pluginKey)])
            .then(([detail, list]) => {
                setPlugin(detail)
                setRevisions(list)
                setError(null)
            })
            .catch((reloadError) => {
                setError(errorMessage(reloadError, "The plugin could not be refreshed."))
            })
    }, [pluginKey])

    const currentRevision =
        revisions.filter((revision) => revision.id === (plugin && plugin.currentRevisionId))[0] || null

    const value: WorkbenchContextValue = {
        pluginKey,
        plugin,
        revisions,
        currentRevision,
        contract,
        capabilities,
        loading,
        error,
        reload
    }

    return <WorkbenchContext.Provider value={value}>{children}</WorkbenchContext.Provider>
}

export function useWorkbench(): WorkbenchContextValue {
    const value = useContext(WorkbenchContext)
    if (!value) {
        throw new Error("useWorkbench must be used inside a WorkbenchProvider")
    }
    return value
}
