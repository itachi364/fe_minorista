CREATE TABLE user_account (
    id UUID PRIMARY KEY,
    email VARCHAR(180) NOT NULL,
    full_name VARCHAR(180) NOT NULL,
    password_hash VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_identity_user_account_email UNIQUE (email),
    CONSTRAINT chk_identity_user_account_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE company_membership (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES user_account(id),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_identity_company_membership UNIQUE (company_id, user_id)
);

CREATE TABLE company_membership_role (
    membership_id UUID NOT NULL REFERENCES company_membership(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL,
    PRIMARY KEY (membership_id, role),
    CONSTRAINT chk_identity_membership_role CHECK (role IN ('OWNER', 'ADMIN', 'CASHIER', 'ACCOUNTANT', 'AUDITOR'))
);

CREATE TABLE user_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_account(id),
    token_hash VARCHAR(120) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_identity_user_session_token_hash UNIQUE (token_hash)
);

CREATE TABLE identity_access_audit (
    id UUID PRIMARY KEY,
    company_id UUID,
    user_id UUID,
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(120),
    result VARCHAR(40) NOT NULL,
    detail VARCHAR(500),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_identity_access_audit_result CHECK (result IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_identity_membership_user
    ON company_membership (user_id, active);

CREATE INDEX idx_identity_membership_company
    ON company_membership (company_id, active);

CREATE INDEX idx_identity_session_user
    ON user_session (user_id, expires_at DESC);

CREATE INDEX idx_identity_access_audit_company_at
    ON identity_access_audit (company_id, occurred_at DESC);

CREATE INDEX idx_identity_access_audit_user_at
    ON identity_access_audit (user_id, occurred_at DESC);
