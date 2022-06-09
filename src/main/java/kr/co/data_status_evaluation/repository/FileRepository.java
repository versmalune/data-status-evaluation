package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
