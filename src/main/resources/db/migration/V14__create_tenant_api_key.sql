CREATE TABLE tenant_api_key (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID         NOT NULL UNIQUE,
    eposlovanje_api_key     VARCHAR(512),
    f1_web_api_key          VARCHAR(512),
    pondi_api_key           VARCHAR(512),
    ais_eposlovanje_api_key VARCHAR(512),
    hub_tenant_id           VARCHAR(255)
);
