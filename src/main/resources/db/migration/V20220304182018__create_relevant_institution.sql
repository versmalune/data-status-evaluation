CREATE TABLE IF NOT EXISTS tb_rev_evl_idx_rlvt_instt
(
    evl_idx_id        BIGINT,
    instt_category_id BIGINT,
    rlvt_instt_yn     CHAR(1) DEFAULT 'N',
    PRIMARY KEY (evl_idx_id, instt_category_id),
    FOREIGN KEY (evl_idx_id) REFERENCES tb_rev_evl_idx (id) ON DELETE CASCADE,
    FOREIGN KEY (instt_category_id) REFERENCES tb_rev_instt_category (id)
)