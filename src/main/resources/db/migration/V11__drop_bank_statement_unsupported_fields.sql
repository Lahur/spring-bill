ALTER TABLE bank_statement
    DROP COLUMN sequence_number,
    DROP COLUMN bank_oib;

ALTER TABLE bank_transaction
    DROP COLUMN counterparty_address,
    DROP COLUMN entry_reference;
