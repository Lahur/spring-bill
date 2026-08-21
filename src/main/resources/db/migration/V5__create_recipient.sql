CREATE TABLE recipient (
    id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE
);
