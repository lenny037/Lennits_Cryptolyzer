import React from 'react';

interface LennitLogoProps {
  /** Design system theme selector */
  mode: 'light' | 'dark';
  /** Responsive dimension scaling */
  width?: string | number;
  height?: string | number;
  /** Opacity transition overrides */
  className?: string;
}

/**
 * LENNIT_CRYPTOLYZER — Production Adaptive Logo System
 * Consolidates Hybrid A (Dark Mode: The Conduit Gate) and Hybrid B (Light Mode: The Shielded Loop).
 * Enforces precise chromatic gradient balancing and real-time SVG neon glow filters.
 */
export const LennitLogo: React.FC<LennitLogoProps> = ({
  mode,
  width = '100%',
  height = '100%',
  className = '',
}) => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 800 600"
      width={width}
      height={height}
      className={className}
      style={{ transition: 'all 0.3s ease-in-out' }}
    >
      <defs>
        {/* Chromatic Matrix: Bright Copper Gradient Maps */}
        <linearGradient id="copper-metallic" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#783A14" />
          <stop offset="25%" stopColor="#D47A3B" />
          <stop offset="50%" stopColor="#F6A262" />
          <stop offset="75%" stopColor="#D47A3B" />
          <stop offset="100%" stopColor="#783A14" />
        </linearGradient>

        <linearGradient id="copper-horizontal" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stopColor="#D47A3B" />
          <stop offset="50%" stopColor="#F6A262" />
          <stop offset="100%" stopColor="#783A14" />
        </linearGradient>

        {/* Physics Core: Fire-Engine Red Neon Emissive Glow Filter */}
        <filter id="neon-fire-engine-glow" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="6" result="core-blur" />
          <feGaussianBlur stdDeviation="15" result="ambient-blur" />
          <feGaussianBlur stdDeviation="30" result="deep-falloff" />
          <feColorMatrix
            type="matrix"
            values="1 0 0 0 0.98
                    0 1 0 0 0.11
                    0 0 1 0 0.15
                    0 0 0 1 0"
            in="deep-falloff"
            result="colored-glow"
          />
          <feMerge>
            <feMergeNode in="colored-glow" />
            <feMergeNode in="ambient-blur" />
            <feMergeNode in="core-blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      {/* Canvas Layer Configuration */}
      {mode === 'dark' ? (
        <rect width="800" height="600" fill="#0D0D11" rx="8" />
      ) : (
        <rect width="800" height="600" fill="#FFFFFF" rx="8" />
      )}

      {/* EMBLEM RENDERING LOGIC */}
      <g transform="translate(40, 50)">
        {mode === 'dark' ? (
          /* =================================================================
             DARK MODE CONFIGURATION: HYBRID A — THE CONDUIT GATE
             ================================================================= */
          <g id="hybrid-a-conduit-gate">
            {/* The Structural Anchor: Matte Black Gateway Pillars */}
            <path
              d="M 120 80 L 150 50 L 150 450 L 120 420 Z"
              fill="#09090B"
              stroke="#18181B"
              strokeWidth="2"
            />
            <path
              d="M 380 80 L 350 50 L 350 450 L 380 420 Z"
              fill="#09090B"
              stroke="#18181B"
              strokeWidth="2"
            />
            <path
              d="M 150 110 L 350 110 L 350 140 L 150 140 Z"
              fill="#09090B"
            />

            {/* The Data Pipeline Flow: Interlocking Copper L and C Paths */}
            {/* Abstract Loop C */}
            <path
              d="M 310 180 
                 C 200 180, 180 230, 180 250 
                 C 180 270, 200 320, 310 320"
              fill="none"
              stroke="url(#copper-metallic)"
              strokeWidth="24"
              strokeLinecap="round"
            />
            {/* Abstract Loop L */}
            <path
              d="M 220 210 
                 L 220 290 
                 C 220 310, 240 320, 270 320 
                 L 320 320"
              fill="none"
              stroke="url(#copper-metallic)"
              strokeWidth="16"
              strokeLinecap="round"
            />

            {/* Neon Activation Element: Deep Fire-Engine Red Core Intersection */}
            <circle
              cx="250"
              cy="250"
              r="18"
              fill="#FF1E27"
              filter="url(#neon-fire-engine-glow)"
            />
            <line
              x1="150"
              y1="250"
              x2="350"
              y2="250"
              stroke="#FF1E27"
              strokeWidth="4"
              strokeDasharray="8 4"
              filter="url(#neon-fire-engine-glow)"
              opacity="0.8"
            />
          </g>
        ) : (
          /* =================================================================
             LIGHT MODE CONFIGURATION: HYBRID B — THE SHIELDED LOOP
             ================================================================= */
          <g id="hybrid-b-shielded-loop">
            {/* The Primary Frame: Woven Copper Shield Track */}
            <path
              d="M 250 60 
                 L 360 120 
                 C 360 280, 310 390, 250 440 
                 C 190 390, 140 280, 140 120 
                 Z"
              fill="none"
              stroke="url(#copper-metallic)"
              strokeWidth="28"
              strokeLinejoin="round"
            />

            {/* The Contrast Anchor: Matte Black Vertical Substrate Aperture */}
            <path
              d="M 235 60 L 265 60 L 265 435 L 235 435 Z"
              fill="#09090B"
            />

            {/* High-Intensity Laser Line: Tucked Inside the Black Shadowbox */}
            <line
              x1="250"
              y1="80"
              x2="250"
              y2="410"
              stroke="#FF1E27"
              strokeWidth="6"
              strokeLinecap="round"
              filter="url(#neon-fire-engine-glow)"
            />

            {/* Secondary Geometric Balance Rings */}
            <circle
              cx="250"
              cy="160"
              r="12"
              fill="none"
              stroke="url(#copper-horizontal)"
              strokeWidth="4"
            />
            <circle
              cx="250"
              cy="300"
              r="12"
              fill="none"
              stroke="url(#copper-horizontal)"
              strokeWidth="4"
            />
          </g>
        )}
      </g>

      {/* TYPOGRAPHY BLOCK RENDER LAYER */}
      <g transform="translate(420, 0)">
        {mode === 'dark' ? (
          /* =================================================================
             DARK TYPOGRAPHY SYSTEM
             ================================================================= */
          <g>
            {/* LENNIT Logotype: Matte Black font with Copper Outer-Inline stroke */}
            <text
              x="20"
              y="280"
              fontFamily="system-ui, -apple-system, sans-serif"
              fontSize="76"
              fontWeight="900"
              fill="#000000"
              stroke="url(#copper-horizontal)"
              strokeWidth="2"
              letterSpacing="0.08em"
            >
              LENNIT
            </text>
            {/* CRYPTOLYZER Subtext: Clean tracking with ambient copper backglow */}
            <text
              x="25"
              y="340"
              fontFamily="ui-monospace, SFMono-Regular, monospace"
              fontSize="24"
              fontWeight="600"
              fill="#09090B"
              stroke="url(#copper-horizontal)"
              strokeWidth="0.5"
              letterSpacing="0.42em"
            >
              CRYPTOLYZER
            </text>
            {/* Core Status Neon Marker */}
            <circle
              cx="335"
              cy="328"
              r="5"
              fill="#FF1E27"
              filter="url(#neon-fire-engine-glow)"
            />
          </g>
        ) : (
          /* =================================================================
             LIGHT TYPOGRAPHY SYSTEM
             ================================================================= */
          <g>
            {/* LENNIT Logotype: Flawless corporate-grade solid Matte Black */}
            <text
              x="20"
              y="280"
              fontFamily="system-ui, -apple-system, sans-serif"
              fontSize="76"
              fontWeight="900"
              fill="#09090B"
              letterSpacing="0.08em"
            >
              LENNIT
            </text>
            {/* CRYPTOLYZER Subtext: Precision high-contrast line anchor */}
            <text
              x="25"
              y="340"
              fontFamily="ui-monospace, SFMono-Regular, monospace"
              fontSize="24"
              fontWeight="700"
              fill="#09090B"
              letterSpacing="0.42em"
            >
              CRYPTOLYZER
            </text>
            {/* Security Core Indicator */}
            <circle
              cx="335"
              cy="328"
              r="5"
              fill="#FF1E27"
              filter="url(#neon-fire-engine-glow)"
            />
          </g>
        )}
      </g>
    </svg>
  );
};
