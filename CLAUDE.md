# AI Rules for {{project-name}}

{{project-description}}

## CODING_PRACTICES

### Guidelines for SUPPORT_LEVEL

#### SUPPORT_EXPERT

- Favor elegant, maintainable solutions over verbose code. Assume understanding of language idioms and design patterns.
- Highlight potential performance implications and optimization opportunities in suggested code.
- Frame solutions within broader architectural contexts and suggest design alternatives when appropriate.
- Focus comments on 'why' not 'what' - assume code readability through well-named functions and variables.
- Proactively address edge cases, race conditions, and security considerations without being prompted.
- When debugging, provide targeted diagnostic approaches rather than shotgun solutions.
- Suggest comprehensive testing strategies rather than just example tests, including considerations for mocking, test organization, and coverage.


### Guidelines for DOCUMENTATION

#### DOC_UPDATES

- Update relevant documentation in /docs when modifying features
- Keep README.md in sync with new capabilities
- Maintain changelog entries in CHANGELOG.md

#### SWAGGER

- Define comprehensive schemas for all request and response objects
- Use semantic versioning in API paths to maintain backward compatibility
- Implement detailed descriptions for endpoints, parameters, and {{domain_specific_concepts}}
- Configure security schemes to document authentication and authorization requirements
- Use tags to group related endpoints by resource or functional area
- Implement examples for all endpoints to facilitate easier integration by consumers


### Guidelines for VERSION_CONTROL

#### GIT

- Use conventional commits to create meaningful commit messages
- Use feature branches with descriptive names following {{branch_naming_convention}}
- Write meaningful commit messages that explain why changes were made, not just what
- Keep commits focused on single logical changes to facilitate code review and bisection
- Use interactive rebase to clean up history before merging feature branches
- Leverage git hooks to enforce code quality checks before commits and pushes

#### GITHUB

- Use pull request templates to standardize information provided for code reviews
- Implement branch protection rules for {{protected_branches}} to enforce quality checks
- Configure required status checks to prevent merging code that fails tests or linting
- Use GitHub Actions for CI/CD workflows to automate testing and deployment
- Implement CODEOWNERS files to automatically assign reviewers based on code paths
- Use GitHub Projects for tracking work items and connecting them to code changes

#### CONVENTIONAL_COMMITS

- Follow the format: type(scope): description for all commit messages
- Use consistent types (feat, fix, docs, style, refactor, test, chore) across the project
- Define clear scopes based on {{project_modules}} to indicate affected areas
- Include issue references in commit messages to link changes to requirements
- Use breaking change footer (!: or BREAKING CHANGE:) to clearly mark incompatible changes
- Configure commitlint to automatically enforce conventional commit format


### Guidelines for ARCHITECTURE

#### ADR

- Create ADRs in /docs/adr/{name}.md for:
- 1) Major dependency changes
- 2) Architectural pattern changes
- 3) New integration patterns
- 4) Database schema changes

#### CLEAN_ARCHITECTURE

- Strictly separate code into layers: entities, use cases, interfaces, and frameworks
- Ensure dependencies point inward, with inner layers having no knowledge of outer layers
- Implement domain entities that encapsulate {{business_rules}} without framework dependencies
- Use interfaces (ports) and implementations (adapters) to isolate external dependencies
- Create use cases that orchestrate entity interactions for specific business operations
- Implement mappers to transform data between layers to maintain separation of concerns

#### DDD

- Define bounded contexts to separate different parts of the domain with clear boundaries
- Implement ubiquitous language within each context to align code with business terminology
- Create rich domain models with behavior, not just data structures, for {{core_domain_entities}}
- Use value objects for concepts with no identity but defined by their attributes
- Implement domain events to communicate between bounded contexts
- Use aggregates to enforce consistency boundaries and transactional integrity

#### MONOREPO

- Configure workspace-aware tooling to optimize build and test processes
- Implement clear package boundaries with explicit dependencies between packages
- Use consistent versioning strategy across all packages (independent or lockstep)
- Configure CI/CD to build and test only affected packages for efficiency
- Implement shared configurations for linting, testing, and {{development_tooling}}
- Use code generators to maintain consistency across similar packages or modules


### Guidelines for STATIC_ANALYSIS

#### ESLINT

