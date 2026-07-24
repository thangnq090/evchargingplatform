# UX Guide

## Overview
UI/UX standards for the EV Charging Platform ensuring consistent, accessible, and performant user experiences across Admin Dashboard, Vendor Portal, and Customer Mobile App. Built on React 18 + Vite + TypeScript.

## Design System / Component Library

**Choice**: **Custom component library** (built on Radix UI primitives + Tailwind CSS)
- **Why**: Full control over design tokens, no vendor lock-in, aligns with modular monolith architecture
- **Radix UI**: Unstyled, accessible primitives (Dialog, Select, Tabs, Tooltip, etc.)
- **Tailwind CSS**: Utility-first styling with design token integration
- **Storybook**: Component documentation and visual testing

**Alternative Considered**:
- MUI: Heavy, opinionated, harder to customize deeply
- Chakra UI: Good but less control over internals
- shadcn/ui: Great starting point but we need our own token system

**Structure**:
```
packages/ui/                          # Shared component library (internal package)
├── src/
│   ├── components/                   # Composed components
│   │   ├── button/
│   │   ├── card/
│   │   ├── table/
│   │   ├── form/
│   │   ├── data-display/
│   │   ├── navigation/
│   │   └── feedback/
│   ├── primitives/                   # Radix wrappers with our tokens
│   │   ├── dialog/
│   │   ├── select/
│   │   └── ...
│   ├── hooks/                        # Shared UI hooks
│   ├── utils/                        # Classname utilities, formatters
│   ├── tokens/                       # Design tokens (colors, spacing, etc.)
│   └── theme/                        # Tailwind config, CSS variables
├── .storybook/
└── package.json
```

**Token System** (CSS variables + Tailwind config):
```css
/* Core tokens */
--color-primary-500: #0066CC;
--color-success-500: #10B981;
--color-warning-500: #F59E0B;
--color-error-500: #EF4444;
--color-background: #FFFFFF;
--color-surface: #F9FAFB;
--color-text-primary: #111827;
--color-text-secondary: #6B7280;
--spacing-unit: 4px;  /* Base unit for all spacing */
--radius-sm: 4px;
--radius-md: 8px;
--radius-lg: 12px;
--font-sans: 'Inter', system-ui, sans-serif;
--font-mono: 'JetBrains Mono', monospace;
```

## Styling Approach

**Tailwind CSS** (v4) with **CSS Variables** for theming

**Why Tailwind**:
- Design token integration via CSS variables
- Zero-runtime (build-time CSS generation)
- Works perfectly with React/Vite
- Easy dark mode via `dark:` variant
- Tree-shakeable, small production bundle

**Configuration**:
```js
// tailwind.config.js
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: 'var(--color-primary-50)',
          // ... 100-900
          500: 'var(--color-primary-500)',
        },
        // semantic colors
        success: 'var(--color-success-500)',
        warning: 'var(--color-warning-500)',
        error: 'var(--color-error-500)',
        background: 'var(--color-background)',
        surface: 'var(--color-surface)',
        text: {
          primary: 'var(--color-text-primary)',
          secondary: 'var(--color-text-secondary)',
        },
      },
      spacing: {
        // spacing scale based on 4px unit
        '1': 'var(--spacing-unit)',  /* 4px */
        '2': 'calc(var(--spacing-unit) * 2)',  /* 8px */
        '3': 'calc(var(--spacing-unit) * 3)',  /* 12px */
        '4': 'calc(var(--spacing-unit) * 4)',  /* 16px */
        '5': 'calc(var(--spacing-unit) * 5)',  /* 20px */
        '6': 'calc(var(--spacing-unit) * 6)',  /* 24px */
        '8': 'calc(var(--spacing-unit) * 8)',  /* 32px */
      },
      borderRadius: {
        sm: 'var(--radius-sm)',
        md: 'var(--radius-md)',
        lg: 'var(--radius-lg)',
        full: '9999px',
      },
      fontFamily: {
        sans: ['var(--font-sans)'],
        mono: ['var(--font-mono)'],
      },
    },
  },
};
```

