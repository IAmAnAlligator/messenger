-- Удаляем избыточный индекс по chat_id.
--
-- Уникальный индекс по (chat_id, user_id)
-- уже используется для поиска участника
-- по chat_id + user_id.

DROP INDEX IF EXISTS idx_chat_id;


-- Переименовываем существующий UNIQUE constraint,
-- чтобы его имя соответствовало JPA entity.

ALTER TABLE chat_members
    RENAME CONSTRAINT chat_members_chat_id_user_id_key
    TO uk_chat_members_chat_user;