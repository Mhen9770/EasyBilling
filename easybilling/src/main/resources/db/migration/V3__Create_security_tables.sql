-- Create security_groups table
CREATE TABLE IF NOT EXISTS security_groups (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    tenant_id VARCHAR(36) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Create security_group_permissions table
CREATE TABLE IF NOT EXISTS security_group_permissions (
    security_group_id VARCHAR(36) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (security_group_id, permission),
    FOREIGN KEY (security_group_id) REFERENCES security_groups(id) ON DELETE CASCADE
);

-- Create user_security_groups table
CREATE TABLE IF NOT EXISTS user_security_groups (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    security_group_id VARCHAR(36) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    UNIQUE KEY unique_user_security_group (user_id, security_group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (security_group_id) REFERENCES security_groups(id) ON DELETE CASCADE
);

-- Create indexes for security groups
CREATE INDEX idx_security_group_tenant ON security_groups(tenant_id);
CREATE INDEX idx_security_group_name ON security_groups(name);
CREATE INDEX idx_security_group_status ON security_groups(is_active);

-- Create indexes for user security groups
CREATE INDEX idx_user_security_group_user ON user_security_groups(user_id);
CREATE INDEX idx_user_security_group_group ON user_security_groups(security_group_id);

-- Comments
-- security_groups: Groups that define sets of permissions for staff users
-- security_group_permissions: Permissions assigned to each security group
-- user_security_groups: Many-to-many relationship between users and security groups
