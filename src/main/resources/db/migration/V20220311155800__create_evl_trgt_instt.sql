CREATE TABLE IF NOT EXISTS tb_rev_evl_trgt_instt
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    instt_id        BIGINT,
    evl_yr          CHAR(4),
    trgt_instt_yn   CHAR(1) DEFAULT 'N',
    FOREIGN KEY (instt_id) REFERENCES tb_rev_instt (id) ON DELETE CASCADE
)