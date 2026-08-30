ALTER TABLE meet
    ADD COLUMN IF NOT EXISTS ends_at TIMESTAMP;

UPDATE meet
SET ends_at = starts_at + INTERVAL '1 hour'
WHERE ends_at IS NULL;

ALTER TABLE meet
    ALTER COLUMN ends_at SET NOT NULL;
