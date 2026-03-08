// src/components/StageTimeline.jsx
import { formatDate } from "../utils/stage";

// ✅ 기본 단계 목록(몽땅 카페처럼 "항상 전체 노출")
const DEFAULT_STAGES = [
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

// "STEP_03" -> 3
function extractOrder(stageKey) {
  if (!stageKey) return NaN;
  const m = String(stageKey).match(/(\d+)/);
  return m ? Number(m[1]) : NaN;
}

// "YYYY-MM-DD" -> "YYYY.MM.DD"
function formatDay(day) {
  if (!day) return "";
  const s = String(day).trim();
  if (s.includes("-")) {
    const [y, m, d] = s.split("-");
    return `${y}.${Number(m)}.${Number(d)}`;
  }
  return s;
}

export default function StageTimeline({
  stages = [],
  loading = false,
  error = null,
}) {
  if (loading) {
    return (
      <div style={wrapStyle}>
        <h3 style={{ margin: "0 0 10px 0" }}>사업 진행단계</h3>
        <div>불러오는 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={wrapStyle}>
        <h3 style={{ margin: "0 0 10px 0" }}>사업 진행단계</h3>
        <div style={{ color: "#b91c1c" }}>타임라인 로딩 실패</div>
      </div>
    );
  }

  // ✅ order -> api stage 매핑
  const apiMap = new Map();
  let currentOrder = null;

  for (const s of stages || []) {
    const order =
      Number(s.order ?? s.stepNo ?? s.stageOrder) || extractOrder(s.stageKey);
    if (!Number.isFinite(order)) continue;

    apiMap.set(order, s);
    if (s.isCurrent) currentOrder = order;
  }

  // ✅ DEFAULT + API merge
  const timeline = DEFAULT_STAGES.map((base) => {
    const api = apiMap.get(base.order);

    const label = api?.label ?? base.label;
    const date = api?.date ?? null;

    // done: API done 우선, 없으면 date 있으면 done으로 처리
    const done = typeof api?.done === "boolean" ? api.done : Boolean(date);

    const isCurrent = Boolean(api?.isCurrent) || currentOrder === base.order;

    return {
      order: base.order,
      label,
      date,
      done,
      isCurrent,
    };
  });

  return (
    <div style={wrapStyle}>
      <h3 style={{ margin: "0 0 10px 0" }}>사업 진행단계</h3>
      <div style={{ color: "#666", fontSize: 13, marginBottom: 12 }}>
        본 타임라인은 <b>공식 진행단계</b>를 표시합니다.
      </div>

      <div>
        {timeline.map((t, idx) => {
          const highlight = t.isCurrent || t.done;
          const isLast = idx === timeline.length - 1;

          return (
            <div key={t.order} style={rowStyle}>
              <div
                style={{ width: 26, display: "flex", justifyContent: "center" }}
              >
                <div style={dotStyle(t.done, t.isCurrent)} />
              </div>

              <div style={{ flex: 1 }}>
                <div
                  style={{
                    fontWeight: 800,
                    color: highlight ? "#1a5fb4" : "#666",
                  }}
                >
                  {t.label}
                </div>
                {t.isCurrent && (
                  <div
                    style={{
                      fontSize: 12,
                      color: "#2563EB",
                      marginTop: 3,
                      fontWeight: 800,
                    }}
                  >
                    현재 단계
                  </div>
                )}
              </div>

              <div
                style={{
                  width: 110,
                  textAlign: "right",
                  color: highlight ? "#1a5fb4" : "#aaa",
                  fontWeight: 700,
                }}
              >
                {t.date ? formatDay(t.date) : ""}
              </div>

              {!isLast && <div style={lineStyle(t.done)} />}
            </div>
          );
        })}
      </div>
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

const rowStyle = {
  position: "relative",
  display: "flex",
  alignItems: "center",
  padding: "10px 0",
};

const dotStyle = (done, isCurrent) => ({
  width: 12,
  height: 12,
  borderRadius: "50%",
  background: isCurrent ? "#2563EB" : done ? "#1a5fb4" : "#cfcfcf",
});

const lineStyle = (done) => ({
  position: "absolute",
  left: 13,
  top: 28,
  width: 2,
  height: 24,
  background: done ? "#1a5fb4" : "#e0e0e0",
});
