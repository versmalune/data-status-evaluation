package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Board;
import kr.co.data_status_evaluation.model.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BoardRepository  extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    Page<Board> findAllByNttTy(Pageable pageable, BoardType nttTy);
    List<Board> findTop5ByNttTy(BoardType boardType, Sort sort);
}
