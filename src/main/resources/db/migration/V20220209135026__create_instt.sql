CREATE TABLE IF NOT EXISTS tb_rev_instt_info_dev
(
    instt_cd   VARCHAR(30) PRIMARY KEY,
    instt_nm   VARCHAR(1000),
    instt_ty   VARCHAR(2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_rev_instt_dev
(
    instt_cd   VARCHAR(30),
    evl_year  VARCHAR(4),
    instt_nm   VARCHAR(1000),
    instt_ty   VARCHAR(2),
    sttus_cd  VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (instt_cd, evl_year),
    FOREIGN KEY fk_tb_rev_instt_instt_cd (instt_cd) REFERENCES tb_rev_instt_info (instt_cd)
);