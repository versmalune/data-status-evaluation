CREATE TABLE IF NOT EXISTS tb_rev_evl_result_total (
    instt_id BIGINT,
    fld_id BIGINT,
    evl_yr CHAR(4),
    tot_rvw VARCHAR(4000),
    PRIMARY KEY (instt_id, fld_id),
    FOREIGN KEY (instt_id) REFERENCES tb_rev_instt (id) ON DELETE CASCADE,
    FOREIGN KEY (fld_id) REFERENCES tb_rev_evl_fld (id)
)