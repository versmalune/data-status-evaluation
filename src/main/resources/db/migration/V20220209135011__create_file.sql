CREATE TABLE IF NOT EXISTS tb_rev_file
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_rev_file_attchement
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id         BIGINT NOT NULL,
    filename        VARCHAR(255),
    origin_filename VARCHAR(255),
    content_type    VARCHAR(255),
    file_size       NUMERIC,
    ext             VARCHAR(10),
    relative_path   VARCHAR(255),
    seq             INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_tb_file_attchement_file_id (file_id) REFERENCES tb_rev_file (id) ON DELETE CASCADE
);