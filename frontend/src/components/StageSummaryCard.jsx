// src/components/StageSummaryCard.jsx
export default function StageSummaryCard({ name, currentStage, currentDate }) {
  return (
    <div style={cardStyle}>
      <h2 style={{ margin: 0 }}>{name}</h2>

      <div style={{ marginTop: 10, fontSize: 14, color: "#666" }}>
        <span style={badgeStyle}>공식 문서 기반</span>
      </div>

      <div style={{ marginTop: 12 }}>
        <div style={{ fontSize: 16 }}>
          <b>현재 공식 단계:</b> {currentStage || "없음"}
        </div>
        <div style={{ marginTop: 6, color: "#666" }}>
          <b>확정일:</b> {currentDate || "-"}
        </div>
      </div>
    </div>
  );
}

const cardStyle = {
  marginTop: 16,
  padding: 16,
  border: "1px solid #ddd",
  borderRadius: 12,
  background: "white",
};

const badgeStyle = {
  display: "inline-block",
  padding: "4px 10px",
  borderRadius: 999,
  background: "#eef6ff",
  border: "1px solid #cfe6ff",
  color: "#1a5fb4",
  fontWeight: 700,
};
