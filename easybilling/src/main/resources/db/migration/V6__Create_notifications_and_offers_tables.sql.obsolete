-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_message TEXT,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notification_tenant (tenant_id),
    INDEX idx_notification_status (status),
    INDEX idx_notification_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Offers table
CREATE TABLE IF NOT EXISTS offers (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    offer_type VARCHAR(50) NOT NULL,
    discount_type VARCHAR(50) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    minimum_purchase_amount DECIMAL(10, 2),
    maximum_discount_amount DECIMAL(10, 2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    applicable_to_all_products BOOLEAN DEFAULT FALSE,
    applicable_to_all_categories BOOLEAN DEFAULT FALSE,
    max_uses_per_customer INT,
    total_max_uses INT,
    current_uses INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_offer_tenant (tenant_id),
    INDEX idx_offer_status (status),
    INDEX idx_offer_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Offer-Product mapping table (for specific products)
CREATE TABLE IF NOT EXISTS offer_products (
    offer_id VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (offer_id, product_id),
    FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Offer-Category mapping table (for specific categories)
CREATE TABLE IF NOT EXISTS offer_categories (
    offer_id VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (offer_id, category_id),
    FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

