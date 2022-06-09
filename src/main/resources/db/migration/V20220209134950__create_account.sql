CREATE TABLE IF NOT EXISTS tb_rev_account
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(320) NOT NULL,
    flnm       VARCHAR(100),
    password   VARCHAR(255) NOT NULL,
    company    varchar(200),
    telno      varchar(11),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
