-- V3: Claims table
-- A claim is a request for reimbursement submitted by a hospital on behalf of a member

CREATE TABLE IF NOT EXISTS claims (
    id            BIGSERIAL PRIMARY KEY,
    claim_number  VARCHAR(30)    NOT NULL UNIQUE,
    hospital_id   BIGINT         NOT NULL REFERENCES hospitals(id),
    member_id     BIGINT         NOT NULL REFERENCES members(id),
    submitted_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    status        VARCHAR(30)    NOT NULL DEFAULT 'SUBMITTED',
    total_amount  NUMERIC(14, 2) NOT NULL,
    notes         VARCHAR(500)
);

-- Index every foreign key — missing FK indexes cause full table scans on JOINs
CREATE INDEX IF NOT EXISTS idx_claims_number       ON claims (claim_number);
CREATE INDEX IF NOT EXISTS idx_claims_hospital     ON claims (hospital_id);
CREATE INDEX IF NOT EXISTS idx_claims_member       ON claims (member_id);
CREATE INDEX IF NOT EXISTS idx_claims_status       ON claims (status);
CREATE INDEX IF NOT EXISTS idx_claims_submitted_at ON claims (submitted_at);