**Usage Pattern**:
```tsx
// Good - uses design tokens
<Button className="bg-primary-500 text-white px-4 py-2 rounded-md hover:bg-primary-600">
  Start Charging
</Button>

// Bad - hardcoded values
<button className="bg-[#0066CC] text-white px-16 py-8 rounded-[8px]">
  Start Charging
</button>
```

## Accessibility Standards

**Target**: WCAG 2.1 Level AA compliance

**Requirements**:
- **Color Contrast**: Minimum 4.5:1 for text, 3:1 for UI components
- **Keyboard Navigation**: All interactive elements reachable and operable via keyboard
- **Focus Management**: Visible focus indicators (`outline: 2px solid var(--color-primary-500); outline-offset: 2px`)
- **ARIA**: Proper labels, roles, states (use Radix primitives which handle this)
- **Screen Readers**: Semantic HTML, `aria-live` for dynamic content (notifications, session status)
- **Motion**: Respect `prefers-reduced-motion` (disable animations)

**Implementation**:
```tsx
// Focus visible utility (applied globally)
:focus-visible {
  outline: 2px solid var(--color-primary-500);
  outline-offset: 2px;
}

// Skip link for keyboard users
<a href="#main-content" className="sr-only focus:not-sr-only fixed top-4 left-4 z-50">
  Skip to main content
</a>

// Live region for session updates
<div aria-live="polite" aria-atomic="true" className="sr-only">
  {sessionStatusMessage}
</div>
```

**Testing**:
- axe-core in CI (via Vitest + @axe-core/react)
- Manual keyboard testing in PR checklist
- Screen reader testing (NVDA/VoiceOver) for critical flows

## Responsive Design Strategy

**Breakpoints** (Tailwind defaults, mobile-first):
| Breakpoint | Width | Target Devices |
|------------|-------|----------------|
| `sm` | 640px | Large phones / small tablets |
| `md` | 768px | Tablets |
| `lg` | 1024px | Laptops / small desktop |
| `xl` | 1280px | Desktop |
| `2xl` | 1536px | Large desktop |

**Approach**:
- **Mobile-first**: Base styles for mobile, enhance upward
- **Fluid typography**: `clamp()` for responsive text scaling
- **Container queries**: For component-level responsiveness (where supported)
- **Touch targets**: Minimum 44x44px (use `min-h-[44px] min-w-[44px]`)

**Layout Patterns**:
```tsx
// Dashboard: Sidebar + content
<div className="flex min-h-screen">
  <aside className="hidden lg:block w-64 bg-surface border-r">
    <Navigation />
  </aside>
  <main className="flex-1 p-4 md:p-6 lg:p-8">
    <DashboardContent />
  </main>
</div>

// Data table: Horizontal scroll on mobile, full on desktop
<div className="overflow-x-auto">
  <table className="min-w-full">
    ...
  </table>
</div>

// Cards: Stack on mobile, grid on desktop
<div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
  {stations.map(s => <StationCard key={s.id} station={s} />)}
</div>
```

**Portal-Specific**:
- **Admin Dashboard**: Desktop-first (primary use case), tablet support
- **Vendor Portal**: Desktop-first, responsive down to tablet
- **Customer Mobile App**: Mobile-first, PWA, offline-capable

## Component Standards

**Base Component Props** (all components extend):
```typescript
interface BaseComponentProps {
  className?: string;           // Tailwind overrides
  'data-testid'?: string;       // Testing hook
  children?: React.ReactNode;   // For composition
}
```

**Variant System** (using `class-variance-authority`):
```typescript
// button-variants.ts
import { cva, VariantProps } from 'class-variance-authority';

export const buttonVariants = cva(
  'inline-flex items-center justify-center font-medium transition-colors ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 ' +
  'disabled:opacity-50 disabled:pointer-events-none',
  {
    variants: {
      variant: {
        primary: 'bg-primary-500 text-white hover:bg-primary-600',
        secondary: 'bg-surface text-text-primary hover:bg-surface/80 border border-border',
        outline: 'border-2 border-primary-500 text-primary-500 hover:bg-primary-50',
        ghost: 'text-text-primary hover:bg-surface',
        destructive: 'bg-error-500 text-white hover:bg-error-600',
      },
      size: {
        sm: 'h-8 px-3 text-sm rounded-sm',
        md: 'h-10 px-4 text-base rounded-md',
        lg: 'h-12 px-6 text-lg rounded-lg',
        icon: 'h-10 w-10',
      },
    },
    defaultVariants: { variant: 'primary', size: 'md' },
  }
);

export type ButtonVariants = VariantProps<typeof buttonVariants>;
```

