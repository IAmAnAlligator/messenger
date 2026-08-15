CREATE TABLE file_attachments
(
    id                  UUID PRIMARY KEY,
    original_file_name  VARCHAR(255) NOT NULL,
    storage_file_name   VARCHAR(255) NOT NULL UNIQUE,
    content_type        VARCHAR(255) NOT NULL,
    size                BIGINT       NOT NULL,
    storage_path        VARCHAR(500) NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL
);


ALTER TABLE messages
    ALTER COLUMN content DROP NOT NULL;


ALTER TABLE messages
    ADD COLUMN type VARCHAR(20);


UPDATE messages
SET type = 'TEXT';


ALTER TABLE messages
    ALTER COLUMN type SET NOT NULL;


ALTER TABLE messages
    ADD COLUMN file_attachment_id UUID;


ALTER TABLE messages
    ADD CONSTRAINT fk_messages_file_attachment
        FOREIGN KEY (file_attachment_id)
        REFERENCES file_attachments (id);


CREATE UNIQUE INDEX uk_messages_file_attachment
    ON messages(file_attachment_id)
    WHERE file_attachment_id IS NOT NULL;


ALTER TABLE messages
    ADD CONSTRAINT chk_messages_type
    CHECK (type IN ('TEXT', 'FILE'));


ALTER TABLE messages
    ADD CONSTRAINT chk_messages_type_content
    CHECK (
        (type = 'TEXT'
            AND content IS NOT NULL
            AND file_attachment_id IS NULL)

        OR

        (type = 'FILE'
            AND content IS NULL
            AND file_attachment_id IS NOT NULL)
    );