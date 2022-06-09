package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.Pagination;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.model.search.AccountSearchParam;
import kr.co.data_status_evaluation.service.AccountService;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/account")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationFieldService evaluationFieldService;

    @GetMapping("")
    public String index(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                        @CurrentUser Account account, AccountSearchParam searchParam, Model model) {
        Page<Account> accounts = accountService.findBySearchParam(pageable, searchParam);

        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(accounts.getTotalPages());
        pagination.setTotalElements(accounts.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        model.addAttribute("searchParam", searchParam);
        model.addAttribute("pagination", pagination);
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentUser", account);
        model.addAttribute("subActiveMenu", "account");

        return "app/admin/accounts/index";
    }

    @GetMapping("/{id}")
    public String showAccount(@PathVariable("id") String id, @CurrentUser Account account, Model model) {
        Account oldAccount = accountService.findById(Long.parseLong(id));
        String currentYear = (String) model.getAttribute("currentYear");
        List<EvaluationIndex> evaluationIndices = evaluationIndexService.findByYear(currentYear);

        model.addAttribute("account", oldAccount);
        model.addAttribute("currentUser", account);
        model.addAttribute("indices", evaluationIndices);
        model.addAttribute("subActiveMenu", "account");

        return "app/admin/accounts/show";
    }

    @GetMapping("/new")
    public String newAccount(@CurrentUser Account account, Model model) {
        Account newAccount = new Account();
        Institution institution = new Institution();
        newAccount.setInstitution(institution);

        model.addAttribute("account", newAccount);
        model.addAttribute("currentUser", account);
        model.addAttribute("subActiveMenu", "account");

        return "app/admin/accounts/new";
    }

    @PostMapping("")
    public String saveAccount(Account account, Author author) {
        accountService.save(account, author);
        return "redirect:/admin/account";
    }

    @PostMapping("/{id}")
    public String updateAccount(Account account, Author author) {
        accountService.save(account, author);
        return "redirect:/admin/account";
    }

    @PostMapping("/{id}/delete")
    public String removeAccount(@PathVariable("id") String id) {
        accountService.deleteById(Long.parseLong(id));

        return "redirect:/admin/account";
    }

    @PostMapping("/{userId}/checkId")
    @ResponseBody
    public ResponseEntity<?> checkId(@PathVariable String userId) {
        boolean flag = accountService.checkId(userId);
        return new ResponseEntity<>(flag, HttpStatus.OK);
    }

    @PostMapping("/{userId}/checkPw")
    @ResponseBody
    public ResponseEntity<?> checkPw(@PathVariable String userId,
                                     @RequestParam String oldPassword) {
        boolean flag = accountService.checkPw(userId, oldPassword);
        return new ResponseEntity<>(flag, HttpStatus.OK);
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "account");
    }

    @ModelAttribute
    public void setCurrentYear(Model model) {
        List<String> evlYears = this.evaluationFieldService.findYears();
        String currentYear = evlYears.get(0);
        model.addAttribute("currentYear", currentYear);
    }
}
