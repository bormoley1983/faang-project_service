ALTER TABLE donation
    ADD COLUMN IF NOT EXISTS idempotency_key UUID,
    ADD COLUMN IF NOT EXISTS status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE donation
SET idempotency_key = gen_random_uuid(),
    status = 'COMPLETED'
WHERE idempotency_key IS NULL OR status IS NULL;

ALTER TABLE donation
    ALTER COLUMN idempotency_key SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_donation_idempotency_key
    ON donation (idempotency_key);

ALTER TABLE campaign
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
