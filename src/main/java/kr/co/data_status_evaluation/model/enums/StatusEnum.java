package kr.co.data_status_evaluation.model.enums;

public enum StatusEnum {
    OK(200, "OK"),
    INTERNAL_SEVER_ERROR(500, "INTERNAL_SEVER_ERROR");

    int statusCode;
    String code;

    StatusEnum(int statusCode, String code) {
        this.statusCode = statusCode;
        this.code = code;
    }
}
