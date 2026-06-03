import { useMemo } from 'react'
import { Text } from '@react-three/drei'
import type { ShelfConfig } from '@/types'

interface ShelfProps {
  config: ShelfConfig
  storedLocations?: Set<string>
}

// ─── Standard Euro Plastic Bin Colours ──────────────────────────────
const BIN_COLORS = [
  '#b8b8b8', // light gray (standard)
  '#a8b8b0', // gray-green
  '#b0b0b8', // blue-gray
  '#b8b0a8', // warm gray
  '#aab0b0', // neutral gray
  '#b8b0b0', // rose-gray
]

// ─── Structural constants ──────────────────────────────────────────
const RACK_HEIGHT = 4.0
const POST_RADIUS = 0.035
const POST_RADIUS_BOT = 0.045
const BEAM_W = 0.05
const BEAM_H = 0.06
const DECK_T = 0.025
const POST_INSET = 0.08

// ─── Material palette (industrial steel) ────────────────────────────
const COLORS = {
  post: '#7a8a9a',
  beam: '#9aabb8',
  deck: '#b0c0cc',
  brace: '#8a9aaa',
  label: '#4a5a6a',
  idLabel: '#2a3a4a',
  accent: '#ecc94b',
  connector: '#6a7a8a',
  binRim: '#c0c0c0',
  binDark: '#888888',
}

// ─── Euro Plastic Bin Component ─────────────────────────────────────
function EuroBin({ pos, colorIdx, size = 1 }: { pos: [number, number, number]; colorIdx: number; size?: number }) {
  const bw = 0.32 * size
  const bd = 0.28 * size
  const bh = 0.18 * size
  const color = BIN_COLORS[colorIdx % BIN_COLORS.length]

  return (
    <group position={pos}>
      {/* Main body – tapered look (slightly narrower bottom) */}
      <mesh position={[0, bh / 2, 0]} castShadow>
        <boxGeometry args={[bw, bh, bd]} />
        <meshStandardMaterial color={color} metalness={0.05} roughness={0.85} />
      </mesh>

      {/* Outer rim / lip (top edge) */}
      <mesh position={[0, bh + 0.005, 0]}>
        <boxGeometry args={[bw + 0.02, 0.012, bd + 0.02]} />
        <meshStandardMaterial color={COLORS.binRim} metalness={0.1} roughness={0.75} />
      </mesh>

      {/* Inner rim lip (slightly inset) */}
      <mesh position={[0, bh + 0.005, 0]}>
        <boxGeometry args={[bw - 0.04, 0.008, bd - 0.04]} />
        <meshStandardMaterial color="#aaa" metalness={0.05} roughness={0.9} />
      </mesh>

      {/* Front label area (slight recessed panel) */}
      <mesh position={[0, bh * 0.45, bd / 2 + 0.002]}>
        <planeGeometry args={[bw * 0.5, bh * 0.35]} />
        <meshStandardMaterial color="#d0d0d0" metalness={0.0} roughness={0.9} />
      </mesh>

      {/* Vertical reinforcing ribs (sides) */}
      {[-1, 1].map((side) => (
        <>
          {[-0.08, 0, 0.08].map((rz, i) => (
            <mesh key={`rib-${side}-${i}`} position={[side * bw / 2, bh * 0.35, rz * bd * 0.3]}>
              <boxGeometry args={[0.003, bh * 0.4, 0.003]} />
              <meshStandardMaterial color={COLORS.binDark} metalness={0.1} roughness={0.8} />
            </mesh>
          ))}
        </>
      ))}

      {/* Bottom edge detail */}
      <mesh position={[0, 0.004, 0]}>
        <boxGeometry args={[bw - 0.02, 0.006, bd - 0.02]} />
        <meshStandardMaterial color="#aaa" metalness={0.1} roughness={0.8} />
      </mesh>
    </group>
  )
}

