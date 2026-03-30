import React, { useEffect, useMemo, useState } from "react";
import "./PostDetailPage.css";

const SIGUNGU_LABEL_MAP = {
  11110: "종로구",
  11140: "중구",
  11170: "용산구",
  11200: "성동구",
  11215: "광진구",
  11230: "동대문구",
  11260: "중랑구",
  11290: "성북구",
  11305: "강북구",
  11320: "도봉구",
  11350: "노원구",
  11380: "은평구",
  11410: "서대문구",
  11440: "마포구",
  11470: "양천구",
  11500: "강서구",
  11530: "구로구",
  11545: "금천구",
  11560: "영등포구",
  11590: "동작구",
  11620: "관악구",
  11650: "서초구",
  11680: "강남구",
  11710: "송파구",
  11740: "강동구",
};

function getSigunguLabel(sigunguCd) {
  if (!sigunguCd) return "-";
  return SIGUNGU_LABEL_MAP[String(sigunguCd)] || sigunguCd;
}

function normalizePhoneForTel(phone) {
  if (!phone) return "";
  return String(phone).replace(/[^0-9+]/g, "");
}

export default function PostDetailPage({
  postId,
  onBack,
  onDeleted,
  onEdit,
  isBroker = false,
  currentAgentId = null,
}) {
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);

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

  const canManagePost = useMemo(() => {
    if (!isBroker || !post) return false;

    const writerAgentId =
      post.agentId ?? post.writerAgentId ?? post.authorAgentId ?? null;

    if (writerAgentId == null || currentAgentId == null) return false;

    return Number(writerAgentId) === Number(currentAgentId);
  }, [isBroker, post, currentAgentId]);

  const handleDelete = async () => {
    if (!postId) return;

    const confirmed = window.confirm("이 게시글을 삭제하시겠습니까?");
    if (!confirmed) return;

    try {
      setDeleting(true);

      const response = await fetch(`/api/posts/${postId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        let errorMessage = `게시글 삭제 실패: ${response.status}`;

        try {
          const errorData = await response.json();
          if (errorData?.message) {
            errorMessage = errorData.message;
          }
        } catch {
          // 응답 본문이 JSON이 아니어도 기본 메시지 유지
        }

        throw new Error(errorMessage);
      }

      alert("게시글이 삭제되었습니다.");

      if (onDeleted) {
        onDeleted(postId);
      } else if (onBack) {
        onBack();
      }
    } catch (error) {
      console.error("게시글 삭제 실패:", error);
      alert(error.message || "게시글 삭제 중 오류가 발생했습니다.");
    } finally {
      setDeleting(false);
    }
  };

  const contentHtml = post?.contentHtml || post?.content || "";
  const regionText = getSigunguLabel(post?.sigunguCd);
  const phoneText = post?.agentPhone || "-";
  const openChatUrl = post?.kakaoOpenchatUrl || "";
  const telValue = normalizePhoneForTel(post?.agentPhone);

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

            <div className="post-detail-header-actions">
              <button
                className="post-detail-done-btn"
                onClick={onBack}
                type="button"
              >
                닫기
              </button>
            </div>
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

            <div className="post-detail-header-actions">
              <button
                className="post-detail-done-btn"
                onClick={onBack}
                type="button"
              >
                닫기
              </button>
            </div>
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

          <div className="post-detail-header-actions">
            {canManagePost && (
              <>
                <button
                  className="post-detail-edit-btn"
                  onClick={() => onEdit?.(postId)}
                  type="button"
                >
                  수정
                </button>

                <button
                  className="post-detail-delete-btn"
                  onClick={handleDelete}
                  type="button"
                  disabled={deleting}
                >
                  {deleting ? "삭제중" : "삭제"}
                </button>
              </>
            )}

            <button
              className="post-detail-done-btn"
              onClick={onBack}
              type="button"
            >
              닫기
            </button>
          </div>
        </header>

        <div className="post-detail-body">
          <div className="post-detail-meta-box">
            <div className="post-detail-meta-row">
              <span className="meta-label">카테고리</span>
              <span className="meta-value">{post.categoryName || "-"}</span>
            </div>

            <div className="post-detail-meta-row">
              <span className="meta-label">지역</span>
              <span className="meta-value">{regionText}</span>
            </div>

            <div className="post-detail-meta-row">
              <span className="meta-label">작성일</span>
              <span className="meta-value">{formatDate(post.createdAt)}</span>
            </div>

            <div className="post-detail-meta-row">
              <span className="meta-label">작성자</span>
              <span className="meta-value">{post.agentName || "-"}</span>
            </div>

            <div className="post-detail-meta-row">
              <span className="meta-label">중개인 연락처</span>
              <span className="meta-value">
                {telValue ? (
                  <a className="post-detail-link" href={`tel:${telValue}`}>
                    {phoneText}
                  </a>
                ) : (
                  "-"
                )}
              </span>
            </div>

            <div className="post-detail-meta-row">
              <span className="meta-label">오픈채팅 주소</span>
              <span className="meta-value">
                {openChatUrl ? (
                  <a
                    className="post-detail-link"
                    href={openChatUrl}
                    target="_blank"
                    rel="noreferrer"
                  >
                    오픈채팅 바로가기
                  </a>
                ) : (
                  "-"
                )}
              </span>
            </div>
          </div>

          <div className="post-detail-content-box">
            <h2 className="post-detail-title">{post.title || "-"}</h2>

            <div
              className="post-detail-content-html"
              dangerouslySetInnerHTML={{ __html: contentHtml }}
            />
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
