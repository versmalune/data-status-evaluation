package kr.co.data_status_evaluation.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class InsttEvaluationDto {
    Long resultId;
    MultipartFile [] multipartFiles;
    String objection;
}
