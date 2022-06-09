CREATE TABLE IF NOT EXISTS tb_rev_evl_schd
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    schd_nm  VARCHAR(200),
    evl_yr   CHAR(4),
    begin_dt DATE,
    end_dt   DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);