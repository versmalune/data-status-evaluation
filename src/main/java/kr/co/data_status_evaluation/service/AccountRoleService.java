package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.AccountRole;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.repository.AccountRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountRoleService {
    private final AccountRoleRepository accountRoleRepository;

    public String upsert(Account account, Author author) {
        AccountRole accountRole = accountRoleRepository.findByAuthorAndAccount(author, account);
        if (Objects.isNull(accountRole)) {
            accountRoleRepository.deleteAllByAccount(account);
            accountRole = AccountRole.builder()
                    .author(author)
                    .account(account).build();
            account.getRoles().clear();
            account.getRoles().add(accountRole);
            accountRoleRepository.save(accountRole);
            return "AccountRole: INSERT after DELETE";
        }
        return "AccountRole: not changed";
    }

    public void upsertAll(Map<Account, Author> accountAuthorMapForUpsert) {
        List<AccountRole> accountRoleListForSave = new ArrayList<>();
        for (Account account: accountAuthorMapForUpsert.keySet()) {
            Author author = accountAuthorMapForUpsert.get(account);
            AccountRole accountRole = accountRoleRepository.findByAuthorAndAccount(author, account);
            if (Objects.isNull(accountRole)) {
                accountRoleRepository.deleteAllByAccount(account);
                accountRole = AccountRole.builder()
                        .author(author)
                        .account(account).build();
                account.getRoles().add(accountRole);
                accountRoleListForSave.add(accountRole);
            }
        }
        accountRoleRepository.saveAll(accountRoleListForSave);
    }
}
