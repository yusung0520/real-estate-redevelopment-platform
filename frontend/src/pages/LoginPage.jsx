import React, { useState } from "react";
import { apiPost } from "../api/client";
import "./LoginPage.css";

export default function LoginPage({
  onLoginSuccess,
  onBack,
  onGoSignup,
  onGoFindAccount,
}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await apiPost("/api/agents/login", { email, password });

      if (res && res.message === "로그인 성공") {
        console.log("로그인 성공:", res);
        alert(`${res.name} 중개사님, 환영합니다!`);
        onLoginSuccess();
      } else {
        alert("로그인 정보가 올바르지 않습니다.");
      }
    } catch (error) {
      console.error("로그인 실패:", error);
      alert(error.message || "아이디 또는 비밀번호가 올바르지 않습니다.");
    } finally {
      setLoading(false);
    }
  };

  // ✅ 수정: alert 대신 화면 전환 함수를 호출합니다.
  const handleFindAccount = (e) => {
    e.preventDefault();
    if (onGoFindAccount) {
      onGoFindAccount();
    }
  };

  return (
    <div className="apple-login-container">
      <div className="login-box">
        <div className="login-header">
          <div className="brand-logo">🏗️</div>
          <h1>재개발 Connect</h1>
          <p>공인중개사 전용 관리 시스템</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="input-field">
            <input
              type="email"
              placeholder="아이디 (이메일)"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          <div className="input-field">
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <button type="submit" className="apple-submit-btn" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </button>
        </form>

        <div className="login-footer">
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              onBack();
            }}
          >
            지도로 돌아가기
          </a>
          <div className="divider"></div>
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              onGoSignup();
            }}
          >
            중개사 회원가입
          </a>
          <div className="divider"></div>
          <a href="#" onClick={handleFindAccount}>
            아이디/비밀번호 찾기
          </a>
        </div>
      </div>
    </div>
  );
}
