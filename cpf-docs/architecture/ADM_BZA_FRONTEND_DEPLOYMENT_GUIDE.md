# CPF ADM·BZA Frontend·Deployment Guide

## 1. 목적

`cpf-admin`과 `cpf-biz-admin` Frontend를 독립 Application과 Artifact로 설계·검증·배포하는 기준을 정의한다.

## 2. 책임 분리

### ADM

플랫폼 거래, Batch, Center-Cut, Agent/Runner/Worker, Gateway, Log, Security, Approval, Audit와 Recovery를 관리한다.

### BZA

고객 업무 Menu, 조회, Download, Approval, 고객 Extension과 선택형 Customization Sample을 관리한다. Platform Runtime을 직접 제어하지 않는다.

## 3. Frontend 구조

```text
src/
  app/
  router/
  auth/
  api/
  shared/
  features/<feature>/
    api/
    components/
    pages/
    stores/
    types/
    tests/
```

거대한 단일 Vue 파일, 전역 shared 남용과 feature 간 직접 내부 import를 금지한다.

## 4. API와 권한

- Frontend menu visibility는 편의 기능
- 실제 권한은 Server API에서 검증
- route/action/API permission code 정합성
- session expiry와 re-auth
- 401/403/409/422/429/5xx 표준 처리
- 위험 조치 reason/approval/confirmation
- 개인정보 masking과 download governance

## 5. 목록·상세 UX

거래·Batch·Center-Cut 목록은 시작/종료시간, 상태, 소요시간, transactionGlobalId, segment, failure stage, 주요 Header와 운영 가치가 있는 DB Column을 초기 설계에 포함한다.

- filter allowlist
- sort allowlist
- offset/keyset pagination
- deep link
- saved search 후보
- export limit와 asynchronous download
- accessibility와 keyboard

## 6. Build

각 Frontend에서 다음을 독립 실행한다.

```text
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

`package-lock.json` integrity를 유지하고 Build 결과에 source map, secret, internal URL이 노출되지 않는지 확인한다.

## 7. 독립 배포

- ADM/BZA Static Artifact 별도 Version
- Nginx/Apache/Object Storage/CDN 후보
- Java API/WAS와 분리
- SPA base path와 deep-link fallback
- API base URL 외부화
- CSP, HSTS, secure cookie와 CSRF
- cache busting과 immutable asset
- frontend/backend compatibility matrix
- independent rollback

## 8. Browser E2E

- login/session expiry
- role별 menu와 API denial
- list/search/detail
- approve/reject
- pause/resume/cancel/reprocess
- validation/error boundary
- deep link/reload
- download permission
- rollback compatibility

## 9. 완료 조건

Dev server 화면만으로 완료 처리하지 않는다. Production artifact를 실제 Web Server에 배포하고 Java API와 분리된 topology에서 Browser Evidence를 제출한다.
