DROP INDEX IF EXISTS idx_chat_created;
DROP INDEX IF EXISTS idx_chat_id_id;

CREATE INDEX idx_messages_chat_created_id
    ON messages (chat_id, created_at DESC, id DESC);

CREATE INDEX idx_messages_file_attachment_id
    ON messages (file_attachment_id);