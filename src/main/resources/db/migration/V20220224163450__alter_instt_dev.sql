ALTER TABLE tb_rev_instt_dev
    add id bigint NOT NULL AUTO_INCREMENT first;

ALTER TABLE tb_rev_instt_dev drop constraint pk_tb_rev_instt_instt_cd_evl_year;

ALTER TABLE tb_rev_instt_dev
    ADD CONSTRAINT tb_rev_instt_dev_pk
        PRIMARY KEY (id);

ALTER TABLE tb_rev_instt_dev
    ADD CONSTRAINT uk_tb_rev_instt_instt_cd_evl_year
        UNIQUE (instt_cd, evl_year);