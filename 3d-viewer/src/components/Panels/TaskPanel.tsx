import { useSimulatorStore } from '@/stores/simulatorStore'

const STATUS_BADGES: Record<string, string> = {
  QUEUED: 'bg-gray-200 text-gray-800',
  ASSIGNED: 'bg-blue-100 text-blue-800',
  MOVING_TO_PICKUP: 'bg-green-100 text-green-800',
  LOADING: 'bg-yellow-100 text-yellow-800',
  MOVING_TO_DESTINATION: 'bg-green-100 text-green-800',
  UNLOADING: 'bg-yellow-100 text-yellow-800',
  COMPLETED: 'bg-emerald-100 text-emerald-800',
  FAILED: 'bg-red-100 text-red-800',
  CANCELED: 'bg-gray-100 text-gray-500',
}

export function TaskPanel() {
  const tasks = useSimulatorStore(s => s.tasks)
  const completedTasks = useSimulatorStore(s => s.completedTasks)

  return (
    <div className="absolute right-0 top-12 bottom-0 w-72 bg-white/90 backdrop-blur border-l border-gray-200 overflow-y-auto p-3">
      <h3 className="text-sm font-semibold text-gray-700 mb-2">Active Tasks ({tasks.length})</h3>
      {tasks.length === 0 && <p className="text-xs text-gray-400">No active tasks</p>}
      {tasks.map(task => (
        <div key={task.taskCode} className="mb-2 p-2 bg-gray-50 rounded text-xs">
          <div className="flex justify-between items-center mb-1">
            <span className="font-mono font-medium">{task.taskCode}</span>
            <span className={`px-1.5 py-0.5 rounded text-[10px] ${STATUS_BADGES[task.status] || 'bg-gray-100'}`}>
              {task.status}
            </span>
          </div>
          <div className="text-gray-500">
            <div>Container: {task.containerCode}</div>
            <div>{task.startLocation} &rarr; {task.destination || '?'}</div>
            {task.assignedRobot && <div>Robot: {task.assignedRobot}</div>}
          </div>
        </div>
      ))}

      {completedTasks.length > 0 && (
        <>
          <h3 className="text-sm font-semibold text-gray-700 mt-4 mb-2">Recent ({completedTasks.length})</h3>
          {completedTasks.map(task => (
            <div key={task.taskCode} className="mb-1 p-1.5 bg-gray-50 rounded text-[10px] text-gray-400">
              {task.taskCode} &mdash; {task.containerCode}
            </div>
          ))}
        </>
      )}
    </div>
  )
}
