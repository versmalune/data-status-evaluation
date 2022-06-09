CREATE TABLE IF NOT EXISTS tb_rev_evl_fld
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    fld_no     INT,
    fld_nm     VARCHAR(50),
    evl_yr     CHAR(4),
    fld_scr    INT       DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_rev_evl_idx
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    fld_id           BIGINT,
    evl_ty           VARCHAR(30),
    idx_no           VARCHAR(20),
    idx_nm           VARCHAR(1000),
    idx_qestn        VARCHAR(1000),
    idx_dc           VARCHAR(1000),
    evl_scr_yn       CHAR(1),
    prfrmnc_input_yn CHAR(1),
    rao_posbl_yn     CHAR(1),
    begin_dt         DATE,
    end_dt           DATE,
    ca_yn            CHAR(1)   DEFAULT 'N',
    lg_yn            CHAR(1)   DEFAULT 'N',
    mc_yn            CHAR(1)   DEFAULT 'N',
    go_yn            CHAR(1)   DEFAULT 'N',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_rev_evl_fld_tb_evl_idx (fld_id) REFERENCES tb_rev_evl_fld (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tb_rev_evl_scr
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    idx_id     BIGINT,
    idx_lvl    int,
    ca_scr     FLOAT,
    lg_scr     FLOAT,
    mc_scr     FLOAT,
    go_scr     FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_evl_idx_scr_tb_evl_idx (idx_id) REFERENCES tb_rev_evl_idx (id) ON DELETE CASCADE
);