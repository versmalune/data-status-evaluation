package kr.co.data_status_evaluation.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class AccountDto {
    private String userId;

    private String email;

    private String name;

    private String password;

    private String company;

    private String phoneNumber;

    private Long institutionId;
}
