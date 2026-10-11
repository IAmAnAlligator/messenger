CREATE TABLE refresh_tokens
(
    id UUID PRIMARY KEY,

    session_id UUID NOT NULL,

    user_id UUID NOT NULL,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,

    revoked_at TIMESTAMPTZ,

    replaced_by_token_id UUID,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_token_id)
        REFERENCES refresh_tokens (id),

    CONSTRAINT chk_refresh_tokens_expiration
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_session_id
    ON refresh_tokens (session_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);