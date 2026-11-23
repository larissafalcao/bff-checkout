CREATE TABLE cart (
    id                 UUID PRIMARY KEY,
    status             VARCHAR(20)    NOT NULL,
    total              NUMERIC(12, 2) NOT NULL,
    payment_method     VARCHAR(100),
    last_digits        VARCHAR(4),
    pix_code           VARCHAR(50),
    paid_at            TIMESTAMP WITH TIME ZONE,
    payment_attempt    INT            NOT NULL DEFAULT 1,
    payment_started_at TIMESTAMP WITH TIME ZONE,
    version            BIGINT         NOT NULL
);

CREATE TABLE cart_product (
    cart_id     UUID           NOT NULL REFERENCES cart (id),
    position    INT            NOT NULL,
    slug        VARCHAR(50)    NOT NULL,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(255),
    price       NUMERIC(12, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    PRIMARY KEY (cart_id, position)
);

CREATE TABLE cart_payment_method (
    cart_id       UUID         NOT NULL REFERENCES cart (id),
    position      INT          NOT NULL,
    name          VARCHAR(100) NOT NULL,
    requires_card BOOLEAN      NOT NULL,
    PRIMARY KEY (cart_id, position)
);
