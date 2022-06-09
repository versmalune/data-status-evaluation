package kr.co.data_status_evaluation.model;

import lombok.*;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import java.util.Set;

@Entity
@Table(name = "tb_rev_file")
@Getter
@Setter
@RequiredArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileNm;

    private String orgnlFileNm;

    private String fileTy;

    private BigInteger fileSize;

    private String extsn;

    private String relativePath;

    private int seq;

    @ManyToMany(mappedBy = "files", cascade = CascadeType.ALL)
    Set<EvaluationResult> evaluationResults;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public File(String dirPath, int seq, MultipartFile multipartFile) {
        String extsn = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String fileName = String.format("%s_%d.%s", timeStamp, seq, extsn);

        this.fileNm = fileName;
        this.orgnlFileNm = multipartFile.getOriginalFilename();
        this.fileTy = multipartFile.getContentType();
        this.fileSize = BigInteger.valueOf(multipartFile.getSize());
        this.extsn = extsn;
        this.relativePath = String.format("%s/%s", dirPath, fileName);
    }

    public String getDownloadUrl() {
        return String.format("/file/download/%d", this.id);

    }
}
