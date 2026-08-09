CREATE TABLE cash_withdrawal_balance (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    total               NUMERIC(19, 2) NOT NULL,
    balance             NUMERIC(19, 2) NOT NULL,
    bank_transaction_id UUID           NOT NULL REFERENCES bank_transaction (id)
);

CREATE TABLE accounts_statement (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    date        DATE           NOT NULL,
    amount      NUMERIC(19, 2) NOT NULL,
    description VARCHAR(255)   NOT NULL,
    bill_path   VARCHAR(255)
);

CREATE TABLE cash_withdrawal_accounts_statement (
    cash_withdrawal_balance_id UUID NOT NULL REFERENCES cash_withdrawal_balance (id),
    accounts_statement_id      UUID NOT NULL REFERENCES accounts_statement (id),
    PRIMARY KEY (cash_withdrawal_balance_id, accounts_statement_id)
);