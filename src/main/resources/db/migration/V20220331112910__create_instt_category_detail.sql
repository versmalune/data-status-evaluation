CREATE TABLE IF NOT EXISTS tb_rev_instt_category_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    detail_category_cd VARCHAR(10),
    detail_category_nm VARCHAR(300),
    detail_category_dc VARCHAR(2000),
    creat_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updt_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES tb_rev_instt_category (id) ON DELETE CASCADE
    );