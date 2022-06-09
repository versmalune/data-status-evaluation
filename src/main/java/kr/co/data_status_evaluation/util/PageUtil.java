package kr.co.data_status_evaluation.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {

    /**
     * List를 Page 객체로 변환하는 제네릭 메서드
     */
    public static Page<?> listToPage(List<?> list, Pageable pageable) {
        int total = list.size();
        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min((start + pageable.getPageSize()), total);

        List<?> subList = new ArrayList<>();
        if (start <= end) {
            subList = list.subList(start, end);
        }
        return new PageImpl<>(subList, pageable, total);
    }
}
