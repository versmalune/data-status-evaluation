CREATE TABLE tb_rev_instt_detail
(
    instt_id          BIGINT,
    instt_category_id BIGINT,
    instt_ty          VARCHAR(40),
    evl_yr            CHAR(4),
    PRIMARY KEY (instt_id, evl_yr),
    FOREIGN KEY fk_tb_rev_instt_detail_instt_id (instt_id) REFERENCES tb_rev_instt (id) ON DELETE CASCADE,
    FOREIGN KEY fk_tb_rev_instt_detail_instt_category_id (instt_category_id) REFERENCES tb_rev_instt_category (id)
);