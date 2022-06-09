CREATE TABLE IF NOT EXISTS tb_rev_account_role
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    author     VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_account_roles_tb_account_id (account_id) REFERENCES tb_rev_account (id) ON DELETE CASCADE
);