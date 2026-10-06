ALTER TABLE chats
    ALTER COLUMN version SET DEFAULT 0;

UPDATE chats
SET version = 0
WHERE version IS NULL;

ALTER TABLE chats
    ALTER COLUMN version SET NOT NULL;