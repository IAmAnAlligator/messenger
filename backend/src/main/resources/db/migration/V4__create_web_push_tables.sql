CREATE TABLE web_push_subscriptions
(
    id           UUID         PRIMARY KEY,
    user_id      UUID         NOT NULL,
    endpoint     TEXT         NOT NULL,
    p256dh       VARCHAR(255) NOT NULL,
    auth         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    last_used_at TIMESTAMPTZ,

    CONSTRAINT fk_web_push_subscriptions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_web_push_subscriptions_endpoint
        UNIQUE (endpoint)
);

CREATE INDEX idx_web_push_subscriptions_user_id
    ON web_push_subscriptions (user_id);


CREATE TABLE user_notification_settings
(
    user_id          UUID        PRIMARY KEY,
    web_push_enabled BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_notification_settings_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);