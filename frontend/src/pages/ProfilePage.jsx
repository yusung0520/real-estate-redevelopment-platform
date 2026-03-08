import React, { useState } from "react";
import "./ProfilePage.css";

export default function ProfilePage({ onBack }) {
  // ✅ 주력 구역 리스트 상태 관리
  const [zones, setZones] = useState(["자양 4동 A구역", "성수 전략 1지구"]);
  const [newZone, setNewZone] = useState("");

  // ✅ 구역 추가 핸들러
  const handleAddZone = (e) => {
    e.preventDefault();
    const trimmed = newZone.trim();
    if (trimmed && !zones.includes(trimmed)) {
      setZones([...zones, trimmed]);
      setNewZone("");
    }
  };

  // ✅ 구역 삭제 핸들러
  const handleRemoveZone = (target) => {
    setZones(zones.filter((z) => z !== target));
  };

  return (
    <div className="profile-page-wrapper">
      <div className="profile-card">
        {/* 상단 헤더 */}
        <header className="profile-page-header">
          <button className="back-link-btn" onClick={onBack}>
            ✕ 닫기
          </button>
          <h1>중개사 프로필</h1>
          <button className="done-btn" onClick={onBack}>
            완료
          </button>
        </header>

        <div className="profile-scroll-area">
          {/* 사진 섹션 */}
          <section className="profile-photo-section">
            <div className="avatar-container">
              <div className="big-avatar">👤</div>
              <button className="edit-photo-txt-btn">사진 수정</button>
            </div>
          </section>

          {/* 정보 그룹 1: 기본 정보 */}
          <section className="info-group-box">
            <div className="info-item">
              <label>이름</label>
              <input type="text" defaultValue="김중개" />
            </div>
            <div className="info-item">
              <label>중개사무소</label>
              <input type="text" defaultValue="행복한 공인중개사사무소" />
            </div>
            <div className="info-item">
              <label>연락처</label>
              <input type="text" defaultValue="010-1234-5678" />
            </div>
          </section>

          {/* 정보 그룹 2: 주력 담당 구역 (추가/삭제 기능) */}
          <section className="info-group-box">
            <div className="zone-header">
              <label>주력 담당 구역</label>
            </div>

            <div className="zone-content">
              {/* 구역 추가 입력란 */}
              <form className="zone-input-wrapper" onSubmit={handleAddZone}>
                <input
                  type="text"
                  placeholder="구역명 입력..."
                  value={newZone}
                  onChange={(e) => setNewZone(e.target.value)}
                />
                <button type="submit" className="add-zone-btn">
                  +
                </button>
              </form>

              {/* 구역 태그 리스트 */}
              <div className="zone-tag-list">
                {zones.map((zone, index) => (
                  <span key={index} className="zone-pill">
                    {zone}
                    <button
                      className="remove-zone-btn"
                      onClick={() => handleRemoveZone(zone)}
                    >
                      ✕
                    </button>
                  </span>
                ))}
              </div>
            </div>
          </section>

          <p className="profile-footer-msg">
            위 정보는 재개발 구역 상세 페이지에서 투자자들에게 공개됩니다.
          </p>
        </div>
      </div>
    </div>
  );
}
