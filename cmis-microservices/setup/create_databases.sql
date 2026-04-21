-- Run this script once as PostgreSQL superuser before starting the microservices
-- Example: psql -U postgres -f create_databases.sql

CREATE USER cmis_user WITH PASSWORD 'cmis_pass';

CREATE DATABASE hospital_db     OWNER cmis_user;
CREATE DATABASE member_db       OWNER cmis_user;
CREATE DATABASE claims_db       OWNER cmis_user;
CREATE DATABASE adjudication_db OWNER cmis_user;

GRANT ALL PRIVILEGES ON DATABASE hospital_db     TO cmis_user;
GRANT ALL PRIVILEGES ON DATABASE member_db       TO cmis_user;
GRANT ALL PRIVILEGES ON DATABASE claims_db       TO cmis_user;
GRANT ALL PRIVILEGES ON DATABASE adjudication_db TO cmis_user;
