package kr.co.data_status_evaluation.model.enums;

import java.util.ArrayList;
import java.util.List;

public enum Author {
    ADMIN("관리자"),
    INSTITUTION("피평가기관"),
    COMMITTEE("실태평가단");

    private final String title;

    Author(String title) {
        this.title = title;
    }

    public String authority() {
        return "ROLE_" + this.name();
    }

    public String getTitle() {
        return this.title;
    }

    public static List<Author> getValuesNotInstitution() {
        List<Author> authors = new ArrayList<>();
        for (Author author : Author.values()) {
            if (author.equals(Author.INSTITUTION)) {
                continue;
            }
            authors.add(author);
        }
        return authors;
    }
}
