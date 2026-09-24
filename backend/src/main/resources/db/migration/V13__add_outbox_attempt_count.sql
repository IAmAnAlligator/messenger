ALTER TABLE outbox_events
ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE outbox_events
ADD CONSTRAINT chk_outbox_events_attempt_count
CHECK (attempt_count >= 0);

ALTER TABLE outbox_events
ADD COLUMN next_attempt_at TIMESTAMP WITH TIME ZONE;

UPDATE outbox_events
SET next_attempt_at = created_at;

ALTER TABLE outbox_events
ALTER COLUMN next_attempt_at SET NOT NULL;

ALTER TABLE outbox_events
ALTER COLUMN next_attempt_at SET DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_outbox_events_status_next_attempt
ON outbox_events (status, next_attempt_at, id);