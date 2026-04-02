# ADR-011: MUI-Based Design System Replacing Custom CSS

## Status
Accepted

## Context
The application used a custom CSS approach (883-line `index.css` with CSS custom properties) for all styling. This made it difficult to maintain consistency, lacked built-in accessibility, and didn't support responsive layouts or advanced table features (sorting, filtering).

## Decision
Adopt Material UI (MUI) as the centralized design system with a custom theme (`createTheme`) defining the "Chocolate Truffle" palette, Outfit typography, and component overrides. Replace all custom CSS classes with MUI components and `sx` prop styling.

## Key Components
- **Theme** (`theme.ts`): Centralized palette (Primary #713600, Secondary #C05800, Background #FDFBD4, Text #38240D), Outfit font, component defaults
- **AppTable**: Reusable table component with sorting, global filtering, ellipsis overflow, sticky header, max-height scroll
- **AppModal**: Dialog wrapper with slide transition
- **Sidebar**: MUI Drawer with responsive behavior (permanent on desktop, temporary on mobile)
- **Layout**: 100vh flex layout with fixed sidebar and scrollable content area

## Alternatives Considered
- **Tailwind CSS** — Utility-first approach would require adding a build dependency and wouldn't provide component abstractions (tables, dialogs, etc.)
- **Keep custom CSS + add features** — Would require writing sorting/filtering/responsive logic from scratch without the component library benefits.
- **Ant Design or Chakra UI** — MUI was already a dependency (`@mui/material` v7.3.9) so it was the natural choice.

## Consequences
- `index.css` reduced from 883 lines to a minimal reset (20 lines)
- All 7 pages rewritten to use MUI components exclusively
- `@mui/icons-material` added as a dependency for icon buttons
- Tables support column sorting (click header), global text filtering, and internal scrolling
- Editable tables (contractor types, schedule) use form-below-table pattern instead of inline editing
- Responsive: sidebar collapses to hamburger menu on screens below 900px
