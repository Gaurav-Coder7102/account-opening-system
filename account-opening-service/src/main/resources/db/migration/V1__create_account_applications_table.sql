-- Create the account_db schema
CREATE SCHEMA IF NOT EXISTS account_db;

-- Create the account_applications table
CREATE TABLE account_db.account_applications (
    id                 BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(50)         NOT NULL UNIQUE,
    customer_id        BIGINT              NOT NULL,
    user_id            VARCHAR(255)        NOT NULL,
    account_type       VARCHAR(20)         NOT NULL,
    status             VARCHAR(250)        NOT NULL DEFAULT 'DRAFT',
    initial_deposit    DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    remarks            TEXT,

    created_at         TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Check constraints
ALTER TABLE account_db.account_applications ADD CONSTRAINT chk_application_status
    CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ACCOUNT_CREATED'));

ALTER TABLE account_db.account_applications ADD CONSTRAINT chk_account_type
    CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY'));

-- Indexes for fast lookups
CREATE INDEX idx_applications_customer_id ON account_db.account_applications (customer_id);
CREATE INDEX idx_applications_user_id     ON account_db.account_applications (user_id);
CREATE INDEX idx_applications_status      ON account_db.account_applications (status);

COMMENT ON TABLE account_db.account_applications IS 'Stores state-machine driven account opening applications';
