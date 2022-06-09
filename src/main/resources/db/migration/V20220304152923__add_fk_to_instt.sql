ALTER TABLE tb_rev_instt ADD COLUMN instt_category_id BIGINT AFTER instt_ty;
ALTER TABLE tb_rev_instt ADD FOREIGN KEY (instt_category_id) REFERENCES tb_rev_instt_category(id);