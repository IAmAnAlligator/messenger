CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL
);

-- Username больше не является уникальным.
-- Пользователи могут иметь одинаковые username.
CREATE INDEX idx_username
    ON users(username);

-- Email является уникальным без учёта регистра.
CREATE UNIQUE INDEX uk_users_email_lower
    ON users (LOWER(email));


CREATE TABLE chats
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    last_message_at TIMESTAMPTZ,
    private_key     VARCHAR(39) UNIQUE,
    version         BIGINT
);


CREATE TABLE chat_members
(
    id                  BIGSERIAL PRIMARY KEY,
    chat_id             BIGINT      NOT NULL,
    user_id             BIGINT      NOT NULL,
    role                VARCHAR(20) NOT NULL,
    joined_at           TIMESTAMPTZ NOT NULL,
    last_read_message_id BIGINT,

    CONSTRAINT fk_chat_members_chat
        FOREIGN KEY (chat_id)
            REFERENCES chats (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_chat_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT uk_chat_members_chat_user
        UNIQUE (chat_id, user_id)
);

CREATE INDEX idx_chat_members_chat_id
    ON chat_members(chat_id);

CREATE INDEX idx_chat_members_user_id
    ON chat_members(user_id);


CREATE TABLE messages
(
    id                  BIGSERIAL PRIMARY KEY,
    chat_id             BIGINT       NOT NULL,
    sender_id           BIGINT       NOT NULL,
    content             VARCHAR(2000),
    created_at          TIMESTAMPTZ  NOT NULL,
    type                VARCHAR(20)  NOT NULL,
    file_attachment_id  UUID,

    CONSTRAINT fk_messages_chat
        FOREIGN KEY (chat_id)
            REFERENCES chats (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id)
            REFERENCES users (id),

    CONSTRAINT chk_messages_type
        CHECK (type IN ('TEXT', 'FILE')),

    CONSTRAINT chk_messages_type_content
        CHECK (
            (
                type = 'TEXT'
                AND content IS NOT NULL
                AND file_attachment_id IS NULL
            )
            OR
            (
                type = 'FILE'
                AND content IS NULL
                AND file_attachment_id IS NOT NULL
            )
        )
);


CREATE TABLE file_attachments
(
    id                 UUID PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    storage_file_name  VARCHAR(255) NOT NULL UNIQUE,
    content_type       VARCHAR(100) NOT NULL,
    size               BIGINT       NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL
);


ALTER TABLE messages
    ADD CONSTRAINT fk_messages_file_attachment
        FOREIGN KEY (file_attachment_id)
        REFERENCES file_attachments (id);


CREATE UNIQUE INDEX uk_messages_file_attachment
    ON messages(file_attachment_id)
    WHERE file_attachment_id IS NOT NULL;

CREATE INDEX idx_messages_chat_created_id
    ON messages (chat_id, created_at DESC, id DESC);

CREATE INDEX idx_messages_file_attachment_id
    ON messages (file_attachment_id);


ALTER TABLE chat_members
    ADD CONSTRAINT fk_chat_members_last_read_message
        FOREIGN KEY (last_read_message_id)
        REFERENCES messages (id)
        ON DELETE SET NULL;


CREATE TABLE processed_events
(
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);


CREATE TABLE outbox_events
(
    id             BIGSERIAL PRIMARY KEY,
    event_id       UUID         NOT NULL UNIQUE,
    topic          VARCHAR(255) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    payload        TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    attempt_count  INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_outbox_events_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_outbox_status_id
    ON outbox_events(status, id);

CREATE INDEX idx_outbox_created_at
    ON outbox_events(created_at);

CREATE INDEX idx_outbox_topic
    ON outbox_events(topic);

CREATE INDEX idx_outbox_events_status_next_attempt
    ON outbox_events(status, next_attempt_at, id);