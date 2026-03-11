import { useEffect, useState } from "react";
import { apiGet } from "../api/client";
import "./AreaDetail.css";

const ALL_BUSINESS_STAGES = [
  { order: 1, label: "기본계획수립" },
  { order: 2, label: "안전진단" },
  { order: 3, label: "정비구역지정" },
  { order: 4, label: "조합설립추진위원회승인" },
  { order: 5, label: "조합설립인가" },
  { order: 6, label: "사업시행인가" },
  { order: 7, label: "관리처분인가" },
  { order: 8, label: "철거신고" },
  { order: 9, label: "착공신고" },
  { order: 10, label: "일반분양승인" },
  { order: 11, label: "준공인가" },
  { order: 12, label: "이전고시" },
  { order: 13, label: "조합해산" },
  { order: 14, label: "조합청산" },
];

function formatDay(day) {
  if (!day) return "";
  const s = String(day).replace(/-/g, "").trim();
  if (s.length === 8)
    return `${s.slice(0, 4)}.${Number(s.slice(4, 6))}.${Number(s.slice(6, 8))}`;
  return s;
}

export default function AreaDetailPage({ areaId, isBroker, onExit }) {
  const [areaData, setAreaData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!areaId) return;
    (async () => {
      setLoading(true);
      try {
        const response = await apiGet(`/api/areas/${areaId}/detail`);
        setAreaData(response);
      } catch (e) {
        console.error("데이터 로드 실패:", e);
      } finally {
        setLoading(false);
      }
    })();
  }, [areaId]);

  if (loading) return <div className="detail-loading">데이터 분석 중...</div>;
  if (!areaData)
    return <div className="detail-error">정보를 불러올 수 없습니다.</div>;

  return (
    <div className="area-detail-container">
      {isBroker && (
        <div className="broker-admin-status-bar">
          <span className="admin-dot"></span>
          <span className="admin-label">중개사 전용 모드 활성</span>
        </div>
      )}

      <header className="detail-header-v2">
        <button className="header-close-btn" onClick={onExit}>
          ✕
        </button>
        <div className="header-content-center">
          <div className="tag-group">
            <span className="type-tag">재개발</span>
            {areaData.stage && (
              <span className="stage-tag">{areaData.stage}</span>
            )}
          </div>
          <h2 className="area-title">{areaData.name}</h2>
          <p className="area-address">
            {areaData.sigunguCd} {areaData.emdNm || ""}
          </p>
        </div>
      </header>

      <section className="timeline-section">
        <h3 className="section-title">사업 진행 현황</h3>
        <div className="Apple-Timeline-List">
          {ALL_BUSINESS_STAGES.map((baseStage, idx) => {
            const isLast = idx === ALL_BUSINESS_STAGES.length - 1;
            const apiItem = areaData.timeline?.find(
              (item) => item.label === baseStage.label,
            );
            let statusClass = "TODO";
            if (apiItem?.isCurrent) statusClass = "CURRENT";
            else if (apiItem?.done) statusClass = "DONE";

            return (
              <div
                key={baseStage.order}
                className={`timeline-row ${statusClass}`}
              >
                <div className="timeline-left">
                  <div className="node-dot">
                    {statusClass === "DONE" && "✓"}
                    {statusClass === "CURRENT" && (
                      <div className="inner-dot"></div>
                    )}
                  </div>
                  {!isLast && <div className="node-line"></div>}
                </div>
                <div className="timeline-right">
                  <div className="step-content">
                    <div className="step-main">
                      <span className="order-num">{baseStage.order}</span>
                      <span className="label-text">{baseStage.label}</span>
                    </div>
                    <span className="step-date">
                      {apiItem?.date ? formatDay(apiItem.date) : ""}
                    </span>
                  </div>
                  {statusClass === "CURRENT" && (
                    <div className="current-badge">현재 단계</div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </section>

      <section className="broker-briefing-preview">
        <h3 className="section-title">전문가 현장 브리핑</h3>
        <div className="briefing-box-placeholder">
          <p>중개사님이 등록한 최신 소식이 이곳에 표시됩니다.</p>
        </div>
      </section>
    </div>
  );
}
