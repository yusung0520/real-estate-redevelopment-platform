// src/api/areaStageDetail.js
import { apiGet } from "./client";

// stage-detail 전용
export function getAreaStageDetail(areaId) {
  return apiGet(`/api/areas/${areaId}/stage-detail`);
}
