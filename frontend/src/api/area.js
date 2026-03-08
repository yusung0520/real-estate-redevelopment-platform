// src/api/areas.js
import { apiGet } from "./client";

// ✅ 구역 목록
export function getAreas() {
  return apiGet("/api/areas");
}

// ✅ 통합 검색 API (추가됨)
export function searchAreas(query) {
  const encoded = encodeURIComponent(query);
  return apiGet(`/api/areas/search?q=${encoded}`);
}

// ✅ 구역 상세 정보 조회
export function getAreaDetail(areaId) {
  return apiGet(`/api/areas/${areaId}/detail`);
}

// ✅ (기존) 전체 폴리곤
export function getAreaPolygons(page = 0, size = 2656) {
  return apiGet(`/api/areas/polygons?page=${page}&size=${size}`);
}

/**
 * ✅ (추가) 서비스 코어 폴리곤만
 * - is_active=1
 * - polygon 존재
 * - 문서 OR 이벤트 존재(= 지금 너가 "크롤링해서 가지고 온 것" 필터에 해당)
 *
 * ⚠️ 백엔드는 /areas/... 인데
 * 프론트는 /api/... 로 쓰고 있으니
 * 여기서는 일단 /areas 로 바로 때림(아래 2번 참고)
 */
export function getServiceCorePolygons(page = 0, size = 5000) {
  return apiGet(`/areas/polygons/service/core?page=${page}&size=${size}`);
}

/** ✅ (추가) 서비스 전체 폴리곤 */
export function getServiceAllPolygons(page = 0, size = 5000) {
  return apiGet(`/areas/polygons/service/all?page=${page}&size=${size}`);
}

// ✅ 크롤링된 구역 폴리곤만
export function getCrawledAreaPolygons(page = 0, size = 500) {
  return apiGet(`/api/areas/polygons/crawled?page=${page}&size=${size}`);
}

export function getCleanupMatchedPolygons(page = 0, size = 2000) {
  return apiGet(
    `/api/areas/polygons/service/cleanup-matched?page=${page}&size=${size}`,
  );
}
