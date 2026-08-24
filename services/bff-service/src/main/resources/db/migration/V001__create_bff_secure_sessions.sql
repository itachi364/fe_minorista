CREATE TABLE IF NOT EXISTS secure_sessions (
    id VARCHAR(128) PRIMARY KEY,
    session_type VARCHAR(32) NOT NULL,
    nonce BYTEA NOT NULL,
    encrypted_payload BYTEA NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_secure_sessions_type_expires
    ON secure_sessions (session_type, expires_at);
