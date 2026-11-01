ALTER TABLE notification_outbox
    ADD COLUMN channel VARCHAR(20) NOT NULL DEFAULT 'WEB_PUSH';

ALTER TABLE notification_outbox
    ADD CONSTRAINT chk_notification_outbox_channel
        CHECK (channel IN ('WEB_PUSH', 'EMAIL'));

ALTER TABLE notification_outbox
    DROP CONSTRAINT uq_notification_outbox_event_recipient;

ALTER TABLE notification_outbox
    ADD CONSTRAINT uq_notification_outbox_event_recipient_channel
        UNIQUE (event_id, recipient_user_id, channel);