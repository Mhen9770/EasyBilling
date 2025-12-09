rootProject.name = "easybilling-backend"

// Shared Libraries
include("libs:common")
include("libs:security")
include("libs:multi-tenancy")

// Microservices
// Only include services that have been implemented
include("services:tenant-service")
include("services:auth-service")
include("services:gateway-service")
// include("services:billing-service")
// include("services:inventory-service")
// include("services:customer-service")
// include("services:pricing-service")
// include("services:tax-service")
// include("services:reporting-service")
// include("services:notification-service")

// Configure project paths
project(":libs:common").projectDir = file("libs/common")
project(":libs:security").projectDir = file("libs/security")
project(":libs:multi-tenancy").projectDir = file("libs/multi-tenancy")

project(":services:tenant-service").projectDir = file("services/tenant-service")
project(":services:auth-service").projectDir = file("services/auth-service")
project(":services:gateway-service").projectDir = file("services/gateway-service")
// project(":services:billing-service").projectDir = file("services/billing-service")
// project(":services:inventory-service").projectDir = file("services/inventory-service")
// project(":services:customer-service").projectDir = file("services/customer-service")
// project(":services:pricing-service").projectDir = file("services/pricing-service")
// project(":services:tax-service").projectDir = file("services/tax-service")
// project(":services:reporting-service").projectDir = file("services/reporting-service")
// project(":services:notification-service").projectDir = file("services/notification-service")
