package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.File;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.repository.FileRepository;
import kr.co.data_status_evaluation.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
@Service
public class FileService {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void save(File file) {
        fileRepository.save(file);
    }

    public Optional<File> findById(Long id) {
        return fileRepository.findById(id);
    }

    public String getDirPath(MaterialType materialType, Long atchTrgtId) {
        return String.format("%s/%s/%d", uploadPath, materialType.toString(), atchTrgtId);
    }

    public void initDir(String dirPath) {
        try {
            Path root = Paths.get(dirPath);
            if (!Files.exists(root))
                Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder");
        }
    }

    public void uploadFile(MultipartFile multipartFile, File file) {
        try {
            Files.copy(multipartFile.getInputStream(), Paths.get(uploadPath + file.getRelativePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Transactional
    public boolean deleteFile(File file, EvaluationResult evaluationResult) throws IOException {
        evaluationResult.removeFile(file);
        fileRepository.deleteById(file.getId());
        boolean flag = FileUtils.deleteFile(uploadPath + file.getRelativePath());

        return flag;

    }

    public ResponseEntity<?> downloadFile(Long id) {
        try {
            File file = this.findById(id).get();
            String originalFilename = URLEncoder.encode(file.getOrgnlFileNm(), "UTF-8");
            originalFilename = originalFilename.replaceAll("\\+", "%20");

            Path path = Paths.get(uploadPath + file.getRelativePath());
            if (!Files.exists(path)) {
                return ResponseEntity.ok().body("해당 파일이 존재하지 않습니다.");
            }

            Resource resource = new InputStreamResource(Files.newInputStream(path));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("Application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
