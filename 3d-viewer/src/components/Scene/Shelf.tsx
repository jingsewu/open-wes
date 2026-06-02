import { useMemo } from 'react'
import { Text } from '@react-three/drei'
import type { ShelfConfig } from '@/types'

interface ShelfProps {
  config: ShelfConfig
  storedLocations?: Set<string>
}

// ─── Structural constants ──────────────────────────────────────────
const RACK_HEIGHT = 2.8
const POST_RADIUS = 0.035
const POST_RADIUS_BOT = 0.045
const BEAM_W = 0.05
const BEAM_H = 0.06
const DECK_T = 0.025
const POST_INSET = 0.08

// ─── Container colours ─────────────────────────────────────────────
const CONTAINER_COLORS = ['#c0392b', '#2980b9', '#27ae60', '#8e44ad', '#d35400', '#1abc9c']

// ─── Material palette (light industrial) ────────────────────────────
const COLORS = {
  post: '#7a8a9a',
  beam: '#9aabb8',
  deck: '#b0c0cc',
  brace: '#8a9aaa',
  label: '#4a5a6a',
  idLabel: '#2a3a4a',
  accent: '#ecc94b',
}

export function Shelf({ config, storedLocations }: ShelfProps) {
  const numTiers = config.locationCodes.length

  // Guard: no location codes → flat platform
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

      {/* ═══ Cross bracing – front face only (saves geometry) ═══ */}
      {/* X-brace across the front */}
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

            {/* Deck plate (semi-transparent grate look) */}
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

            {/* Deck slats (visual detail – 3 thin strips per tier) */}
            {[-deckW * 0.25, 0, deckW * 0.25].map((ox, si) => (
              <mesh key={`slat-${t}-${si}`} position={[ox, yPos + BEAM_H / 2 + DECK_T, 0]} receiveShadow>
                <boxGeometry args={[0.03, 0.005, deckD - 0.1]} />
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

            {/* Stored container on this tier */}
            {storedLocations?.has(config.locationCodes[t]) && (
              <mesh position={[0, yPos + BEAM_H + DECK_T + 0.06, 0]} castShadow>
                <boxGeometry args={[deckW * 0.4, 0.12, deckD * 0.35]} />
                <meshStandardMaterial
                  color={CONTAINER_COLORS[t % CONTAINER_COLORS.length]}
                  metalness={0.2}
                  roughness={0.7}
                />
              </mesh>
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
