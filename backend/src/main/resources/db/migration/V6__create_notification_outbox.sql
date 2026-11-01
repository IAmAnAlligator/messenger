CREATE TABLE notification_outbox
(
    id                UUID PRIMARY KEY,

    event_id          UUID         NOT NULL,
    message_id        UUID         NOT NULL,
    chat_id           UUID         NOT NULL,
    recipient_user_id UUID         NOT NULL,
    sender_user_id    UUID         NOT NULL,
    sender_username   VARCHAR(255) NOT NULL,

    status            VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    attempts          INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    locked_until      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at           TIMESTAMPTZ,
    last_error        TEXT,

    CONSTRAINT uq_notification_outbox_event_recipient
        UNIQUE (event_id, recipient_user_id),

    CONSTRAINT chk_notification_outbox_status
        CHECK (status IN (
            'NEW',
            'PROCESSING',
            'SENT',
            'SKIPPED',
            'RETRY',
            'DEAD'
        )),

    CONSTRAINT chk_notification_outbox_attempts
        CHECK (attempts >= 0)
);

CREATE INDEX idx_notification_outbox_pending
    ON notification_outbox (next_attempt_at, created_at)
    WHERE status IN ('NEW', 'RETRY');

CREATE INDEX idx_notification_outbox_expired_lock
    ON notification_outbox (locked_until)
    WHERE status = 'PROCESSING';