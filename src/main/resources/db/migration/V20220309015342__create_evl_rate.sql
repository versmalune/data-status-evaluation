CREATE TABLE IF NOT EXISTS tb_rev_evl_idx_rate
(
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    evl_yr CHAR(4),
    lvl    INT,
    rate   INT
)