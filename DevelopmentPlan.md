Use this ENTIRE document as a single prompt. Develop in parts but keep the proper documentation for future reference as your are only going to build this completely 
You are NOT allowed to ignore or simplify requirements.
You must design and generate production-grade architecture, code, and documentation.

give your Best


---

1. ROLE & OBJECTIVE FOR THE AI

You are a Principal Solution Architect + Senior Full Stack Engineer.
Your goal is to design and help implement a large-scale, multi-tenant Billing Platform that can serve all kinds of shops and showrooms (grocery, garments, electronics, jewellery, furniture, pharmacies, etc.).

Tech stack must use latest stable versions at execution time:

Backend:

Java 21

Spring Boot 3.4.*

Spring Security 6+, Spring Data JPA, Spring Cloud (latest compatible)

Prefer Gradle (Kotlin DSL) but Maven is acceptable if justified


Frontend:

Next.js (latest) with App Router

React (latest), TypeScript (strict mode), Server Actions, RSC where suitable

Tailwind CSS, shadcn/ui, TanStack Query (React Query), Zustand/Jotai/Recoil for state


Databases:

Primary RDBMS: MySQL 8 (PostgreSQL support can be added later if needed)

Caching: Redis

Search/analytics (optional but design-ready): OpenSearch / Elasticsearch


Infra & DevOps (design only, code if asked):

Docker, Docker Compose

Kubernetes-ready manifests or Helm charts (optional but designable)

CI/CD (GitHub Actions / GitLab CI example)



You must produce clean, scalable, enterprise-grade architecture.
All generated code must be well-structured, commented, and follow SOLID, Clean Architecture, and DDD principles.


---

2. HIGH LEVEL PRODUCT VISION

Build a unified Billing SaaS that can onboard any kind of shop/showroom with minimal configuration:

Support single shop, multi-branch, franchise, and enterprise chains

Multi-tenant: one platform instance, many merchants (tenants)

POS + Backoffice + Admin in one ecosystem:

POS (billing counter)

Inventory & Product Catalog

Customers, Suppliers, and Staff

Purchases & GRN

Sales, Returns, Exchanges

Offers, Discounts, Coupons, Loyalty

GST/tax rules, multi-tax support

Reports, dashboards, data export

Integrations: SMS/Email/WhatsApp, payment gateways, UPI, PDF/print


Support multiple device form factors: desktop, tablet, large POS screens (responsive UI)



---

3. ARCHITECTURE STYLE

3.1. Overall Architecture

Microservices-based architecture

Some services are multi-tenant (data & behavior per merchant)

Some services are global / single-tenant (shared infra, configuration)


Use a modular monolith approach within each service, but the overall system is microservices so that later we can scale and deploy independently.

3.2. Services Classification

Multi-tenant Services (per merchant/shop)

1. Billing & POS Service

Invoice creation, returns, exchanges

Multiple payment modes per bill (cash, UPI, card, wallet, credit)

Hold/Resume bill, park bill, reprint

Barcode scanning & on-screen search

Multiple counters / terminals per store



2. Inventory & Product Catalog Service

Products, variants (size, color, attributes)

Stock, stock movements, stock adjustments

Purchase, GRN, purchase returns

Batching & expiry (for medical/pharma, food)



3. Customer & Loyalty Service

Customer master, segmentation

Loyalty points, membership tiers, wallet

Customer-specific pricing/discount rules



4. Supplier & Purchase Service

Supplier master

Purchase orders, purchase invoices, returns



5. Pricing, Discount & Offer Engine

Rule-based discounts (flat %, buy X get Y, bundle offers, happy hours)

Customer-specific & category-specific pricing

Priority and conflict resolution between offers



6. Tax & Compliance Service

GST/VAT configuration per tenant

HSN/SAC mapping

Output/input tax and summary views for filings



7. Reporting & Analytics Service

Sales reports (daily/weekly/monthly)

Product, category, counter, staff performance

Inventory ageing, slow/fast movers

Customer loyalty, visit frequency, etc.



8. Notification Service (Tenant-aware)

SMS, Email, WhatsApp templates per tenant

Transactional & promotional messages



9. Document & Template Service

Invoice templates (print format, thermal vs A4)

Email/SMS templates

Branding (logo, colors, header/footer)




Global / Single-tenant (Normal) Services

1. Auth & IAM Service

Master user identities, roles, permissions

Support for:

System Super Admins

Tenant Admins (merchant owners)

