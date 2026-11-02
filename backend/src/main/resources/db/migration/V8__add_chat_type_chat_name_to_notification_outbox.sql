ALTER TABLE notification_outbox
    ADD COLUMN chat_type VARCHAR(20),
    ADD COLUMN chat_name VARCHAR(255);