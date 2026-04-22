-- Outbox table for syncing hospital changes back to the monolith legacy database.
-- This keeps writes flowing while claims and other dependent modules still use cmis_db.
-- Temporary migration scaffolding: remove after all hospital-dependent modules
-- are extracted to microservices and cmis_db.hospitals is retired.

CREATE TABLE IF NOT EXISTS hospital_sync_outbox (
    id             BIGSERIAL PRIMARY KEY,
    hospital_code  VARCHAR(20)  NOT NULL,
    name           VARCHAR(150) NOT NULL,
    region         VARCHAR(100) NOT NULL,
    contact_email  VARCHAR(160) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    synced_at      TIMESTAMPTZ,
    attempts       INT          NOT NULL DEFAULT 0,
    last_error     VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_hospital_sync_outbox_pending
    ON hospital_sync_outbox (synced_at);

CREATE INDEX IF NOT EXISTS idx_hospital_sync_outbox_hospital_code
    ON hospital_sync_outbox (hospital_code);
