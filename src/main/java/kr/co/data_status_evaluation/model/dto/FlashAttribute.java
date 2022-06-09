package kr.co.data_status_evaluation.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.Errors;

import java.util.Map;

@Getter
@Setter
public class FlashAttribute {
    private Map<String, String> message;
    private String alertClass;

    public FlashAttribute() {
        this.alertClass = "alert-danger";
    }
}
