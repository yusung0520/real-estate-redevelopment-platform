package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "areas_v3")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // 이 어노테이션들이 있어야 builder()와 get/set 에러가 사라집니다.
public class AreaV3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long areaId;

    @Column(name = "present_sn", nullable = false, length = 64, unique = true)
    private String presentSn; // UQ181 고유 키

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "sigungu_cd", length = 16)
    private String sigunguCd;

    @Column(name = "emd_nm", length = 64)
    private String emdNm;

    // ✅ 추가: 에러 메시지에서 찾고 있던 필드들입니다.
    @Column(name = "wtnnc_sn")
    private String wtnncSn; // 서면통지 일련번호

    @Column(name = "ntfc_sn")
    private String ntfcSn;  // 고시 일련번호

    @Lob
    @Column(name = "polygon_geojson", columnDefinition = "LONGTEXT")
    private String polygonGeojson;

    @Column(name = "stage")
    private String stage;

    @Column(name = "center_lat", precision = 10, scale = 7)
    private BigDecimal centerLat;

    @Column(name = "center_lng", precision = 10, scale = 7)
    private BigDecimal centerLng;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}