package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.vo.EvaluationScheduleVo;
import kr.co.data_status_evaluation.model.vo.EvaluationScheduleWrap;
import kr.co.data_status_evaluation.repository.EvaluationScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationScheduleService {

    private final EvaluationScheduleRepository evaluationScheduleRepository;

    public EvaluationSchedule findByYearAndName(String year, EvaluationStatus name) {
        return this.evaluationScheduleRepository.findByYearAndName(year, name);
    }
    public List<EvaluationSchedule> findAllByYear(String year) {
        return this.evaluationScheduleRepository.findAllByYear(year);
    }

    public List<EvaluationSchedule> findAllByYear(String year, Sort sort) {
        return this.evaluationScheduleRepository.findAllByYear(year, sort);
    }

    public List<EvaluationSchedule> findAllByLastYear() {
        return this.evaluationScheduleRepository.findAllByLastYear();
    }

    @Transactional
    public void save(EvaluationScheduleWrap wrap) throws ParseException {
        String year = wrap.getYear();
        List<EvaluationScheduleVo> scheduleVos = wrap.getSchedules();
        List<EvaluationSchedule> schedules = new ArrayList<>();

        for (EvaluationScheduleVo vo : scheduleVos) {
            EvaluationSchedule schedule = new EvaluationSchedule(vo);
            schedule.setYear(year);
            schedules.add(schedule);
        }

        this.save(schedules);
    }

    @Transactional
    public void save(List<EvaluationSchedule> schedules) {
        this.evaluationScheduleRepository.saveAll(schedules);
    }

    public void update(EvaluationScheduleWrap wrap) throws ParseException {
        String year = wrap.getYear();
        List<EvaluationScheduleVo> scheduleVos = wrap.getSchedules();
        List<EvaluationSchedule> schedules = this.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt"));
        for (int i = 0; i < schedules.size(); i++) {
            schedules.get(i).setYear(year);
            schedules.get(i).setBeginDt(scheduleVos.get(i).getBeginDt());
            schedules.get(i).setEndDt(scheduleVos.get(i).getEndDt());
        }

        this.save(schedules);
    }

    public String findEvlYr(Instant now) {
        EvaluationSchedule evaluationSchedule = evaluationScheduleRepository.findOneScheduleByNow(now, now);
        if (evaluationSchedule != null)
            return evaluationSchedule.getYear();
        else
            return null;
    }
}
