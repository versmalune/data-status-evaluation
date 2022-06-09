package kr.co.data_status_evaluation.model.enums;

public enum MaterialType {
    ATCH("첨부자료"),
    FORM("양식자료"),
    EVIDENCE("증빙자료");

    private final String title;

    MaterialType(String title) {
        this.title = title;
    }

    public String getType() {
        return this.title;
    }
}
