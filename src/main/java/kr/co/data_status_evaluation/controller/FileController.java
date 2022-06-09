package kr.co.data_status_evaluation.controller;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.File;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequestMapping("/file")
@Controller
@RequiredArgsConstructor
public class FileController {
    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    private final FileService fileService;
    private final InstitutionCategoryService institutionCategoryService;
    private final InstitutionService institutionService;
    private final EvaluationIndexService evaluationIndexService;
    private final MaterialService materialService;

    @PostMapping(value="/upload/{fileType}")
    public ResponseEntity<?> upload(@PathVariable("fileType") String fileType, MultipartFile[] files) {
        try {
            List<File> fileList = materialService.save(fileType, files);
            return ResponseEntity.ok().body(fileList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> fileDownload(@PathVariable("id") Long id) {
        return fileService.downloadFile(id);
    }

    @GetMapping("/download/zip/insttId/{insttId}/indexId/{indexId}")
    public ResponseEntity<Resource> zipDownloadEvaluationResultFilesByInsttIdAndIndexId(@PathVariable("insttId") Long insttId,
                                                                       @PathVariable("indexId") Long indexId) throws IOException {
        String zipName = insttId + "-" + indexId + ".zip"; // 다운로드용으로 서버에 생성되는 zip 파일명, 사용자에게 다운로드되는 파일명은 js단에 설정
        String strZipPath = uploadPath + "/evaluationResult/zip/" + zipName;
        Path zipPath = Paths.get(strZipPath);
        if (Files.exists(zipPath)) {
            int cnt = 0;
            do {
                zipName = insttId + "-" + indexId + "-" + cnt++ + ".zip";
                strZipPath = uploadPath + "/evaluationResult/zip/" + zipName;
                zipPath = Paths.get(strZipPath);
            } while(Files.exists(zipPath));
        }

        Institution institution = institutionService.findById(insttId);
        EvaluationIndex evaluationIndex = evaluationIndexService.findById(indexId);
        EvaluationResult evaluationResult = institution.getEvaluationResult(evaluationIndex);
        Set<File> files = evaluationResult.getFiles();

        FileOutputStream fout = null;
        ZipOutputStream zout = null;
        FileInputStream fin = null;
        try {
            // 복사할 대상 폴더가 있는지 체크해서 없으면 생성
            if (!Files.exists(zipPath.getParent())) {
                Files.createDirectories(zipPath.getParent());
            }

            fout = new FileOutputStream(strZipPath);
            zout = new ZipOutputStream(fout);

            for (File file : files) {
                String originalFilename = file.getOrgnlFileNm();
                String strPath = uploadPath + file.getRelativePath();

                zout.putNextEntry(new ZipEntry(originalFilename));

                fin = new FileInputStream(strPath);

                StreamUtils.copy(fin, zout);
                zout.closeEntry();

                if (!Objects.isNull(fin)) fin.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (!Objects.isNull(fin)) fin.close();
            if (!Objects.isNull(zout)) zout.close();
            if (!Objects.isNull(fout)) fout.close();
        }

        Resource resource = new InputStreamResource(Files.newInputStream(zipPath));

        String filename = URLEncoder.encode(zipName, "UTF-8");

        // 다운로드용으로 만들었던 zip 파일 삭제
        Files.deleteIfExists(zipPath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("Application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/download/zip/indexId/{indexId}")
    public ResponseEntity<Resource> zipDownloadEvaluationResultFilesByIndexId(@PathVariable("indexId") Long indexId) throws IOException {
        String zipName = indexId + ".zip"; // 다운로드용으로 서버에 생성되는 zip 파일명, 사용자에게 다운로드되는 파일명은 js단에 설정
        String strZipPath = uploadPath + "/evaluationResult/zip/" + zipName;
        Path zipPath = Paths.get(strZipPath);
        if (Files.exists(zipPath)) {
            int cnt = 0;
            do {
                zipName = indexId + "-" + cnt++ + ".zip";
                strZipPath = uploadPath + "/evaluationResult/zip/" + zipName;
                zipPath = Paths.get(strZipPath);
            } while(Files.exists(zipPath));
        }

        EvaluationIndex evaluationIndex = evaluationIndexService.findById(indexId);
        List<EvaluationResult> evaluationResults = evaluationIndex.getResults();

        Set<File> totalFiles = new HashSet<>();

        // 해당 index에 속한 모든 Result의 파일을 totalFiles에 수집
        for (EvaluationResult evaluationResult : evaluationResults) {
            Set<File> files = evaluationResult.getFiles();
            if (files.size() > 0)
                totalFiles.addAll(files);
        }

        FileOutputStream fout = null;
        ZipOutputStream zout = null;
        FileInputStream fin = null;
        try {
            // 복사할 대상 폴더가 있는지 체크해서 없으면 생성
            if (!Files.exists(zipPath.getParent())) {
                Files.createDirectories(zipPath.getParent());
            }

            fout = new FileOutputStream(strZipPath);
            zout = new ZipOutputStream(fout);

            for (File file : totalFiles) {
                String originalFilename = file.getOrgnlFileNm();
                String strPath = uploadPath + file.getRelativePath();

                zout.putNextEntry(new ZipEntry(originalFilename));

                fin = new FileInputStream(strPath);

                StreamUtils.copy(fin, zout);
                zout.closeEntry();

                if (!Objects.isNull(fin)) fin.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (!Objects.isNull(fin)) fin.close();
            if (!Objects.isNull(zout)) zout.close();
            if (!Objects.isNull(fout)) fout.close();
        }

        Resource resource = new InputStreamResource(Files.newInputStream(zipPath));

        String filename = URLEncoder.encode(zipName, "UTF-8");

        // 다운로드용으로 만들었던 zip 파일 삭제
        Files.deleteIfExists(zipPath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("Application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/download/zip/categoryId/{categoryId}")
    public ResponseEntity<Resource> zipDownloadEvaluationResultFilesByCategoryId(@PathVariable("categoryId") Long categoryId,
                                                                                 @RequestParam(value = "year") String year) throws IOException {
        String zipName = year + "-" + categoryId + ".zip"; // 다운로드용으로 서버에 생성되는 zip 파일명, 사용자에게 다운로드되는 파일명은 js단에 설정
        String strZipPath = uploadPath + "/evaluationResult/zip/category/" + zipName;
        Path zipPath = Paths.get(strZipPath);
        if (Files.exists(zipPath)) {
            int cnt = 0;
            do {
                zipName = year + "-" + categoryId + "-" + cnt++ + ".zip";
                strZipPath = uploadPath + "/evaluationResult/zip/category/" + zipName;
                zipPath = Paths.get(strZipPath);
            } while(Files.exists(zipPath));
        }

        List<File> files = institutionCategoryService.findAllFileByYearAndCategoryId(year, categoryId);

        FileOutputStream fout = null;
        ZipOutputStream zout = null;
        FileInputStream fin = null;
        try {
            // 복사할 대상 폴더가 있는지 체크해서 없으면 생성
            if (!Files.exists(zipPath.getParent())) {
                Files.createDirectories(zipPath.getParent());
            }

            fout = new FileOutputStream(strZipPath);
            zout = new ZipOutputStream(fout);

            for (File file : files) {
                String originalFilename = file.getOrgnlFileNm();
                String strPath = uploadPath + file.getRelativePath();

                zout.putNextEntry(new ZipEntry(originalFilename));

                fin = new FileInputStream(strPath);

                StreamUtils.copy(fin, zout);
                zout.closeEntry();

                if (!Objects.isNull(fin)) fin.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (!Objects.isNull(fin)) fin.close();
            if (!Objects.isNull(zout)) zout.close();
            if (!Objects.isNull(fout)) fout.close();
        }

        Resource resource = new InputStreamResource(Files.newInputStream(zipPath));

        String filename = URLEncoder.encode(zipName, "UTF-8");

        // 다운로드용으로 만들었던 zip 파일 삭제
        Files.deleteIfExists(zipPath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("Application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
