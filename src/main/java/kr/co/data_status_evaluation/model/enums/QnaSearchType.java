package kr.co.data_status_evaluation.model.enums;

import lombok.Getter;

@Getter
public enum QnaSearchType {
    NAME("기관명"),
    CODE("기관코드");

    private String title;

    QnaSearchType(String title) {
        this.title = title;
    }
}
