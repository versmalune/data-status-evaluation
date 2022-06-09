ALTER TABLE tb_rev_bbs MODIFY NTT_TY VARCHAR(40);


ALTER TABLE tb_rev_dw_fact_idx_result MODIFY SCR NUMERIC(6,3) NOT NULL;


ALTER TABLE tb_rev_dw_fact_instt_result MODIFY SCR NUMERIC(6,3) NOT NULL;
ALTER TABLE tb_rev_dw_fact_instt_result MODIFY EXT_SCR NUMERIC(6,3) NOT NULL;
--컬럼명 변경
ALTER TABLE tb_rev_dw_fact_instt_result RENAME COLUMN process_sttus TO process_sttus_cd;


ALTER TABLE tb_rev_evl_idx MODIFY IDX_QESTN VARCHAR(2000);
ALTER TABLE tb_rev_evl_idx MODIFY BEGIN_DT TIMESTAMP;
ALTER TABLE tb_rev_evl_idx MODIFY END_DT TIMESTAMP;


ALTER TABLE tb_rev_evl_result MODIFY SCR NUMERIC(6,3) NOT NULL;
--컬럼명 변경
ALTER TABLE tb_rev_evl_result RENAME COLUMN process_sttus TO process_sttus_cd;
ALTER TABLE tb_rev_evl_result MODIFY OBJC VARCHAR(4000);


ALTER TABLE tb_rev_evl_schd MODIFY BEGIN_DT TIMESTAMP;
ALTER TABLE tb_rev_evl_schd MODIFY END_DT TIMESTAMP;


ALTER TABLE tb_rev_evl_scr MODIFY IDX_SCR NUMERIC(12,9);


ALTER TABLE tb_rev_file MODIFY FILE_TY VARCHAR(40);
ALTER TABLE tb_rev_file MODIFY extsn VARCHAR(40);


ALTER TABLE tb_rev_instt_category MODIFY category_DC VARCHAR(2000);


ALTER TABLE tb_rev_list_regist_mmg_sys_instt_user_info MODIFY PASSWORD VARCHAR(256);


ALTER TABLE tb_rev_material MODIFY MTL_TY VARCHAR(40);
ALTER TABLE tb_rev_material MODIFY ATCH_TRGT_ID BIGINT;