package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.enums.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public class BoardSpecification {

    public static Specification<Board> equalBoardType(BoardType boardType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("nttTy"), boardType);
    }

    public static Specification<Board> equalQnaStatus(QnaStatus qnaStatus) {
        return ((root, query, criteriaBuilder) -> {
            Subquery<Board> subquery = query.subquery(Board.class);
            Root<Answer> answerRoot = subquery.from(Answer.class);
            Join<Answer, Board> subqueryBoard = answerRoot.join("board");
            subquery.select(subqueryBoard);
            if (qnaStatus == QnaStatus.ANSWERED) {
                return criteriaBuilder.in(root).value(subquery);
            } else {
                return criteriaBuilder.in(root).value(subquery).not();
            }
        });
    }

    public static Specification<Board> equalInstitution(Long institutionId) {
        return (root, query, cb) -> {
            Join<Board, Account> accountJoin = root.join(Board_.account);
            Join<Account, Institution> institutionJoin = accountJoin.join(Account_.institution);
            return cb.equal(institutionJoin.get("id"), institutionId);
        };
    }

    public static Specification<Board> notEqualHidden(boolean isHidden) {
        return (root, query, cb) -> cb.notEqual(root.get(Board_.hiddenYn), isHidden);
    }

    public static Specification<Board> likeSearchWordInstt(QnaSearchType qnaSearchType, String searchWord) {
        return (root, query, criteriaBuilder) -> {
            Join<Board, Account> accountJoin = root.join("account");
            Join<Account, Institution> institutionInfoJoin = accountJoin.join("institution");
            if (qnaSearchType == QnaSearchType.CODE)
                return criteriaBuilder.like(criteriaBuilder.upper(institutionInfoJoin.get("code")), "%"+searchWord.toUpperCase()+"%");
            else
                return criteriaBuilder.like(institutionInfoJoin.get("name"), "%"+searchWord+"%");
        };
    }

    public static Specification<Board> likeSearchWord(SearchType searchType, String searchWord) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (searchType == SearchType.WHOLE) {
                list.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("cn")), "%"+searchWord.toUpperCase()+"%"),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("sj")), "%"+searchWord.toUpperCase()+"%")
                ));
            } else if (searchType == SearchType.CONTENT) {
                list.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("cn")), "%"+searchWord.toUpperCase()+"%"));
            } else if (searchType == SearchType.SUBJECT) {
                list.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("sj")), "%"+searchWord.toUpperCase()+"%"));
            }

            Predicate[] predicates = new Predicate[list.size()];
            return criteriaBuilder.or(list.toArray(predicates));
        };
    }
}
