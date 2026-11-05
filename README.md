# 서울 정비구역 지도

구역을 선택하면 사업이 어느 단계에 있는지, 이전 단계는 언제 진행됐는지 확인할 수 있는 지도 서비스입니다. 아버지의 재개발 컨설팅 업무에서 구역 경계와 사업 이력을 따로 조회하는 불편을 보고 시작한 개인 프로젝트입니다.

**담당 범위**는 정비사업 데이터 수집과 구역 매칭, MySQL 모델링, Spring Boot 조회 API, React 지도 및 상세 화면 연동입니다. 백엔드는 Java 17, Spring Boot 3.4.1, Spring Data JPA, MySQL을 사용하고 프론트엔드는 React, Vite, Kakao Maps SDK를 사용합니다.

## 지도에서 한 구역을 조회하는 과정

1. 지도는 목록 API에서 구역 경계와 중심 좌표를 페이지 단위로 읽습니다. 조회 대상은 정비사업 정보몽땅의 사업장 정보와 연결된 구역입니다.
2. 구역을 선택하면 상세 API에서 현재 단계, 단계별 날짜, 활동 이력과 연결된 중개사 정보를 함께 조회합니다.
3. 상세 화면은 14개 기준 단계를 먼저 배치한 뒤 API에서 받은 이력을 이름으로 대응시킵니다. 현재 단계가 우선 표시되며, 이력이 없는 단계는 날짜를 비워 둡니다.

> **시연 자료**　구역 선택과 14단계 타임라인을 보여 줄 화면을 추가할 자리입니다.
<!-- 이 문단을 실제 촬영한 GIF 또는 이미지와 한 줄 설명으로 교체합니다. -->

## 데이터 처리에서 결정한 것

**비슷한 구역명을 무조건 연결하지 않습니다.** 자동 매칭의 완전 일치 단계는 이름을 정규화하고 같은 이름의 후보가 여러 개인 경우 건너뜁니다. 부분 일치 단계는 최소 점수 외에 1위와 2위 후보의 점수 차이도 확인합니다. 후보가 비슷하거나 이미 연결된 구역이면 미연결 상태로 남길 수 있고, `dryRun`으로 저장 전에 결과를 살펴볼 수 있습니다. [자동 매칭 코드](backend/Real_estate_backend/src/main/java/com/yusung/realestateapi/backend/integration/cleanup/application/CleanupCafeAreaAutoMatchService.java)

**같은 구역의 같은 단계를 다시 수집하면 기존 행을 찾아 갱신합니다.** 수집 시 `areaId`와 `stepNo`로 이력을 찾습니다. 현재 단계 번호가 확인된 수집에서만 해당 구역의 현재 표시를 재설정합니다. 상세 조회에서는 사업장 단계명(값이 없으면 구역 단계명)과 이력의 단계명을 비교해 화면에 전달할 `isCurrent`를 계산합니다. [이력 저장](backend/Real_estate_backend/src/main/java/com/yusung/realestateapi/backend/integration/cleanup/application/CleanupProgressUpsertService.java) / [상세 조회](backend/Real_estate_backend/src/main/java/com/yusung/realestateapi/backend/area/application/AreaQueryService.java)

**목록과 상세 응답을 분리했습니다.** 목록은 지도 표시용 경계와 좌표를 담고, 상세는 `timeline`, `history`, `agents`로 데이터를 구분합니다. `timeline`은 기준 단계의 진행 날짜를, `history`는 개별 진행 기록의 날짜와 제목을 제공합니다. [응답 DTO](backend/Real_estate_backend/src/main/java/com/yusung/realestateapi/backend/area/dto/AreaDto.java) / [상세 화면](frontend/src/pages/AreaDetailPage.jsx)

## 주요 API

| 요청 | 용도 |
| --- | --- |
| `GET /api/areas?page=0&size=20` | 매칭된 구역을 페이지 단위로 조회 |
| `GET /api/areas/search?q=성수` | 구역명 또는 사업명으로 검색. 최대 20건 |
| `GET /api/areas/{areaId}/detail` | 구역 정보, 단계별 이력, 활동 기록, 중개사 조회 |

경로는 [AreaController](backend/Real_estate_backend/src/main/java/com/yusung/realestateapi/backend/area/api/AreaController.java)에서, 목록 필터와 검색 조건은 [AreaV3Repository](backend/Real_estate_backend/src/main/java/com/yusung/realestateapi/backend/area/infra/AreaV3Repository.java)에서 확인할 수 있습니다.

## 로컬에서 확인하기

JDK 17, MySQL, Node.js와 npm, Kakao Maps JavaScript 키가 필요합니다. 구역을 실제로 보려면 로컬 DB에 경계와 사업장 매칭 데이터가 있어야 합니다.

- **백엔드**　`backend/Real_estate_backend`에서 로컬 DB 연결 설정을 준비하고 `./gradlew bootRun`으로 실행합니다.
- **프론트엔드**　`frontend/.env.local`에 `VITE_KAKAO_MAP_KEY`를 설정한 뒤 `frontend`에서 `npm install`, `npm run dev`를 실행합니다. Vite는 `/api` 요청을 로컬 백엔드의 8080 포트로 전달합니다.
- **다음 작업**　매칭하지 못한 구역과 중복 후보를 실제 데이터로 점검하고, 수집처와 화면 사이에서 단계명이 다르게 표기되는 경우를 테스트할 예정입니다.

중개사 브리핑과 거래 상태 관리 등 초기 구상은 [기획 문서](https://github.com/yusung0520/real-estate-redevelopment-platform/blob/0160d5abe5a81590a6f3346b8d95d08b59aafb60/README.md)에서 볼 수 있습니다.
