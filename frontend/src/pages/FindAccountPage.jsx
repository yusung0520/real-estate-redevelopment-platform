// src/pages/FindAccountPage.jsx
import React, { useState } from "react";
import { apiPost } from "../api/client";
import "./LoginPage.css"; // 디자인 통일

export default function FindAccountPage({ onBack }) {
  const [tab, setTab] = useState("email"); // email 또는 password
  const [formData, setFormData] = useState({ name: "", phone: "" });
  const [result, setResult] = useState(null);

  const handleFind = async (e) => {
    e.preventDefault();
    try {
      const res = await apiPost("/api/agents/find-account", {
        ...formData,
        type: tab,
      });
      setResult(res.result);
    } catch (error) {
      alert(error.message || "정보를 찾을 수 없습니다.");
    }
  };

  return (
    <div className="apple-login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>{tab === "email" ? "아이디 찾기" : "비밀번호 찾기"}</h1>
          <p>등록된 이름과 연락처를 입력하세요.</p>
        </div>

        {/* 탭 전환 버튼 */}
        <div style={{ display: "flex", gap: "10px", marginBottom: "20px" }}>
          <button
            onClick={() => {
              setTab("email");
              setResult(null);
            }}
            style={{
              flex: 1,
              padding: "10px",
              borderRadius: "8px",
              border: "none",
              backgroundColor: tab === "email" ? "#5757e9" : "#d2d2d7",
              color: "white",
            }}
          >
            아이디 찾기
          </button>
          <button
            onClick={() => {
              setTab("password");
              setResult(null);
            }}
            style={{
              flex: 1,
              padding: "10px",
              borderRadius: "8px",
              border: "none",
              backgroundColor: tab === "password" ? "#5757e9" : "#d2d2d7",
              color: "white",
            }}
          >
            비밀번호 찾기
          </button>
        </div>

        {!result ? (
          <form className="login-form" onSubmit={handleFind}>
            <div className="input-field">
              <input
                placeholder="성함"
                required
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
              />
            </div>
            <div className="input-field">
              <input
                placeholder="연락처 (010-0000-0000)"
                required
                onChange={(e) =>
                  setFormData({ ...formData, phone: e.target.value })
                }
              />
            </div>
            <button type="submit" className="apple-submit-btn">
              찾기
            </button>
          </form>
        ) : (
          <div
            style={{
              padding: "20px",
              background: "#f5f5f7",
              borderRadius: "12px",
              marginBottom: "20px",
            }}
          >
            <p style={{ color: "#86868b", fontSize: "14px" }}>
              찾으시는 정보는 다음과 같습니다:
            </p>
            <h2 style={{ color: "#5757e9", marginTop: "10px" }}>{result}</h2>
          </div>
        )}

        <div className="login-footer">
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              onBack();
            }}
          >
            로그인으로 돌아가기
          </a>
        </div>
      </div>
    </div>
  );
}
