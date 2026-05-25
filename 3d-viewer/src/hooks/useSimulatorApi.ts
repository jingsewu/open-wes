import { useCallback } from 'react'
import { useSimulatorStore } from '@/stores/simulatorStore'
import type { WarehouseLayout } from '@/types'

const API_BASE = import.meta.env.VITE_SIMULATOR_API_URL || 'http://localhost:8091'

async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  return res.json()
}

export function useSimulatorApi() {
  const setLayout = useSimulatorStore(s => s.setLayout)

  const fetchLayout = useCallback(async () => {
    const layout = await apiFetch<WarehouseLayout>('/api/simulator/layout')
    setLayout(layout)
    return layout
  }, [setLayout])

  const resetSimulator = useCallback(async () => {
    await apiFetch('/api/simulator/reset', { method: 'POST' })
  }, [])

  const updateConfig = useCallback(async (config: Record<string, unknown>) => {
    await apiFetch('/api/simulator/config', {
      method: 'PUT',
      body: JSON.stringify(config),
    })
  }, [])

  const injectError = useCallback(async (robotCode: string) => {
    await apiFetch(`/api/simulator/robots/${robotCode}/error`, { method: 'POST' })
  }, [])

  const recoverRobot = useCallback(async (robotCode: string) => {
    await apiFetch(`/api/simulator/robots/${robotCode}/recover`, { method: 'POST' })
  }, [])

  return { fetchLayout, resetSimulator, updateConfig, injectError, recoverRobot }
}
