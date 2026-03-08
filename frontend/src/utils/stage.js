// src/utils/stage.js
export const TIMELINE_STAGES = [
  "정비구역 지정",
  "조합 설립",
  "사업시행 인가",
  "관리처분 인가",
  "착공/준공",
];

// eventLabel 정규화 (필요하면 계속 추가)
export function normalizeStageLabel(label) {
  if (!label) return "";

  const s = String(label).replace(/\s+/g, "").trim();

  if (s.includes("정비구역") && s.includes("지정")) return "정비구역 지정";
  if (s.includes("조합") && (s.includes("설립") || s.includes("인가")))
    return "조합 설립";
  if (s.includes("사업시행") && s.includes("인가")) return "사업시행 인가";
  if (s.includes("관리처분") && s.includes("인가")) return "관리처분 인가";
  if (s.includes("착공") || s.includes("준공")) return "착공/준공";

  // 못 맞추면 원본을 적당히 반환(디버깅 위해)
  return label;
}

export function formatDate(iso) {
  if (!iso) return "-";
  return String(iso).slice(0, 10).replaceAll("-", ".");
}
