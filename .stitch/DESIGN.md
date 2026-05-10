# Design System: Performance Athletic

## 1. Visual Theme & Atmosphere
A high-performance, action-oriented interface engineered for speed and athletic excellence. The atmosphere is bold, aggressive yet disciplined. It leverages "High-Contrast Minimalism," using massive typographic weight and a singular, high-energy accent color against a backdrop of expansive whitespace to eliminate cognitive load.

## 2. Color Palette & Roles
- **Canvas White** (#FFFFFF) — Primary background surface.
- **Off-Black Charcoal** (#18181B) — Primary text, headings, and high-contrast structural elements. Never use pure black.
- **Velocity Red** (#D90429) — Singular accent color for primary CTAs and active states. High-energy but disciplined saturation.
- **Muted Slate** (#71717A) — Secondary text, metadata.
- **Whisper Grey** (#F4F4F5) — Very subtle background for section differentiation or input fields.

## 3. Typography Rules
- **Display:** Poppins — Track-tight, controlled scale, ExtraBold to Black weights.
- **Body:** Inter — Relaxed leading (1.6), max 65ch width.
- **Banned:** Generic system fonts for headings. No pure black text.

## 4. Component Stylings
- **Buttons:** Tactile push feedback. Primary buttons are solid Velocity Red with white text. Secondary buttons are outline-only with a 2px Charcoal stroke.
- **Cards:** No aggressive borders. Use a diffused "ambient shadow" (e.g., `0 10px 30px rgba(24, 24, 27, 0.05)`) to lift elements off the surface. Soft radius of 4px.
- **Inputs:** Minimalist with a 1px bottom border that transforms into a 2px Velocity Red border on focus.

## 5. Layout Principles
- **Grid:** 12-column fixed grid on desktop with generous 24px gutters.
- **Spacing:** High-contrast spacing. Use massive whitespace (`stack-lg` 64px+) between major sections.
- **Alignment:** Rigid left-alignment for all text blocks to maintain a strong vertical axis of stability.

## 6. Anti-Patterns (Banned)
- No emojis anywhere.
- No pure black (`#000000`).
- No neon/outer glow shadows.
- No 3-column equal grids; prefer asymmetric or generous 4-column setups with ample breathing room.
- No filler UI text ("Scroll to explore", bouncing chevrons).
