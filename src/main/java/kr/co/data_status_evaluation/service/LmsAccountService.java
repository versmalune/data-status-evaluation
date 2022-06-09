package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.LmsAccount;
import kr.co.data_status_evaluation.model.vo.LmsAccountVo;
import kr.co.data_status_evaluation.repository.LmsAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
public class LmsAccountService {

    private LmsAccountRepository lmsAccountRepository;

    public void save(LmsAccount lmsAccount) {
        lmsAccountRepository.save(lmsAccount);
    }

    @Transactional
    public String upsert(LmsAccountVo vo) throws Exception{
        String processType = "LmsAccount: ";
        LmsAccount lmsAccount = null;
        Optional<LmsAccount> optionalLmsAccount = lmsAccountRepository.findByMberId(vo.getMberId());
        if (optionalLmsAccount.isPresent()) { // UPDATE
            processType += "UPDATE ";
            lmsAccount = optionalLmsAccount.get();
            lmsAccount.setFromVo(vo);
        } else { // INSERT
            processType += "INSERT ";
            lmsAccount = new LmsAccount(vo);
        }
        lmsAccountRepository.save(lmsAccount);
        return processType;
    }

    public Optional<LmsAccount> findByMberId(String id) {
        return lmsAccountRepository.findByMberId(id);
    }

    public void deleteByMberId(String mberId) {
        lmsAccountRepository.deleteByMberId(mberId);
    }

    @Transactional
    public void upsertAll(LmsAccountVo[] lmsAccountVos) {
        // 저장하고자 하는 Account mberId List
        List<String> mberIdListForUpsert = new ArrayList<>();
        List<LmsAccountVo> voListForUpsert = new ArrayList<>();
        for (LmsAccountVo vo : lmsAccountVos) {
            if (!vo.isValueEmpty()) {
                mberIdListForUpsert.add(vo.getMberId());
                voListForUpsert.add(vo);
            }
        }
        // FOR UPDATE
        List<LmsAccount> lmsAccountListForUpdate = lmsAccountRepository.findAllByMberIds(mberIdListForUpsert);
        Map<String, LmsAccount> lmsAccountMapForUpdate = new HashMap<>();
        if (lmsAccountListForUpdate != null && !lmsAccountListForUpdate.isEmpty()) {
            for (LmsAccount lmsAccount: lmsAccountListForUpdate) {
                lmsAccountMapForUpdate.put(lmsAccount.getMberId(), lmsAccount);
            }
        }
        List<LmsAccount> lmsAccountListForSave = new ArrayList<>();
        for (LmsAccountVo vo : voListForUpsert) {
            LmsAccount lmsAccount = lmsAccountMapForUpdate.getOrDefault(vo.getMberId(), null);
            if (lmsAccount == null) { // insert
                lmsAccount = new LmsAccount(vo);
            } else { // update
                lmsAccount.setFromVo(vo);
            }
            lmsAccountListForSave.add(lmsAccount);
        }
        lmsAccountRepository.saveAll(lmsAccountListForSave);
    }

    public void remapAccount(Institution newInstt, Institution beforeInstt) {
        lmsAccountRepository.updateByBeforeInstt(newInstt.getCode(), beforeInstt.getCode());
    }
}
