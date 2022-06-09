CREATE TABLE IF NOT EXISTS tb_rev_answer
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cn VARCHAR(2000),
    bbs_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_rev_bbs_id(bbs_id) REFERENCES tb_rev_bbs (id) ON DELETE CASCADE,
    FOREIGN KEY fk_tb_rev_account_id(account_id) REFERENCES tb_rev_account (id) ON DELETE CASCADE
    );