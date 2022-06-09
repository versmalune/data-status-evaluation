package kr.co.data_status_evaluation.model.enums;

public enum BoardType {
    NOTICE("공지사항"),
    QNA("Q&A"),
    FAQ("FAQ");

    private final String title;

    BoardType(String title) { this.title = title; }

    public String getType() { return this.title; }
}
