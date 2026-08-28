ALTER TABLE pos_transaction
    ADD COLUMN sent_count INT NOT NULL DEFAULT 0;

ALTER TABLE cash_withdrawal_balance
    ADD COLUMN sent_count INT NOT NULL DEFAULT 0;

ALTER TABLE accounts_statement
    ADD COLUMN sent_count INT NOT NULL DEFAULT 0;
