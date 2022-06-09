package kr.co.data_status_evaluation.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.LmsInstitution;
import kr.co.data_status_evaluation.model.RestMessage;
import kr.co.data_status_evaluation.model.enums.StatusEnum;
import kr.co.data_status_evaluation.model.vo.LmsInstitutionVo;
import kr.co.data_status_evaluation.service.InstitutionService;
import kr.co.data_status_evaluation.service.LmsInstitutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("/rest/institution")
@RestController
@RequiredArgsConstructor
public class RestInstitutionController {
    private static final String TAG = "[RestInstitutionController] ";

    private final LmsInstitutionService lmsInstitutionService;
    private final InstitutionService institutionService;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 목록등록관리시스템 기관 정보 단건 UPSERT, 기관 유형은 처리하지 않음.
     */
    @PostMapping("")
    public ResponseEntity upsert(@RequestBody String requestBody) {
        try {
            String message = "";
            // Json to Object
            LmsInstitutionVo lmsInstitutionVo = mapper.readValue(requestBody, LmsInstitutionVo.class);
            if (lmsInstitutionVo.isValueEmpty()) throw new Exception("기관 코드, 기관 명칭, 존립 여부는 필수 입력 값입니다.");
            // UPSERT LmsInstitution
            message += lmsInstitutionService.upsert(lmsInstitutionVo);
            // UPSERT Institution
            message += institutionService.upsert(lmsInstitutionVo);
            // 변경 이전 기관 코드들의 데이터들을 현재 새로운 기관 코드로 다시 mapping 해주어야 하는 경우
            if (lmsInstitutionService.isTargetForRemapping(lmsInstitutionVo))
                message += lmsInstitutionService.remap(lmsInstitutionVo);
            else
                message += "REMAPPING: 현재 폐지된 기관이거나, 이전 기관 코드가 존재하지 않습니다.";
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.OK)
                    .message(message)
                    .data(lmsInstitutionVo).build();
            return new ResponseEntity(restMessage, null, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.INTERNAL_SEVER_ERROR)
                    .message(e.getMessage()).build();
            return new ResponseEntity(restMessage, null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 목록등록관리시스템 기관 정보 migration
     */
    @PostMapping("/migrate")
    public ResponseEntity initiate(@RequestBody String requestBody) {
        try {
            // json array to object array
            LmsInstitutionVo[] lmsInstitutionVos = mapper.readValue(requestBody, LmsInstitutionVo[].class);
            // UPSERT 대상 instt Code, vo
            List<String> insttCdListForUpsert = new ArrayList<>();
            List<LmsInstitutionVo> voListForUpsert = new ArrayList<>();
            for (LmsInstitutionVo vo : lmsInstitutionVos) {
                if (!vo.isValueEmpty()) {
                    insttCdListForUpsert.add(vo.getInsttCd());
                    voListForUpsert.add(vo);
                }
            }
            // UPSERT LmsInstitution
            lmsInstitutionService.upsertAll(voListForUpsert, insttCdListForUpsert);
            // UPSERT Institution
            institutionService.upsertAll(voListForUpsert, insttCdListForUpsert);
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.OK)
                    .message("UPSERT Institution List").build();
            return new ResponseEntity(restMessage, null, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.INTERNAL_SEVER_ERROR)
                    .message(e.getMessage()).build();
            return new ResponseEntity(restMessage, null, HttpStatus.BAD_REQUEST);
        }
    }
}
