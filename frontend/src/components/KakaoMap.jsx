import { useEffect, useRef, useState } from "react";
import { apiGet } from "../api/client";

function getStageColor(stage) {
  const s = (stage || "").trim();

  if (!s) return "#9CA3AF";
  if (s.includes("착공") || s.includes("준공") || s.includes("철거")) {
    return "#28CD41";
  }
  if (s.includes("관리처분")) return "#FF3B30";
  if (s.includes("사업시행")) return "#FF9500";
  if (s.includes("조합")) return "#AF52DE";
  if (s.includes("정비구역")) return "#007AFF";

  return "#5856D6";
}

function parseGeoJsonToPaths(polygonGeojson, kakao) {
  if (!polygonGeojson) return [];

  let gj = polygonGeojson;

  try {
    if (typeof polygonGeojson === "string") {
      gj = JSON.parse(polygonGeojson);
    }
  } catch {
    return [];
  }

  if (gj?.type === "Feature") gj = gj.geometry;
  if (!gj || !gj.type || !gj.coordinates) return [];

  const toLatLngRing = (ring) =>
    ring
      .filter((pt) => Array.isArray(pt) && pt.length >= 2)
      .map(([lng, lat]) => new kakao.maps.LatLng(lat, lng));

  if (gj.type === "Polygon") {
    return [toLatLngRing(gj.coordinates?.[0] || [])];
  }

  if (gj.type === "MultiPolygon") {
    return (gj.coordinates || []).map((poly) => toLatLngRing(poly[0] || []));
  }

  return [];
}

function getPolygonCenter(pathsList, kakao) {
  if (!pathsList || pathsList.length === 0) return null;

  let minLat = Infinity;
  let maxLat = -Infinity;
  let minLng = Infinity;
  let maxLng = -Infinity;

  pathsList.forEach((path) => {
    path.forEach((latlng) => {
      const lat = latlng.getLat();
      const lng = latlng.getLng();

      if (lat < minLat) minLat = lat;
      if (lat > maxLat) maxLat = lat;
      if (lng < minLng) minLng = lng;
      if (lng > maxLng) maxLng = lng;
    });
  });

  if (
    minLat === Infinity ||
    maxLat === -Infinity ||
    minLng === Infinity ||
    maxLng === -Infinity
  ) {
    return null;
  }

  return new kakao.maps.LatLng((minLat + maxLat) / 2, (minLng + maxLng) / 2);
}

export default function KakaoMap({ onAreaClick, center, selectedAreaId }) {
  const [areas, setAreas] = useState([]);
  const [mapReady, setMapReady] = useState(false);

  const mapRef = useRef(null);
  const containerRef = useRef(null);
  const polygonsRef = useRef([]);
  const selectedPolygonRef = useRef(null);
  const onAreaClickRef = useRef(onAreaClick);

  useEffect(() => {
    onAreaClickRef.current = onAreaClick;
  }, [onAreaClick]);

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

  const highlightPolygon = (polygon) => {
    if (selectedPolygonRef.current && selectedPolygonRef.current !== polygon) {
      selectedPolygonRef.current.setOptions({
        strokeWeight: 1,
        strokeColor: selectedPolygonRef.current.originalColor,
        strokeOpacity: 0.6,
        fillColor: selectedPolygonRef.current.originalColor,
        fillOpacity: 0.15,
        zIndex: 1,
      });
    }

    polygon.setOptions({
      strokeWeight: 3,
      strokeColor: "#5757e9",
      strokeOpacity: 1,
      fillColor: polygon.originalColor,
      fillOpacity: 0.3,
      zIndex: 100,
    });

    selectedPolygonRef.current = polygon;
  };

  useEffect(() => {
    if (!mapReady || !mapRef.current || !center) return;

    const kakao = window.kakao;

    if (
      center.lat !== undefined &&
      center.lat !== null &&
      center.lng !== undefined &&
      center.lng !== null
    ) {
      const moveLatLng = new kakao.maps.LatLng(center.lat, center.lng);
      mapRef.current.panTo(moveLatLng);
      mapRef.current.setLevel(5);
    }

    if (center.targetId) {
      const targetPolygon = polygonsRef.current.find(
        (polygon) => polygon.areaId === center.targetId,
      );

      if (targetPolygon) {
        highlightPolygon(targetPolygon);
      }
    }
  }, [center, mapReady]);

  useEffect(() => {
    if (!mapReady || !selectedAreaId) return;

    const targetPolygon = polygonsRef.current.find(
      (polygon) => polygon.areaId === selectedAreaId,
    );

    if (targetPolygon) {
      highlightPolygon(targetPolygon);
    }
  }, [selectedAreaId, mapReady]);

  useEffect(() => {
    if (!window.kakao || !window.kakao.maps || !mapReady || areas.length === 0)
      return;

    const kakao = window.kakao;
    const map = mapRef.current;

    polygonsRef.current.forEach((polygon) => polygon.setMap(null));
    polygonsRef.current = [];
    selectedPolygonRef.current = null;

    areas.forEach((area) => {
      const baseColor = getStageColor(area.stage);
      const pathsList = parseGeoJsonToPaths(area.polygon, kakao);

      if (!pathsList.length) return;

      const fallbackCenter = getPolygonCenter(pathsList, kakao);

      const areaCenter =
        area.centerLat != null && area.centerLng != null
          ? new kakao.maps.LatLng(
              Number(area.centerLat),
              Number(area.centerLng),
            )
          : fallbackCenter;

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
        polygon.areaId = area.areaId;
        polygon.originalColor = baseColor;
        polygonsRef.current.push(polygon);

        kakao.maps.event.addListener(polygon, "click", () => {
          highlightPolygon(polygon);

          if (areaCenter) {
            map.panTo(areaCenter);
            map.setLevel(5);
          }

          if (onAreaClickRef.current) {
            onAreaClickRef.current({
              areaId: area.areaId,
              name: area.name,
              stage: area.stage,
              sigunguCd: area.sigunguCd,
              centerLat: area.centerLat,
              centerLng: area.centerLng,
            });
          }
        });
      });
    });
  }, [areas, mapReady]);

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
