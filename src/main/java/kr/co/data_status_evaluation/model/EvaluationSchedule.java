package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.vo.EvaluationScheduleVo;
import kr.co.data_status_evaluation.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_rev_evl_schd")
@DynamicUpdate
public class EvaluationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schd_nm")
    @Enumerated(EnumType.STRING)
    private EvaluationStatus name;

    @Column(name = "evl_yr", columnDefinition = "CHAR")
    private String year;

    @Column(name = "begin_dt")
    private Instant beginDt;

    @Column(name = "end_dt")
    private Instant endDt;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date getBeginDt() {
        return Objects.isNull(this.beginDt) ? null : Date.from(this.beginDt);
    }

    public Instant getBeginDtOrigin() {
        return this.beginDt;
    }

    public void setBeginDt(Date beginDt) {
        this.beginDt = Objects.isNull(beginDt) ? null : beginDt.toInstant();
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date getEndDt() {
        return Objects.isNull(this.endDt) ? null : Date.from(this.endDt);
    }

    public Instant getEndDtOrigin() {
        return this.endDt;
    }

    public void setEndDt(Date endDt) {
        this.endDt = Objects.isNull(endDt) ? null : endDt.toInstant();
    }

    public EvaluationSchedule(EvaluationScheduleVo vo) throws ParseException {
        setName(vo.getName());
        setBeginDt(vo.getBeginDt());
        setEndDt(vo.getEndDt());
    }

    public String getDateRange() {
        DateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일");

        StringBuffer sb = new StringBuffer();
        sb.append(df.format(this.getBeginDt()));
        sb.append(" - ");
        sb.append(df.format(this.getEndDt()));

        return sb.toString();
    }

    public boolean isDone() {
        Date current = new Date();
        Date endDt = DateUtils.getDateWithLastTime(this.getEndDt());
        return current.after(endDt);
    }

    public boolean isActive() {
        Date current = new Date();
        Date endDt = DateUtils.getDateWithLastTime(this.getEndDt());
        return current.after(this.getBeginDt()) && current.before(endDt);
    }

    public boolean isNone() {
        Date current = new Date();
        return current.before(this.getBeginDt());
    }
}
