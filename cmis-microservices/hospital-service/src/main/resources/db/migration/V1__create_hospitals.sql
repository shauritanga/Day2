-- Hospital Service: V1
CREATE TABLE IF NOT EXISTS hospitals (
    id             BIGSERIAL PRIMARY KEY,
    hospital_code  VARCHAR(20)  NOT NULL UNIQUE,
    name           VARCHAR(150) NOT NULL,
    region         VARCHAR(100) NOT NULL,
    contact_email  VARCHAR(160) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_hospitals_code   ON hospitals (hospital_code);
CREATE INDEX IF NOT EXISTS idx_hospitals_status ON hospitals (status);
