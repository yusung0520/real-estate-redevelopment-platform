import React, { useState, useEffect, useRef } from "react";
import KakaoMap from "./components/KakaoMap";
import AreaDetailPage from "./pages/AreaDetailPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import FindAccountPage from "./pages/FindAccountPage";
import ProfilePage from "./pages/ProfilePage";
import PostWritePage from "./pages/PostWritePage";
import PostDetailPage from "./pages/PostDetailPage";
import { apiGet } from "./api/client";
import "./App.css";

export default function App() {
  const [view, setView] = useState("map");
  const [selectedAreaId, setSelectedAreaId] = useState(null);
  const [selectedPostId, setSelectedPostId] = useState(null);
  const [mapCenter, setMapCenter] = useState(null);
  const [isBroker, setIsBroker] = useState(false);
  const [activeTab, setActiveTab] = useState("home");
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const [brokerInfo, setBrokerInfo] = useState({
    id: null,
    name: "",
    email: "",
    officeName: "",
    phone: "",
  });

  const searchContainerRef = useRef(null);

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

  const handleLoginButtonClick = () => {
    if (isBroker) {
      setIsBroker(false);
      setSelectedAreaId(null);
      setSelectedPostId(null);
      setView("map");
      setBrokerInfo({
        id: null,
        name: "",
        email: "",
        officeName: "",
        phone: "",
      });
      alert("로그아웃 되었습니다.");
    } else {
      setView("login");
    }
  };

  const handleLoginSuccess = (loginResult) => {
    setIsBroker(true);

    if (loginResult) {
      setBrokerInfo({
        id: loginResult.agentId ?? loginResult.id ?? null,
        name: loginResult.name ?? "",
        email: loginResult.email ?? "",
        officeName: loginResult.officeName ?? "",
        phone: loginResult.phone ?? "",
      });
    }

    setView("map");
  };

  if (view === "login") {
    return (
      <LoginPage
        onLoginSuccess={handleLoginSuccess}
        onBack={() => setView("map")}
        onGoSignup={() => setView("signup")}
        onGoFindAccount={() => setView("findAccount")}
      />
    );
  }

  if (view === "signup") {
    return (
      <SignupPage
        onBack={() => setView("login")}
        onSignupSuccess={() => {
          alert("가입 신청이 완료되었습니다. 로그인 해주세요.");
          setView("login");
        }}
      />
    );
  }

  if (view === "findAccount") {
    return <FindAccountPage onBack={() => setView("login")} />;
  }

  if (view === "profile") {
    return (
      <ProfilePage
        agentData={brokerInfo}
        onBack={() => setView("map")}
        onGoWrite={() => setView("write")}
        onOpenPostDetail={(postId) => {
          setSelectedPostId(postId);
          setView("postDetail");
        }}
      />
    );
  }

  if (view === "write") {
    return (
      <PostWritePage
        agentData={brokerInfo}
        onBack={() => setView("profile")}
        onSuccess={() => setView("profile")}
      />
    );
  }

  if (view === "postDetail") {
    return (
      <PostDetailPage
        postId={selectedPostId}
        onBack={() => setView("profile")}
      />
    );
  }

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
          <div className="nav-user-zone">
            {isBroker && (
              <button
                className="profile-circle-btn"
                onClick={() => setView("profile")}
                type="button"
              >
                👤
              </button>
            )}

            <button
              className="broker-btn"
              onClick={handleLoginButtonClick}
              type="button"
            >
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
            style={{ flex: 1, overflowY: "auto" }}
          >
            {selectedAreaId ? (
              <div className="sidebar-inner" style={{ height: "100%" }}>
                <AreaDetailPage
                  areaId={selectedAreaId}
                  isBroker={isBroker}
                  onExit={() => {
                    setSelectedAreaId(null);
                    setSearchTerm("");
                    setMapCenter(null);
                  }}
                />
              </div>
            ) : (
              <div className="sidebar-placeholder">
                <div className="placeholder-content">
                  <div className="apple-icon-circle">📍</div>
                  <p>구역명을 검색, 클릭하여 정보를 확인하세요.</p>
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
