-- V4: Claim items table
-- Individual line items (procedures/services) within a claim

CREATE TABLE IF NOT EXISTS claim_items (
    id             BIGSERIAL PRIMARY KEY,
    claim_id       BIGINT         NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    diagnosis_code VARCHAR(20)    NOT NULL,
    procedure_code VARCHAR(20)    NOT NULL,
    description    VARCHAR(255)   NOT NULL,
    quantity       INT            NOT NULL CHECK (quantity > 0),
    unit_cost      NUMERIC(14, 2) NOT NULL CHECK (unit_cost > 0),
    total_cost     NUMERIC(14, 2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_claim_items_claim ON claim_items (claim_id);
