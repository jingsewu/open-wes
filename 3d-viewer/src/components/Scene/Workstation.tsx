import { Text } from '@react-three/drei'
import type { WorkstationConfig } from '@/types'

interface WorkstationProps {
  config: WorkstationConfig
}

// ─── Type colour palette ────────────────────────────────────────────
const PALETTE: Record<string, { main: string; dark: string; accent: string; screen: string }> = {
  PICKING: { main: '#2ecc71', dark: '#1a7a44', accent: '#f1c40f', screen: '#00ff88' },
  RECEIVING: { main: '#3498db', dark: '#1a5276', accent: '#85c1e9', screen: '#00ccff' },
}

const DEF = { main: '#9b59b6', dark: '#5b2c8a', accent: '#d4a5f0', screen: '#cc88ff' }

// ─── Sub-component: Conveyor Roller ─────────────────────────────────
function Roller({ pos }: { pos: [number, number, number] }) {
  return (
    <mesh position={pos} rotation={[0, 0, Math.PI / 2]} castShadow>
      <cylinderGeometry args={[0.035, 0.035, 0.55, 12]} />
      <meshStandardMaterial color="#555566" metalness={0.7} roughness={0.3} />
    </mesh>
  )
}

// ─── Sub-component: Safety stripe ───────────────────────────────────
function SafetyStripe({ pos, width, height }: { pos: [number, number, number]; width: number; height: number }) {
  return (
    <mesh position={pos}>
      <boxGeometry args={[width, 0.01, height]} />
      <meshStandardMaterial color="#ecc94b" emissive="#ecc94b" emissiveIntensity={0.1} />
    </mesh>
  )
}

