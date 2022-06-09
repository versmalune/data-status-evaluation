package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.enums.BoardType;
import kr.co.data_status_evaluation.model.vo.NewBoardVo;
import kr.co.data_status_evaluation.util.BooleanToYNConverter;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_bbs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "accnt_id")
    private Account account;

    private String sj;

    @Enumerated(EnumType.STRING)
    private BoardType nttTy;

    private String cn;

    @Column(name = "inqire_cnt", columnDefinition = "NUMERIC(7)")
    private Long viewCount;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "board")
    private Answer answer;

    @Convert(converter = BooleanToYNConverter.class)
    private Boolean noticeYn;

    @Convert(converter = BooleanToYNConverter.class)
    private Boolean smsgYn;

    @Convert(converter = BooleanToYNConverter.class)
    private Boolean hiddenYn;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;


    public Board(NewBoardVo boardVo) {
        this.sj = boardVo.getSubject();
        this.cn = boardVo.getContent();
        this.nttTy = boardVo.getType();
        this.viewCount = Long.valueOf(0);
        this.noticeYn = boardVo.isNoticeYn();
        this.smsgYn = boardVo.isSmsgYn();
        this.hiddenYn = boardVo.isHiddenYn();
    }

    public Board(BoardType boardType) {
        this.nttTy = boardType;
    }

    public void updateWithVo(NewBoardVo boardVo) {
        this.sj = boardVo.getSubject();
        this.cn = boardVo.getContent();
        this.nttTy = boardVo.getType();
        this.noticeYn = boardVo.isNoticeYn();
        this.smsgYn = boardVo.isSmsgYn();
        this.hiddenYn = boardVo.isHiddenYn();
    }
}
