package kr.co.data_status_evaluation.model.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResultUploadFileVo {
    private MultipartFile[] uploadFiles;
}
