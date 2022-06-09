package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.config.SHA256CodeEncoder;
import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.AccountAdapter;
import kr.co.data_status_evaluation.model.AccountRole;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.model.search.AccountSearchParam;
import kr.co.data_status_evaluation.model.vo.LmsAccountVo;
import kr.co.data_status_evaluation.repository.AccountRepository;
import kr.co.data_status_evaluation.repository.AccountRoleRepository;
import kr.co.data_status_evaluation.repository.InstitutionRepository;
import kr.co.data_status_evaluation.specification.AccountSpecification;
import kr.co.data_status_evaluation.util.SecurityUtil;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.security.InvalidParameterException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final InstitutionRepository institutionRepository;
    private final SHA256CodeEncoder sha256CodeEncoder;
    private final AccountRoleService accountRoleService;

    @Value("${app.telnoSeed}")
    private String SEEDKEY;

    /* Generate Test Account */
    @PostConstruct
    public void initialize() {
        String testUserId = "admin1";
        String testEmail = "admin@admin.com";
        String testPassword = "admin";
        String testName = "admin";
        String testCompany = "admin";
        String testPhoneNumber = "010-1234-5678";

        Account adminAccount = this.findByUserId(testUserId);
        if (adminAccount == null) {
            adminAccount = new Account(testUserId);
            adminAccount.setEmail(testEmail);
            adminAccount.setPassword(testPassword);
            adminAccount.setName(testName);
            adminAccount.setCompany(testCompany);
            adminAccount.setPhoneNumber(testPhoneNumber);
            this.save(adminAccount, Author.ADMIN);
        }

        testUserId = "institution";
        testEmail = "institution@institution.com";
        testPassword = "institution";
        testName = "institution";
        testCompany = "institution";
        testPhoneNumber = "010-1234-5678";

        Account institutionAccount = this.findByUserId(testUserId);
        if (institutionAccount == null) {
            institutionAccount = new Account(testUserId);
            institutionAccount.setEmail(testEmail);
            institutionAccount.setPassword(testPassword);
            institutionAccount.setName(testName);
            institutionAccount.setCompany(testCompany);
            institutionAccount.setPhoneNumber(testPhoneNumber);
            institutionAccount.setInstitution(institutionRepository.findByCode("1040000"));
            this.save(institutionAccount, Author.INSTITUTION);
        }

        testUserId = "jee1707";
        testEmail = "jee1707";
        testPassword = "jee1707";
        testName = "jee1707";

        Account jee1707 = this.findByUserId(testUserId);
        if (jee1707 == null) {
            jee1707 = new Account(testUserId);
            jee1707.setEmail(testEmail);
            jee1707.setPassword(testPassword);
            jee1707.setName(testName);
            jee1707.setInstitution(institutionRepository.findByCode("6110000"));
            this.save(jee1707, Author.INSTITUTION);
        }

        testUserId = "youstina95";
        testEmail = "youstina95";
        testPassword = "youstina95";
        testName = "youstina95";

        Account youstina95 = this.findByUserId(testUserId);
        if (youstina95 == null) {
            youstina95 = new Account(testUserId);
            youstina95.setEmail(testEmail);
            youstina95.setPassword(testPassword);
            youstina95.setName(testName);
            youstina95.setInstitution(institutionRepository.findByCode("B553714"));
            this.save(youstina95, Author.INSTITUTION);
        }
    }

    public void save(Account account) {
        account.setPassword(sha256CodeEncoder.encode(account.getPassword()));
        account.setRoles(new HashSet<>(accountRoleRepository.findAll()));
        accountRepository.save(account);
    }

    public void save(Account account, Author author) {
        AccountRole accountRole = new AccountRole();
        if (Objects.isNull(account.getId())) {
            Account oldAccount = this.findByUserId(account.getUserId(), "Y");
            if (oldAccount != null) {
                account = oldAccount;
                account.setDeleted("N");
            }
            accountRole.setAuthor(author);
            accountRole.setAccount(account);
        } else {
            AccountRole accountRoleObj = accountRoleRepository.findByAccount(account);
            accountRoleObj.setAuthor(author);
            accountRoleObj.setAccount(account);

            accountRole = accountRoleObj;
        }

        if (StringUtils.isNullOrEmpty(account.getPassword())) {
            // 사용자 수정 화면에서 password 생략된 경우 기존 패스워드 Mapping
            Account orgAccount = this.findByUserId(account.getUserId());
            account.setPassword(orgAccount.getPassword());
        } else {
            account.setPassword(sha256CodeEncoder.encode(account.getPassword()));
        }

        if (!StringUtils.isNullOrEmpty(account.getPhoneNumber())) {
            account.setPhoneNumber(SecurityUtil.encryptBySeed(SEEDKEY.trim(), account.getPhoneNumber()));
        }

        accountRepository.save(account);
        accountRoleRepository.save(accountRole);

        account.getRoles().add(accountRole);
    }

    public Page<Account> findBySearchParam(Pageable pageable, AccountSearchParam searchParam) {
        Specification specification = Specification.where(AccountSpecification.search(searchParam));
        return accountRepository.findAll(specification, pageable);
    }

    public Account findById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(InvalidParameterException::new);
        try {
            if (!StringUtils.isNullOrEmpty(account.getPhoneNumber()) &&
                    account.getPhoneNumber().length() >= 32) {
                account.setPhoneNumber(SecurityUtil.decryptBySeed(SEEDKEY.trim(), account.getPhoneNumber()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Account account = this.findByUserId(userId);
        logger.debug("======== loadUserByUsername called: " + userId);
        if (account == null) {
            throw new UsernameNotFoundException("user not found");
        }
        List<GrantedAuthority> authorities = buildUserAuthority(account.getRoles());

        return new AccountAdapter(account);
//        return buildUserForAuthentication(account, authorities);
    }

    public void signIn(Account account) {
        Authentication authentication = authenticate(account);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Authentication authenticate(Account account) {
        List<GrantedAuthority> authorities = buildUserAuthority(account.getRoles());
        return new UsernamePasswordAuthenticationToken(buildUserForAuthentication(account, authorities), null, authorities);
    }

    private List<GrantedAuthority> buildUserAuthority(Set<AccountRole> roles) {
        Set<GrantedAuthority> setAuths = new HashSet<>();

        // Build user's authorities
        for (AccountRole role : roles) {
            setAuths.add(new SimpleGrantedAuthority(role.getAuthor().authority()));
        }

        List<GrantedAuthority> result = new ArrayList<>(setAuths);

        return result;
    }

    private User buildUserForAuthentication(Account account, List<GrantedAuthority> authorities) {
        return new User(account.getEmail(), account.getPassword(), true, true, true, true, authorities);
    }

    public boolean checkId(String id) {
        Account account = this.findByUserId(id);
        return account == null;
    }

    public boolean checkPw(String id, String oldPassword) {
        Account account = this.findByUserId(id);
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        SHA256CodeEncoder encoder = new SHA256CodeEncoder();

        return encoder.matches(oldPassword, account.getPassword());
    }

    public Account deleteByUserId(String mberId) {
        Account account = this.findByUserId(mberId);
        accountRepository.delete(account);
        return account;
    }

    @Transactional
    public String upsert(LmsAccountVo vo) throws Exception {
        String processType = "Account: ";
        // Author가 기관이나 관리자가 아닐 경우 저장하지 않음.
        if (Objects.isNull(vo.getStatusAuthor())) {
            processType += setDeletedIfExists(vo);
            return processType + "Member Author " + vo.getMberAuthor() + ", can not save";
        }
        // Institution을 찾을 수 없을 경우 저장하지 않음.
        Institution institution = institutionRepository.findByCodeRegardlessOfDelYn(vo.getInsttCd());
        if (institution == null) {
            processType += setDeletedIfExists(vo);
            return processType + "can not find institution. insttCd: " + vo.getInsttCd();
        }
        Account account = accountRepository.findByUserIdRegardlessOfDelYn(vo.getMberId());
        if (Objects.isNull(account)) { // INSERT
            processType += "INSERT ";
            account = new Account(vo);
        } else { // UPDATE
            processType += "UPDATE ";
            account.setFromLmsVo(vo);
        }
        account.setInstitution(institution);
        accountRepository.save(account);
        if (account.getDeleted().equals("N")) {
            processType += accountRoleService.upsert(account, vo.getStatusAuthor());
        } else {
            accountRoleRepository.deleteAllByAccount(account);
            processType += "DEACTIVATED, because of " + vo.getMberSttus();
        }
        return processType;
    }

    public Account findOneWithInstitutionById(Long id) {
        return accountRepository.findOneWithInstitutionById(id);
    }

    public Account findByUserId(String username) {
        return accountRepository.findByUserIdAndDeleted(username, "N");
    }

    public Account findByUserId(String username, String deleted) {
        return accountRepository.findByUserIdAndDeleted(username, deleted);
    }

    public void deleteAllByUserId(String userId) {
        accountRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void upsertAll(LmsAccountVo[] lmsAccountVos) throws Exception {
        List<String> mberIdListForUpsert = new ArrayList<>();
        List<String> insttCdListForUpsert = new ArrayList<>();
        List<LmsAccountVo> voListForUpsert = new ArrayList<>();
        for (LmsAccountVo vo : lmsAccountVos) {
            if (!vo.isValueEmpty() && !Objects.isNull(vo.getStatusAuthor())) {
                mberIdListForUpsert.add(vo.getMberId());
                insttCdListForUpsert.add(vo.getInsttCd());
                voListForUpsert.add(vo);
            }
        }
        // 이미 저장된 Account List
        List<Account> accountListForUpdate = accountRepository.findAllByMberIdsRegardlessOfDelYn(mberIdListForUpsert);
        Map<String, Account> accountMapForUpdate = new HashMap<>();
        if (accountListForUpdate != null && !accountListForUpdate.isEmpty()) {
            for (Account account : accountListForUpdate) {
                accountMapForUpdate.put(account.getUserId(), account);
            }
        }
        // 필요한 기관들 find
        List<Institution> institutionList = institutionRepository.findAllByInsttCdsRegardlessOfDelYn(insttCdListForUpsert);
        Map<String, Institution> institutionMap = new HashMap<>();
        if (institutionList != null && !institutionList.isEmpty()) {
            for (Institution institution : institutionList)
                institutionMap.put(institution.getCode(), institution);
        }
        List<Account> accountListForSave = new ArrayList<>();
        Map<Account, Author> accountAuthorMapForUpsert = new HashMap<>();
        for (LmsAccountVo vo : voListForUpsert) {
            Institution institution = institutionMap.getOrDefault(vo.getInsttCd(), null);
            if (Objects.isNull(institution)) continue;
            Account account = accountMapForUpdate.getOrDefault(vo.getMberId(), null);
            if (Objects.isNull(account)) { // INSERT
                account = new Account(vo);
            } else { // UPDATE
                account.setFromLmsVo(vo);
            }
            account.setInstitution(institution);
            accountListForSave.add(account);
            if (!account.isDeleted() && !account.hasAnyRole()) {
                accountAuthorMapForUpsert.put(account, vo.getStatusAuthor());
            }
        }
        accountRepository.saveAll(accountListForSave);
        accountRoleService.upsertAll(accountAuthorMapForUpsert);
    }

    /**
     * 목록등록관리시스템에서 계정 권한이 실태평가시스템에 접속할 수 없는 권한으로 변경될 경우, 해당 계정 삭제
     * 혹은, 실태평가시스템에서 찾을 수 없는 기관으로 변경될 경우, 해당 계정 삭제
     */
    @Transactional
    public String setDeletedIfExists(LmsAccountVo vo) {
        Account account = accountRepository.findByUserIdRegardlessOfDelYn(vo.getMberId());
        if (!Objects.isNull(account)) {
            account.setDeleted("Y");
            accountRoleRepository.deleteAllByAccount(account);
            return "DEACTIVATED ";
        } else {
            return "";
        }
    }

    public void remapAccount(Institution newInstt, Institution beforeInstt) {
        accountRepository.updateByBeforeInstt(newInstt.getId(), beforeInstt.getId());
    }
}
