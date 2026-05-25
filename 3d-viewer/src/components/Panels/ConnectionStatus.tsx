import { useSimulatorStore } from '@/stores/simulatorStore'
import type { ConnectionStatus as StatusType } from '@/types'

const STATUS_CONFIG: Record<StatusType, { label: string; color: string; bg: string }> = {
  connected: { label: 'Connected', color: 'text-green-700', bg: 'bg-green-100' },
  connecting: { label: 'Reconnecting...', color: 'text-yellow-700', bg: 'bg-yellow-100' },
  disconnected: { label: 'Disconnected', color: 'text-red-700', bg: 'bg-red-100' },
}

export function ConnectionStatus() {
  const status = useSimulatorStore(s => s.connectionStatus)
  const config = STATUS_CONFIG[status]

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${config.color} ${config.bg}`}>
      <span className={`w-2 h-2 rounded-full ${status === 'connected' ? 'bg-green-500' : status === 'connecting' ? 'bg-yellow-500 animate-pulse' : 'bg-red-500'}`} />
      {config.label}
    </div>
  )
}
