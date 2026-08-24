ALTER TABLE user_account
    ADD COLUMN IF NOT EXISTS cognito_subject VARCHAR(120);

CREATE UNIQUE INDEX IF NOT EXISTS uk_identity_user_account_cognito_subject
    ON user_account (cognito_subject)
    WHERE cognito_subject IS NOT NULL;
