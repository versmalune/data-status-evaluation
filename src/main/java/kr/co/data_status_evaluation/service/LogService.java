package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.Log;
import kr.co.data_status_evaluation.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final EvaluationScheduleService evaluationScheduleService;

    public void logLogin(Account account) {
        String evlYr = evaluationScheduleService.findEvlYr(account.getLastAccessDt());
        Log log = Log.builder()
                .userId(account.getUserId())
                .insttCd(Objects.isNull(account.getInstitution()) ? null : account.getInstitution().getCode())
                .actionName("로그인")
                .actionDt(account.getLastAccessDt())
                .year(evlYr).build();
        logRepository.save(log);
    }

    public void logEvalResult(EvaluationResult evaluationResult, Account account) {
        Log log = Log.builder()
                .userId(account.getUserId())
                .insttCd(account.getInstitution().getCode())
                .actionName("실태평가 파일 등록")
                .actionDt(Instant.now())
                .year(evaluationResult.getYear()).build();
        logRepository.save(log);
    }

    public void remapLog(Institution newInstt, Institution beforeInstt) {
        logRepository.updateByBeforeInstt(newInstt.getCode(), beforeInstt.getCode());
    }
}
