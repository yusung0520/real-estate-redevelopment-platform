import React, { useState, useEffect, useRef } from "react";
import KakaoMap from "./components/KakaoMap";
import AreaDetailPage from "./pages/AreaDetailPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage"; // ✅ 프로필 페이지 컴포넌트 추가 필요
import { apiGet } from "./api/client";
import "./App.css";

export default function App() {
  const [view, setView] = useState("map"); // 'map', 'login', 'profile' 제어
  const [selectedAreaId, setSelectedAreaId] = useState(null);
  const [mapCenter, setMapCenter] = useState(null);
  const [isBroker, setIsBroker] = useState(false);
  const [activeTab, setActiveTab] = useState("home");
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const searchContainerRef = useRef(null);

  // 실시간 검색 및 "정보 부족 구역" 완전 제외 로직
  useEffect(() => {
    const fetchResults = async () => {
      if (searchTerm.trim().length < 2) {
        setSearchResults([]);
        setShowDropdown(false);
        return;
      }
      try {
        const encodedTerm = encodeURIComponent(searchTerm.trim());
        const res = await apiGet(`/api/areas/search?q=${encodedTerm}`);

        const rawResults = Array.isArray(res)
          ? res
          : res?.data || res?.content || [];

        const filteredResults = rawResults.filter((area) => {
          const stage = (area.stage || "").trim();
          return stage !== "" && stage !== "정보 없음";
        });

        setSearchResults(filteredResults);
        setShowDropdown(filteredResults.length > 0);
      } catch (error) {
        console.error("검색 실패:", error);
      }
    };
    const timer = setTimeout(fetchResults, 500);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const handleSelectResult = (area) => {
    setSelectedAreaId(area.areaId || area.id);
    setSearchTerm(area.name);
    setShowDropdown(false);
    if (area.centerLat && area.centerLng) {
      setMapCenter({
        lat: Number(area.centerLat),
        lng: Number(area.centerLng),
        targetId: area.areaId || area.id,
      });
    }
  };

  // ✅ 중개사 로그인/로그아웃 버튼 핸들러
  const handleLoginButtonClick = () => {
    if (isBroker) {
      setIsBroker(false);
      setSelectedAreaId(null);
      setView("map");
      alert("로그아웃 되었습니다.");
    } else {
      setView("login");
    }
  };

  // ✅ 로그인 성공 시 호출될 함수
  const handleLoginSuccess = () => {
    setIsBroker(true);
    setView("map");
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (
        searchContainerRef.current &&
        !searchContainerRef.current.contains(e.target)
      ) {
        setShowDropdown(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // ---------------------------------------------------------
  // 화면 렌더링 분기 (View 제어)
  // ---------------------------------------------------------

  // 1. 로그인 화면
  if (view === "login") {
    return (
      <LoginPage
        onLoginSuccess={handleLoginSuccess}
        onBack={() => setView("map")}
      />
    );
  }

  // 2. 프로필 관리 화면
  if (view === "profile") {
    return <ProfilePage onBack={() => setView("map")} />;
  }

  // 3. 메인 지도 화면
  return (
    <div className="app-container">
      <header className="navbar">
        <div className="nav-left">
          <div className="logo">🏗️ 재개발 Connect</div>
        </div>
        <div className="nav-center">
          <nav className="nav-tabs">
            {["home", "sintong", "moa"].map((t) => (
              <button
                key={t}
                className={`tab ${activeTab === t ? "active" : ""}`}
                onClick={() => setActiveTab(t)}
              >
                {t === "home"
                  ? "홈"
                  : t === "sintong"
                    ? "신통기획"
                    : "모아타운"}
              </button>
            ))}
          </nav>
        </div>
        <div className="nav-right">
          <div
            className="nav-user-zone"
            style={{ display: "flex", alignItems: "center", gap: "12px" }}
          >
            {/* ✅ 로그인 시에만 나타나는 둥근 프로필 버튼 */}
            {isBroker && (
              <button
                className="profile-circle-btn"
                onClick={() => setView("profile")}
                style={{
                  width: "36px",
                  height: "36px",
                  borderRadius: "50%",
                  backgroundColor: "#e5e5ea",
                  border: "none",
                  cursor: "pointer",
                  fontSize: "18px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                👤
              </button>
            )}

            <button className="broker-btn" onClick={handleLoginButtonClick}>
              {isBroker ? "로그아웃" : "중개사 로그인"}
            </button>
          </div>
        </div>
      </header>

      <div className="content-body">
        <aside className="sidebar">
          <div className="sidebar-search-area">
            <div className="search-container" ref={searchContainerRef}>
              <div className="search-box">
                <input
                  type="text"
                  placeholder="구역명 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onFocus={() =>
                    searchResults.length > 0 && setShowDropdown(true)
                  }
                />
                <span style={{ color: "#86868b" }}>🔍</span>
              </div>
              {showDropdown && (
                <div className="search-dropdown">
                  {searchResults.map((area, idx) => (
                    <div
                      key={area.id || idx}
                      className="dropdown-item"
                      onMouseDown={(e) => {
                        e.preventDefault();
                        handleSelectResult(area);
                      }}
                    >
                      <div className="item-info">
                        <span className="item-name">{area.name}</span>
                        <span className="item-address">{area.stage}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div
            className="sidebar-scroll-content"
            style={{ flex: 1, overflowY: "auto", position: "relative" }}
          >
            {selectedAreaId ? (
              <div className="sidebar-inner" style={{ position: "relative" }}>
                <button
                  className="close-btn"
                  onClick={() => {
                    setSelectedAreaId(null);
                    setSearchTerm("");
                    setMapCenter(null);
                  }}
                >
                  ×
                </button>
                <AreaDetailPage areaId={selectedAreaId} isBroker={isBroker} />
              </div>
            ) : (
              <div className="sidebar-placeholder">
                <div className="placeholder-content">
                  <div className="apple-icon-circle">📍</div>
                  <p
                    style={{
                      fontSize: "16px",
                      color: "#86868b",
                      fontWeight: "500",
                    }}
                  >
                    구역명을 검색, 클릭하여 정보를 확인하세요.
                  </p>
                </div>
              </div>
            )}
          </div>
        </aside>

        <main className="map-wrapper">
          <KakaoMap
            onAreaClick={(id) => setSelectedAreaId(id)}
            center={mapCenter}
            selectedAreaId={selectedAreaId}
          />
        </main>
      </div>
    </div>
  );
}
