import React, { useEffect, useState } from "react";
import "./ProfilePage.css";

export default function ProfilePage({
  agentData,
  onBack,
  onGoWrite,
  onOpenPostDetail,
}) {
  const [posts, setPosts] = useState([]);
  const [loadingPosts, setLoadingPosts] = useState(true);

  useEffect(() => {
    const fetchMyPosts = async () => {
      if (!agentData?.id) {
        setPosts([]);
        setLoadingPosts(false);
        return;
      }

      try {
        setLoadingPosts(true);

        const response = await fetch(`/api/posts/my?agentId=${agentData.id}`);
        if (!response.ok) {
          throw new Error(`게시글 목록 조회 실패: ${response.status}`);
        }

        const data = await response.json();
        setPosts(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error("내 게시글 목록 불러오기 실패:", error);
        setPosts([]);
      } finally {
        setLoadingPosts(false);
      }
    };

    fetchMyPosts();
  }, [agentData?.id]);

  const handleEditPhoto = () => {
    alert("사진 수정 기능은 다음 단계에서 연결할 예정입니다.");
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);

    if (Number.isNaN(date.getTime())) return "";

    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");

    return `${yyyy}.${mm}.${dd}`;
  };

  return (
    <div className="profile-page-wrapper">
      <div className="profile-card">
        <header className="profile-page-header">
          <button className="back-link-btn" onClick={onBack} type="button">
            ✕
          </button>
          <h1>중개사 관리 센터</h1>
          <button className="done-btn" onClick={onBack} type="button">
            완료
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
              <input type="text" defaultValue={agentData?.name || ""} />
            </div>

            <div className="info-item">
              <label>중개사무소</label>
              <input type="text" defaultValue={agentData?.officeName || ""} />
            </div>

            <div className="info-item">
              <label>연락처</label>
              <input type="text" defaultValue={agentData?.phone || ""} />
            </div>

            <div className="info-item">
              <label>이메일(아이디)</label>
              <input
                type="text"
                defaultValue={agentData?.email || ""}
                readOnly
              />
            </div>
          </section>

          <section className="info-group-box">
            <div className="briefing-header">
              <label>내 현장 브리핑 관리</label>
              <button
                className="small-add-btn"
                onClick={onGoWrite}
                type="button"
              >
                + 새 글 작성
              </button>
            </div>

            <div className="post-list-apple">
              {loadingPosts ? (
                <div className="no-posts">게시글을 불러오는 중입니다.</div>
              ) : posts.length > 0 ? (
                posts.map((post) => (
                  <div
                    key={post.postId}
                    className="post-item-row"
                    onClick={() => onOpenPostDetail?.(post.postId)}
                    role="button"
                    tabIndex={0}
                  >
                    <div className="post-info">
                      <span className="post-title-txt">{post.title}</span>
                      <span className="post-date-txt">
                        {formatDate(post.createdAt)}
                        {post.guName ? ` · ${post.guName}` : ""}
                      </span>
                    </div>
                    <span className="arrow-icon">〉</span>
                  </div>
                ))
              ) : (
                <div className="no-posts">등록된 브리핑이 없습니다.</div>
              )}
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
