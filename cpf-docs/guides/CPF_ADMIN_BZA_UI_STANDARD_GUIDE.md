# CPF ADM·BZA 화면 표준 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 화면 설계자, 프런트엔드 개발자, 접근성 검수자
> **목적**: ADM·BZA 화면을 일관된 탐색·상태·오류·권한 기준으로 구현한다.
> **관련 문서**: [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md) · [업무 관리자](CPF_BIZ_ADMIN_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-admin/frontend`, `cpf-biz-admin/frontend` |
| 이 문서로 완료하는 일 | 기능별 Route·API·상태·Component 책임을 분리하고, 검색·Paging·오류·권한·위험 조치·접근성을 같은 화면 규칙으로 구현한다. |
| 적용 범위 | ADM/BZA의 목록·상세·등록·승인·실행·다운로드 화면과 공통 UI Component |
| 주요 독자 | 화면 설계자, Vue 개발자, API 개발자, 접근성·보안 검수자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

ADM과 BZA는 실제 운영자가 매일 사용하는 운영 화면이다. 기능 존재 여부뿐 아니라 검색성, 오류 복구, 권한, 안전성, 접근성과 유지보수성을 제품 기준으로 통일한다.

## 2. Feature 구조

```text
features/<feature>/
├─ api/
├─ model/
├─ state/
├─ components/
├─ pages/
├─ routes.ts
└─ tests/
```

대형 단일 Component에 API, 상태, 표, Form과 Dialog를 집중시키지 않는다.

## 3. 경로

각 메뉴는 목적별 경로와 Component를 가진다. 여러 메뉴를 같은 화면 별칭으로 연결하지 않는다.

경로 메타데이터:

- title
- group
- permission
- breadcrumb
- helpLink
- dangerous
- keepAlive 정책

Lazy Import 대상 실파일을 Gate로 확인한다.

## 4. 디자인 체계

공통 요소:

- Typography
- Spacing
- Form
- Button
- Table
- Badge
- 경보
- Dialog
- Drawer
- Empty/오류 State
- Skeleton
- Icon

외부 실행 환경 CDN, Font, Script에 의존하지 않는다.

## 5. 공통 Component

- `CpfCodeSelect`
- `CpfDateRange`
- `CpfPageTable`
- `CpfStatusBadge`
- `CpfErrorPanel`
- `CpfConfirmDialog`
- `CpfReasonInput`
- `CpfPermissionGuard`
- `CpfSensitiveValue`
- `CpfJsonViewer` 보조용
- `CpfTraceLink`
- `CpfAuditSummary`

## 6. 검색 화면

필수 상태:

- 초기
- Loading
- Empty
- 성공
- 검증 오류
- 401
- 403
- 시간 제한
- Server 오류
- Stale 응답
- 취소

검색 UX:

- Enter 검색
- Reset
- URL 조회 동기화
- 저장 조건
- 기간 기본값
- 페이지 변경
- Sort
- 결과 건수
- 마지막 조회시각

## 7. 페이징

- Server 페이지 또는 Cursor
- 페이지 Size 상한
- Stable Order
- 로딩 중 중복 호출 방지
- Filter 변경 시 첫 페이지
- Count 실패와 목록 실패 구분
- 대량 반출은 비동기 작업

## 8. 상세 화면

상세는 다음을 구조화한다.

- 기본 정보
- 상태
- 소유자
- 버전
- 관계
- 시간선
- 오류
- 감사
- 가능한 Action

Raw JSON만 표시하지 않는다. JSON Viewer는 진단용 보조 영역으로 둔다.

## 9. Form

### 참조 Catalog 선택기

서비스, 서버 그룹, 경로, 작업정의, 비밀값 참조 등 다른 관리 대상의 식별자를 선택하는 필드는 자유 입력보다 검색 가능한 Catalog 선택기를 사용한다.

- 입력 검색과 선택 목록을 분리한다.
- 상위 매개변수에 따라 하위 후보를 제한한다.
- 상위 값 변경 시 기존 하위 선택을 초기화한다.
- 비활성 항목은 선택을 막고 사유를 표시한다.
- 조회 실패와 후보 없음 상태를 구분한다.
- 680px 이하에서는 검색창과 선택 목록을 한 열로 배치한다.
- 민감 참조는 원문을 표시하지 않고 참조 식별자만 다룬다.

참조 Catalog는 서버 Capability가 명확할 때만 사용할 수 있다.

- `available=false` 또는 조회 오류면 자유 입력으로 자동 전환하지 않고 선택을 막는다.
- 지원하지 않는 Reference Type은 Field 오류와 운영 진단을 함께 제공한다.
- 상위 Reference가 바뀌면 진행 중 요청의 Sequence를 폐기하고 기존 하위 값을 즉시 초기화한다.
- 비동기 응답 순서가 뒤바뀌어 오래된 후보가 최신 Form을 덮지 않도록 요청 Sequence를 비교한다.
- `AdmParameterReferenceProperties` 등 Backend 설정은 Type별 Endpoint·Provider를 명시하고 미구성 상태를 안전 차단한다.

Form 공통 기준:

- Label과 설명
- 필수 표시
- Client 검증
- Server 검증 매핑
- 필드 단위 오류
- Form 상단 오류 요약
- 변경 감지
- 이탈 확인
- 저장 중 비활성
- Double Submit 방지
- 성공 후 최신 재조회

## 10. 상태

상태 Badge는 Catalog를 사용한다.

- label
- severity
- icon
- description
- allowedActions

임의 문자열 비교를 Component마다 반복하지 않는다.

## 11. 권한

```text
Menu visibility
+ Route Guard
+ Button Guard
+ API Server Enforcement
```

화면에서 숨겼다고 권한이 보장되는 것은 아니다.

READ 사용자는 변경 버튼을 볼 수 없다. 위험 Action은 별도 권한을 사용한다.

## 12. 위험 조치

Dialog 필수 항목:

- 대상
- 현재 상태
- 영향
- 사유
- 승인 필요 여부
- 버전
- 최종 확인 문구
- 실행 후 결과

고위험 조치는 단순 `confirm()`을 사용하지 않는다.

## 13. 민감정보

기본은 마스킹이다.

원문 보기:

- 권한
- 사유
- 재인증
- 표시 시간 제한
- 클립보드 통제
- 감사
- 화면 이탈 시 제거

## 14. 오류 처리

표준 오류별 UX:

| 오류 | UX |
|---|---|
| 400 검증 | 필드 오류와 요약 |
| 401 | 세션 정리 후 로그인 |
| 403 | 권한 부족 설명 |
| 404 | 대상 변경/삭제 안내 |
| 409 | 최신 데이터 비교와 재조회 |
| 429 | 재시도 시각 표시 |
| 시간 제한 | 결과 불명 가능성 안내 |
| 500 | transactionId 제공 |
| Network | 연결 복구 후 재시도 |

명령 시간 제한은 곧바로 실패로 단정하지 않고 결과 조회 링크를 제공한다.

## 15. Stale 응답

검색 조건이 빠르게 바뀌면 이전 응답이 최신 화면을 덮지 않도록 요청 Token 또는 Abort 컨트롤러를 사용한다.

## 16. 접근성

- Keyboard 탐색
- Focus 순서
- Dialog Focus Trap
- Label 연결
- 상태를 색상만으로 표현하지 않음
- Screen Reader 설명
- 충분한 대비
- Zoom과 반응형
- Skip Link
- Table Caption

## 17. 반응형

- 넓은 화면: Table + Detail
- 중간 화면: Table + Drawer
- 작은 화면: Card/List + Detail 페이지
- 고정 폭 Raw JSON 금지
- 긴 ID는 Copy와 줄바꿈

## 18. 성능

- 경로 단위 Code Split
- 대형 Table Virtualization
- 검색 Debounce
- API 캐시 정책
- 중복 호출 제거
- 긴 시간선 Cursor
- Bundle Size Gate

## 19. 테스트

### Component

- Render
- 검증
- 권한
- 상태
- Dialog

### 통합

- API 성공/오류
- 401/403/409
- Stale 응답
- Double Click
- Cursor

### 브라우저

- 실제 경로
- Keyboard
- 권한별 메뉴
- 위험 Action
- 감사 결과
- Console 오류 0
- 외부 실행 환경 URL 0

## 20. 완료 체크리스트

- [ ] 기능별 Directory와 경로다.
- [ ] Raw JSON 중심이 아니다.
- [ ] 검색·페이징·상세가 있다.
- [ ] 모든 오류 상태가 있다.
- [ ] 권한이 화면과 서버에 적용된다.
- [ ] 위험 조치에 사유·확인·승인이 있다.
- [ ] 접근성과 반응형을 지원한다.
- [ ] 외부 CDN/Font/Script에 의존하지 않는다.

## 부록 A. 반응형 기준

- 모바일에서 가로 ASCII 도식과 고정 폭 표를 사용하지 않는다.
- 핵심 정보는 세로 흐름으로 재배치하고 부가 열은 상세 화면으로 이동한다.
- 표는 열 숨김·행 카드·가로 스크롤 중 기능에 맞는 방식을 명시한다.
- 위험 명령의 대상·영향·사유·승인은 작은 화면에서도 생략하지 않는다.
- 터치 대상은 충분한 크기와 간격을 유지한다.

## 부록 B. 상태 화면 계약

모든 비동기 화면은 다음 상태를 별도로 표현한다.

- 최초 로딩
- 부분 로딩
- 자료 없음
- 검색 결과 없음
- 권한 없음
- 연결 실패
- 서버 오류
- 오래된 응답
- 저장 중
- 성공
- 충돌과 최신 자료 다시 불러오기

## 부록 C. 접근성 검수

- 키보드만으로 모든 기능 수행
- 논리적인 초점 이동과 보이는 초점 표시
- 제목·랜드마크·레이블 연결
- 색상만으로 상태 전달 금지
- 오류 요약과 필드별 오류 연결
- 대화상자 초점 고정과 닫은 뒤 원위치 복귀
- 표 머리글·정렬 상태·페이지 정보 제공
- 화면 확대 200%와 고대비 점검

## 부록 D. 금지 사례

- 한 파일에 여러 기능의 경로·상태·API 호출 집중
- 화면이 전체 목록을 받은 뒤 브라우저에서 잘라내기
- 권한 판단을 버튼 숨김으로만 처리
- 오류 응답을 사용자에게 원문 그대로 표시
- 위험 조치를 일반 저장 버튼과 같은 흐름으로 처리
- 외부 CDN·폰트·스크립트에 실행 시점 의존

## 29. 기능 Package 표준

하나의 운영 기능은 Route, Page, API Client, State, Component와 Test를 같은 기능 Directory에서 찾을 수 있어야 한다.

```text
features/<feature-name>/
├─ routes.ts
├─ api.ts
├─ state.ts
├─ types.ts
├─ <Feature>Page.vue
├─ components/
└─ __tests__/
```

공통 Component는 두 기능 이상에서 같은 계약으로 재사용될 때만 `components/`로 올린다. 기능별 API 호출과 상태 전이를 하나의 전역 파일에 계속 추가하지 않는다.

## 30. 목록 화면 상태 모델

모든 목록 화면은 다음 상태를 구분한다.

- `idle`: 아직 조회하지 않음
- `loading`: 최초 또는 조건 변경 조회 중
- `refreshing`: 기존 목록을 유지한 채 재조회
- `success`: 결과 있음
- `empty`: 정상 응답이지만 결과 없음
- `error`: 요청 실패
- `stale`: 과거 응답이 최신 조건 뒤에 도착

검색 조건은 URL Query 또는 명시적 State에 저장해 뒤로가기와 새로고침에서 재현되게 한다. Page, Size, Sort, Filter를 API와 같은 기준으로 유지하고 Browser에서 전체 목록을 잘라 Paging하지 않는다.

## 31. 상세·편집 화면

- 조회 DTO와 저장 Command를 분리한다.
- 숨겨진 Field나 화면의 Operator ID를 신뢰하지 않는다.
- `expectedVersion`, `operationId`, `reason`, 필요 시 `approvalId`를 명시한다.
- 저장 전 변경 Diff와 영향 범위를 보여준다.
- 409 충돌이면 최신 값, 사용자가 편집한 값과 충돌 Field를 비교한다.
- 성공 후 목록을 추정 갱신하지 않고 서버 결과 또는 재조회로 확정한다.

## 32. 위험 조치 UX

| 위험도 | 예 | UX 요구 |
|---|---|---|
| 낮음 | 조회 조건 초기화 | 즉시 실행 가능 |
| 중간 | Cache Refresh, 재조회 | 대상과 영향 표시 |
| 높음 | Drain, Replay, Log Export | 사유·권한·확인·결과 표시 |
| 매우 높음 | Binding 활성, Batch 게시, Rollback | 작성/승인 분리·Snapshot·Version·중단 조건 |

Confirm Dialog만으로 위험 통제가 끝나지 않는다. 서버가 권한·승인·Version·사유를 다시 검증하고 Audit ID를 반환해야 한다.

## 33. 접근성·반응형 수용 기준

- Keyboard만으로 검색·표·상세·Modal·승인 조작이 가능하다.
- Focus가 Dialog 안에 유지되고 닫을 때 원래 Trigger로 돌아간다.
- 상태를 색상 하나로 표현하지 않고 Text·Icon·ARIA Label을 함께 사용한다.
- 200% 확대에서 가로 Scroll 없이 핵심 조작을 수행할 수 있다.
- 표가 좁아지면 중요 Column을 Card/Detail로 전환하고 모든 Column을 억지로 축소하지 않는다.
- 비동기 오류는 `role="alert"` 또는 적절한 Live Region으로 전달한다.
- Loading Skeleton이 실제 Content Layout과 크게 달라 Layout Shift를 만들지 않는다.

## 34. Reference Catalog 수용 기준

| 시나리오 | 기대 동작 |
|---|---|
| 최초 진입 | Type·상위 Context로 후보 조회, Loading 표시 |
| 검색어 변경 | 이전 요청을 논리적으로 폐기하고 최신 응답만 반영 |
| 상위 값 변경 | 하위 선택 초기화 후 새 Parent ID로 재조회 |
| Provider 미구성 | `available=false`, 선택 비활성, 원인 표시 |
| 후보 없음 | 정상 Empty 상태, 직접 ID 입력으로 우회 금지 |
| 비활성 항목 | 표시하되 선택 금지와 사유 제공 |
| 모바일 680px 이하 | 검색·선택을 한 열로 배치 |
| 저장 | 화면의 Label이 아니라 검증된 Reference ID만 전송 |

Component Test는 응답 순서 역전, Parent 변경, Provider 오류, 비활성 항목과 Keyboard 조작을 포함한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Frontend Root | `cpf-admin/frontend/src`, `cpf-biz-admin/frontend/src` | 기능별 Route·API·State·Component 책임 |
| 공통 Parameter UI | `ReferenceCatalogSelect.vue`, `parameterSchema.ts`, `DynamicParameterForm.vue` | Typed Parameter·Parent Reset·Stale 응답 차단 |
| Backend Catalog | `AdmParameterReferenceCatalogAdapter.java`, `AdmParameterReferenceProperties.java` | Reference Type별 Provider·Fail-closed 설정 |
| 기능 화면 | `cpf-admin/frontend/src/features/` | Service Registry, Gateway, Batch, Log Policy 등 |
| 정적 검증 | `npm run lint`, `npm run typecheck`, `npm run test`, `npm run build` | 문법·형식·행동·Bundle 검증 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
