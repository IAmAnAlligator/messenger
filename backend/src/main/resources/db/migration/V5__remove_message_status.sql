-- MessageStatus больше не используется.
ALTER TABLE messages
    DROP COLUMN status;