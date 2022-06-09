ALTER TABLE tb_rev_account ADD COLUMN instt_id BIGINT;
ALTER TABLE tb_rev_account ADD FOREIGN KEY (instt_id) REFERENCES tb_rev_instt(id);