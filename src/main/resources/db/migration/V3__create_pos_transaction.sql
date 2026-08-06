CREATE TABLE pos_transaction (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_path           VARCHAR(255),
    bank_transaction_id UUID           NOT NULL REFERENCES bank_transaction (id)
);