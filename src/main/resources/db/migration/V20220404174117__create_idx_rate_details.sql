ALTER TABLE tb_rev_evl_idx_rate
    DROP COLUMN lvl;
ALTER TABLE tb_rev_evl_idx_rate
    DROP COLUMN rate;

CREATE TABLE IF NOT EXISTS tb_rev_evl_idx_rate_details
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    idx_rate_id BIGINT        NOT NULL,
    lvl         NUMERIC(3, 0) NOT NULL,
    rate        NUMERIC(6, 3) NOT NULL,
    FOREIGN KEY (idx_rate_id) REFERENCES tb_rev_evl_idx_rate (id) ON DELETE CASCADE
);