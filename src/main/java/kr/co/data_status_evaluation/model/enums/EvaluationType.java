package kr.co.data_status_evaluation.model.enums;

public enum EvaluationType {
    QUANTITATION("정량평가"),
    QUALITATIVE("정성평가");

    private final String title;

    EvaluationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
