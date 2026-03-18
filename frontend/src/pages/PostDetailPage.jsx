import React, { useEffect, useState } from "react";
import "./PostDetailPage.css";

export default function PostDetailPage({ postId, onBack }) {
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPostDetail = async () => {
      if (!postId) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);

        const response = await fetch(`/api/posts/${postId}`);
        if (!response.ok) {
          throw new Error(`게시글 상세 조회 실패: ${response.status}`);
        }

        const data = await response.json();
        setPost(data);
      } catch (error) {
        console.error("게시글 상세 조회 실패:", error);
        setPost(null);
      } finally {
        setLoading(false);
      }
    };

    fetchPostDetail();
  }, [postId]);

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);

    if (Number.isNaN(date.getTime())) return "";

    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");

    return `${yyyy}.${mm}.${dd}`;
  };

  const getImageSrc = (imageUrl) => {
    if (!imageUrl) return "";
    if (imageUrl.startsWith("http")) return imageUrl;
    return `http://localhost:8080${imageUrl}`;
  };

  if (loading) {
    return (
      <div className="post-detail-page-wrapper">
        <div className="post-detail-card">
          <header className="post-detail-header">
            <button
              className="post-detail-back-btn"
              onClick={onBack}
              type="button"
            >
              ✕
            </button>
            <h1>브리핑 상세</h1>
            <button
              className="post-detail-done-btn"
              onClick={onBack}
              type="button"
            >
              완료
            </button>
          </header>
          <div className="post-detail-body loading-text">
            게시글을 불러오는 중입니다.
          </div>
        </div>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="post-detail-page-wrapper">
        <div className="post-detail-card">
          <header className="post-detail-header">
            <button
              className="post-detail-back-btn"
              onClick={onBack}
              type="button"
            >
              ✕
            </button>
            <h1>브리핑 상세</h1>
            <button
              className="post-detail-done-btn"
              onClick={onBack}
              type="button"
            >
              완료
            </button>
          </header>
          <div className="post-detail-body loading-text">
            게시글을 찾을 수 없습니다.
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="post-detail-page-wrapper">
      <div className="post-detail-card">
        <header className="post-detail-header">
          <button
            className="post-detail-back-btn"
            onClick={onBack}
            type="button"
          >
            ✕
          </button>
          <h1>브리핑 상세</h1>
          <button
            className="post-detail-done-btn"
            onClick={onBack}
            type="button"
          >
            완료
          </button>
        </header>

        <div className="post-detail-body">
          <div className="post-detail-meta-box">
            <div className="post-detail-meta-row">
              <span className="meta-label">카테고리</span>
              <span className="meta-value">{post.categoryName || "-"}</span>
            </div>
            <div className="post-detail-meta-row">
              <span className="meta-label">지역</span>
              <span className="meta-value">{post.guName || "-"}</span>
            </div>
            <div className="post-detail-meta-row">
              <span className="meta-label">작성일</span>
              <span className="meta-value">{formatDate(post.createdAt)}</span>
            </div>
            <div className="post-detail-meta-row">
              <span className="meta-label">작성자</span>
              <span className="meta-value">{post.agentName || "-"}</span>
            </div>
          </div>

          <div className="post-detail-content-box">
            <h2 className="post-detail-title">{post.title}</h2>
            <p className="post-detail-content">{post.content}</p>
          </div>

          {post.images && post.images.length > 0 && (
            <div className="post-detail-images-box">
              <h3 className="post-detail-section-title">첨부 사진</h3>
              <div className="post-detail-image-list">
                {post.images.map((image) => (
                  <img
                    key={image.imageId}
                    src={getImageSrc(image.imageUrl)}
                    alt="첨부 이미지"
                    className="post-detail-image"
                  />
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
