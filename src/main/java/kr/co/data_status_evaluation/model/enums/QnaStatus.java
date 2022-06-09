package kr.co.data_status_evaluation.model.enums;

import lombok.Getter;

@Getter
public enum QnaStatus {
    RECEIVED("접수"),
    ANSWERED("답변완료");

    private final String title;

    QnaStatus(String title) {
        this.title = title;
    }
}
