CREATE TABLE tb_rev_dw_fact_instt_result
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    instt_id      BIGINT      NOT NULL,
    evl_yr        CHAR(4)     NOT NULL,
    scr           FLOAT       NOT NULL,
    ext_scr       FLOAT       NOT NULL,
    process_sttus VARCHAR(40) NOT NULL,
    creat_dt      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updt_dt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_rev_dw_fact_idx_result
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    instt_id BIGINT  NOT NULL,
    idx_id   BIGINT  NOT NULL,
    fld_id   BIGINT  NOT NULL,
    evl_yr   CHAR(4) NOT NULL,
    scr      FLOAT   NOT NULL,
    creat_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updt_dt  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX tb_rev_dw_fact_instt_result_index1 ON tb_rev_dw_fact_instt_result (instt_id, evl_yr);
CREATE INDEX tb_rev_dw_fact_instt_result_index2 ON tb_rev_dw_fact_instt_result (process_sttus, evl_yr);
CREATE INDEX tb_rev_dw_fact_idx_result_index_1 ON tb_rev_dw_fact_idx_result (instt_id);
CREATE INDEX tb_rev_dw_fact_idx_result_index_2 ON tb_rev_dw_fact_idx_result (idx_id);
CREATE INDEX tb_rev_dw_fact_idx_result_index_3 ON tb_rev_dw_fact_idx_result (fld_id);
CREATE INDEX tb_rev_dw_fact_idx_result_index_4 ON tb_rev_dw_fact_idx_result (evl_yr);