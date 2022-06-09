package kr.co.data_status_evaluation.controller.rest;

import kr.co.data_status_evaluation.model.Board;
import kr.co.data_status_evaluation.model.Pagination;
import kr.co.data_status_evaluation.model.RestMessage;
import kr.co.data_status_evaluation.model.dto.LmsNoticeDto;
import kr.co.data_status_evaluation.model.enums.BoardType;
import kr.co.data_status_evaluation.model.enums.StatusEnum;
import kr.co.data_status_evaluation.model.search.BoardSearchParam;
import kr.co.data_status_evaluation.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rest/notice")
@RequiredArgsConstructor
public class RestNoticeController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity showLmsNotices(@SortDefault.SortDefaults({
            @SortDefault(sort = "noticeYn", direction = Sort.Direction.DESC),
            @SortDefault(sort = "id", direction = Sort.Direction.DESC)
    })Pageable pageable) {
        try {
            BoardSearchParam searchParam = new BoardSearchParam();
            searchParam.setBoardType(BoardType.NOTICE);
            Pagination pagination = new Pagination(pageable);
            Page<Board> noticeList = boardService.findAllBySearchParam(pagination, searchParam);
//            Page<Board> noticeList = boardService.findByNttTy(pagination, BoardType.NOTICE);
            Page<LmsNoticeDto> lmsNoticeDtoPage = noticeList.map(board -> {
                LmsNoticeDto lmsNoticeDto = new LmsNoticeDto();
                lmsNoticeDto.setId(board.getId());
                lmsNoticeDto.setSj(board.getSj());
                lmsNoticeDto.setViewCount(board.getViewCount());
                lmsNoticeDto.setCreatedAt(String.valueOf(board.getCreatedAt()));
                lmsNoticeDto.setUpdatedAt(String.valueOf(board.getUpdatedAt()));
                lmsNoticeDto.setNoticeYn(board.getNoticeYn());
                return lmsNoticeDto;
            });
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.OK)
                    .message("공지사항을 정상적으로 호출하였습니다.")
                    .data(lmsNoticeDtoPage).build();
            return new ResponseEntity(restMessage, null, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            RestMessage restMessage = RestMessage.builder()
                    .statusEnum(StatusEnum.INTERNAL_SEVER_ERROR)
                    .message("오류가 발생했습니다.").build();
            return new ResponseEntity(restMessage, null, HttpStatus.BAD_REQUEST);
        }
    }
}
