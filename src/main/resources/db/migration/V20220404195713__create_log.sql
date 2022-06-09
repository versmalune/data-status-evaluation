CREATE TABLE IF NOT EXISTS tb_rev_log(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seq NUMERIC(10),
    user_id VARCHAR(200),
    instt_cd CHAR(7),
    actn_nm VARCHAR(100),
    acct_seq CHAR(7),
    author VARCHAR(20),
    ip_adres VARCHAR(20),
    actn_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actn_uri VARCHAR(80),
    browser VARCHAR(20),
    evl_yr CHAR(4)
    );