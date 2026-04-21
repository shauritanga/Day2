-- Hospital Service: Seed data for training
INSERT INTO hospitals (hospital_code, name, region, contact_email, status) VALUES
('HOSP-ZNZ-001', 'Mnazi Mmoja Referral Hospital', 'Mjini Magharibi', 'claims@mnazimmoja.go.tz', 'ACTIVE'),
('HOSP-ZNZ-002', 'Kivunge District Hospital',    'Kaskazini Unguja', 'billing@kivunge.go.tz',   'ACTIVE'),
('HOSP-ZNZ-003', 'Chake Chake Hospital',          'Pemba South',     'claims@chakechake.go.tz', 'ACTIVE'),
('HOSP-ZNZ-004', 'Wete District Hospital',        'Pemba North',     'billing@wete.go.tz',      'SUSPENDED')
ON CONFLICT (hospital_code) DO NOTHING;
