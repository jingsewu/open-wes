import { useEffect, useRef, useCallback } from 'react'
import { useSimulatorStore } from '@/stores/simulatorStore'
import type { WebSocketMessage } from '@/types'

const BASE_DELAY = 1000
const MAX_DELAY = 30000

export function useRobotWebSocket(url: string) {
  const wsRef = useRef<WebSocket | null>(null)
  const retryCountRef = useRef(0)
  const retryTimerRef = useRef<number>()
  const updateRobotState = useSimulatorStore(s => s.updateRobotState)
  const setConnectionStatus = useSimulatorStore(s => s.setConnectionStatus)

  const connect = useCallback(() => {
    setConnectionStatus('connecting')
    const ws = new WebSocket(url)

    ws.onopen = () => {
      retryCountRef.current = 0
      setConnectionStatus('connected')
    }

    ws.onmessage = (event) => {
      try {
        const msg: WebSocketMessage = JSON.parse(event.data)
        if (msg.type === 'ROBOT_STATE_UPDATE') {
          updateRobotState(msg.robots, msg.tasks)
        }
      } catch {
        // ignore parse errors
      }
    }

    ws.onclose = () => {
      setConnectionStatus('disconnected')
      scheduleReconnect()
    }

    ws.onerror = () => {
      ws.close()
    }

    wsRef.current = ws
  }, [url, updateRobotState, setConnectionStatus])

  const scheduleReconnect = useCallback(() => {
    const delay = Math.min(BASE_DELAY * Math.pow(2, retryCountRef.current), MAX_DELAY)
    retryCountRef.current++
    retryTimerRef.current = window.setTimeout(connect, delay)
  }, [connect])

  useEffect(() => {
    connect()
    return () => {
      if (retryTimerRef.current) clearTimeout(retryTimerRef.current)
      wsRef.current?.close()
    }
  }, [connect])
}
