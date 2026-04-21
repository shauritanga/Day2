-- V2: Members / Beneficiaries table
-- ZHSF insured members whose claims are submitted by hospitals

CREATE TABLE IF NOT EXISTS members (
    id            BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(30)  NOT NULL UNIQUE,
    full_name     VARCHAR(150) NOT NULL,
    date_of_birth DATE         NOT NULL,
    gender        VARCHAR(10)  NOT NULL,
    policy_number VARCHAR(30)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX IF NOT EXISTS idx_members_number ON members (member_number);
CREATE INDEX IF NOT EXISTS idx_members_policy ON members (policy_number);
CREATE INDEX IF NOT EXISTS idx_members_status ON members (status);