export function Shelf({ config, storedLocations }: ShelfProps) {
  const numTiers = config.locationCodes.length

  // Guard: no location codes -> flat platform
  if (numTiers === 0) {
    return (
      <group position={[config.x + config.width / 2, 0.5, config.y + config.height / 2]}>
        <mesh castShadow receiveShadow>
          <boxGeometry args={[config.width, 0.08, config.height]} />
          <meshStandardMaterial color={COLORS.brace} metalness={0.4} roughness={0.7} />
        </mesh>
      </group>
    )
  }

  const hw = config.width / 2
  const hd = config.height / 2
  const pi = POST_INSET
  const tierH = RACK_HEIGHT / numTiers

  // 4 corner positions (relative to group centre)
  const corners = useMemo<[number, number][]>(
    () => [
      [-hw + pi, -hd + pi],
      [hw - pi, -hd + pi],
      [-hw + pi, hd - pi],
      [hw - pi, hd - pi],
    ],
    [hw, hd, pi],
  )

  return (
    <group position={[config.x + config.width / 2, 0, config.y + config.height / 2]}>
      {/* ═══ 4 Corner posts ═══ */}
      {corners.map(([px, pz], i) => (
        <mesh key={`post-${i}`} position={[px, RACK_HEIGHT / 2, pz]} castShadow>
          <cylinderGeometry args={[POST_RADIUS, POST_RADIUS_BOT, RACK_HEIGHT, 8]} />
          <meshStandardMaterial color={COLORS.post} metalness={0.7} roughness={0.5} />
        </mesh>
      ))}

      {/* ═══ Foot plates ═══ */}
      {corners.map(([px, pz], i) => (
        <mesh key={`foot-${i}`} position={[px, 0.015, pz]}>
          <boxGeometry args={[0.18, 0.03, 0.18]} />
          <meshStandardMaterial color={COLORS.brace} metalness={0.5} roughness={0.7} />
        </mesh>
      ))}

      {/* ═══ Post base collars ═══ */}
      {corners.map(([px, pz], i) => (
        <mesh key={`collar-${i}`} position={[px, 0.035, pz]}>
          <cylinderGeometry args={[POST_RADIUS_BOT + 0.008, POST_RADIUS_BOT + 0.008, 0.015, 8]} />
          <meshStandardMaterial color={COLORS.connector} metalness={0.6} roughness={0.5} />
        </mesh>
      ))}

      {/* ═══ Cross bracing – front face only ═══ */}
      {(() => {
        const braceW = hw - 2 * pi
        const braceLen = Math.sqrt(RACK_HEIGHT * RACK_HEIGHT + braceW * braceW)
        const angle = Math.atan2(RACK_HEIGHT, braceW)
        return (
          <group position={[0, RACK_HEIGHT / 2, -hd + pi]}>
            <mesh rotation={[0, 0, angle]}>
              <boxGeometry args={[BEAM_W * 0.4, braceLen * 1.05, BEAM_W * 0.4]} />
              <meshStandardMaterial color={COLORS.brace} metalness={0.4} roughness={0.7} />
            </mesh>
            <mesh rotation={[0, 0, -angle]}>
              <boxGeometry args={[BEAM_W * 0.4, braceLen * 1.05, BEAM_W * 0.4]} />
              <meshStandardMaterial color={COLORS.brace} metalness={0.4} roughness={0.7} />
            </mesh>
          </group>
        )
      })()}

      {/* ═══ Beam connectors (decorative clips at post-beam junctions) ═══ */}
      {corners.map(([px, pz], ci) =>
        Array.from({ length: numTiers }).map((_, t) => (
          <mesh
            key={`conn-${ci}-${t}`}
            position={[px, (t + 0.5) * tierH + BEAM_H / 2, pz]}
          >
            <boxGeometry args={[0.025, 0.025, 0.025]} />
            <meshStandardMaterial color={COLORS.connector} metalness={0.7} roughness={0.4} />
          </mesh>
        )),
      )}

      {/* ═══ Tier decks ═══ */}
      {Array.from({ length: numTiers }).map((_, t) => {
        const yPos = (t + 0.5) * tierH
        const deckW = config.width - 2 * pi - 0.02
        const deckD = config.height - 2 * pi - 0.02

        return (
          <group key={`tier-${t}`}>
            {/* Front beam */}
            <mesh position={[0, yPos, -hd]} castShadow>
              <boxGeometry args={[deckW, BEAM_H, BEAM_W]} />
              <meshStandardMaterial color={COLORS.beam} metalness={0.6} roughness={0.5} />
            </mesh>
            {/* Back beam */}
            <mesh position={[0, yPos, hd]} castShadow>
              <boxGeometry args={[deckW, BEAM_H, BEAM_W]} />
              <meshStandardMaterial color={COLORS.beam} metalness={0.6} roughness={0.5} />
            </mesh>
            {/* Left beam */}
            <mesh position={[-hw + pi, yPos, 0]} castShadow>
              <boxGeometry args={[BEAM_W, BEAM_H, deckD]} />
              <meshStandardMaterial color={COLORS.beam} metalness={0.6} roughness={0.5} />
            </mesh>
            {/* Right beam */}
            <mesh position={[hw - pi, yPos, 0]} castShadow>
              <boxGeometry args={[BEAM_W, BEAM_H, deckD]} />
              <meshStandardMaterial color={COLORS.beam} metalness={0.6} roughness={0.5} />
            </mesh>

            {/* Deck plate (semi-transparent wire deck look) */}
            <mesh position={[0, yPos + BEAM_H / 2 + DECK_T / 2, 0]} receiveShadow>
              <boxGeometry args={[deckW, DECK_T, deckD]} />
              <meshStandardMaterial
                color={COLORS.deck}
                metalness={0.3}
                roughness={0.8}
                transparent
                opacity={0.55}
              />
            </mesh>

            {/* Deck slats / wire decking detail */}
            {[-deckW * 0.3, -deckW * 0.1, deckW * 0.1, deckW * 0.3].map((ox, si) => (
              <mesh key={`slat-${t}-${si}`} position={[ox, yPos + BEAM_H / 2 + DECK_T, 0]} receiveShadow>
                <boxGeometry args={[0.03, 0.005, deckD - 0.08]} />
                <meshStandardMaterial color={COLORS.deck} metalness={0.4} roughness={0.7} />
              </mesh>
            ))}

            {/* Location-code label */}
            <Text
              position={[0, yPos - tierH * 0.15, hd + 0.08]}
              fontSize={0.18}
              color={COLORS.label}
              anchorX="center"
              anchorY="middle"
            >
              {config.locationCodes[t]}
            </Text>

            {/* Stored container on this tier – standard gray euro bin */}
            {storedLocations?.has(config.locationCodes[t]) && (
              <EuroBin
                pos={[0, yPos + BEAM_H + DECK_T + 0.005, 0]}
                colorIdx={t}
                size={1.0}
              />
            )}

            {/* Small shelf-level marker */}
            <mesh position={[-hw + pi - 0.03, yPos, -hd + pi + 0.04]}>
              <boxGeometry args={[0.01, BEAM_H * 0.6, 0.01]} />
              <meshStandardMaterial color={COLORS.accent} emissive={COLORS.accent} emissiveIntensity={0.3} />
            </mesh>
          </group>
        )
      })}

      {/* ═══ Top cap beam ═══ */}
      <mesh position={[0, RACK_HEIGHT - BEAM_H / 2, 0]}>
        <boxGeometry args={[config.width - 2 * pi - 0.04, BEAM_H, config.height - 2 * pi - 0.04]} />
        <meshStandardMaterial
          color={COLORS.brace}
          metalness={0.4}
          roughness={0.7}
          transparent
          opacity={0.25}
        />
      </mesh>

      {/* ═══ Post top caps ═══ */}
      {corners.map(([px, pz], i) => (
        <mesh key={`topcap-${i}`} position={[px, RACK_HEIGHT, pz]}>
          <cylinderGeometry args={[POST_RADIUS + 0.005, POST_RADIUS, 0.01, 8]} />
          <meshStandardMaterial color={COLORS.connector} metalness={0.6} roughness={0.5} />
        </mesh>
      ))}

      {/* ═══ Shelf ID label (floating above) ═══ */}
      <Text
        position={[0, RACK_HEIGHT + 0.25, 0]}
        fontSize={0.22}
        color={COLORS.idLabel}
        anchorX="center"
        anchorY="bottom"
      >
        {config.id}
      </Text>
    </group>
  )
}
