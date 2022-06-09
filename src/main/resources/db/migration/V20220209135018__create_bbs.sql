CREATE TABLE IF NOT EXISTS tb_rev_bbs
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT,
    sj         VARCHAR(200) NOT NULL,
    ntt_ty     VARCHAR(10)  NOT NULL,
    cn         VARCHAR(2000),
    view_count BIGINT    DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_bbs_tb_account_id (account_id) REFERENCES tb_rev_account (id)
);