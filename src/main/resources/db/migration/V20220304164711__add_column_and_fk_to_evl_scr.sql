ALTER TABLE tb_rev_evl_scr ADD COLUMN idx_scr FLOAT AFTER idx_lvl;
ALTER TABLE tb_rev_evl_scr ADD COLUMN instt_category_id BIGINT AFTER idx_id;
ALTER TABLE tb_rev_evl_scr ADD FOREIGN KEY (instt_category_id) REFERENCES tb_rev_instt_category(id);