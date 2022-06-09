package kr.co.data_status_evaluation.model.enums;

public enum SearchType {
    WHOLE("전체"),
    SUBJECT("제목"),
    CONTENT("내용");

    private String title;

    SearchType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
