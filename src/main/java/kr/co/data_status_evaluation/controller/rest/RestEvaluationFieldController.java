package kr.co.data_status_evaluation.controller.rest;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/rest/evalField")
@RequiredArgsConstructor
public class RestEvaluationFieldController {

    private final EvaluationFieldService evaluationFieldService;

    @GetMapping("/year/{year}")
    public ResponseEntity<?> getEvalField(@PathVariable("year") String currentYear) {

        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);

        return new ResponseEntity<>(fields, HttpStatus.OK);
    }
}
