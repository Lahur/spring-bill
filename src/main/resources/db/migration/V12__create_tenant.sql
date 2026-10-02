CREATE TABLE tenant (
    id           UUID         PRIMARY KEY,
    oib          VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    street       VARCHAR(255) NOT NULL,
    city         VARCHAR(255) NOT NULL,
    postal_zone  VARCHAR(255) NOT NULL,
    country_code VARCHAR(255) NOT NULL,
    contact_oib  VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    phone        VARCHAR(255),
    email        VARCHAR(255),
    iban         VARCHAR(255) NOT NULL
);
