ALTER TABLE user_notification_settings
    DROP COLUMN web_push_enabled,
    DROP COLUMN email_enabled;

ALTER TABLE user_notification_settings
    ADD COLUMN web_push_private_chats_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN web_push_group_chats_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_private_chats_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_group_chats_enabled BOOLEAN NOT NULL DEFAULT FALSE;