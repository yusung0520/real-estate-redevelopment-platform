// src/components/DocumentList.jsx
import { formatDate, normalizeStageLabel } from "../utils/stage";

export default function DocumentList({ events }) {
  const docs = (events || [])
    .map((ev) => {
      const d = ev.document || ev; // ✅ 중첩/평면 둘 다 대응
      if (!d?.detailUrl || !d?.title) return null;

      return {
        title: d.title,
        detailUrl: d.detailUrl,
        publishedDate: d.publishedDate || ev.eventDate || null,
        stage: normalizeStageLabel(ev.eventLabel) || null,
      };
    })
    .filter(Boolean);

  return (
    <div style={wrapStyle}>
      <h3 style={{ margin: "0 0 10px 0" }}>근거 문서</h3>

      {docs.length === 0 ? (
        <div style={{ color: "#666" }}>
          현재는 문서 목록이 비어있습니다. (추후 확장 예정)
        </div>
      ) : (
        <div style={{ display: "grid", gap: 10 }}>
          {docs.map((d, idx) => (
            <a
              key={idx}
              href={d.detailUrl}
              target="_blank"
              rel="noreferrer"
              style={itemStyle}
              title="원문 열기"
            >
              <div style={{ fontWeight: 800 }}>{d.title}</div>
              <div style={{ marginTop: 6, color: "#666", fontSize: 13 }}>
                {d.stage ? `단계: ${d.stage} · ` : ""}
                {d.publishedDate
                  ? `일자: ${formatDate(d.publishedDate)}`
                  : "일자: -"}
              </div>
              <div
                style={{
                  marginTop: 6,
                  color: "#1a5fb4",
                  fontSize: 13,
                  fontWeight: 700,
                }}
              >
                원문 보기 →
              </div>
            </a>
          ))}
        </div>
      )}
    </div>
  );
}

const wrapStyle = {
  marginTop: 16,
  padding: 16,
  border: "1px solid #ddd",
  borderRadius: 12,
  background: "white",
};

const itemStyle = {
  display: "block",
  padding: 12,
  borderRadius: 12,
  border: "1px solid #eee",
  textDecoration: "none",
  color: "inherit",
  background: "#fafafa",
};
