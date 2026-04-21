-- V5: Adjudications table
-- One adjudication per claim — the review outcome from ZHSF staff

CREATE TABLE IF NOT EXISTS adjudications (
    id               BIGSERIAL PRIMARY KEY,
    claim_id         BIGINT         NOT NULL UNIQUE REFERENCES claims(id),
    reviewed_by      VARCHAR(100)   NOT NULL,
    reviewed_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    decision         VARCHAR(30)    NOT NULL,
    approved_amount  NUMERIC(14, 2) NOT NULL,
    rejection_reason VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_adjudications_claim ON adjudications (claim_id);
