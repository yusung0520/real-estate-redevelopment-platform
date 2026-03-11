import React, { useState } from "react";
import { apiPost } from "../api/client";
import "./PostWritePage.css";

export default function PostWritePage({ agentId, onBack, onSuccess }) {
  const [title, setTitle] = useState("");
  const [categoryName, setCategoryName] = useState("재개발 소식");
  const [content, setContent] = useState("");
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(false);

  // 이미지 선택 핸들러
  const handleImageChange = (e) => {
    if (e.target.files) {
      setImages([...e.target.files]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // 💡 파일 전송을 위해 FormData를 사용합니다.
    const formData = new FormData();
    formData.append("title", title);
    formData.append("content", content);
    formData.append("categoryName", categoryName);
    formData.append("agentId", agentId);

    // 여러 장의 이미지 추가
    images.forEach((image) => {
      formData.append("images", image);
    });

    try {
      // ✅ multipart/form-data 형식으로 전송
      await apiPost("/api/posts/write", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      alert("투자 소식이 성공적으로 등록되었습니다!");
      onSuccess(); // 목록으로 돌아가기
    } catch (error) {
      alert("등록 실패: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="post-write-container">
      <div className="post-write-box">
        <header className="write-header">
          <button className="back-btn" onClick={onBack}>
            ✕
          </button>
          <h1>투자 소식 작성</h1>
          <button
            className="submit-btn"
            onClick={handleSubmit}
            disabled={loading || !title || !content}
          >
            {loading ? "등록 중..." : "게시"}
          </button>
        </header>

        <form className="write-form">
          <input
            className="title-input"
            type="text"
            placeholder="제목을 입력하세요"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />

          <select
            className="category-select"
            value={categoryName}
            onChange={(e) => setCategoryName(e.target.value)}
          >
            <option value="재개발 소식">🏗️ 재개발 소식</option>
            <option value="급매물 정보">💰 급매물 정보</option>
            <option value="투자 가이드">📚 투자 가이드</option>
            <option value="현장 사진">📸 현장 사진</option>
          </select>

          <textarea
            className="content-textarea"
            placeholder="투자자들에게 전달할 상세한 정보를 적어주세요..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />

          <div className="image-upload-section">
            <label htmlFor="image-input" className="image-label">
              📷 사진 첨부 ({images.length})
            </label>
            <input
              id="image-input"
              type="file"
              multiple
              accept="image/*"
              onChange={handleImageChange}
              style={{ display: "none" }}
            />
            <div className="image-preview-list">
              {images.map((file, idx) => (
                <div key={idx} className="preview-item">
                  {file.name}
                </div>
              ))}
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
