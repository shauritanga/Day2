-- Run this script once as PostgreSQL superuser (e.g., postgres) before starting the monolith
-- Example: psql -U postgres -f create_db.sql

CREATE USER cmis_user WITH PASSWORD 'cmis_pass';

CREATE DATABASE cmis_db OWNER cmis_user;
GRANT ALL PRIVILEGES ON DATABASE cmis_db TO cmis_user;
