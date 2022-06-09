package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.enums.BoardType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class NewBoardVo {
    private String subject;
    private String content;
    private BoardType type;
    private MultipartFile[] atchFiles;
    private String[] deleteMaterialIds;
    private boolean noticeYn = false;
    private boolean smsgYn = false;
    private boolean hiddenYn = false;

    public boolean hasAtchFiles() {
        return (this.atchFiles.length > 0 && !this.atchFiles[0].isEmpty());
    }

    public boolean hasDeletedMaterialIds() {
        return (this.deleteMaterialIds != null && this.deleteMaterialIds.length > 0);
    }
}
