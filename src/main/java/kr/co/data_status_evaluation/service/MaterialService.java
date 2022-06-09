package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.File;
import kr.co.data_status_evaluation.model.Material;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.repository.MaterialRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialService {
    private final MaterialRepository materialRepository;
    private final FileService fileService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    public void save(Material material) {
        materialRepository.save(material);
    }

    public List<Material> findAllByAtchTrgtId(Long id) {
        return materialRepository.findAllByAtchTrgtId(id);
    }

    public Material findFirstByMtlTyAndAtchTrgtId(MaterialType materialType, Long id) {
        return materialRepository.findFirstByMtlTyAndAtchTrgtId(materialType, id);
    }

    public List<Material> findAllByMtlTyAndAtchTrgtId(MaterialType materialType, Long id) {
        return materialRepository.findAllByMtlTyAndAtchTrgtId(materialType, id);
    }

    public List<Material> findAllByMtlTyAndAtchTrgtIds(MaterialType materialType, List<Long> ids) {
        return materialRepository.findAllByMtlTyAndAtchTrgtIdIn(materialType, ids);
    }

    public void deleteById(Long id) {
        materialRepository.deleteById(id);
    }

    public List<File> save(String fileType, MultipartFile[] files) {
        try {
            if (files.length == 0) {
                throw new Exception("ERROR : File is empty.");
            }
            String dirPath = String.format("%s/%s", uploadPath, fileType);
            fileService.initDir(dirPath);

            List<File> fileList = new ArrayList<>();
            int i = 0;
            for (MultipartFile multipartFile : files) {
                File file = new File(String.format("/%s", fileType), i, multipartFile);
                fileService.save(file);
                fileService.uploadFile(multipartFile, file);
                fileList.add(file);
                i++;
            }
            return fileList;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public List<Material> save(MaterialType materialType, Long atchTrgtId, MultipartFile[] files) {
        try {
            if (files.length == 0) throw new Exception("ERROR : File is empty.");
            String dirPath = String.format("%s/%s/%d", uploadPath, materialType, atchTrgtId);
            fileService.initDir(dirPath);
            List<Material> materialList = new ArrayList<>();
            int i = 0;
            for (MultipartFile multipartFile : files) {
                File file = new File(String.format("/%s/%d", materialType, atchTrgtId), i, multipartFile);
                fileService.save(file);
                Material material = Material.builder()
                        .mtlTy(materialType)
                        .file(file)
                        .atchTrgtId(atchTrgtId)
                        .build();
                this.save(material);
                materialList.add(material);
                fileService.uploadFile(multipartFile, file);
                i++;
            }
            return materialList;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Material save(MaterialType materialType, Long atchTrgtId, MultipartFile multipartFile) {
        try {
            if (multipartFile == null) throw new Exception("ERROR : File is empty.");
            String dirPath = String.format("%s/%s/%d", uploadPath, materialType, atchTrgtId);
            fileService.initDir(dirPath);
            File file = new File(String.format("/%s/%d", materialType, atchTrgtId), 0, multipartFile);
            fileService.save(file);
            Material material = Material.builder()
                    .mtlTy(materialType)
                    .file(file)
                    .atchTrgtId(atchTrgtId)
                    .build();
            this.save(material);
            fileService.uploadFile(multipartFile, file);
            return material;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }
}
