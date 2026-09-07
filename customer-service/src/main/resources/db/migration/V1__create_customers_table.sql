-- Create the customer_db schema
CREATE SCHEMA IF NOT EXISTS customer_db;

-- Create the customers table
CREATE TABLE customer_db.customers (
    id                BIGSERIAL PRIMARY KEY,
    user_id           VARCHAR(255)        NOT NULL,
    first_name        VARCHAR(100)        NOT NULL,
    last_name         VARCHAR(100)        NOT NULL,
    email             VARCHAR(150)        NOT NULL,
    phone_number      VARCHAR(15)         NOT NULL,
    date_of_birth     DATE                NOT NULL,

    -- Address fields
    address_line1     TEXT                NOT NULL,
    address_line2     TEXT,
    city              VARCHAR(100)        NOT NULL,
    state             VARCHAR(100)        NOT NULL,
    pincode           VARCHAR(10)         NOT NULL,

    -- KYC fields
    pan_number        VARCHAR(10)         NOT NULL,
    aadhaar_number    VARCHAR(12)         NOT NULL,
    kyc_status        VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    kyc_remarks       TEXT,

    -- Audit
    created_at        TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Unique constraints
ALTER TABLE customer_db.customers ADD CONSTRAINT uk_customers_email          UNIQUE (email);
ALTER TABLE customer_db.customers ADD CONSTRAINT uk_customers_pan            UNIQUE (pan_number);
ALTER TABLE customer_db.customers ADD CONSTRAINT uk_customers_aadhaar        UNIQUE (aadhaar_number);
ALTER TABLE customer_db.customers ADD CONSTRAINT uk_customers_user_id        UNIQUE (user_id);

-- Check constraint on KYC status
ALTER TABLE customer_db.customers ADD CONSTRAINT chk_kyc_status
    CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED'));

-- Indexes for fast lookups
CREATE INDEX idx_customers_user_id    ON customer_db.customers (user_id);
CREATE INDEX idx_customers_kyc_status ON customer_db.customers (kyc_status);

COMMENT ON TABLE customer_db.customers IS 'Stores customer profile and KYC information for the banking Account Opening System';
