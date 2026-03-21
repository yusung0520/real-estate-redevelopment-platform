import React, { useEffect, useRef, useState } from "react";
import Editor from "@toast-ui/editor";
import "@toast-ui/editor/dist/toastui-editor.css";
import "./ProfilePage.css";
import "./PostWritePage.css";

const API_BASE_URL = "http://localhost:8080";

export default function PostWritePage({ agentData, onBack, onSuccess }) {
  const editorRootRef = useRef(null);
  const editorInstanceRef = useRef(null);

  const [title, setTitle] = useState("");
  const [guName, setGuName] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleEditPhoto = () => {
    alert("사진 수정 기능은 다음 단계에서 연결할 예정입니다.");
  };

  useEffect(() => {
    if (!editorRootRef.current) return;
    if (editorInstanceRef.current) return;

    editorInstanceRef.current = new Editor({
      el: editorRootRef.current,
      height: "400px",
      initialEditType: "wysiwyg",
      previewStyle: "vertical",
      initialValue: "",
      hooks: {
        addImageBlobHook: async (blob, callback) => {
          try {
            const formData = new FormData();
            formData.append("file", blob);

            const response = await fetch("/api/uploads/images", {
              method: "POST",
              body: formData,
            });

            if (!response.ok) {
              throw new Error(`이미지 업로드 실패: ${response.status}`);
            }

            const data = await response.json();

            // 상대경로(/uploads/xxx.png)를 절대경로로 변환
            const imageUrl = data.url.startsWith("http")
              ? data.url
              : `${API_BASE_URL}${data.url}`;

            callback(imageUrl, "image");
          } catch (error) {
            console.error("이미지 업로드 실패:", error);
            alert("이미지 업로드에 실패했습니다.");
          }
        },
      },
    });

    return () => {
      if (editorInstanceRef.current) {
        editorInstanceRef.current.destroy();
        editorInstanceRef.current = null;
      }
    };
  }, []);

  const handleSubmit = async () => {
    if (!agentData?.id) {
      alert("중개사 정보가 없습니다. 다시 로그인해주세요.");
      return;
    }

    if (!title.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!guName.trim()) {
      alert("자치구를 입력해주세요.");
      return;
    }

    const contentHtml = editorInstanceRef.current?.getHTML()?.trim();

    if (!contentHtml || contentHtml === "<p><br></p>") {
      alert("내용을 입력해주세요.");
      return;
    }

    try {
      setSubmitting(true);

      const response = await fetch("/api/posts/write", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          agentId: agentData.id,
          title: title.trim(),
          guName: guName.trim(),
          categoryName: "브리핑",
          contentHtml,
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
                <div ref={editorRootRef} className="toast-editor-wrapper" />
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