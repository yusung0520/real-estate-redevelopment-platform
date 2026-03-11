import React, { useState } from "react";
import { apiPost } from "../api/client";
import "./SignupPage.css"; // ✅ 새로 만든 전용 CSS 연결

export default function SignupPage({ onBack, onSignupSuccess }) {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    name: "",
    officeName: "",
    phone: "",
    kakaoOpenchatUrl: "",
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // ✅ 백엔드 AgentController의 /api/agents/signup 호출
      await apiPost("/api/agents/signup", formData);
      alert("회원가입 신청이 완료되었습니다!");
      onSignupSuccess(); // 가입 성공 후 로그인 화면으로 이동
    } catch (error) {
      alert(error.message || "회원가입 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="apple-signup-container">
      <div className="signup-box">
        {/* 헤더 섹션 */}
        <div className="signup-header">
          <div className="brand-logo">🏗️</div>
          <h1>중개사 회원가입</h1>
          <p>정보를 입력하여 가입을 신청하세요.</p>
        </div>

        {/* 가입 폼 섹션 */}
        <form className="signup-form" onSubmit={handleSignup}>
          <div className="input-group">
            {/* 1. 이름 */}
            <div className="input-field">
              <input
                name="name"
                type="text"
                placeholder="성함"
                required
                onChange={handleChange}
              />
            </div>
            {/* 2. 아이디(이메일) */}
            <div className="input-field">
              <input
                name="email"
                type="email"
                placeholder="아이디 (이메일)"
                required
                onChange={handleChange}
              />
            </div>
            {/* 3. 비밀번호 */}
            <div className="input-field">
              <input
                name="password"
                type="password"
                placeholder="비밀번호"
                required
                onChange={handleChange}
              />
            </div>
            {/* 4. 중개사무소 명칭 */}
            <div className="input-field">
              <input
                name="officeName"
                type="text"
                placeholder="중개사무소 명칭"
                required
                onChange={handleChange}
              />
            </div>
            {/* 5. 연락처 */}
            <div className="input-field">
              <input
                name="phone"
                type="text"
                placeholder="연락처 (010-0000-0000)"
                required
                onChange={handleChange}
              />
            </div>
            {/* 6. 카카오톡 오픈채팅 */}
            <div className="input-field">
              <input
                name="kakaoOpenchatUrl"
                type="text"
                placeholder="카카오톡 오픈채팅 URL (선택)"
                onChange={handleChange}
              />
            </div>
          </div>

          <button
            type="submit"
            className="signup-submit-btn"
            disabled={loading}
          >
            {loading ? "가입 신청 중..." : "가입 신청하기"}
          </button>
        </form>

        {/* 하단 섹션 */}
        <div className="signup-footer">
          <button className="footer-link" onClick={onBack}>
            이미 계정이 있으신가요? <span>로그인</span>
          </button>
        </div>
      </div>
    </div>
  );
}
