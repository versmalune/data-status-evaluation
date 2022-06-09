ALTER TABLE tb_rev_bbs DROP FOREIGN KEY fk_tb_bbs_tb_account_id;
ALTER TABLE tb_rev_bbs ADD CONSTRAINT FOREIGN KEY fk_tb_bbs_tb_account_id (account_id) REFERENCES tb_rev_account(id) ON DELETE CASCADE;