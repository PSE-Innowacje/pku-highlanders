# Iteration 99 — UI/UX Redesign — Application Revamp & Centralized Design System

## Objective
Deliver a complete redesign of the application to improve user experience, visual consistency, and maintainability. Establish a unified, scalable design system based on MUI, ensuring consistency, accessibility, and responsiveness across all modules.

## Design System & Visual Language
- Implement a centralized design system based on MUI `createTheme`
- Use the **"Outfit"** font as the primary application typeface
- Apply the global **"Chocolate Truffle"** palette:
  - Primary: #4CBB17
  - Secondary: #48872B
  - Light / Background: #39542C
  - Dark / Text: #293325
- All UI elements (buttons, backgrounds, icons, gradients, etc.) must follow the palette
- Inputs must have visual contrast from page backgrounds (not the same color as the background)
- Text overflow should use ellipsis (`...`) instead of hard clipping
- Reuse UI components instead of duplicating them to ensure consistency and maintainability
- Use snoppy image in folder 'frontend/public' as logo,

## UX & Interaction Guidelines
- Use gradients to add depth and visual appeal
- Add micro-animations and transitions to avoid static or “raw” UI feel
- Prefer icon buttons over text buttons where contextually appropriate (e.g., table actions)
- Ensure interactions are intuitive and minimalistic, reducing unnecessary steps

## Table Behaviors & Data Handling
- All tables must support:
  - Column sorting (with ability to clear/reset sorting)
  - Filtering
- Design tables to avoid horizontal scrolling when possible
- Long text values must use ellipsis
- Tables must have a maximum height with internal scrolling enabled
- Global page scroll must not occur due to table content overflow

### Editable Tables (e.g. Schedule Dialog, "Typy oświadczeń")
- Table displays data only, not inline editable fields
- Editing and adding records is handled through a form displayed **below the table**
- The form appears when the user:
  - clicks **Edit** on an existing row
  - clicks **Add** to create a new entry
- After saving:
  - the form collapses
  - table updates with modified or newly created data

## Layout & Navigation
- Sidebar navigation on desktop must always occupy full viewport height (`100vh`)
- Scrolling is allowed only inside the main content container (not the entire page)
- Main content should remain fixed while the content area scrolls

## Responsiveness & Accessibility
- The UI must be responsive and usable on mobile devices
- Interactions and layout must be optimized for both desktop and mobile experiences
- Buttons, forms, and touch targets must remain accessible on smaller screens

## Acceptance Criteria
- Unified MUI-based design system is implemented with defined palette and typography
- Components are reused consistently across the app
- All tables support sorting, filtering, ellipsis handling, and internal scroll behavior
- Editable table behavior follows defined interaction rules
- Sidebar remains fixed and spans full viewport height
- No global page scroll caused by large tables
- Animations and gradients are used to enhance UX
- Application remains intuitive and functional on mobile devices