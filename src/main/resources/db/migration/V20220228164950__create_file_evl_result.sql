CREATE TABLE IF NOT EXISTS tb_rev_file_evl_result (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id                 BIGINT NOT NULL,
    evl_result_id           BIGINT NOT NULL
);