package com.easybilling.enums;

/**
 * System permissions for granular access control.
 * These permissions are assigned to security groups.
 */
public enum Permission {
    // User Management
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    USER_LIST,
    
    // Product Management
    PRODUCT_CREATE,
    PRODUCT_READ,
    PRODUCT_UPDATE,
    PRODUCT_DELETE,
    PRODUCT_LIST,
    
    // Inventory Management
    INVENTORY_CREATE,
    INVENTORY_READ,
    INVENTORY_UPDATE,
    INVENTORY_DELETE,
    INVENTORY_LIST,
    STOCK_ADJUSTMENT,
    
    // Customer Management
    CUSTOMER_CREATE,
    CUSTOMER_READ,
    CUSTOMER_UPDATE,
    CUSTOMER_DELETE,
    CUSTOMER_LIST,
    
    // Invoice/Billing Management
    INVOICE_CREATE,
    INVOICE_READ,
    INVOICE_UPDATE,
    INVOICE_DELETE,
    INVOICE_LIST,
    INVOICE_VOID,
    
    // Payment Management
    PAYMENT_CREATE,
    PAYMENT_READ,
    PAYMENT_LIST,
    PAYMENT_REFUND,
    
    // Reports
    REPORT_SALES,
    REPORT_INVENTORY,
    REPORT_CUSTOMER,
    REPORT_FINANCIAL,
    
    // Offers & Promotions
    OFFER_CREATE,
    OFFER_READ,
    OFFER_UPDATE,
    OFFER_DELETE,
    OFFER_LIST,
    
    // Supplier Management
    SUPPLIER_CREATE,
    SUPPLIER_READ,
    SUPPLIER_UPDATE,
    SUPPLIER_DELETE,
    SUPPLIER_LIST,
    
    // Notifications
    NOTIFICATION_CREATE,
    NOTIFICATION_READ,
    NOTIFICATION_DELETE,
    
    // Settings & Configuration
    SETTINGS_VIEW,
    SETTINGS_UPDATE,
    
    // Security Group Management (Admin only)
    SECURITY_GROUP_CREATE,
    SECURITY_GROUP_READ,
    SECURITY_GROUP_UPDATE,
    SECURITY_GROUP_DELETE,
    SECURITY_GROUP_LIST
}
