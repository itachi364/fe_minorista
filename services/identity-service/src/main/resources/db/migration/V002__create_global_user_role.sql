CREATE TABLE global_user_role (
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    role_code VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_code),
    CONSTRAINT chk_identity_global_user_role_code CHECK (role_code IN ('ROOT'))
);

CREATE INDEX idx_identity_global_user_role_user
    ON global_user_role (user_id);