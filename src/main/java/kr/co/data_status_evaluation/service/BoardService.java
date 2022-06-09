package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.Board;
import kr.co.data_status_evaluation.model.File;
import kr.co.data_status_evaluation.model.Material;
import kr.co.data_status_evaluation.model.enums.BoardType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.search.BoardSearchParam;
import kr.co.data_status_evaluation.model.vo.NewBoardVo;
import kr.co.data_status_evaluation.repository.BoardRepository;
import kr.co.data_status_evaluation.repository.FileRepository;
import kr.co.data_status_evaluation.repository.MaterialRepository;
import kr.co.data_status_evaluation.specification.BoardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final FileRepository fileRepository;
    private final MaterialRepository materialRepository;

    private final FileService fileService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    public Page<Board> findByNttTy(Pageable pageable, BoardType nttTy){
        return boardRepository.findAllByNttTy(pageable, nttTy);
    }

    public Optional<Board> findById(Long id){
        return boardRepository.findById(id);
    }

    public Page<Board> findAllBySearchParam(Pageable pageable, BoardSearchParam boardSearchParam) {
        Specification<Board> specification = Specification.where(BoardSpecification.equalBoardType(boardSearchParam.getBoardType()));

        if(boardSearchParam.getBoardType().equals(BoardType.NOTICE) && boardSearchParam.isHidden()) {
            specification = specification.and(BoardSpecification.notEqualHidden(boardSearchParam.isHidden()));
        }
        if(boardSearchParam.getBoardType().equals(BoardType.QNA) && boardSearchParam.getInstitutionId() != null) {
            specification = specification.and(BoardSpecification.equalInstitution(boardSearchParam.getInstitutionId()));
        }

        if (boardSearchParam.getQnaSearchType() != null) { // QNA일 경우
            if (boardSearchParam.getSearchWord() != null && !boardSearchParam.getSearchWord().equals("")) {
                specification = specification.and(BoardSpecification.likeSearchWordInstt(
                        boardSearchParam.getQnaSearchType(), boardSearchParam.getSearchWord()
                ));
            }
            // qnaStatus값이 없는, 즉 첫 로드이거나 '전체'를 선택하지 않았을 경우
            if (boardSearchParam.getQnaStatus() != null) {
                specification = specification.and(BoardSpecification.equalQnaStatus(boardSearchParam.getQnaStatus()));
            }
        }
        if (boardSearchParam.getSearchType() != null) { // FAQ, 공지사항일 경우
            specification = specification.and(BoardSpecification.likeSearchWord(
                    boardSearchParam.getSearchType(), boardSearchParam.getSearchWord()
            ));
        }
        return boardRepository.findAll(specification, pageable);
    }

    public void save(Board board){
        boardRepository.save(board);
    }

    @Transactional
    public void delete(Board board) throws IOException {
        List<Material> materialList = materialRepository.findAllByMtlTyAndAtchTrgtId(MaterialType.ATCH, board.getId());
        // DB정보 삭제
        if (!materialList.isEmpty()) {
            for (Material material : materialList) {
                fileRepository.deleteById(material.getFile().getId());
            }
        }
        boardRepository.deleteById(board.getId());

        // 실제 파일 삭제
        if (!materialList.isEmpty()) {
            Material material = materialList.get(0);
            Path folderPath = Paths.get(uploadPath + material.getFile().getRelativePath()).getParent();
            boolean flag = FileSystemUtils.deleteRecursively(folderPath);
            if (!flag)
                throw new FileNotFoundException();
        }
    }

    public List<Board> findTop5ByNttTy(BoardType boardType, Sort sort) {
        return boardRepository.findTop5ByNttTy(boardType, sort);
    }

    @Transactional
    public void update(Board board, NewBoardVo vo) throws IOException {
        // 글 업데이트
        board.updateWithVo(vo);
        boardRepository.save(board);
        // 첨부 파일 삭제했다면 DB정보 삭제
        List<Material> deletedMaterialList = null;
        if (vo.hasDeletedMaterialIds()) {
            deletedMaterialList = new ArrayList<>();
            for (String i : vo.getDeleteMaterialIds()) {
                Long id = Long.parseLong(i);
                Material deletedMaterial = materialRepository.findById(id).get();
                deletedMaterialList.add(deletedMaterial);
                materialRepository.deleteById(id);
            }
        }
        // 첨부 파일 추가했다면 DB 정보 추가
        List<File> addedFileList = null;
        String dirPath = null;
        if (vo.hasAtchFiles()) {
            addedFileList = new ArrayList<>();
            dirPath = fileService.getDirPath(MaterialType.ATCH, board.getId());
            int i = 0;
            for (MultipartFile multipartFile : vo.getAtchFiles()) {
                File file = new File(String.format("/%s/%d", MaterialType.ATCH, board.getId()), i, multipartFile);
                fileService.save(file);
                addedFileList.add(file);
                Material material = Material.builder()
                        .mtlTy(MaterialType.ATCH)
                        .file(file)
                        .atchTrgtId(board.getId())
                        .build();
                materialRepository.save(material);
                i++;
            }
        }
        // 실제 파일 삭제
        if (deletedMaterialList != null) {
            for (Material deletedMaterial : deletedMaterialList) {
                Path path = Paths.get(deletedMaterial.getFile().getRelativePath());
                FileSystemUtils.deleteRecursively(path);
            }
        }
        // 실제 파일 저장
        if (addedFileList != null && dirPath != null) {
            fileService.initDir(dirPath);
            for (int idx = 0; idx < addedFileList.size(); idx++) {
                fileService.uploadFile(vo.getAtchFiles()[idx], addedFileList.get(idx));
            }
        }
    }

}