export function Workstation({ config }: WorkstationProps) {
  const p = PALETTE[config.type] || DEF

  // ─── Station footprint ───
  const FW = 2.0 // total floor width
  const FD = 2.0 // total floor depth
  const CONV_W = 1.6 // conveyor width
  const CONV_D = 0.7 // conveyor depth
  const CONV_H = 0.45 // conveyor height
  const DESK_H = 0.7

  return (
    <group position={[config.x, 0, config.y]}>
      {/* ═══ Floor platform (light concrete) ═══ */}
      <mesh position={[0, 0.02, 0]} receiveShadow>
        <boxGeometry args={[FW, 0.04, FD]} />
        <meshStandardMaterial color="#c8ccd0" roughness={0.9} metalness={0.0} />
      </mesh>

      {/* ═══ Safety border (yellow strips) ═══ */}
      <SafetyStripe pos={[0, 0.045, -FD / 2 + 0.04]} width={FW - 0.08} height={0.04} />
      <SafetyStripe pos={[0, 0.045, FD / 2 - 0.04]} width={FW - 0.08} height={0.04} />
      <SafetyStripe pos={[-FW / 2 + 0.04, 0.045, 0]} width={0.04} height={FD - 0.08} />
      <SafetyStripe pos={[FW / 2 - 0.04, 0.045, 0]} width={0.04} height={FD - 0.08} />

      {/* ═══ Safety corner markers ═══ */}
      {[
        [-FW / 2 + 0.04, -FD / 2 + 0.04],
        [FW / 2 - 0.04, -FD / 2 + 0.04],
        [-FW / 2 + 0.04, FD / 2 - 0.04],
        [FW / 2 - 0.04, FD / 2 - 0.04],
      ].map(([cx, cz], i) => (
        <mesh key={`corner-${i}`} position={[cx, 0.045, cz]}>
          <boxGeometry args={[0.06, 0.01, 0.06]} />
          <meshStandardMaterial color={p.main} emissive={p.main} emissiveIntensity={0.15} />
        </mesh>
      ))}

      {/* ═══ Conveyor section (front half) ═══ */}
      <group position={[0, 0.04, FD * 0.18]}>
        {/* Conveyor base */}
        <mesh position={[0, CONV_H / 2, 0]} castShadow>
          <boxGeometry args={[CONV_W, CONV_H, CONV_D]} />
          <meshStandardMaterial color="#b8c0c8" metalness={0.2} roughness={0.7} />
        </mesh>

        {/* Side rails */}
        <mesh position={[-CONV_W / 2 - 0.02, CONV_H / 2 + 0.04, 0]}>
          <boxGeometry args={[0.04, 0.08, CONV_D]} />
          <meshStandardMaterial color="#889098" metalness={0.5} roughness={0.5} />
        </mesh>
        <mesh position={[CONV_W / 2 + 0.02, CONV_H / 2 + 0.04, 0]}>
          <boxGeometry args={[0.04, 0.08, CONV_D]} />
          <meshStandardMaterial color="#889098" metalness={0.5} roughness={0.5} />
        </mesh>

        {/* Rollers */}
        {[-0.2, -0.07, 0.07, 0.2].map((rz, i) => (
          <Roller key={`roller-${i}`} pos={[0, CONV_H + 0.04, rz]} />
        ))}

        {/* Type-colour accent strip on conveyor */}
        <mesh position={[0, CONV_H + 0.08, -CONV_D / 2 + 0.02]}>
          <boxGeometry args={[CONV_W - 0.1, 0.015, 0.04]} />
          <meshStandardMaterial color={p.main} emissive={p.main} emissiveIntensity={0.3} />
        </mesh>
      </group>

      {/* ═══ Desk / terminal section (back half) ═══ */}
      <group position={[0, 0.04, -FD * 0.22]}>
        {/* Desk base */}
        <mesh position={[0, DESK_H / 2, 0]} castShadow>
          <boxGeometry args={[CONV_W, DESK_H, CONV_D * 0.85]} />
          <meshStandardMaterial color="#a8b0b8" metalness={0.2} roughness={0.7} />
        </mesh>

        {/* Desk top surface */}
        <mesh position={[0, DESK_H + 0.01, 0]}>
          <boxGeometry args={[CONV_W - 0.05, 0.02, CONV_D * 0.8]} />
          <meshStandardMaterial color="#c8d0d8" metalness={0.3} roughness={0.5} />
        </mesh>

        {/* ═══ Monitor screen ═══ */}
        {/* Screen panel */}
        <mesh position={[0, DESK_H + 0.22, -CONV_D * 0.25]} castShadow>
          <boxGeometry args={[0.4, 0.3, 0.02]} />
          <meshStandardMaterial color="#111" metalness={0.9} roughness={0.1} />
        </mesh>
        {/* Screen glow */}
        <mesh position={[0, DESK_H + 0.22, -CONV_D * 0.25 + 0.012]}>
          <planeGeometry args={[0.36, 0.26]} />
          <meshBasicMaterial
            color={p.screen}
            transparent
            opacity={0.85}
          />
        </mesh>
        {/* Screen stand */}
        <mesh position={[0, DESK_H + 0.06, -CONV_D * 0.25]}>
          <boxGeometry args={[0.05, 0.1, 0.02]} />
          <meshStandardMaterial color="#222" metalness={0.5} roughness={0.5} />
        </mesh>

        {/* ─── Keyboard / input area ─── */}
        <mesh position={[0.08, DESK_H + 0.025, -CONV_D * 0.08]}>
          <boxGeometry args={[0.2, 0.008, 0.08]} />
          <meshStandardMaterial color="#333" metalness={0.1} roughness={0.9} />
        </mesh>

        {/* ─── Type-specific elements ─── */}
        {config.type === 'PICKING' ? (
          <>
            {/* Picking: bin shelf on the right */}
            <group position={[CONV_W * 0.35, DESK_H * 0.5, 0]}>
              {/* Shelf frame */}
              {[-1, 1].map((side) => (
                <mesh key={`bin-post-${side}`} position={[side * 0.1, 0.15, 0]}>
                  <boxGeometry args={[0.02, 0.3, 0.15]} />
                  <meshStandardMaterial color="#555566" metalness={0.5} roughness={0.5} />
                </mesh>
              ))}
              {/* Bin shelf */}
              <mesh position={[0, 0.15, 0]} castShadow>
                <boxGeometry args={[0.22, 0.02, 0.15]} />
                <meshStandardMaterial color="#444455" metalness={0.4} roughness={0.6} />
              </mesh>
              {/* Bins on shelf */}
              {[-0.04, 0.04].map((ox, bi) => (
                <mesh key={`bin-${bi}`} position={[ox, 0.27, 0]}>
                  <boxGeometry args={[0.07, 0.08, 0.1]} />
                  <meshStandardMaterial color="#e8c84a" metalness={0.1} roughness={0.8} />
                </mesh>
              ))}
            </group>

            {/* Pick list display (small screen on left) */}
            <mesh position={[-CONV_W * 0.3, DESK_H + 0.18, -CONV_D * 0.1]}>
              <boxGeometry args={[0.12, 0.18, 0.015]} />
              <meshStandardMaterial color="#111" metalness={0.8} roughness={0.2} />
            </mesh>
            <mesh position={[-CONV_W * 0.3, DESK_H + 0.18, -CONV_D * 0.1 + 0.008]}>
              <planeGeometry args={[0.1, 0.15]} />
              <meshBasicMaterial color={p.screen} transparent opacity={0.6} />
            </mesh>
          </>
        ) : config.type === 'RECEIVING' ? (
          <>
            {/* Receiving: scanner post on the right */}
            <group position={[CONV_W * 0.35, 0, 0]}>
              {/* Post */}
              <mesh position={[0, DESK_H * 0.7, 0]}>
                <cylinderGeometry args={[0.02, 0.025, DESK_H * 1.4, 8]} />
                <meshStandardMaterial color="#556677" metalness={0.6} roughness={0.4} />
              </mesh>
              {/* Scanner head */}
              <mesh position={[0, DESK_H * 1.4, -0.08]}>
                <boxGeometry args={[0.08, 0.04, 0.06]} />
                <meshStandardMaterial color="#333" metalness={0.7} roughness={0.3} />
              </mesh>
              {/* Scanner beam (glow) */}
              <mesh position={[0, DESK_H * 1.38, -0.15]}>
                <planeGeometry args={[0.02, 0.15]} />
                <meshBasicMaterial color="#ff3333" transparent opacity={0.2} />
              </mesh>
              {/* Status light on scanner */}
              <mesh position={[0.035, DESK_H * 1.4, -0.08]}>
                <sphereGeometry args={[0.01, 6, 6]} />
                <meshBasicMaterial color="#00ff00" />
              </mesh>
            </group>

            {/* Receiving: label/printer on the left */}
            <mesh position={[-CONV_W * 0.3, DESK_H * 0.5, 0]} castShadow>
              <boxGeometry args={[0.12, 0.1, 0.14]} />
              <meshStandardMaterial color="#444455" metalness={0.3} roughness={0.7} />
            </mesh>
            {/* Slot / display */}
            <mesh position={[-CONV_W * 0.3, DESK_H * 0.5, 0.075]}>
              <planeGeometry args={[0.06, 0.04]} />
              <meshBasicMaterial color={p.screen} transparent opacity={0.4} />
            </mesh>
          </>
        ) : null}
      </group>

      {/* ═══ Type-color accent column (on left side of platform) ═══ */}
      <mesh position={[-FW / 2 + 0.04, 0.3, 0]}>
        <boxGeometry args={[0.02, 0.6, 0.5]} />
        <meshStandardMaterial color={p.main} emissive={p.main} emissiveIntensity={0.08} metalness={0.5} roughness={0.5} />
      </mesh>

      {/* ═══ Labels (dark text on light background) ═══ */}
      {/* Station name */}
      <Text
        position={[0, 1.1, 0]}
        fontSize={0.22}
        color="#2a3a4a"
        anchorX="center"
        anchorY="bottom"
      >
        {config.id}
      </Text>

      {/* Type badge */}
      <Text
        position={[0, 0.88, 0]}
        fontSize={0.1}
        color={p.main}
        anchorX="center"
        anchorY="bottom"
      >
        {config.type}
      </Text>
    </group>
  )
}
