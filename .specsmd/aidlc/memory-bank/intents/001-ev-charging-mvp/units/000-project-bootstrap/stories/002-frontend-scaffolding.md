# Story: Frontend Project Scaffolding with React + Vite

## User Story
As a **Developer**
I want to **scaffold the React 18 + Vite + TypeScript frontend project with pnpm**
So that **the frontend team has a consistent starting point aligned with the backend modules**

## Acceptance Criteria
- [ ] Given pnpm workspace, When `pnpm install` runs, Then dependencies are resolved
- [ ] Given Vite project, When `pnpm build` runs, Then production build succeeds
- [ ] Given project structure, When created, Then feature-based modules mirror backend modules (auth, stations, sessions, billing, vehicles, admin)
- [ ] Given ESLint + Prettier, When configured, Then code style is enforced
- [ ] Given Tailwind CSS, When configured, Then design tokens from UI standards are available

## Technical Notes
- Feature folders mirror backend module names
- Shared components, hooks, utils in `src/shared/`
- TanStack Query for server state
- React Router for routing

## Dependencies
- Story 000-001 (Backend scaffolding — API contracts defined)
