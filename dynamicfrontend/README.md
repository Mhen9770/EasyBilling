# Dynamic Frontend Runtime Engine

A metadata-driven React + TypeScript UI engine that reads backend metadata and renders forms, lists, workflows, dashboards, and pages at runtime — all tenant-configurable with no new code deployments.

## 🎯 Vision

Build a completely dynamic UI system where all forms, tables, workflows, and pages are defined by JSON metadata stored in the backend. No frontend code changes required for new entities or business logic.

## ✨ Features Implemented (Phase 0 - Scaffolding)

- ✅ **Component Registry**: Runtime registry for mapping component names to React components
- ✅ **Metadata Client**: Fetch and cache metadata with Zustand state management
- ✅ **Form Renderer**: Dynamic form generation from metadata with React Hook Form
- ✅ **Zod Validation**: Runtime validation schema generation from field metadata
- ✅ **Primitive Components**: Text, TextArea, Number, Select, Checkbox, Date inputs
- ✅ **Tailwind CSS**: Utility-first styling with theme support
- ✅ **TypeScript**: Full type safety throughout the application

## 🏗️ Architecture

### High-Level Principles

1. **Metadata-First**: UI defined by JSON metadata from backend
2. **Component Registry**: Atomic, composable components registered dynamically
3. **Separation of Concerns**: UI rendering separate from business actions
4. **Config-Driven Permissions**: Visibility controlled by permission metadata
5. **Performance-First**: Heavy caching, lazy-loading, virtualization
6. **Extensible**: Plugin hook points for custom components
7. **Testable**: Vitest + React Testing Library

### Tech Stack

- **React 18** + TypeScript
- **Vite** for build tooling
- **React Query** (TanStack Query) for server state
- **Zustand** for local engine state
- **React Hook Form** + **Zod** for forms and validation
- **dnd-kit** for drag & drop (ready to use)
- **Tailwind CSS** for styling
- **Headless UI / Radix** for accessible primitives
- **Vitest** for testing

## 📁 Project Structure

```
dynamicfrontend/
├── src/
│   ├── api/                    # API clients
│   ├── components/
│   │   ├── primitives/         # Atomic components (TextInput, etc.)
│   │   ├── form-controls/      # Form wrappers
│   │   ├── dynamic/            # Dynamic renderers
│   │   └── widgets/            # Reusable widgets
│   ├── engine/
│   │   ├── registry.ts         # Component registry
│   │   ├── metadataClient.ts   # Metadata fetching & caching
│   │   ├── renderer.tsx        # Form/Page renderers
│   │   └── permissions.ts      # Permission helpers
│   ├── hooks/                  # Custom React hooks
│   ├── pages/
│   │   ├── admin/              # Admin metadata editor
│   │   └── tenant/             # Tenant runtime pages
│   ├── styles/                 # Global styles
│   ├── utils/                  # Utility functions
│   ├── plugins/                # Plugin loader
│   ├── i18n/                   # Internationalization
│   ├── App.tsx                 # Main app component
│   └── main.tsx                # Entry point
├── public/                     # Static assets
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── tsconfig.json
```

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ 
- npm 10+

### Installation

```bash
# Navigate to the frontend directory
cd dynamicfrontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run tests
npm run test
```

### Development Server

The app will be available at `http://localhost:5173`

## 📖 Core Concepts

### 1. Component Registry

The component registry maps string names to React components:

```typescript
import componentRegistry from './engine/registry';

// Register a component
componentRegistry.register('CustomInput', MyCustomInput);

// Resolve a component
const Component = componentRegistry.resolve('CustomInput');
```

### 2. Metadata Client

Fetches and caches metadata from the backend:

```typescript
import { metadataClient } from './engine/metadataClient';

// Fetch form metadata
const formMeta = await metadataClient.fetchForm('customer.form.basic');

// Fetch list metadata
const listMeta = await metadataClient.fetchList('customer.list');
```

