import React, { useState } from "react";
import "./ProfilePage.css";
import "./PostWritePage.css";

export default function PostWritePage({ agentData, onBack, onSuccess }) {
  const [title, setTitle] = useState("");
  const [guName, setGuName] = useState("");
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleEditPhoto = () => {
    alert("사진 수정 기능은 다음 단계에서 연결할 예정입니다.");
  };

  const handleSubmit = async () => {
    if (!agentData?.id) {
      alert("중개사 정보가 없습니다. 다시 로그인해주세요.");
      return;
    }

    if (!title.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!content.trim()) {
      alert("내용을 입력해주세요.");
      return;
    }

    try {
      setSubmitting(true);

      const response = await fetch("/api/posts", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          agentId: agentData.id,
          title: title.trim(),
          guName: guName.trim(),
          content: content.trim(),
        }),
      });

      if (!response.ok) {
        throw new Error(`게시글 작성 실패: ${response.status}`);
      }

      alert("브리핑이 등록되었습니다.");
      onSuccess?.();
    } catch (error) {
      console.error("게시글 작성 실패:", error);
      alert("브리핑 등록 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="profile-page-wrapper">
      <div className="profile-card">
        <header className="profile-page-header">
          <button className="back-link-btn" onClick={onBack} type="button">
            ✕
          </button>
          <h1>중개사 관리 센터</h1>
          <button
            className="done-btn"
            onClick={handleSubmit}
            type="button"
            disabled={submitting}
          >
            {submitting ? "등록중" : "완료"}
          </button>
        </header>

        <div className="profile-scroll-area">
          <section className="profile-photo-section">
            <div className="avatar-container">
              <div className="big-avatar">👤</div>
              <button
                className="edit-photo-txt-btn"
                onClick={handleEditPhoto}
                type="button"
              >
                사진 수정
              </button>
            </div>
          </section>

          <section className="info-group-box">
            <div className="info-item">
              <label>이름</label>
              <input type="text" value={agentData?.name || ""} readOnly />
            </div>

            <div className="info-item">
              <label>중개사무소</label>
              <input type="text" value={agentData?.officeName || ""} readOnly />
            </div>

            <div className="info-item">
              <label>연락처</label>
              <input type="text" value={agentData?.phone || ""} readOnly />
            </div>

            <div className="info-item">
              <label>이메일(아이디)</label>
              <input type="text" value={agentData?.email || ""} readOnly />
            </div>
          </section>

          <section className="info-group-box">
            <div className="briefing-header">
              <label>새 현장 브리핑 작성</label>
            </div>

            <div className="write-form-area">
              <div className="info-item">
                <label>브리핑 제목</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="예: 노량진 재개발 최근 사업 진행 상황"
                />
              </div>

              <div className="info-item">
                <label>자치구</label>
                <input
                  type="text"
                  value={guName}
                  onChange={(e) => setGuName(e.target.value)}
                  placeholder="예: 관악구"
                />
              </div>

              <div className="info-item">
                <label>브리핑 내용</label>
                <textarea
                  className="post-write-textarea"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="현장 분위기, 진행 단계, 투자 포인트 등을 작성해주세요."
                  rows={10}
                />
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
