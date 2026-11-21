# 서울 정비구역 지도

구역을 선택하면 사업이 어느 단계에 있는지, 이전 단계는 언제 진행됐는지 확인할 수 있는 지도 서비스입니다. 아버지의 재개발 컨설팅 업무에서 구역 경계와 사업 이력을 따로 조회하는 불편을 보고 시작한 개인 풀스택 프로젝트입니다.

**담당 범위**는 React와 Kakao Maps로 구현한 지도 및 상세 화면, Spring Boot 조회 API, MySQL 모델링, 정비사업 데이터 수집과 구역 매칭입니다. 사용자가 지도에서 구역을 선택하면 그 ID를 기준으로 이력을 조회해 14단계 타임라인에 표시합니다. 백엔드는 Java 17, Spring Boot 3.4.1, Spring Data JPA, MySQL을 사용하고 프론트엔드는 React, Vite, Kakao Maps SDK를 사용합니다.

## 지도에서 한 구역을 조회하는 과정

1. 지도는 목록 API에서 구역 경계와 중심 좌표를 페이지 단위로 읽습니다. 조회 대상은 정비사업 정보몽땅의 사업장 정보와 연결된 구역입니다.
2. 구역을 선택하면 상세 API에서 현재 단계, 단계별 날짜, 활동 이력과 연결된 중개사 정보를 함께 조회합니다.
3. 상세 화면은 14개 기준 단계를 먼저 배치한 뒤 API에서 받은 이력을 이름으로 대응시킵니다. 현재 단계가 우선 표시되며, 이력이 없는 단계는 날짜를 비워 둡니다.

> **시연 자료**　구역 선택과 14단계 타임라인을 보여 줄 화면을 추가할 자리입니다.
<!-- 이 문단을 실제 촬영한 GIF 또는 이미지와 한 줄 설명으로 교체합니다. -->

## 로컬 실행 검증 결과

2026-03-19 SQL 백업을 기존 DB와 분리된 MySQL 8.0.44에 복원해 검증했습니다. 아래 수치는 복원 후 SQL 조회 및 API 응답으로 확인한 범위입니다.

| 구분 | 확인한 값 |
| --- | --- |
| 저장된 전체 구역 | 2,302건 |
| 사업장 정보와 연결된 목록 조회 대상 | 440개 구역 |
| 저장된 단계 이력 | 1,869건 |
| 목록 / 검색 / 상세 API | 모두 HTTP 200 |
| 브라우저 검증 | 배경 지도, 구역 경계, 성수 검색, 구역 선택, 상세 및 타임라인 표시 |

목록 API는 전체 구역 중 사업장 정보와 연결된 구역만 반환합니다. **14개 단계는 화면의 기준 구조**이며, 시연한 성수전략정비구역 제3구역(`areaId=678`)에는 실제 이력 2건이 있습니다. 기록이 없는 단계는 날짜 없이 비활성 상태로 표시합니다.

백업 기준의 데이터이므로 현재 정비사업 현황을 나타내지 않습니다. 구역 678의 활동 기록과 중개사 응답은 비어 있습니다. 자세한 검증 범위와 남은 실행 환경 문제는 [실행 검증 기록](https://github.com/yusung0520/yusung0520/blob/main/projects/redevelopment-verification.md)에 정리했습니다.

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

- **백엔드**　`backend/Real_estate_backend`에서 로컬 DB 연결 설정을 준비합니다. 현재 저장소에는 Gradle wrapper JAR와 실행 권한이 누락되어 `./gradlew bootRun`만으로 실행할 수 없습니다. 이번 로컬 검증은 JDK 17과 설치된 Gradle 9.7.1의 `bootRun`을 사용했습니다. 저장소 wrapper 설정(9.2.1)과 실행 버전의 통일 및 테스트 환경 정리가 남아 있습니다.
- **프론트엔드**　`frontend/.env.local`에 `VITE_KAKAO_MAP_KEY`를 설정한 뒤 `frontend`에서 `npm install`, `npm run dev`를 실행합니다. Vite는 `/api` 요청을 로컬 백엔드의 8080 포트로 전달합니다. 검증 환경의 Kakao 허용 출처는 `http://localhost:5173`이며, `127.0.0.1` 주소는 별도 등록 없이는 동일하게 동작하지 않습니다.
- **다음 작업**　매칭하지 못한 구역과 중복 후보를 실제 데이터로 점검하고, 수집처와 화면 사이에서 단계명이 다르게 표기되는 경우를 테스트할 예정입니다.

중개사 브리핑과 거래 상태 관리 등 초기 구상은 [기획 문서](https://github.com/yusung0520/real-estate-redevelopment-platform/blob/0160d5abe5a81590a6f3346b8d95d08b59aafb60/README.md)에서 볼 수 있습니다.