- Configure project-specific rules in eslint.config.js to enforce consistent coding standards
- Use shareable configs like eslint-config-airbnb or eslint-config-standard as a foundation
- Implement custom rules for {{project_specific_patterns}} to maintain codebase consistency
- Configure integration with Prettier to avoid rule conflicts for code formatting
- Use the --fix flag in CI/CD pipelines to automatically correct fixable issues
- Implement staged linting with husky and lint-staged to prevent committing non-compliant code

#### SONARQUBE

- Configure quality gates with appropriate thresholds for {{critical_metrics}}
- Set up branch analysis to track quality metrics across different development branches
- Implement custom quality profiles tailored to {{language_specific_requirements}}
- Configure security hotspot reviews as part of the development workflow
- Use SonarLint IDE integration to catch issues before they reach the repository
- Set up pull request decoration to provide feedback during code review process

## FRONTEND

### Guidelines for REACT

#### REACT_CODING_STANDARDS

- Use functional components with hooks instead of class components
- Implement React.memo() for expensive components that render often with the same props
- Utilize React.lazy() and Suspense for code-splitting and performance optimization
- Use the useCallback hook for event handlers passed to child components to prevent unnecessary re-renders
- Prefer useMemo for expensive calculations to avoid recomputation on every render
- Implement useId() for generating unique IDs for accessibility attributes
- Use the new use hook for data fetching in React 19+ projects
- Leverage Server Components for {{data_fetching_heavy_components}} when using React with Next.js or similar frameworks
- Consider using the new useOptimistic hook for optimistic UI updates in forms
- Use useTransition for non-urgent state updates to keep the UI responsive

#### REACT_ROUTER

- Use createBrowserRouter instead of BrowserRouter for better data loading and error handling
- Implement lazy loading with React.lazy() for route components to improve initial load time
- Use the useNavigate hook instead of the navigate component prop for programmatic navigation
- Leverage loader and action functions to handle data fetching and mutations at the route level
- Implement error boundaries with errorElement to gracefully handle routing and data errors
- Use relative paths with dot notation (e.g., "../parent") to maintain route hierarchy flexibility
- Utilize the useRouteLoaderData hook to access data from parent routes
- Implement fetchers for non-navigation data mutations
- Use route.lazy() for route-level code splitting with automatic loading states
- Implement shouldRevalidate functions to control when data revalidation happens after navigation

#### REACT_QUERY

- Use TanStack Query (formerly React Query) with appropriate staleTime and gcTime based on data freshness requirements
- Implement the useInfiniteQuery hook for pagination and infinite scrolling
- Use optimistic updates for mutations to make the UI feel more responsive
- Leverage queryClient.setQueryDefaults to establish consistent settings for query categories
- Use suspense mode with <Suspense> boundaries for a more declarative data fetching approach
- Implement retry logic with custom backoff algorithms for transient network issues
- Use the select option to transform and extract specific data from query results
- Implement mutations with onMutate, onError, and onSettled for robust error handling
- Use Query Keys structuring pattern ([entity, params]) for better organization and automatic refetching
- Implement query invalidation strategies to keep data fresh after mutations

#### ZUSTAND

- Create separate stores for distinct state domains instead of one large store
- Use immer middleware for complex state updates to maintain immutability when dealing with nested data
- Implement selectors to derive state and prevent unnecessary re-renders
- Leverage the persist middleware for automatic state persistence in localStorage or other storage
- Use TypeScript with strict typing for store definitions to catch errors at compile time
- Prefer shallow equality checks with useShallow for performance optimization in component re-renders
- Combine stores using composition for sharing logic between stores
- Implement subscriptions to react to state changes outside of React components
- Use devtools middleware for Redux DevTools integration in development
- Create custom hooks to encapsulate store access and related business logic


### Guidelines for STYLING

#### TAILWIND

- Use the @layer directive to organize styles into components, utilities, and base layers
- Implement Just-in-Time (JIT) mode for development efficiency and smaller CSS bundles
- Use arbitrary values with square brackets (e.g., w-[123px]) for precise one-off designs
- Leverage the @apply directive in component classes to reuse utility combinations
- Implement the Tailwind configuration file for customizing theme, plugins, and variants
- Use component extraction for repeated UI patterns instead of copying utility classes
- Leverage the theme() function in CSS for accessing Tailwind theme values
- Implement dark mode with the dark: variant
- Use responsive variants (sm:, md:, lg:, etc.) for adaptive designs
- Leverage state variants (hover:, focus:, active:, etc.) for interactive elements


