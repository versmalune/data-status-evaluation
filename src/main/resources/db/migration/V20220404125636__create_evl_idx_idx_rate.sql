CREATE TABLE IF NOT EXISTS tb_rev_evl_idx_idx_rate
(
    idx_id      BIGINT,
    idx_rate_id BIGINT,
    CONSTRAINT tb_rev_evl_idx_idx_rate_pk PRIMARY KEY (idx_id, idx_rate_id),
    CONSTRAINT tb_rev_evl_idx_idx_rate_fk1 FOREIGN KEY (idx_id) REFERENCES tb_rev_evl_idx (id),
    CONSTRAINT tb_rev_evl_idx_idx_rate_fk2 FOREIGN KEY (idx_rate_id) REFERENCES tb_rev_evl_idx_rate (id)
)