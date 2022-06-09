CREATE TABLE IF NOT EXISTS tb_rev_atch_material
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bbs_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_rev_bbs_id(bbs_id) REFERENCES tb_rev_bbs (id) ON DELETE CASCADE,
    FOREIGN KEY fk_tb_rev_file_id(file_id) REFERENCES tb_rev_file (id) ON DELETE CASCADE
    );