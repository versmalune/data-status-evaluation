package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.model.vo.LmsAccountVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "tb_rev_account")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE tb_rev_account SET del_yn = 'Y' WHERE id=?")
//@Where(clause = "del_yn='N'")
public class Account implements Serializable {
    public static final String ANONYMOUS = "ANONYMOUS";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String email;

    @Column(name = "flnm")
    private String name;

    private String password;

    @Column(name = "co")
    private String company;

    @Column(name = "enc_telno")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instt_id")
    private Institution institution;

    @CreationTimestamp
    @Column(updatable = false, name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @Column(columnDefinition = "CHAR", name = "del_yn")
    private String deleted = "N";

    @Column(name = "last_access_dt")
    private Instant lastAccessDt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @OrderBy("author ASC")
    private Set<AccountRole> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "tb_rev_account_evl_idx",
            joinColumns = @JoinColumn(name = "accnt_id"),
            inverseJoinColumns = @JoinColumn(name = "evl_idx_id"))
    private List<EvaluationIndex> indices;

    public Account(String userId) {
        this.userId = userId;
    }

    public Account(LmsAccountVo lmsAccountVo) {
        this.userId = lmsAccountVo.getMberId();
        this.email = lmsAccountVo.getChargerEmail();
        this.name = lmsAccountVo.getChargerNm();
        this.password = lmsAccountVo.getPassword();
        this.phoneNumber = lmsAccountVo.getChargerTelnoEnc();
        if (lmsAccountVo.getMberSttus().equals("MBST04") || lmsAccountVo.getMberSttus().equals("MBST11"))
            this.deleted = "Y";
        else
            this.deleted = "N";
    }

    public boolean hasAuthor(String author) {
        for (AccountRole role: this.roles) {
            if(role.getAuthor().toString().equals(author)) {
                return true;
            }
        }
        return false;
    }

    public Institution getInstitution() {
        if (this.institution == null) {
            Institution institution = new Institution(" ", " ");
            return institution;
        } else {
            return this.institution;
        }
    }

    public boolean isAdmin() {
        for (AccountRole role : this.roles) {
            if (role.getAuthor() == Author.ADMIN) {
                return true;
            }
        }
        return false;
    }

    public boolean isCommittee() {
        for (AccountRole role : this.roles) {
            if (role.getAuthor() == Author.COMMITTEE) {
                return true;
            }
        }
        return false;
    }

    public boolean isInstitution() {
        for (AccountRole role : this.roles) {
            if (role.getAuthor() == Author.INSTITUTION) {
                return true;
            }
        }
        return false;
    }

    public String getRoleString() {
        StringBuffer buf = new StringBuffer();
        for (AccountRole role : this.roles) {
            buf.append("[");
            buf.append(role.getAuthor().getTitle());
            buf.append("]");
        }
        return buf.toString();
    }

    public String getAuthorString() {
        StringBuffer buf = new StringBuffer();
        boolean isFirst = true;
        for (AccountRole role : this.roles) {
            if (!isFirst) {
                buf.append(" , ");
            }
            buf.append(role.getAuthor().getTitle());
            if (isFirst) {
                isFirst = false;
            }
        }
        return buf.toString();
    }


    public void setFromLmsVo(LmsAccountVo lmsAccountVo) {
        this.userId = lmsAccountVo.getMberId();
        this.email = lmsAccountVo.getChargerEmail();
        this.name = lmsAccountVo.getChargerNm();
        this.password = lmsAccountVo.getPassword();
        this.phoneNumber = lmsAccountVo.getChargerTelnoEnc();
        if (lmsAccountVo.getMberSttus().equals("MBST04") || lmsAccountVo.getMberSttus().equals("MBST11"))
            this.deleted = "Y";
        else
            this.deleted = "N";
    }

    public String getCompany() {
        if (this.institution == null || this.institution.getId() == null) {
            return this.company;
        }
        return Objects.isNull(this.institution) ? "" : this.institution.getName();
    }

    public String getCompanyType() {
        if (this.institution == null || this.institution.getId() == null) {
            return this.company;
        }

        return Objects.isNull(this.institution) ? "" : Objects.isNull(this.institution.getType()) ? "" : this.institution.getType();
    }

    public List<Institution> getAssignedInstitutions() {
        List<Institution> institutions = new ArrayList<>();
        for (EvaluationIndex index : this.indices) {
            institutions.addAll(index.getAssignedInstitutions());
        }

        return institutions.stream().distinct().collect(Collectors.toList());
    }

    public List<Long> getIndicesIdByYear(String year) {
        List<Long> ids = new ArrayList<>();
        for (EvaluationIndex index : this.indices) {
            if (index.getEvaluationField().getYear().equals(year)) {
                ids.add(index.getId());
            }
        }
        return ids;
    }

    public List<EvaluationIndex> getIndicesByYear(String year) {
        List<EvaluationIndex> indices = new ArrayList<>();
        for (EvaluationIndex index : this.indices) {
            if (index.getEvaluationField().getYear().equals(year)) {
                indices.add(index);
            }
        }
        return indices;
    }

    public void restoreDeleted(Account account) {
        this.email = account.getEmail();
        this.name = account.getName();
        this.password = account.getPassword();
        this.phoneNumber = account.getPhoneNumber();
        this.company = account.getCompany();
        this.deleted = "N";
    }

    public boolean hasAnyRole() {
        return !this.roles.isEmpty();
    }

    public boolean isDeleted() {
        return this.deleted.equals("Y");
    }
}
