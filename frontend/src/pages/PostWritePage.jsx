<<<<<<< HEAD
import React, { useEffect, useRef, useState } from "react";
import Editor from "@toast-ui/editor";
import "@toast-ui/editor/dist/toastui-editor.css";
=======
import React, { useMemo, useState } from "react";
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
>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
import "./ProfilePage.css";
import "./PostWritePage.css";

const API_BASE_URL = "http://localhost:8080";

<<<<<<< HEAD
=======
/**
 * CKEditor 5 커스텀 업로드 어댑터
 * 기존 Toast UI의 addImageBlobHook 역할을 대신함
 */
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

  abort() {
    // 필요하면 나중에 AbortController 연결 가능
  }
}

/**
 * CKEditor에 업로드 어댑터 연결
 */
function CustomUploadAdapterPlugin(editor) {
  editor.plugins.get("FileRepository").createUploadAdapter = (loader) => {
    return new CustomUploadAdapter(loader);
  };
}

>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
export default function PostWritePage({ agentData, onBack, onSuccess }) {
  const editorRootRef = useRef(null);
  const editorInstanceRef = useRef(null);

  const [title, setTitle] = useState("");
  const [guName, setGuName] = useState("");
<<<<<<< HEAD
=======
  const [contentHtml, setContentHtml] = useState("");
>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
  const [submitting, setSubmitting] = useState(false);

  const handleEditPhoto = () => {
    alert("사진 수정 기능은 다음 단계에서 연결할 예정입니다.");
  };

<<<<<<< HEAD
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
=======
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
>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
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

<<<<<<< HEAD
    const contentHtml = editorInstanceRef.current?.getHTML()?.trim();

    if (!contentHtml || contentHtml === "<p><br></p>") {
=======
    const trimmedContent = contentHtml.trim();

    if (
      !trimmedContent ||
      trimmedContent === "<p>&nbsp;</p>" ||
      trimmedContent === "<p><br></p>"
    ) {
>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
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
<<<<<<< HEAD
          contentHtml,
=======
          contentHtml: trimmedContent,
>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
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
<<<<<<< HEAD
                <div ref={editorRootRef} className="toast-editor-wrapper" />
=======
                <div className="ckeditor-wrapper">
                  <CKEditor
                    editor={ClassicEditor}
                    config={editorConfig}
                    data={contentHtml}
                    onChange={(event, editor) => {
                      const data = editor.getData();
                      setContentHtml(data);
                    }}
                  />
                </div>
>>>>>>> 8f122b2 (글쓰기 페이지 기능 추가 및 수정)
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