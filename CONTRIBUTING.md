# Contributing to EasyBilling

Thank you for your interest in contributing to EasyBilling! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

We are committed to providing a welcoming and inclusive experience for everyone. Please be respectful and constructive in all interactions.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/EasyBilling.git
   cd EasyBilling
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/Mhen9770/EasyBilling.git
   ```
4. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Workflow

### Backend Development

1. **Navigate to backend directory**:
   ```bash
   cd backend
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Run tests**:
   ```bash
   ./gradlew test
   ```

4. **Run a specific service**:
   ```bash
   ./gradlew :services:tenant-service:bootRun
   ```

5. **Format code** (if formatter is configured):
   ```bash
   ./gradlew spotlessApply
   ```

### Frontend Development

1. **Navigate to frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Run development server**:
   ```bash
   npm run dev
   ```

4. **Build for production**:
   ```bash
   npm run build
   ```

5. **Lint code**:
   ```bash
   npm run lint
   ```

## Coding Standards

### Java/Backend

- **Java Version**: Java 17
- **Code Style**: Follow standard Java conventions
- **Formatting**: Use 4 spaces for indentation
- **Naming Conventions**:
  - Classes: PascalCase (e.g., `TenantService`)
  - Methods: camelCase (e.g., `createTenant`)
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
  - Packages: lowercase (e.g., `com.easybilling.tenant`)

#### Best Practices

- Use `@Slf4j` for logging
- Validate inputs with Jakarta Validation annotations
- Use DTOs for API requests/responses
- Keep business logic in service layer
- Use MapStruct for DTO-Entity mapping
- Write meaningful log messages
- Handle exceptions appropriately

#### Example Service Structure

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService {
    
    private final ExampleRepository repository;
    private final ExampleMapper mapper;
    
    @Transactional
    public ExampleResponse createExample(ExampleRequest request) {
        log.info("Creating example: {}", request);
        
        // Validate
        validateRequest(request);
        
        // Business logic
        Example entity = mapper.toEntity(request);
        entity = repository.save(entity);
        
        log.info("Example created: {}", entity.getId());
        return mapper.toResponse(entity);
    }
}
```

### TypeScript/Frontend

- **TypeScript**: Strict mode enabled
- **Code Style**: Follow Airbnb style guide
- **Formatting**: Use Prettier (2 spaces)
- **Naming Conventions**:
  - Components: PascalCase (e.g., `TenantList`)
  - Functions: camelCase (e.g., `fetchTenants`)
  - Constants: UPPER_SNAKE_CASE
  - Files: kebab-case (e.g., `tenant-list.tsx`)

#### Best Practices

- Use TypeScript types (avoid `any`)
- Use functional components with hooks
- Extract reusable logic into custom hooks
- Keep components small and focused
- Use TanStack Query for server state
- Use Zustand for client state
- Handle loading and error states

#### Example Component Structure

```typescript
'use client';

interface TenantListProps {
  searchQuery?: string;
}

export function TenantList({ searchQuery }: TenantListProps) {
  const { data, isLoading, error } = useTenants(searchQuery);
  
  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage error={error} />;
  
  return (
    <div className="space-y-4">
      {data?.map(tenant => (
        <TenantCard key={tenant.id} tenant={tenant} />
      ))}
    </div>
  );
}
```

### Database

- **Migrations**: Use Flyway with versioned SQL files
- **Naming**: 
  - Tables: plural, snake_case (e.g., `tenants`)
  - Columns: snake_case (e.g., `created_at`)
  - Indexes: `idx_{table}_{column}` (e.g., `idx_tenants_slug`)

## Testing Guidelines

### Backend Tests

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test API endpoints end-to-end
- **Test Naming**: `methodName_scenario_expectedBehavior`

```java
@Test
void createTenant_validRequest_returnsTenant() {
    // Arrange
    TenantRequest request = createValidRequest();
    
    // Act
    TenantResponse response = tenantService.createTenant(request);
    
    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
}

@Test
void createTenant_duplicateSlug_throwsBusinessException() {
    // Arrange
    TenantRequest request = createRequestWithExistingSlug();
    
    // Act & Assert
    assertThatThrownBy(() -> tenantService.createTenant(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("slug already exists");
}
```

### Frontend Tests

- **Unit Tests**: Test individual functions and components
- **Integration Tests**: Test user flows

```typescript
describe('TenantList', () => {
  it('renders tenant list correctly', () => {
    const tenants = [createMockTenant()];
    render(<TenantList tenants={tenants} />);
    
    expect(screen.getByText(tenants[0].name)).toBeInTheDocument();
  });
  
  it('shows loading state while fetching', () => {
    render(<TenantList />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});
```

## Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```
feat(tenant): add tenant suspension endpoint

Add ability to suspend tenants with optional reason.
Updates tenant status and sends notification.

Closes #123
```

```
fix(billing): correct tax calculation for discounted items

Tax was being calculated on original price instead of 
discounted price, causing overcharge.

Fixes #456
```

## Pull Request Process

1. **Update your branch** with latest changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run all tests** and ensure they pass:
   ```bash
   # Backend
   cd backend && ./gradlew test
   
   # Frontend
   cd frontend && npm test
   ```

3. **Build the project** successfully:
   ```bash
   # Backend
   cd backend && ./gradlew build
   
   # Frontend
   cd frontend && npm run build
   ```

4. **Push your changes**:
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Create Pull Request** on GitHub with:
   - Clear title and description
   - Reference related issues
   - Screenshots (for UI changes)
   - Test results

6. **Code Review**:
   - Address review comments
   - Make requested changes
   - Push updates to same branch

7. **Merge**:
   - Squash commits if needed
   - Ensure CI passes
   - Wait for maintainer approval

## Additional Guidelines

### Documentation

- Update README.md if adding features
- Add JSDoc/JavaDoc for public APIs
- Update API documentation (Swagger)
- Add examples for complex functionality

### Security

- Never commit secrets or credentials
- Use environment variables for config
- Validate all user inputs
- Follow OWASP security guidelines

### Performance

- Optimize database queries
- Use caching where appropriate
- Minimize API calls
- Implement pagination for large datasets

### Accessibility

- Use semantic HTML
- Provide alt text for images
- Ensure keyboard navigation
- Test with screen readers

## Questions?

If you have questions or need help:

1. Check existing documentation
2. Search closed issues
3. Ask in GitHub Discussions
4. Open a new issue

Thank you for contributing to EasyBilling! 🎉
