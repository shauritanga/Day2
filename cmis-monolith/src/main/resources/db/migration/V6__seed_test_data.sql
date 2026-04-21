-- V6: Seed data for development and training
-- WARNING: Do NOT run this migration on production.
-- In a real project, use Spring profiles to apply seed migrations only on DEV/UAT.

INSERT INTO hospitals (hospital_code, name, region, contact_email, status) VALUES
('HOSP-ZNZ-001', 'Mnazi Mmoja Referral Hospital', 'Mjini Magharibi', 'claims@mnazimmoja.go.tz', 'ACTIVE'),
('HOSP-ZNZ-002', 'Kivunge District Hospital',    'Kaskazini Unguja', 'billing@kivunge.go.tz',   'ACTIVE'),
('HOSP-ZNZ-003', 'Chake Chake Hospital',          'Pemba South',     'claims@chakechake.go.tz', 'ACTIVE'),
('HOSP-ZNZ-004', 'Wete District Hospital',        'Pemba North',     'billing@wete.go.tz',      'SUSPENDED')
ON CONFLICT (hospital_code) DO NOTHING;

INSERT INTO members (member_number, full_name, date_of_birth, gender, policy_number, status) VALUES
('MBR-2024-0001', 'Amina Hassan Juma',    '1985-03-12', 'Female', 'POL-ZNZ-001', 'ACTIVE'),
('MBR-2024-0002', 'Khalid Omar Salim',    '1990-07-22', 'Male',   'POL-ZNZ-002', 'ACTIVE'),
('MBR-2024-0003', 'Fatuma Ali Mohammed',  '1978-11-05', 'Female', 'POL-ZNZ-003', 'ACTIVE'),
('MBR-2024-0004', 'Said Rashid Hamad',    '2001-01-30', 'Male',   'POL-ZNZ-004', 'INACTIVE')
ON CONFLICT (member_number) DO NOTHING;
