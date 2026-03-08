// src/pages/AreaListPage.jsx
import { useEffect, useState } from "react";
import { getAreas } from "../api/area";

export default function AreaListPage() {
  const [areas, setAreas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    (async () => {
      try {
        setLoading(true);
        setError("");

        const data = await getAreas();
        if (!ignore) setAreas(Array.isArray(data) ? data : []);
      } catch (e) {
        if (!ignore) setError(e.message || "목록 불러오기 실패");
      } finally {
        if (!ignore) setLoading(false);
      }
    })();

    return () => {
      ignore = true;
    };
  }, []);

  if (loading) return <div style={{ padding: 16 }}>불러오는 중...</div>;
  if (error) return <div style={{ padding: 16, color: "red" }}>{error}</div>;

  return (
    <div style={{ padding: 16 }}>
      <h2>구역 목록</h2>

      {areas.map((a) => {
        const stageText = a.officialStageLabel || a.stage || "데이터 준비중";
        const stageDate = a.officialStageDate; // "2026-01-01" 같은 형태

        return (
          <div
            key={a.areaId}
            style={{
              border: "1px solid #ddd",
              borderRadius: 12,
              padding: 12,
              marginBottom: 12,
            }}
          >
            <div style={{ fontWeight: 700 }}>{a.name}</div>

            <div style={{ color: "#2563EB", fontWeight: 600 }}>
              공식 단계: {a.officialStageLabel}
            </div>
            <div style={{ fontSize: 12, color: "#666" }}>
              기준일: {a.officialStageDate}
            </div>
          </div>
        );
      })}
    </div>
  );
}