### 3. Form Renderer

Renders dynamic forms from metadata:

```typescript
<FormRenderer
  formMeta={metadata}
  onSuccess={(data) => console.log(data)}
  onError={(errors) => console.error(errors)}
/>
```

### 4. Metadata Schema Example

```json
{
  "id": "customer.form.basic",
  "entity": "Customer",
  "title": "Create Customer",
  "layout": { "type": "two-column" },
  "fields": [
    {
      "name": "customerName",
      "component": "Text",
      "label": "Customer Name",
      "required": true,
      "validation": { "minLength": 2, "maxLength": 100 }
    },
    {
      "name": "email",
      "component": "Text",
      "label": "Email",
      "required": true,
      "validation": { "pattern": "^[A-Za-z0-9+_.-]+@(.+)$" }
    }
  ],
  "actions": [
    {
      "id": "save",
      "label": "Save",
      "type": "submit",
      "permission": "entity:Customer:write"
    }
  ]
}
```

## 🎨 Primitive Components

All primitive components are registered automatically:

- **Text**: Single-line text input
- **TextArea**: Multi-line text input
- **Number**: Number input with min/max validation
- **Select**: Dropdown selection
- **Checkbox**: Boolean checkbox
- **Date**: Date picker

## 🔄 Workflow Integration

The system supports workflow-driven UIs:

```typescript
interface WorkflowMetadata {
  id: string;
  steps: Array<{
    id: string;
    type: 'input' | 'server';
    ui?: { component: string };
  }>;
}
```

## 🔐 Permissions

Permission checks are built into the renderer:

```typescript
// Actions with permissions
{
  "id": "delete",
  "label": "Delete",
  "type": "action",
  "permission": "entity:Customer:delete"
}
```

## 📊 Next Phases

### Phase 1 - Forms & Lists
- [ ] List/Table renderer with pagination
- [ ] Sorting and filtering
- [ ] Row actions
- [ ] Bulk operations

### Phase 2 - Workflows & Actions
- [ ] Workflow step UI
- [ ] Action buttons
- [ ] Job status tracking
- [ ] Async operation handling

### Phase 3 - Plugins & Widgets
- [ ] Plugin loader
- [ ] Widget registry
- [ ] Tenant-specific widgets
- [ ] Plugin sandboxing

### Phase 4 - Offline & Sync
- [ ] IndexedDB persistence
- [ ] Sync queue
- [ ] Offline indicators
- [ ] Conflict resolution

### Phase 5 - Admin UI
- [ ] Drag & drop field placement
- [ ] Metadata editor
- [ ] Preview mode
- [ ] Version management
- [ ] Pack import/export

### Phase 6 - Performance & Hardening
- [ ] React.lazy for components
- [ ] Virtual scrolling for lists
- [ ] Memoization optimization
- [ ] Error boundaries
- [ ] Security hardening

## 🧪 Testing

```bash
# Run unit tests
npm run test

# Run with UI
npm run test:ui

# Run in watch mode
npm run test -- --watch
```

## 🌐 Backend Integration

The frontend connects to the Spring Boot backend:

```typescript
// Configure backend URL
metadataClient.setHeaders('tenant-id', 'user-id');

// Backend endpoints expected:
// GET  /api/metadata/form/{id}
// GET  /api/metadata/list/{id}
// GET  /api/metadata/page/{id}
// GET  /api/metadata/workflow/{id}
// POST /api/{entity}/create
// POST /api/{entity}/update
// GET  /api/{entity}/find
```

## 🎯 Demo

The current demo shows a Customer creation form with:
- Dynamic field rendering
- Real-time validation
- Two-column layout
- Multiple field types

## 📝 License

Part of the EasyBilling project.

## 🤝 Contributing

This is Phase 0 of the implementation. Future phases will add more functionality based on the design document provided.

---

**Status**: Phase 0 Complete ✅  
**Next**: Phase 1 - Lists & Tables
