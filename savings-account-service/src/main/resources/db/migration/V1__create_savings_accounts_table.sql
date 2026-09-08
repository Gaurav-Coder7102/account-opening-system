-- Create the savings_db schema
CREATE SCHEMA IF NOT EXISTS savings_db;

-- Thread-safe PostgreSQL sequence for account number generation
-- Each call to nextval() is atomic and cannot produce duplicates even under concurrent load
CREATE SEQUENCE IF NOT EXISTS savings_db.account_number_seq
    START WITH 100000001
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;

-- Create the savings_accounts table
CREATE TABLE savings_db.savings_accounts (
    id                 BIGSERIAL PRIMARY KEY,
    account_number     VARCHAR(20)         NOT NULL UNIQUE,
    customer_id        BIGINT              NOT NULL,
    user_id            VARCHAR(255)        NOT NULL,
    application_id     BIGINT              NOT NULL,
    account_type       VARCHAR(20)         NOT NULL DEFAULT 'SAVINGS',
    status             VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    balance            DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    remarks            TEXT,
    created_at         TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Check constraints
ALTER TABLE savings_db.savings_accounts ADD CONSTRAINT chk_savings_status
    CHECK (status IN ('ACTIVE', 'DORMANT', 'CLOSED'));

ALTER TABLE savings_db.savings_accounts ADD CONSTRAINT chk_savings_account_type
    CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY'));

ALTER TABLE savings_db.savings_accounts ADD CONSTRAINT chk_balance_non_negative
    CHECK (balance >= 0.00);

-- Indexes for fast lookups
CREATE INDEX idx_savings_customer_id    ON savings_db.savings_accounts (customer_id);
CREATE INDEX idx_savings_user_id        ON savings_db.savings_accounts (user_id);
CREATE INDEX idx_savings_application_id ON savings_db.savings_accounts (application_id);
CREATE INDEX idx_savings_status         ON savings_db.savings_accounts (status);

COMMENT ON TABLE savings_db.savings_accounts IS 'Stores savings accounts provisioned after approved account opening applications';
COMMENT ON SEQUENCE savings_db.account_number_seq IS 'Thread-safe PostgreSQL sequence for generating unique, non-sequential account numbers';
