import React, { useEffect, useMemo, useState } from "react";
import { CKEditor } from "@ckeditor/ckeditor5-react";
import {
  ClassicEditor,
  Essentials,
  Paragraph,
  Bold,
  Italic,
  Underline,
  Strikethrough,
  Heading,
  Link,
  List,
  Font,
  Alignment,
  Image,
  ImageToolbar,
  ImageCaption,
  ImageStyle,
  ImageResize,
  ImageUpload,
  Table,
  TableToolbar,
  BlockQuote,
} from "ckeditor5";

import koTranslations from "ckeditor5/translations/ko.js";
import "ckeditor5/ckeditor5.css";
import "./ProfilePage.css";
import "./PostWritePage.css";

const API_BASE_URL = "http://localhost:8080";

const SIGUNGU_OPTIONS = [
  { label: "종로구", value: "11110" },
  { label: "중구", value: "11140" },
  { label: "용산구", value: "11170" },
  { label: "성동구", value: "11200" },
  { label: "광진구", value: "11215" },
  { label: "동대문구", value: "11230" },
  { label: "중랑구", value: "11260" },
  { label: "성북구", value: "11290" },
  { label: "강북구", value: "11305" },
  { label: "도봉구", value: "11320" },
  { label: "노원구", value: "11350" },
  { label: "은평구", value: "11380" },
  { label: "서대문구", value: "11410" },
  { label: "마포구", value: "11440" },
  { label: "양천구", value: "11470" },
  { label: "강서구", value: "11500" },
  { label: "구로구", value: "11530" },
  { label: "금천구", value: "11545" },
  { label: "영등포구", value: "11560" },
  { label: "동작구", value: "11590" },
  { label: "관악구", value: "11620" },
  { label: "서초구", value: "11650" },
  { label: "강남구", value: "11680" },
  { label: "송파구", value: "11710" },
  { label: "강동구", value: "11740" },
];

function getSigunguLabel(sigunguCd) {
  const found = SIGUNGU_OPTIONS.find((option) => option.value === sigunguCd);
  return found ? found.label : "";
}

class CustomUploadAdapter {
  constructor(loader) {
    this.loader = loader;
  }

  async upload() {
    const file = await this.loader.file;

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/api/uploads/images", {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`이미지 업로드 실패: ${response.status}`);
    }

    const data = await response.json();

    const imageUrl = data.url?.startsWith("http")
      ? data.url
      : `${API_BASE_URL}${data.url}`;

    return {
      default: imageUrl,
    };
  }

  abort() {}
}

function CustomUploadAdapterPlugin(editor) {
  editor.plugins.get("FileRepository").createUploadAdapter = (loader) => {
    return new CustomUploadAdapter(loader);
  };
}