Store Managers

Cashiers, Backoffice, etc.


JWT/OAuth2 based authentication

Password, OTP, MFA options



2. Tenant & Subscription Management Service

Tenant onboarding, lifecycle (trial, active, suspended, cancelled)

Subscription plans (basic, pro, enterprise)

Feature flags per plan

Billing of tenants (meta-billing of merchants)



3. Configuration & Master Data Service

Global country/currency/timezone lists

Global tax regimes, templates



4. Integration Gateway Service

Payment gateway adapters

SMS/Email/WhatsApp providers

External accounting systems integration (future)



5. Logging, Audit & Activity Service

Centralized audit logs

User activity, security events



6. API Gateway & Edge Security

Single entry point for web/mobile clients

Rate limiting, tenant resolution from domain/subdomain/header





---

4. MULTI-TENANCY STRATEGY

Design a pluggable multi-tenancy model:

Support:

1. Schema-per-tenant (recommended default)


2. Database-per-tenant (for very large tenants)


3. Shared schema with tenant_id column (for small instances)




Requirements:

Tenant resolution mechanism (in API Gateway / filter layer):

From subdomain (tenantX.app.com)

From request header (X-Tenant-Id)

From authenticated user’s context


Spring Boot multi-tenancy:

Use Hibernate multi-tenancy with dynamic datasource routing

Provide TenantContext (ThreadLocal or reactive context-safe) propagated through layers


All multi-tenant services MUST:

Never access data without tenant resolution

Be compatible with Flyway/Liquibase for per-tenant schema migrations




---

5. NON-FUNCTIONAL REQUIREMENTS

1. Security

Spring Security with JWT/OAuth2

Role-based & permission-based authorization (RBAC + fine-grained permissions)

CSRF protection (where relevant), input validation

Data isolation between tenants



2. Scalability & Reliability

Stateless services where possible

Horizontal scaling via containers

FUTUR -> Async communication via message broker (Kafka/RabbitMQ) for events:

invoice.created, stock.updated, tenant.created, etc.




3. Performance

Caching (Redis) for hot data: product catalog, offers, config, tenant metadata

Efficient queries with indexes

Use pagination & sensible limits (Must)



4. Observability

Centralized logging (structured logs)

Metrics & health checks (Actuator)

Tracing (OpenTelemetry)



5. Extensibility

Clean module boundaries (DDD: domain, application, infrastructure)

Clear interfaces for integrations

Feature flagging system for enabling/disabling modules per tenant/plan



6. Localization

Multi-language support in UI (i18n)

Date, time, currency formatting per tenant

For now build for English and keep the configuration for others.



---

6. BACKEND DESIGN – SPRING BOOT (JAVA 21)

6.1. Common Backend Guidelines

Use Java 21 features where appropriate:

Records for DTOs, pattern matching, sealed interfaces where helpful


Enforce DTO ↔ Entity mapping via MapStruct or explicit mappers

Validation:

Jakarta Validation annotations

Custom validators (e.g., tax rules, offer overlaps)


Error handling:

Central @ControllerAdvice with standard error envelope
BusinessException with RuntimeException



6.2. Example Service Internal Architecture

For each microservice, use a similar folder structure (can be adapted):
Each Service will be multimodule(2)
1 api (Feign Client, Request, Response)
2 service( Controller implement that feign Client, and othe things)

example API and Service
src/main/java/com/company/billing/<service>/

for other service this service can add dependency of api only and call the api easily.
manage proper versining.
  

6.3. Communication Between Services

Sync: REST (Spring Web + OpenAPI/Swagger documentation)

Async: Kafka/RabbitMQ for events (outbox pattern recommended)

Use API Gateway as single entry point for external clients

Consider Spring Cloud Gateway for routing, auth, and tenant resolution

inter service Communication should be using FeaignClient.

---

7. FRONTEND DESIGN – NEXT.JS (LATEST)

7.1. General Guidelines

Use Next.js App Router with:

Server Components for data-heavy pages

Client Components where interactivity is needed (POS screens, forms)

Server Actions for mutations where appropriate


TypeScript strict mode, with clear types for all API responses and DTOs

Styling:

Tailwind CSS with a design system
use the best colors and animation to beat the market.
also keeping UI to be User Friendly.
shadcn/ui for consistent components


State & Data:

TanStack Query for server data fetching/caching

Zustand/Jotai/Recoil for global client state (sessions, POS cart, UI state)



