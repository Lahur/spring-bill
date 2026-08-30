CREATE TABLE tenant_property (
    id       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    property VARCHAR(255) NOT NULL,
    value    VARCHAR(255) NOT NULL
);
