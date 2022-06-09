CREATE TABLE IF NOT EXISTS tb_rev_account_evl_idx
(
    account_id BIGINT,
    evl_idx_id BIGINT,
    CONSTRAINT tb_rev_account_evl_idx_pk PRIMARY KEY (account_id, evl_idx_id),
    CONSTRAINT tb_rev_account_evl_idx_fk1 FOREIGN KEY (account_id) REFERENCES tb_rev_account (id),
    CONSTRAINT tb_rev_account_evl_idx_fk2 FOREIGN KEY (evl_idx_id) REFERENCES tb_rev_evl_idx (id)
);
