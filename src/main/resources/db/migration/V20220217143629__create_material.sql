CREATE TABLE IF NOT EXISTS tb_rev_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mtl_ty VARCHAR(30) NOT NULL,
    file_id BIGINT NOT NULL,
    atch_trgt_id NUMERIC(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_material_tb_file (file_id) REFERENCES tb_rev_file(id) ON DELETE CASCADE
    );