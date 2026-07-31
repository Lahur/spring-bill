CREATE TABLE bill (
    id                UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    system_id         BIGSERIAL,
    full_bill_id      VARCHAR(255)   NOT NULL,
    client_name       VARCHAR(255)   NOT NULL,
    client_oib        VARCHAR(11),
    bill_date         TIMESTAMP      NOT NULL,
    total_amount      NUMERIC(19, 2) NOT NULL,
    document_status   VARCHAR(50),
    bill_type         VARCHAR(50)    NOT NULL,
    sent_count        INT            NOT NULL DEFAULT 0,
    payment_reference VARCHAR(50),
    UNIQUE (system_id, bill_type)
);

CREATE TABLE bill_info (
    id                      UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id                 UUID           NOT NULL UNIQUE REFERENCES bill (id),

    -- mainDataInfo
    main_bill_date          DATE,
    main_due_date           DATE,
    document_type           VARCHAR(50),
    currency                VARCHAR(10),
    bill_period_from        DATE,
    bill_period_till        DATE,

    -- buyerInfo
    buyer_name              VARCHAR(255),
    buyer_oib               VARCHAR(11),
    buyer_address           VARCHAR(255),
    buyer_city              VARCHAR(255),
    buyer_postal_code       VARCHAR(20),

    -- supplierInfo
    supplier_name           VARCHAR(255),
    supplier_oib            VARCHAR(11),
    supplier_address        VARCHAR(255),
    supplier_city           VARCHAR(255),
    supplier_postal_code    VARCHAR(20),
    supplier_contact_name   VARCHAR(255),
    supplier_contact_oib    VARCHAR(11),
    supplier_contact_email  VARCHAR(255),
    supplier_contact_phone  VARCHAR(50),

    -- paymentInfo
    payment_means           VARCHAR(50),
    payment_due_date        DATE,
    payment_iban            VARCHAR(34),
    payment_model           VARCHAR(10),
    payment_reference       VARCHAR(50),
    payment_note            VARCHAR(255),

    -- priceInfo
    vat_exclusive_amount    NUMERIC(19, 2),
    vat_amount              NUMERIC(19, 2),
    vat_inclusive_amount    NUMERIC(19, 2),
    advance_amount          NUMERIC(19, 2),
    total_amount            NUMERIC(19, 2)
);

CREATE TABLE bill_item (
    id                UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_info_id      UUID           NOT NULL REFERENCES bill_info (id),
    item_order        INT            NOT NULL,
    name              VARCHAR(255),
    description       VARCHAR(500),
    quantity          NUMERIC(19, 4),
    unit_of_measure   VARCHAR(10),
    base_amount       NUMERIC(19, 2),
    total_amount      NUMERIC(19, 2),
    vat_category      VARCHAR(50)
);

CREATE TABLE monthly_summary (
    id                      UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    month                   DATE           NOT NULL UNIQUE,
    sales_paid_total        NUMERIC(19, 2) NOT NULL,
    sales_unpaid_total      NUMERIC(19, 2) NOT NULL,
    purchases_paid_total    NUMERIC(19, 2) NOT NULL,
    purchases_unpaid_total  NUMERIC(19, 2) NOT NULL
);

CREATE TABLE bank_statement (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    statement_id        VARCHAR(255)   NOT NULL,
    iban                VARCHAR(34)    NOT NULL,
    currency            VARCHAR(10),
    period_from         DATE,
    period_to           DATE,
    created_at          TIMESTAMP
);

CREATE TABLE bank_transaction (
    id                          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_statement_id           UUID           NOT NULL REFERENCES bank_statement (id),
    amount                      NUMERIC(19, 2),
    credit_debit_indicator      VARCHAR(10),
    sender_iban                 VARCHAR(34),
    receiver_iban               VARCHAR(34),
    reference                   VARCHAR(35),
    additional_remittance_info  VARCHAR(140),
    transaction_date            TIMESTAMP
);