import React, { useState } from "react";
import "./ProfilePage.css";

export default function ProfilePage({ agentData, onBack, onGoWrite }) {
  // ✅ 브리핑 글 목록 상태 (초기 예시 데이터)
  const [posts, setPosts] = useState([
    {
      id: 1,
      title: "자양 4동 최근 현장 분위기 및 매물 상황",
      date: "2024.03.15",
    },
    {
      id: 2,
      title: "성수 전략 1지구 조합원 분양 신청 관련",
      date: "2024.03.10",
    },
  ]);

  return (
    <div className="profile-page-wrapper">
      <div className="profile-card">
        {/* 헤더 */}
        <header className="profile-page-header">
          <button className="back-link-btn" onClick={onBack}>
            ✕
          </button>
          <h1>중개사 관리 센터</h1>
          <button className="done-btn" onClick={onBack}>
            완료
          </button>
        </header>

        <div className="profile-scroll-area">
          {/* ✅ 사진 섹션 복구 (image_d18443.png 구성) */}
          <section className="profile-photo-section">
            <div className="avatar-container">
              <div className="big-avatar">👤</div>
              <button className="edit-photo-txt-btn">사진 수정</button>
            </div>
          </section>

          {/* ✅ 기본 정보 섹션 (이름, 중개사무소, 연락처) */}
          <section className="info-group-box">
            <div className="info-item">
              <label>이름</label>
              <input type="text" defaultValue={agentData?.name || "김중개"} />
            </div>
            <div className="info-item">
              <label>중개사무소</label>
              <input type="text" defaultValue="행복한 공인중개사사무소" />
            </div>
            <div className="info-item">
              <label>연락처</label>
              <input type="text" defaultValue="010-1234-5678" />
            </div>
            {/* 이메일은 읽기전용으로 하나 추가해두면 관리가 편합니다 */}
            <div className="info-item">
              <label>이메일(아이디)</label>
              <input
                type="text"
                defaultValue={agentData?.email || ""}
                readOnly
              />
            </div>
          </section>

          {/* ✅ 하단: 주력 담당 구역 대신 "내 현장 브리핑 관리" */}
          <section className="info-group-box">
            <div className="briefing-header">
              <label>내 현장 브리핑 관리</label>
              <button
                className="small-add-btn"
                onClick={onGoWrite} // App.jsx에서 넘겨받은 글쓰기 페이지 전환 함수
              >
                + 새 글 작성
              </button>
            </div>

            <div className="post-list-apple">
              {posts.length > 0 ? (
                posts.map((post) => (
                  <div key={post.id} className="post-item-row">
                    <div className="post-info">
                      <span className="post-title-txt">{post.title}</span>
                      <span className="post-date-txt">{post.date}</span>
                    </div>
                    <span style={{ color: "#d2d2d7" }}>〉</span>
                  </div>
                ))
              ) : (
                <div
                  style={{
                    textAlign: "center",
                    padding: "20px",
                    color: "#86868b",
                    fontSize: "14px",
                  }}
                >
                  등록된 브리핑이 없습니다.
                </div>
              )}
            </div>
          </section>

          <p
            style={{
              textAlign: "center",
              fontSize: "12px",
              color: "#86868b",
              marginTop: "10px",
            }}
          >
            위 정보는 재개발 구역 상세 페이지에서 투자자들에게 공개됩니다.
          </p>
        </div>
      </div>
    </div>
  );
}
