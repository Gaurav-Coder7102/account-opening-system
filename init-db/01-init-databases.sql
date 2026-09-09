-- Automated initialization for Banking Account Opening System (AOS)
-- Creates isolated databases for each microservice (Database-per-Service)

SELECT 'CREATE DATABASE auth_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db')\gexec
SELECT 'CREATE DATABASE customer_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'customer_db')\gexec
SELECT 'CREATE DATABASE account_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'account_db')\gexec
SELECT 'CREATE DATABASE savings_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'savings_db')\gexec

GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE customer_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE account_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE savings_db TO postgres;
