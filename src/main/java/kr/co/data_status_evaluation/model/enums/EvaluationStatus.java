package kr.co.data_status_evaluation.model.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum EvaluationStatus {
    NONE("평가미진행", "초기 셋업 상태. 한번도 자체평가 등록을 진행하지 않은 상태"),
    START("자체실적등록", "피평가기관이 해당지표에 대한 실적이 한개 이상 등록된 상태"),
    P1_START("1차평가중", "'<등록완료> 기관을 대상으로 1차 평가를 진행 중인 상태"),
    P1_END("1차평가완료", "1차 평가를 완료한 상태"),
    OBJ_START("이의제기검토중", "(평가단)이의제기 검토 기간"),
    OBJ_END("이의제기결과확인", "기관담당자 이의제기 결과 확인 기간"),
    P2("2차평가진행중", "<이의제기 기관>을 대상으로 2차 평가를 진행 중인 상태"),
    END("최종평가완료", "최종평가가 완료된 상태");

    private final String title;
    private final String description;

    EvaluationStatus(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public static EvaluationStatus getNext(EvaluationStatus status) {
        EvaluationStatus[] statuses = EvaluationStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            if (i < statuses.length - 1 && statuses[i].equals(status)) {
                return statuses[i + 1];
            }
        }
        return EvaluationStatus.END;
    }

    public static int getIndex(EvaluationStatus status) {
        EvaluationStatus[] statuses = EvaluationStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }

    public static EvaluationStatus getStatus(int index) {
        EvaluationStatus[] statuses = EvaluationStatus.values();
        return statuses[index];
    }

    public static List<EvaluationStatus> getStatuses() {
        List<EvaluationStatus> statuses = new ArrayList<>();
        for (EvaluationStatus status : EvaluationStatus.values()) {
            if (status.equals(EvaluationStatus.P1_START) || status.equals(EvaluationStatus.P2)) {
                continue;
            }
            statuses.add(status);
        }
        return statuses;
    }
}
