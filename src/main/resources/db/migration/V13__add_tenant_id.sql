-- Every row that predates multi-tenancy belongs to the single tenant this instance used to serve,
-- configured through spring.flyway.placeholders.default_tenant_id.

ALTER TABLE bill ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE bill ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_bill_tenant_id ON bill (tenant_id);

ALTER TABLE bill_info ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE bill_info ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_bill_info_tenant_id ON bill_info (tenant_id);

ALTER TABLE bill_item ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE bill_item ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_bill_item_tenant_id ON bill_item (tenant_id);

ALTER TABLE monthly_summary ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE monthly_summary ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_monthly_summary_tenant_id ON monthly_summary (tenant_id);

ALTER TABLE bank_statement ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE bank_statement ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_bank_statement_tenant_id ON bank_statement (tenant_id);

ALTER TABLE bank_transaction ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE bank_transaction ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_bank_transaction_tenant_id ON bank_transaction (tenant_id);

ALTER TABLE pos_transaction ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE pos_transaction ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_pos_transaction_tenant_id ON pos_transaction (tenant_id);

ALTER TABLE cash_withdrawal_balance ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE cash_withdrawal_balance ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_cash_withdrawal_balance_tenant_id ON cash_withdrawal_balance (tenant_id);

ALTER TABLE accounts_statement ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE accounts_statement ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_accounts_statement_tenant_id ON accounts_statement (tenant_id);

ALTER TABLE recipient ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE recipient ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_recipient_tenant_id ON recipient (tenant_id);

ALTER TABLE tenant_property ADD COLUMN tenant_id UUID NOT NULL DEFAULT '${default_tenant_id}';
ALTER TABLE tenant_property ALTER COLUMN tenant_id DROP DEFAULT;
CREATE INDEX idx_tenant_property_tenant_id ON tenant_property (tenant_id);

-- Natural keys are only unique within a tenant.
ALTER TABLE bill DROP CONSTRAINT bill_system_id_bill_type_key;
ALTER TABLE bill ADD CONSTRAINT uk_bill_tenant_system_id_bill_type UNIQUE (tenant_id, system_id, bill_type);

ALTER TABLE monthly_summary DROP CONSTRAINT monthly_summary_month_key;
ALTER TABLE monthly_summary ADD CONSTRAINT uk_monthly_summary_tenant_month UNIQUE (tenant_id, month);

ALTER TABLE recipient DROP CONSTRAINT recipient_email_key;
ALTER TABLE recipient ADD CONSTRAINT uk_recipient_tenant_email UNIQUE (tenant_id, email);

ALTER TABLE tenant_property ADD CONSTRAINT uk_tenant_property_tenant_property UNIQUE (tenant_id, property);
