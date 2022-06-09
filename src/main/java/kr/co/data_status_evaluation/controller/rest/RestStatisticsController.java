package kr.co.data_status_evaluation.controller.rest;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.dto.EvaluationIndexDto;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.vo.EvaluationIndexVo;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationIndexService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/rest/statistics")
@RequiredArgsConstructor
public class RestStatisticsController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationIndexService evaluationIndexService;

    @GetMapping("/qualitativeRank/year/{year}")
    public ResponseEntity<?> getIndice(@PathVariable("year") String currentYear) {

        List<EvaluationIndex> indices = this.evaluationIndexService.findAllByTypeAndYear(EvaluationType.QUALITATIVE, currentYear);
        List<EvaluationIndexDto> collect = indices.stream().map(evaluationIndex -> new EvaluationIndexDto(evaluationIndex.getId(), evaluationIndex.getNo(), evaluationIndex.getName())).collect(Collectors.toList());

        return new ResponseEntity<>(collect, HttpStatus.OK);
    }

    @GetMapping("/improvementRank/year/{year}")
    public ResponseEntity<?> getEvalField(@PathVariable("year") String currentYear) {

        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);

        return new ResponseEntity<>(fields, HttpStatus.OK);
    }

    @GetMapping("/evaluationRank/year/{year}")
    public ResponseEntity<?> getEvalFieldAndIndex(@PathVariable("year") String currentYear) {

        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);

        Map<String, List<EvaluationIndexVo>> indexMap = new HashMap<>();

        fields.forEach(field -> {
            indexMap.put(field.getName(), field.getEvaluationIndices().stream().map(index -> {
                EvaluationIndexVo indexVo = new EvaluationIndexVo();
                indexVo.setId(index.getId());
                indexVo.setName(index.getName());
                return indexVo;
            }).collect(Collectors.toList()));
        });
        Result<Object, Object> result = new Result<>(fields, indexMap);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @AllArgsConstructor
    @Data
    static class Result<T, S> {
        private T field;
        private S index;
    }
}
