import { useEffect, useRef, useState } from "react";
import { apiGet } from "../api/client";

// ✅ 1. 단계별 폴리곤 색상 지정 (Apple-style Palette)
function getStageColor(stage) {
  const s = (stage || "").trim();
  if (!s) return "#9CA3AF";
  if (s.includes("착공") || s.includes("준공") || s.includes("철거"))
    return "#28CD41";
  if (s.includes("관리처분")) return "#FF3B30";
  if (s.includes("사업시행")) return "#FF9500";
  if (s.includes("조합")) return "#AF52DE";
  if (s.includes("정비구역")) return "#007AFF";
  return "#5856D6";
}

// ✅ 2. GeoJSON 데이터를 카카오 좌표로 변환
function parseGeoJsonToPaths(polygonGeojson, kakao) {
  if (!polygonGeojson) return [];
  let gj = polygonGeojson;
  try {
    if (typeof polygonGeojson === "string") gj = JSON.parse(polygonGeojson);
  } catch {
    return [];
  }

  if (gj?.type === "Feature") gj = gj.geometry;
  if (!gj || !gj.type || !gj.coordinates) return [];

  const toLatLngRing = (ring) =>
    ring
      .filter((pt) => Array.isArray(pt) && pt.length >= 2)
      .map(([lng, lat]) => new kakao.maps.LatLng(lat, lng));

  if (gj.type === "Polygon") return [toLatLngRing(gj.coordinates?.[0] || [])];
  if (gj.type === "MultiPolygon") {
    return (gj.coordinates || []).map((poly) => toLatLngRing(poly[0] || []));
  }
  return [];
}

export default function KakaoMap({ onAreaClick, center }) {
  const [areas, setAreas] = useState([]);
  const [mapReady, setMapReady] = useState(false);
  const mapRef = useRef(null);
  const containerRef = useRef(null);
  const polygonsRef = useRef([]);
  const selectedPolygonRef = useRef(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await apiGet("/api/areas?page=0&size=2000");
        setAreas(data?.content || (Array.isArray(data) ? data : []));
      } catch (e) {
        console.error("데이터 로드 실패:", e);
      }
    })();
  }, []);

  useEffect(() => {
    if (!window.kakao || !window.kakao.maps) return;
    if (mapRef.current) return;

    window.kakao.maps.load(() => {
      const kakao = window.kakao;

      if (containerRef.current) {
        const map = new kakao.maps.Map(containerRef.current, {
          center: new kakao.maps.LatLng(37.5665, 126.978),
          level: 8,
        });

        mapRef.current = map;
        setMapReady(true);
      }
    });
  }, []);

  useEffect(() => {
    if (!mapReady || !mapRef.current || !center || !center.lat) return;
    const kakao = window.kakao;
    const moveLatLon = new kakao.maps.LatLng(center.lat, center.lng);
    mapRef.current.panTo(moveLatLon);
    mapRef.current.setLevel(5);

    if (center.targetId) {
      const target = polygonsRef.current.find(
        (p) => p.areaId === center.targetId,
      );
      if (target) highlightPolygon(target);
    }
  }, [center, mapReady]);

  // ⭐ [디자인 반영] 진한 남색(Midnight) 테두리 강조 함수
  const highlightPolygon = (polygon) => {
    // 1. 이전 선택 스타일 원복
    if (selectedPolygonRef.current) {
      selectedPolygonRef.current.setOptions({
        strokeWeight: 1,
        strokeColor: selectedPolygonRef.current.originalColor,
        strokeOpacity: 0.6,
        fillColor: selectedPolygonRef.current.originalColor,
        fillOpacity: 0.15,
        zIndex: 1,
      });
    }

    // 2. 새로운 선택 강조 (Midnight Navy 테두리)
    polygon.setOptions({
      strokeWeight: 3, // 사용자님이 선호하시는 깔끔한 두께
      strokeColor: "#5757e9", // ✅ 애플 공식 미드나이트 남색 (거의 검정에 가까운 아주 진한 남색)
      strokeOpacity: 1, // 뚜렷하게 강조
      fillColor: polygon.originalColor, // 안쪽 면색은 원래 색상 유지
      fillOpacity: 0.3, // 선택되었으므로 가독성을 위해 살짝 강조
      zIndex: 100, // 최상단 배치
    });
    selectedPolygonRef.current = polygon;
  };

  useEffect(() => {
    if (!window.kakao || !window.kakao.maps || !mapReady || areas.length === 0)
      return;
    const kakao = window.kakao;
    const map = mapRef.current;

    polygonsRef.current.forEach((p) => p.setMap(null));
    polygonsRef.current = [];

    areas.forEach((a) => {
      const baseColor = getStageColor(a.stage);
      const pathsList = parseGeoJsonToPaths(a.polygon, kakao);

      pathsList.forEach((path) => {
        const polygon = new kakao.maps.Polygon({
          path,
          strokeWeight: 1,
          strokeColor: baseColor,
          strokeOpacity: 0.6,
          fillColor: baseColor,
          fillOpacity: 0.15,
          zIndex: 1,
        });

        polygon.setMap(map);
        polygon.areaId = a.areaId;
        polygon.originalColor = baseColor;
        polygonsRef.current.push(polygon);

        kakao.maps.event.addListener(polygon, "click", () => {
          highlightPolygon(polygon);
          if (a.areaId && onAreaClick) onAreaClick(a.areaId);
        });
      });
    });
  }, [areas, mapReady, onAreaClick]);

  return (
    <div style={{ width: "100%", height: "100%", position: "relative" }}>
      <div
        ref={containerRef}
        style={{
          width: "100%",
          height: "100%",
          position: "absolute",
          top: 0,
          left: 0,
        }}
      />
    </div>
  );
}
