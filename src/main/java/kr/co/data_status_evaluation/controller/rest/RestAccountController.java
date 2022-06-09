package kr.co.data_status_evaluation.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.LmsAccount;
import kr.co.data_status_evaluation.model.RestMessage;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.model.enums.StatusEnum;
import kr.co.data_status_evaluation.model.vo.LmsAccountVo;
import kr.co.data_status_evaluation.service.AccountService;
import kr.co.data_status_evaluation.service.LmsAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/rest/account")
@RestController
@RequiredArgsConstructor
public class RestAccountController {
    private static final String TAG = "[RestAccountController] ";

    private final LmsAccountService lmsAccountService;
    private final AccountService accountService;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 목록등록관리시스템에서 account 등록, 수정 시 실태평가시스템에도 반영하는 API
     */
    @PostMapping("")
    public ResponseEntity upsert(@RequestBody String requestBody){
        try {
            // Json to Object
            LmsAccountVo lmsAccountVo = mapper.readValue(requestBody, LmsAccountVo.class);
            if (lmsAccountVo.isValueEmpty()) throw new Exception("ID, Status, Author, PW, Institution Code, Charger Name 값은 필수 입력 입니다.");
            String message = "";
            // LmsAccount UPSERT
            message += lmsAccountService.upsert(lmsAccountVo);
            // Account UPSERT
            message += accountService.upsert(lmsAccountVo);
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.OK)
                    .message(message)
                    .data(lmsAccountVo).build();
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
     * 목록등록관리시스템의 account들을 migration할 떄 호출되는 API
     */
    @PostMapping("/migrate")
    public ResponseEntity initiate(@RequestBody String requestBody) {
        try {
            // Json array to object array
            LmsAccountVo[] lmsAccountVos = mapper.readValue(requestBody, LmsAccountVo[].class);
            // UPSERT LmsAccount
            lmsAccountService.upsertAll(lmsAccountVos);
            // UPSERT ACCOUNT
            accountService.upsertAll(lmsAccountVos);
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.OK)
                    .message("Account값들을 정상적으로 처리하였습니다.").build();
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
