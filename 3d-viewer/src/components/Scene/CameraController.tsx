import { useRef, useEffect } from 'react'
import { useThree, useFrame } from '@react-three/fiber'
import { OrbitControls } from '@react-three/drei'
import * as THREE from 'three'
import { useSimulatorStore } from '@/stores/simulatorStore'

export function CameraController() {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const controlsRef = useRef<any>(null)
  const { camera } = useThree()
  const cameraMode = useSimulatorStore(s => s.cameraMode)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const robots = useSimulatorStore(s => s.robots)
  const layout = useSimulatorStore(s => s.layout)

  // Set initial camera position
  useEffect(() => {
    if (!layout) return
    const w = layout.warehouse.width
    const h = layout.warehouse.height
    if (cameraMode === 'topdown') {
      camera.position.set(w / 2, Math.max(w, h), h / 2)
      camera.lookAt(w / 2, 0, h / 2)
    } else {
      camera.position.set(w * 0.8, w * 0.5, h * 0.8)
      camera.lookAt(w / 2, 0, h / 2)
    }
  }, [cameraMode, layout, camera])

  // Follow selected robot
  useFrame(() => {
    if (cameraMode !== 'follow' || !selectedRobotCode) return
    const robot = robots.find(r => r.robotCode === selectedRobotCode)
    if (!robot || !controlsRef.current) return

    const target = new THREE.Vector3(robot.x, 0, robot.y)
    controlsRef.current.target.lerp(target, 0.1)
  })

  return <OrbitControls ref={controlsRef} enableDamping dampingFactor={0.1} />
}