export default function PostWritePage({
  agentData,
  onBack,
  onSuccess,
  mode = "write",
  postId = null,
}) {
  const [title, setTitle] = useState("");
  const [sigunguCd, setSigunguCd] = useState("");
  const [contentHtml, setContentHtml] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [loadingPost, setLoadingPost] = useState(false);

  useEffect(() => {
    const fetchPost = async () => {
      if (mode !== "edit" || !postId) return;

      try {
        setLoadingPost(true);

        const response = await fetch(`/api/posts/${postId}`);
        if (!response.ok) {
          throw new Error(`게시글 조회 실패: ${response.status}`);
        }

        const data = await response.json();

        setTitle(data.title || "");
        setSigunguCd(data.sigunguCd || "");
        setContentHtml(data.contentHtml || data.content || "");
      } catch (error) {
        console.error("수정할 게시글 조회 실패:", error);
        alert("수정할 게시글 정보를 불러오지 못했습니다.");
      } finally {
        setLoadingPost(false);
      }
    };

    fetchPost();
  }, [mode, postId]);

  const handleEditPhoto = () => {
    alert("사진 수정 기능은 다음 단계에서 연결할 예정입니다.");
  };

  const editorConfig = useMemo(() => {
    return {
      licenseKey: "GPL",
      plugins: [
        Essentials,
        Paragraph,
        Bold,
        Italic,
        Underline,
        Strikethrough,
        Heading,
        Link,
        List,
        Font,
        Alignment,
        Image,
        ImageToolbar,
        ImageCaption,
        ImageStyle,
        ImageResize,
        ImageUpload,
        Table,
        TableToolbar,
        BlockQuote,
      ],
      extraPlugins: [CustomUploadAdapterPlugin],
      toolbar: [
        "undo",
        "redo",
        "|",
        "heading",
        "|",
        "fontSize",
        "fontFamily",
        "fontColor",
        "fontBackgroundColor",
        "|",
        "bold",
        "italic",
        "underline",
        "strikethrough",
        "|",
        "alignment",
        "|",
        "bulletedList",
        "numberedList",
        "|",
        "link",
        "insertTable",
        "uploadImage",
        "blockQuote",
      ],
      image: {
        toolbar: [
          "imageStyle:inline",
          "imageStyle:block",
          "imageStyle:side",
          "|",
          "toggleImageCaption",
          "imageTextAlternative",
        ],
      },
      table: {
        contentToolbar: ["tableColumn", "tableRow", "mergeTableCells"],
      },
      language: "ko",
      translations: [koTranslations],
      placeholder: "브리핑 내용을 입력해주세요.",
    };
  }, []);

  const handleSubmit = async () => {
    if (mode === "write" && !agentData?.id) {
      alert("중개사 정보가 없습니다. 다시 로그인해주세요.");
      return;
    }

    if (!title.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!sigunguCd) {
      alert("자치구를 선택해주세요.");
      return;
    }

    const trimmedContent = contentHtml.trim();

    if (
      !trimmedContent ||
      trimmedContent === "<p>&nbsp;</p>" ||
      trimmedContent === "<p><br></p>"
    ) {
      alert("내용을 입력해주세요.");
      return;
    }

    try {
      setSubmitting(true);

      const url = mode === "edit" ? `/api/posts/${postId}` : `/api/posts/write`;
      const method = mode === "edit" ? "PUT" : "POST";

      const requestBody =
        mode === "edit"
          ? {
              title: title.trim(),
              sigunguCd,
              categoryName: "브리핑",
              contentHtml: trimmedContent,
            }
          : {
              agentId: agentData.id,
              title: title.trim(),
              sigunguCd,
              categoryName: "브리핑",
              contentHtml: trimmedContent,
            };

      console.log("게시글 저장 요청:", requestBody);

      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        let errorMessage =
          mode === "edit"
            ? `게시글 수정 실패: ${response.status}`
            : `게시글 등록 실패: ${response.status}`;

        try {
          const errorData = await response.json();
          if (errorData?.message) {
            errorMessage = errorData.message;
          }
        } catch {
          // 응답 JSON 파싱 실패 시 기본 메시지 유지
        }

        throw new Error(errorMessage);
      }

      alert(
        mode === "edit"
          ? "게시글이 수정되었습니다."
          : "브리핑이 등록되었습니다.",
      );
      onSuccess?.();
    } catch (error) {
      console.error(
        mode === "edit" ? "게시글 수정 실패:" : "게시글 등록 실패:",
        error,
      );
      alert(
        error.message ||
          (mode === "edit"
            ? "게시글 수정 중 오류가 발생했습니다."
            : "브리핑 등록 중 오류가 발생했습니다."),
      );
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
          <h1>{mode === "edit" ? "브리핑 수정" : "중개사 관리 센터"}</h1>
          <button
            className="done-btn"
            onClick={handleSubmit}
            type="button"
            disabled={submitting || loadingPost}
          >
            {loadingPost
              ? "불러오는중"
              : submitting
                ? mode === "edit"
                  ? "수정중"
                  : "등록중"
                : "완료"}
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
              <label>
                {mode === "edit" ? "브리핑 수정" : "새 현장 브리핑 작성"}
              </label>
            </div>

            <div className="write-form-area">
              <div className="info-item">
                <label>브리핑 제목</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="예: 노량진 재개발 최근 사업 진행 상황"
                  disabled={loadingPost}
                />
              </div>

              <div className="info-item">
                <label>자치구</label>
                <select
                  value={sigunguCd}
                  onChange={(e) => setSigunguCd(e.target.value)}
                  disabled={loadingPost}
                >
                  <option value="">자치구를 선택하세요</option>
                  {SIGUNGU_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                {sigunguCd && (
                  <small
                    style={{
                      marginTop: "6px",
                      display: "block",
                      color: "#666",
                    }}
                  >
                    선택된 자치구: {getSigunguLabel(sigunguCd)} ({sigunguCd})
                  </small>
                )}
              </div>

              <div className="info-item">
                <label>브리핑 내용</label>
                <div className="ckeditor-wrapper">
                  <CKEditor
                    editor={ClassicEditor}
                    config={editorConfig}
                    data={contentHtml}
                    disabled={loadingPost}
                    onChange={(event, editor) => {
                      const data = editor.getData();
                      setContentHtml(data);
                    }}
                  />
                </div>
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
