package kr.co.data_status_evaluation.controller;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.model.enums.BoardType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.model.search.BoardSearchParam;
import kr.co.data_status_evaluation.model.vo.NewBoardVo;
import kr.co.data_status_evaluation.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/commons")
@RequiredArgsConstructor
@Slf4j
public class CommonController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BoardService boardService;
    private final AccountService accountService;
    private final FileService fileService;
    private final MaterialService materialService;
    private final AnswerService answerService;

    @GetMapping("/notices")
    public String getIndexByNotice(@SortDefault.SortDefaults({
            @SortDefault(sort = "noticeYn", direction = Sort.Direction.DESC),
            @SortDefault(sort = "hiddenYn", direction = Sort.Direction.DESC),
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
    }) Pageable pageable, Model model, @ModelAttribute BoardSearchParam searchParam) {
        searchParam.setBoardType(BoardType.NOTICE);
        Account currentUser = (Account) model.getAttribute("currentUser");
        if (!Objects.isNull(currentUser) && currentUser.isAdmin()) {
            searchParam.setHidden(false);
        }

        Pagination pagination = new Pagination(pageable);
        Page<Board> boardList = boardService.findAllBySearchParam(pagination, searchParam);

        pagination.setTotalPages(boardList.getTotalPages());
        pagination.setTotalElements(boardList.getTotalElements());
        pagination.setQuery(searchParam.getQuery());

        model.addAttribute("pagination", pagination);
        model.addAttribute("notices", boardList.getContent());
        model.addAttribute("activeMenu", "notice");

        return "app/commons/indexByNotice";
    }

    @GetMapping("/qnas")
    public String getIndexByQna(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model, @ModelAttribute BoardSearchParam searchParam){
        Account currentUser = (Account) model.getAttribute("currentUser");
        if (!Objects.isNull(currentUser) && currentUser.isInstitution()) {
            searchParam.setInstitutionId(currentUser.getInstitution().getId());
        }

        searchParam.setBoardType(BoardType.QNA);
        Pagination pagination = new Pagination(pageable);
        Page<Board> boardList = boardService.findAllBySearchParam(pagination, searchParam);

        pagination.setTotalPages(boardList.getTotalPages());
        pagination.setTotalElements(boardList.getTotalElements());
        pagination.setQuery(searchParam.getQuery());

        model.addAttribute("pagination", pagination);
        model.addAttribute("qnas", boardList.getContent());
        model.addAttribute("activeMenu", "qna");

        return "app/commons/indexByQna";
    }

    @GetMapping("/faqs")
    public String getIndexByFaq(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model, @ModelAttribute BoardSearchParam searchParam){
        searchParam.setBoardType(BoardType.FAQ);
        Pagination pagination = new Pagination(pageable);
        Page<Board> boardList = boardService.findAllBySearchParam(pagination, searchParam);

        pagination.setTotalPages(boardList.getTotalPages());
        pagination.setTotalElements(boardList.getTotalElements());
        pagination.setQuery(searchParam.getQuery());

        model.addAttribute("pagination", pagination);
        model.addAttribute("faqs", boardList.getContent());
        model.addAttribute("activeMenu", "faq");

        return "app/commons/indexByFaq";
    }

    @GetMapping("/{id}")
    public String getShow(@CurrentUser Account user, @PathVariable("id") Long id, Model model, HttpServletRequest request){
        Optional<Board> optionalBoard = boardService.findById(id);
        if(!optionalBoard.isPresent()){
            return "app/commons/error";
        }

        Board board = optionalBoard.get();
        board.setCn(HtmlUtils.htmlUnescape(board.getCn()));
        model.addAttribute("board", board);

        board.setViewCount(board.getViewCount()+1);
        boardService.save(board);

        List<Material> materialList = materialService.findAllByMtlTyAndAtchTrgtId(MaterialType.ATCH, board.getId());

        if (board.getNttTy().equals(BoardType.NOTICE)) {
            model.addAttribute("activeMenu", "notice");
        } else if(board.getNttTy().equals(BoardType.FAQ)) {
            model.addAttribute("activeMenu", "faq");
        } else {
            model.addAttribute("activeMenu", "qna");
        }
        model.addAttribute("materialList", materialList);

        // 삭제, 수정 버튼 노출 유무
        String isDeletable = Objects.isNull(board.getAccount()) ? "false" : ( user.getName().equals(board.getAccount().getUserId()) || request.isUserInRole(Author.ADMIN.authority()) ) ? "true" : "false";
        model.addAttribute("isDeletable", isDeletable);
        String isEditable = "false";
        if (request.isUserInRole(Author.ADMIN.authority())) {
            isEditable = "true";
        } else {
            isEditable = Objects.isNull(board.getAccount()) ? "false" : user.getName().equals(board.getAccount().getUserId()) ? "true" : "false";
        }
        model.addAttribute("isEditable", isEditable);

        return "app/commons/show";
    }

    @GetMapping("/new")
    public String getNew(@RequestParam("boardType") BoardType type, Model model, HttpServletRequest request){
        if (type.equals(BoardType.NOTICE)) {
            model.addAttribute("activeMenu", "notice");
        } else if(type.equals(BoardType.FAQ)) {
            model.addAttribute("activeMenu", "faq");
        } else {
            model.addAttribute("activeMenu", "qna");
        }

        if(type.equals(BoardType.NOTICE) || type.equals(BoardType.FAQ)){
            if(!request.isUserInRole(Author.ADMIN.authority())){
                return "app/commons/error";
            }
        }
        Board newBoard = new Board(type);
        newBoard.setNttTy(type);
        model.addAttribute("board", newBoard);
        return "app/commons/new";
    }

    @GetMapping("/answer/new/{id}")
    public String getNewAnswer(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        if (!request.isUserInRole(Author.ADMIN.authority())) {
            return "app/commons/error";
        }
        Optional<Board> optionalBoard = boardService.findById(id);
        if (!optionalBoard.isPresent()) {
            return "app/commons/error";
        }

        if (optionalBoard.get().getNttTy().equals(BoardType.NOTICE)) {
            model.addAttribute("activeMenu", "notice");
        } else if(optionalBoard.get().getNttTy().equals(BoardType.FAQ)) {
            model.addAttribute("activeMenu", "faq");
        } else {
            model.addAttribute("activeMenu", "qna");
        }

        Board board = optionalBoard.get();
        model.addAttribute("board", board);
        List<Material> materialList = materialService.findAllByMtlTyAndAtchTrgtId(MaterialType.ATCH, board.getId());
        model.addAttribute("materialList", materialList);
        return "app/commons/newAnswer";
    }

    @GetMapping("/edit/{id}")
    public String getNewForUpdate(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes,
                                  HttpServletRequest request){
        Optional<Board> optionalBoard = boardService.findById(id);
        if(!optionalBoard.isPresent()){
            return "app/commons/error";
        }
        Board board = optionalBoard.get();

        // get logged in user's info
        if (!request.isUserInRole(Author.ADMIN.authority())) {
            User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!user.getUsername().equals(board.getAccount().getUserId())){
                redirectAttributes.addFlashAttribute("message", "Account Access Denied");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/commons/"+id;
            }
        }

        // 첨부파일
        List<Material> materialList = materialService.findAllByMtlTyAndAtchTrgtId(MaterialType.ATCH, board.getId());

        if (optionalBoard.get().getNttTy().equals(BoardType.NOTICE)) {
            model.addAttribute("activeMenu", "notice");
        } else if(optionalBoard.get().getNttTy().equals(BoardType.FAQ)) {
            model.addAttribute("activeMenu", "faq");
        } else {
            model.addAttribute("activeMenu", "qna");
        }

        model.addAttribute("board", board);
        model.addAttribute("materialList", materialList);
        return "app/commons/new";
    }

    /**
     * 게시판 글 생성
     * @param boardVo
     * @return
     */
    @PostMapping("")
    @Transactional
    public String create(@ModelAttribute NewBoardVo boardVo){
        // get logged in user's info
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findByUserId(user.getUsername());
        if (account == null)
            return "app/commons/error";

        Board newBoard = new Board(boardVo);
        newBoard.setAccount(account);
        boardService.save(newBoard);

        // 저장경로: /.../upload/{MaterialType}/{atchTrgtId}/{currentTimestamp}_{seq}.{ext}
        if (boardVo.getAtchFiles().length > 0 && !boardVo.getAtchFiles()[0].isEmpty()) {
            String dirPath = fileService.getDirPath(MaterialType.ATCH, newBoard.getId());
            fileService.initDir(dirPath);

            int i = 0;
            for (MultipartFile multipartFile : boardVo.getAtchFiles()) {
                File file = new File(String.format("/%s/%d", MaterialType.ATCH, newBoard.getId()), i, multipartFile);
                fileService.save(file);
                Material material = Material.builder()
                        .mtlTy(MaterialType.ATCH)
                        .file(file)
                        .atchTrgtId(newBoard.getId())
                        .build();
                materialService.save(material);

                fileService.uploadFile(multipartFile, file);
                i++;
            }
        }
        return "redirect:/commons/"+newBoard.getNttTy().name().toLowerCase()+"s";
    }

    @PostMapping("/{id}")
    @Transactional
    public String update(@PathVariable("id") Long id, NewBoardVo boardVo) throws Exception{
        Optional<Board> optionalBoard = boardService.findById(id);
        if(!optionalBoard.isPresent()){
            throw new Exception("Entity Not Found");
        }
        Board boardForUpdate = optionalBoard.get();
        boardService.update(boardForUpdate, boardVo);
        return "redirect:/commons/"+boardForUpdate.getNttTy().name().toLowerCase()+"s";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpServletRequest request) throws IOException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findByUserId(userDetails.getUsername());
        if (request.isUserInRole(Author.ADMIN.authority())
                || (Long.valueOf(request.getParameter("accountId")) == account.getId())) {
            Board board = boardService.findById(id).get();
            boardService.delete(board);
            return "redirect:/commons/" + board.getNttTy().name().toLowerCase() + "s";
        }
        return "redirect:/commons/" + id;
    }

    @PostMapping("/answer/{id}")
    @Transactional
    public String createAnswer(@PathVariable("id") Long id, @RequestParam("content") String content) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findByUserId(user.getUsername());
        if (account == null)
            return "app/commons/error";
        Board board = boardService.findById(id).get();
        Answer answer = Answer.builder()
                .account(account)
                .board(board)
                .cn(content)
                .build();
        answerService.save(answer);
        board.setAnswer(answer);
        return "redirect:/commons/" + id;
    }

    @PostMapping("/answer/{id}/edit")
    @Transactional
    public String editAnswer(@PathVariable("id") Long id, @RequestParam("content") String content) {
        Board board = boardService.findById(id).get();
        Answer answer = board.getAnswer();
        answer.setCn(content);
        answerService.save(answer);
        return "redirect:/commons/" + id;
    }

    @PostMapping("/answer/{id}/delete")
    @Transactional
    public String deleteAnswer(@PathVariable("id") Long id, HttpServletRequest request) {
        if (!request.isUserInRole(Author.ADMIN.authority())) {
            return "/app/commons/error";
        }
        Board board = boardService.findById(id).get();
        board.setAnswer(null);
        boardService.save(board);
        return "redirect:/commons/qnas";
    }

    @ModelAttribute
    public void setCurrentAccount(Model model, @CurrentUser Account currentUser) {
        Account currentAccount = this.accountService.findById(currentUser.getId());
        model.addAttribute("currentUser", currentAccount);
    }
}
