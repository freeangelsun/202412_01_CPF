# ADM / BZA Frontend 개발 표준

## 1. 목적

`cpf-admin`(ADM)과 `cpf-biz-admin`(BZA)의 Vue 화면은 운영 기능이 늘어날수록 하나의 `App.vue` 또는 `console.ts`에 기능을 누적하지 않는다.
Frontend는 Java WAS와 독립적으로 build/deploy/rollback 가능한 Static Web Artifact를 목표로 한다.

## 2. 공통 구조

```text
frontend/src/
  app/                 # App Shell, route/menu registry
  components/          # 재사용 UI component
  features/<feature>/  # 기능별 page/panel + API/state boundary
  state/               # 화면 간 공유 상태(필요한 경우)
  styles/              # 제품 로컬 CSS
  App.vue              # Shell과 인증/Navigation 조립만
  main.ts
```

원칙:

- route/menu registry가 화면 ID, 권한 menuCode, lazy loader를 소유한다.
- 기능 page/panel은 `features/<feature>`가 소유한다.
- 공통 API/session/state는 명확한 boundary로 분리한다.
- route 단위 dynamic import를 사용해 code splitting한다.
- Backend API contract를 화면 구조 변경 때문에 임의로 변경하지 않는다.
- 위험 조치는 권한, 사유, 승인, 감사 계약을 우회하지 않는다.

## 3. ADM

ADM은 Platform Control Plane이다.

R7 기준 분리:

- `features/observability`: 거래/로그/원격로그/관측
- `features/platform`: 실행 카탈로그, 채널, 서비스 Registry, 캐시, 메시지, 코드/설정
- `features/business`: 회원 등 운영 조회
- `features/batch`: Batch/Worker/Center-Cut
- `features/access`: 권한/보안/운영자

`App.vue`는 로그인, 강제 비밀번호 변경, Navigation, 현재 feature lazy loading만 담당한다.
기존 Controller/API의 Owner Boundary를 유지하며 ADM이 다른 업무 Owner DB를 직접 갱신하는 신규 구조를 만들지 않는다.

## 4. BZA

BZA는 Customer Business Admin이다.

R7 기준 분리:

- `features/auth`: 로그인/refresh/logout/session
- `features/dashboard`
- `features/directory`: 조직/직원
- `features/access`: 사용자/Role/Menu/Permission/권한 분석
- `features/approval`
- `features/support`: session/audit/notification/attachment/saved search/setting/download
- `components`: CRUD table, data table, metric 등

BZA의 업무 승인/조직/권한은 ADM Platform Approval과 합치지 않는다.

## 5. 외부 자산 금지

운영 Runtime에서 다음을 참조하지 않는다.

- CDN JavaScript
- remote CSS
- Google Font 등 remote font
- remote icon/image asset

오픈소스를 도입해야 한다면 License를 검토하고 package 또는 Repository 내부 asset으로 고정해 build artifact에 포함한다.
Network가 차단된 운영 환경에서도 정적 artifact만으로 렌더링되어야 한다.

## 6. 검증

최소 검증:

1. `npm ci` 또는 lockfile 기반 재현 설치
2. `npm run test` / `npm run build` 또는 프로젝트 `npm run verify`
3. route lazy chunk 생성 확인
4. 권한별 메뉴 노출/차단
5. 로그인/refresh/logout/password change
6. ADM/BZA 주요 CRUD/조회/위험 조치 Browser E2E
7. Browser network에서 외부 CDN/remote asset 요청 0건
8. Java API 오류/401/403/409/5xx 표현
9. 기존 성공 화면 회귀

실행하지 않은 Browser/Runtime 검증은 `미검증`으로 기록한다.
