CREATE TABLE IF NOT EXISTS tb_rev_instt
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    instt_cd   VARCHAR(30),
    instt_nm   VARCHAR(1000),
    instt_ty   VARCHAR(2),
    deleted    CHAR(1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );