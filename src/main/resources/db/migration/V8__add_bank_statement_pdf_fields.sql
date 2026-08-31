ALTER TABLE bank_statement
    ADD COLUMN sequence_number  INTEGER,
    ADD COLUMN account_name     VARCHAR(255),
    ADD COLUMN owner_name       VARCHAR(255),
    ADD COLUMN owner_address    VARCHAR(255),
    ADD COLUMN owner_oib        VARCHAR(20),
    ADD COLUMN bank_bic         VARCHAR(20),
    ADD COLUMN bank_oib         VARCHAR(20),
    ADD COLUMN opening_balance  NUMERIC(19, 2),
    ADD COLUMN closing_balance  NUMERIC(19, 2),
    ADD COLUMN credit_count     INTEGER,
    ADD COLUMN credit_sum       NUMERIC(19, 2),
    ADD COLUMN debit_count      INTEGER,
    ADD COLUMN debit_sum        NUMERIC(19, 2),
    ADD COLUMN sent_count       INT NOT NULL DEFAULT 0;

ALTER TABLE bank_transaction
    ADD COLUMN counterparty_name     VARCHAR(140),
    ADD COLUMN counterparty_address  VARCHAR(210),
    ADD COLUMN payer_reference       VARCHAR(35),
    ADD COLUMN entry_reference       VARCHAR(64),
    ADD COLUMN transaction_reference VARCHAR(64),
    ADD COLUMN value_date            DATE;