7.2. Main Frontend Apps

We need at least these frontends (can be in a single Next.js project with role-based UIs):

1. Super Admin Portal

Manage tenants, plans, features, global settings

View platform usage, logs, metrics summaries



2. Tenant Admin / Backoffice Portal

Manage stores/branches, users, roles

Manage products, inventory, pricing, offers

View reports & analytics



3. POS (Billing) UI

Highly optimized for keyboard input & barcode scanning

Cart view, quick item search, hold/resume, multiple payment modes

Light/Dark themes for better visibility



4. Setup Wizard for New Tenants

Step-by-step onboarding:

1. Business details


2. Tax configuration


3. First store & user setup


4. Basic product import or manual creation






7.3. Frontend Structure (Example) just an example real can be vary

app/
  (superadmin)/
    tenants/
    plans/
    monitoring/
  (tenant)/
    dashboard/
    products/
    inventory/
    sales/
    reports/
    settings/
  (pos)/
    [storeId]/
      [counterId]/
        page.tsx   # main POS screen
  api/    # Next.js route handlers if BFF pattern is used
lib/
  api/
  auth/
  hooks/
  store/
  utils/
components/
  ui/    # design system
  layout/
  charts/
  forms/


---

8. CORE FEATURES LIST (FUNCTIONAL REQUIREMENTS)

The AI must cover at least these modules in detail:

1. Tenant Lifecycle

Sign-up, trial, payment & subscription, cancellation

Plan selection, upgrade/downgrade
we need to handle schema Creation and table DDL Execution at runtime


2. User & Role Management

User CRUD

Role & permission configuration per tenant

Store assignment to users



3. Product & Inventory

Hierarchy: Category → Subcategory → Product → Variant

Attributes: size, color, brand, season, etc.

Barcode management (auto-generate or store-provided)

Stock tracking per store, stock adjustments, transfers



4. Billing & POS

Create invoice (scan or search items)

Apply discounts (item-level & bill-level)

Multiple payment modes

Returns & exchanges with configurable rules

Reprint, duplicate copy, email/SMS/WhatsApp invoice



5. Customer & Supplier Management

Basic CRM (name, phone, GSTIN, address)

Supplier catalog & purchase history



6. Offers & Promotions

Configurable offer engine with validity dates/times

Offer stacking rules & priority



7. Tax & Compliance

Tax configuration per product/category

GST/VAT calculation in invoices

Summary reports for returns filing



8. Reporting

Sales summary, day end reports (X/Z reports)

Profit & loss (basic)

Inventory ageing & valuation

User/counter wise sales



9. Notifications

Invoice SMS/Email/WhatsApp

Low stock alerts

Subscription renewals & payment reminders



10. Export & Integration

CSV/Excel export for key reports

JSON-based APIs for external integrations





---

9. SECURITY & ACCESS CONTROL

Implement at least:

SUPER_ADMIN, TENANT_ADMIN, STAFF
Super Admin eill have all the access to manage the platform.
Tenant Admin have all the acess to manage tenant specific things.

STAFF is a Role and Their Permissions will be configured for individual user to manage the Security with Groups.


Permissions must be configurable per tenant, not hard-coded

JWT tokens include:

sub (user id), tenantId, roles, and optional storeId


All APIs must:

Validate JWT

Resolve tenant from token or gateway

Enforce authorization based on role/permission matrics

---

11. WHAT YOU (THE AI) MUST OUTPUT WHEN ASKED

When the user asks you to start implementing, you should:

1. Propose/confirm the final tech stack versions (Spring Boot, Next.js, etc.).


2. Generate:

Root architecture diagram explanation (textual)

Service list with responsibilities

Repo structure (multi-repo vs mono-repo with multiple modules) recommendation



3. Create:

README.md with setup instructions

Per-service Maven config

Example entities, DTOs, controllers, services for at least one core service (e.g., Tenant Service, Billing Service)

Basic Next.js layout, routing, and first pages (e.g., login, tenant dashboard, POS shell)



4. Always:

Explain important design decisions

Keep code production-ready, not just demo-level

Avoid hardcoded magic strings; use config and constants





---

12. STYLE & QUALITY RULES

Use consistent naming conventions and clear package/module names

Add TODO comments where something is intentionally left as placeholder

All backend code:

Use @Validated, @NotNull, etc., to enforce input correctness


All frontend code:

Avoid any any types in TypeScript

Use custom hooks for reusable logic

Separate UI components from business logic


