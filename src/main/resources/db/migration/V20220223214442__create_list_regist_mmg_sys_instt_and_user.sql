CREATE TABLE IF NOT EXISTS tb_rev_list_regist_mmg_sys_instt_info (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    instt_cd    VARCHAR(50),
    instt_nm    VARCHAR(50),
    ty_cl_large    VARCHAR(2),
    ty_cl_medium    VARCHAR(2),
    ty_cl_small    VARCHAR(2),
    mntnab_yn    CHAR(1) DEFAULT ('N'),
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_rev_list_regist_mmg_sys_instt_user_info (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    mber_id    VARCHAR (50),
    mber_sttus    VARCHAR (50),
    mber_author    VARCHAR (50),
    password    VARCHAR (50),
    instt_cd    VARCHAR (50),
    charger_nm    VARCHAR (50),
    charger_email    VARCHAR (50),
    charger_telno_enc    VARCHAR (50),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );