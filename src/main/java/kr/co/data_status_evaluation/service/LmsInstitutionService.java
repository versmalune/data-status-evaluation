package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.LmsInstitution;
import kr.co.data_status_evaluation.model.vo.LmsInstitutionVo;
import kr.co.data_status_evaluation.repository.InstitutionRepository;
import kr.co.data_status_evaluation.repository.LmsInstitutionRepository;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
public class LmsInstitutionService {
    private final LmsInstitutionRepository lmsInstitutionRepository;
    private final FactService factService;
    private final EvaluationResultService evaluationResultService;
    private final LogService logService;
    private final EvaluationTargetInstitutionService evaluationTargetInstitutionService;
    private final InstitutionRepository institutionRepository;
    private final AccountService accountService;
    private final LmsAccountService lmsAccountService;

    @Transactional
    public String upsert(LmsInstitutionVo vo) throws Exception {
        Optional<LmsInstitution> optionalLmsInstitution = lmsInstitutionRepository.findByCodeRegardlessOfMntnabYn(vo.getInsttCd());
        LmsInstitution lmsInstitution = null;
        String processType = "LmsInstitution: ";
        if (optionalLmsInstitution.isPresent()) { // UPDATE
            processType += "UPDATE |";
            lmsInstitution = optionalLmsInstitution.get();
            lmsInstitution.setFromLmsVo(vo);
        } else { // INSERT
            processType += "INSERT |";
            lmsInstitution = new LmsInstitution(vo);
        }
        lmsInstitutionRepository.save(lmsInstitution);
        return processType;
    }

    public void deleteByInsttCd(String insttCd) {
        lmsInstitutionRepository.deleteByInsttCd(insttCd);
    }

    public void deleteByVo(LmsInstitutionVo lmsInstitutionVo) {
        lmsInstitutionRepository.deleteAllByInsttCd(lmsInstitutionVo.getInsttCd());
    }

    @Transactional
    public void upsertAll(List<LmsInstitutionVo> voListForUpsert, List<String> insttCdListForUpsert) throws Exception {
        // UPDATE ?????? LmsInstitution
        List<LmsInstitution> lmsInstitutionListForUpdate = lmsInstitutionRepository.findAllByInsttCds(insttCdListForUpsert);
        Map<String, LmsInstitution> lmsInstitutionMapForUpdate = new HashMap<>();
        if (lmsInstitutionListForUpdate != null && !lmsInstitutionListForUpdate.isEmpty()) {
            for (LmsInstitution lmsInstitution : lmsInstitutionListForUpdate) {
                lmsInstitutionMapForUpdate.put(lmsInstitution.getInsttCd().trim(), lmsInstitution);
            }
        }
        // saveAll
        List<LmsInstitution> lmsInstitutionListForSave = new ArrayList<>();
        for (LmsInstitutionVo vo : voListForUpsert) {
            LmsInstitution lmsInstitution = lmsInstitutionMapForUpdate.getOrDefault(vo.getInsttCd(), null);
            if (lmsInstitution == null) { // INSERT
                lmsInstitution = new LmsInstitution(vo);
            } else { // UPDATE
                lmsInstitution.setFromLmsVo(vo);
            }
            lmsInstitutionListForSave.add(lmsInstitution);
        }
        lmsInstitutionRepository.saveAll(lmsInstitutionListForSave);
    }

    public boolean isTargetForRemapping(LmsInstitutionVo vo) {
        if (!StringUtils.isNullOrEmpty(vo.getBeforeInsttCd()) && !vo.getMntnabYn())
            return true;
        return false;
    }

    @Transactional
    public String remap(LmsInstitutionVo vo) {
        String beforeInsttCd = vo.getBeforeInsttCd();
        if (beforeInsttCd.length() > 7)
            return "REMAPPING: ???????????? ????????? ?????? ???????????? ????????????.";
        Institution newInstt = institutionRepository.findByCode(vo.getInsttCd());
        Institution beforeInstt = institutionRepository.findByCodeRegardlessOfDelYn(beforeInsttCd);
        factService.remapInstitutionResultFact(newInstt, beforeInstt);
        factService.remapIndexResultFact(newInstt, beforeInstt);
        evaluationResultService.remapEvalResult(newInstt, beforeInstt);
        evaluationResultService.remapEvalResultTotal(newInstt, beforeInstt);
        evaluationTargetInstitutionService.remapEvalTrgtInstt(newInstt, beforeInstt);
        logService.remapLog(newInstt, beforeInstt);
        // ?????? ???????????? ?????? ???????????? ??????.
        remapCategory(newInstt, beforeInstt);
        // ??????
        accountService.remapAccount(newInstt, beforeInstt);
        lmsAccountService.remapAccount(newInstt, beforeInstt);
        return "REMAPPING: ?????? ?????? ????????? ??????????????? ????????? ???????????? ?????? mapping???????????????.";
    }

    public void remapCategory(Institution newInstt, Institution beforeInstt) {
        if (!Objects.isNull(beforeInstt.getType()))
            newInstt.setType(beforeInstt.getType());
        if (!Objects.isNull(beforeInstt.getCategory())) {
            newInstt.setCategory(beforeInstt.getCategory());
            beforeInstt.getCategory().getInstitutions().add(newInstt);
        }
    }
}
