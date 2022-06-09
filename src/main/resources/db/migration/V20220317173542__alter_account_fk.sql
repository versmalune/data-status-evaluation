ALTER TABLE tb_rev_account DROP FOREIGN KEY fk_tb_rev_account_instt_id;
ALTER TABLE tb_rev_account ADD CONSTRAINT FOREIGN KEY (instt_id) REFERENCES tb_rev_instt(id) ON DELETE SET NULL;