package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "area_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_area_progress",
                        columnNames = {"area_id", "day", "se_cd", "dtl_prcs_cd"}
                )
        }
)
@Getter
@NoArgsConstructor
public class AreaProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    /**
     * areas 테이블 FK
     * (지금 단계에서는 연관관계 안 걸고 Long으로만 유지)
     */
    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "biz_no", length = 30)
    private String bizNo;

    @Column(name = "day", nullable = false, length = 8)
    private String day;

    @Column(name = "se_cd", nullable = false, length = 10)
    private String seCd;

    @Column(name = "se_nm", length = 50)
    private String seNm;

    @Column(name = "dtl_prcs_cd", nullable = false, length = 20)
    private String dtlPrcsCd;

    // DB 컬럼명이 dtl_prcd_nm 이라서 그대로 둠 (DB 컬럼명과 반드시 일치해야 함)
    @Column(name = "dtl_prcd_nm", length = 100)
    private String dtlPrcdNm;

    @Column(name = "ttl", length = 255)
    private String ttl;

    @Column(name = "dtl_cn", columnDefinition = "text")
    private String dtlCn;

    // DB에서 DEFAULT CURRENT_TIMESTAMP면 @CreationTimestamp로 자동처리도 가능하지만
    // 지금은 DB 기본값에 맡기는 형태 (insert 시 null이면 DB가 채움)
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // ✅ 저장(import)용 setter 최소 제공

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setSeCd(String seCd) {
        this.seCd = seCd;
    }

    public void setSeNm(String seNm) {
        this.seNm = seNm;
    }

    public void setDtlPrcsCd(String dtlPrcsCd) {
        this.dtlPrcsCd = dtlPrcsCd;
    }

    public void setDtlPrcdNm(String dtlPrcdNm) {
        this.dtlPrcdNm = dtlPrcdNm;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public void setDtlCn(String dtlCn) {
        this.dtlCn = dtlCn;
    }
}