**Compound Components** (for complex UI):
```tsx
// Select.tsx
<Select>
  <SelectTrigger>
    <SelectValue placeholder="Select station" />
  </SelectTrigger>
  <SelectContent>
    <SelectGroup>
      <SelectLabel>Available Stations</SelectLabel>
      {stations.map(s => (
        <SelectItem key={s.id} value={s.id}>
          {s.name}
        </SelectItem>
      ))}
    </SelectGroup>
  </SelectContent>
</Select>
```

## State Management (UI)

**Server State**: TanStack Query (React Query) v5
- Caching, background refetch, optimistic updates
- Query keys: `['stations', { vendorId, status }]`
- Mutations for create/update/delete with invalidation

**Client State**: 
- **Local**: `useState`, `useReducer` for component-scoped state
- **Global**: Zustand (lightweight) for cross-feature state (auth, theme, notifications)
- **URL State**: React Router search params for filters, pagination, tabs

**Forms**: React Hook Form + Zod validation
```typescript
const schema = z.object({
  stationName: z.string().min(1).max(100),
  connectorType: z.enum(['TYPE_2', 'CCS', 'CHADEMO']),
  maxPowerKw: z.number().min(1).max(350),
});

const form = useForm<StationForm>({
  resolver: zodResolver(schema),
  defaultValues: { stationName: '', connectorType: 'TYPE_2', maxPowerKw: 22 },
});
```

## Performance Standards

**Bundle Size**:
- Initial JS < 150KB gzipped
- Code-split by route (React.lazy + Suspense)
- Dynamic imports for heavy features (maps, charts)

**Rendering**:
- React 18 concurrent features (useTransition, useDeferredValue)
- Virtualization for lists > 50 items (TanStack Virtual)
- Memoization: `React.memo`, `useMemo`, `useCallback` where measured

**Images**:
- WebP/AVIF via Vite plugin
- Responsive images with `srcset`
- Lazy loading (`loading="lazy"`)

**Metrics** (tracked via Web Vitals):
- LCP < 2.5s
- INP < 200ms
- CLS < 0.1

## Icon System

**Library**: Lucide React (tree-shakeable, consistent)
**Usage**:
```tsx
import { Zap, MapPin, Battery, AlertCircle } from 'lucide-react';

<Zap className="w-5 h-5 text-primary-500" aria-hidden="true" />
```
**Accessibility**: `aria-hidden="true"` on decorative icons; meaningful icons get `aria-label` or visible label.

## Animation Standards

**Library**: Tailwind CSS animations + Framer Motion (for complex transitions)
**Principles**:
- Duration: 150-300ms for UI transitions
- Easing: `ease-out` for entering, `ease-in` for exiting
- Respect `prefers-reduced-motion`
- No animation on layout shifts (CLS prevention)

```css
/* Global reduced motion */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

## Dark Mode

**Strategy**: CSS variable swap via `dark:` class on `<html>`
```css
:root {
  --color-background: #FFFFFF;
  --color-surface: #F9FAFB;
  --color-text-primary: #111827;
  --color-text-secondary: #6B7280;
}

.dark {
  --color-background: #111827;
  --color-surface: #1F2937;
  --color-text-primary: #F9FAFB;
  --color-text-secondary: #9CA3AF;
}
```
**Toggle**: Persisted in localStorage, synced with OS preference initially.

## Decision Relationships

- **Tech Stack → UX Guide**: React + Vite + TypeScript → Radix + Tailwind + TanStack Query
- **System Architecture → UX Guide**: Module boundaries → Feature-based component organization
- **Coding Standards → UX Guide**: Component naming, file organization, testing patterns apply
- **API Conventions → UX Guide**: API response types → TypeScript interfaces for UI