ALTER TABLE tb_rev_file ADD (
    file_nm VARCHAR(300),
    orgnl_file_nm VARCHAR(300),
    file_ty CHAR(10),
    file_size NUMERIC(14),
    extsn VARCHAR(5),
    relative_path VARCHAR(320),
    seq NUMERIC(10)
);