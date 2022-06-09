CREATE TABLE IF NOT EXISTS tb_rev_evl_result
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    instt_id   BIGINT NOT NULL,
    idx_id     BIGINT NOT NULL,
    evl_yr     CHAR(4) NOT NULL,
    score      FLOAT NOT NULL,
    process_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_rev_evl_result_tb_rev_instt (instt_id) REFERENCES tb_rev_instt (id) ON DELETE CASCADE,
    FOREIGN KEY fk_tb_rev_evl_result_tb_rev_evl_idx (idx_id) REFERENCES tb_rev_evl_idx (id) ON DELETE CASCADE
);