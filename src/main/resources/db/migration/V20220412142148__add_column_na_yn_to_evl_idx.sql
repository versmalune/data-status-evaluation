ALTER TABLE tb_rev_evl_idx ADD COLUMN na_yn CHAR(1) DEFAULT 'N' AFTER atch_file_yn;
ALTER TABLE tb_rev_evl_result MODIFY scr NUMERIC(6,3) NULL;
ALTER TABLE tb_rev_dw_fact_idx_result MODIFY scr NUMERIC(6,3) NULL;