-- Добавляем read cursor для каждого участника чата.
--
-- NULL означает, что пользователь ещё не прочитал
-- ни одного сообщения в этом чате.

ALTER TABLE chat_members
    ADD COLUMN last_read_message_id BIGINT;


-- Связываем read cursor с существующим сообщением.
--
-- Если сообщение будет удалено, cursor участника
-- автоматически сбрасывается в NULL.

ALTER TABLE chat_members
    ADD CONSTRAINT fk_chat_members_last_read_message
        FOREIGN KEY (last_read_message_id)
        REFERENCES messages (id)
        ON DELETE SET NULL;