CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_username ON users(username);

CREATE TABLE chats
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100),
    type             VARCHAR(20) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    last_message_at  TIMESTAMPTZ,
    private_key      VARCHAR(255) UNIQUE,
    version          BIGINT
);

CREATE TABLE chat_members
(
    id          BIGSERIAL PRIMARY KEY,
    chat_id     BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    role        VARCHAR(20) NOT NULL,
    joined_at   TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_chat_members_chat
        FOREIGN KEY (chat_id)
            REFERENCES chats (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_chat_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT uk_chat_member
        UNIQUE (chat_id, user_id)
);

CREATE INDEX idx_chat_id
    ON chat_members(chat_id);

CREATE INDEX idx_user_id
    ON chat_members(user_id);

CREATE TABLE messages
(
    id          BIGSERIAL PRIMARY KEY,
    chat_id     BIGINT       NOT NULL,
    sender_id   BIGINT       NOT NULL,
    content     VARCHAR(2000) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    status      VARCHAR(20)  NOT NULL,

    CONSTRAINT fk_messages_chat
        FOREIGN KEY (chat_id)
            REFERENCES chats (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
);

CREATE INDEX idx_chat_created
    ON messages(chat_id, created_at DESC);

CREATE INDEX idx_chat_id_id
    ON messages(chat_id, id DESC);

CREATE TABLE processed_messages
(
    event_id      UUID PRIMARY KEY,
    processed_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_events
(
    id            BIGSERIAL PRIMARY KEY,
    event_id      UUID         NOT NULL UNIQUE,
    topic         VARCHAR(255) NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    aggregate_id  VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    payload       TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_outbox_status_id
    ON outbox_events(status, id);

CREATE INDEX idx_outbox_created_at
    ON outbox_events(created_at);

CREATE INDEX idx_outbox_topic
    ON outbox_events(topic);