## BACKEND

### Guidelines for JAVA

#### SPRING_BOOT

- Use Spring Boot for simplified configuration and rapid development with sensible defaults
- Prefer constructor-based dependency injection over `@Autowired`
- Avoid hardcoding values that may change externally, use configuration parameters instead
- For complex logic, use Spring profiles and configuration parameters to control which beans are injected instead of hardcoded conditionals
- If a well-known library simplifies the solution, suggest using it instead of generating a custom implementation
- Use DTOs as immutable `record` types
- Use Bean Validation annotations (e.g., `@Size`, `@Email`, etc.) instead of manual validation logic
- Use `@Valid` on request parameters annotated with `@RequestBody`
- Use custom exceptions for business-related scenarios
- Centralize exception handling with `@ControllerAdvice` and return a consistent error DTO: `{{error_dto}}`
- REST controllers should handle only routing and I/O mapping, not business logic
- Use SLF4J for logging instead of `System.out.println`
- Prefer using lambdas and streams over imperative loops and conditionals where appropriate
- Use `Optional` to avoid `NullPointerException`

#### SPRING_DATA_JPA

- Define repositories as interfaces extending `JpaRepository` or `CrudRepository`
- Never expose JPA entities in API responses – always map them to DTOs
- Use `@Transactional` at the service layer for state-changing methods, and keep transactions as short as possible
- Use `@Transactional(readOnly = true)` for read-only operations
- Use `@EntityGraph` or fetch joins to avoid the N+1 select problem
- Use `@Query` for complex queries
- Use projections (DTOs) in multi-join queries with `@Query`
- Use Specifications for dynamic filtering
- Use pagination when working with large datasets
- Use `@Version` for optimistic locking in concurrent updates
- Avoid `CascadeType.REMOVE` on large entity relationships
- Use HikariCP for efficient connection pooling

#### LOMBOK

- Use Lombok where it clearly simplifies the code
- Use constructor injection with `@RequiredArgsConstructor`
- Prefer Java `record` over Lombok’s `@Value` when applicable
- Avoid using `@Data` in non-DTO classes, instead, use specific annotations like `@Getter`, `@Setter`, and `@ToString`
- Apply Lombok annotations to fields rather than the class if only some fields require them
- Use Lombok’s `@Slf4j` to generate loggers

## DATABASE

### Guidelines for SQL

#### POSTGRES

- Use connection pooling to manage database connections efficiently
- Implement JSONB columns for semi-structured data instead of creating many tables for {{flexible_data}}
- Use materialized views for complex, frequently accessed read-only data

## DEVOPS

### Guidelines for CI_CD

#### GITHUB_ACTIONS

- Check if `package.json` exists in project root and summarize key scripts
- Check if `.nvmrc` exists in project root
- Check if `.env.example` exists in project root to identify key `env:` variables
- Always use terminal command: `git branch -a | cat` to verify whether we use `main` or `master` branch
- Always use `env:` variables and secrets attached to jobs instead of global workflows
- Always use `npm ci` for Node-based dependency setup
- Extract common steps into composite actions in separate files
- Once you're done, as a final step conduct the following: for each public action always use <tool>"Run Terminal"</tool> to see what is the most up-to-date version (use only major version) - extract tag_name from the response:
- ```bash curl -s https://api.github.com/repos/{owner}/{repo}/releases/latest ```


### Guidelines for CONTAINERIZATION

#### DOCKER

- Use multi-stage builds to create smaller production images
- Implement layer caching strategies to speed up builds for {{dependency_types}}
- Use non-root users in containers for better security

## TESTING

### Guidelines for E2E

#### PLAYWRIGHT

- Initialize configuration only with Chromium/Desktop Chrome browser
- Use browser contexts for isolating test environments
- Implement the Page Object Model for maintainable tests
- Use locators for resilient element selection
- Leverage API testing for backend validation
- Implement visual comparison with expect(page).toHaveScreenshot()
- Use the codegen tool for test recording
- Leverage trace viewer for debugging test failures
- Implement test hooks for setup and teardown
- Use expect assertions with specific matchers
- Leverage parallel execution for faster test runs

#### SELENIUM

- Use the page object model to organize test code
- Implement explicit waits instead of implicit waits for {{dynamic_elements}}
- Use the WebDriverWait class for waiting for elements to be ready


#### UI

- UI should by default use Polish language