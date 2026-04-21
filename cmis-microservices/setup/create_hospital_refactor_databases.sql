-- Run this script once as PostgreSQL superuser for the first extraction lesson.
-- It creates only the monolith database and the extracted hospital-service database.
-- Example: psql -U postgres -f create_hospital_refactor_databases.sql

DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cmis_user') THEN
      CREATE USER cmis_user WITH PASSWORD 'cmis_pass';
   END IF;
END
$$;

CREATE DATABASE cmis_db     OWNER cmis_user;
CREATE DATABASE hospital_db OWNER cmis_user;

GRANT ALL PRIVILEGES ON DATABASE cmis_db     TO cmis_user;
GRANT ALL PRIVILEGES ON DATABASE hospital_db TO cmis_user;
