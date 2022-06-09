package kr.co.data_status_evaluation.model.enums;

import lombok.Getter;

@Getter
public enum InstitutionType {
    CA("중앙행정기관", "중앙행정기관, Central Administrative Agency"),
    LG("기초지방자치단체", "시군구 (지방자치단체), Local Governments"),
    MC("광역자치단체", "광역시도 (지방자치단체), Metropolitan Council"),
    GO("공공기관", "공공기관, Government Office");

    private final String title;
    private final String description;

    InstitutionType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }
}
