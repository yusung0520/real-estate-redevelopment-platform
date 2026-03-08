import React, { useState } from "react"; // ✅ useState 추가
import "./LoginPage.css";

export default function LoginPage({ onLoginSuccess, onBack }) {
  // ✅ 입력값을 관리하기 위한 상태 추가
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    // 💡 [참고] 나중에 백엔드와 연결할 때 이곳에서 api 호출을 합니다.
    console.log("로그인 시도:", { email, password });

    // 지금은 테스트 단계이므로 버튼을 누르면 즉시 성공 처리하여 지도로 보냅니다.
    if (onLoginSuccess) {
      onLoginSuccess();
    }
  };

  return (
    <div className="apple-login-container">
      <div className="login-box">
        {/* 헤더 섹션: 브랜드 정체성 */}
        <div className="login-header">
          <div className="brand-logo">🏗️</div>
          <h1>재개발 Connect</h1>
          <p>공인중개사 전용 관리 시스템</p>
        </div>

        {/* 입력 섹션: 깔끔한 인풋창 */}
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="input-field">
            <input
              type="email"
              placeholder="아이디 (이메일)"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="input-field">
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="apple-submit-btn">
            로그인
          </button>
        </form>

        {/* 하단 보조 섹션: 심플한 링크 */}
        <div className="login-footer">
          {/* ✅ 클릭 시 지도로 돌아가는 기능 연결 */}
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
          <a href="#">중개사 회원가입 신청</a>
        </div>
      </div>
    </div>
  );
}
