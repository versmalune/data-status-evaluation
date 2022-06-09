package kr.co.data_status_evaluation.model.enums;

public enum EvaluationFieldType {
    A("관리체계"),
    B("개방"),
    C("활용"),
    D("품질"),
    E("기타(가감점)");

    private final String title;

    EvaluationFieldType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
