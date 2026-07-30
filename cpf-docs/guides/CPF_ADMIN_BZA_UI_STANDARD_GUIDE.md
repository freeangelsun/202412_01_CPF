# CPF ADM·BZA 화면 표준 가이드

## 1. 목적

ADM과 BZA는 실제 운영자가 매일 사용하는 상용 Backoffice다. 기능 존재 여부뿐 아니라 검색성, 오류 복구, 권한, 안전성, 접근성과 유지보수성을 제품 기준으로 통일한다.

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

## 3. Route

각 메뉴는 목적별 Route와 Component를 가진다. 여러 메뉴를 같은 화면 별칭으로 연결하지 않는다.

Route Metadata:

- title
- group
- permission
- breadcrumb
- helpLink
- dangerous
- keepAlive 정책

Lazy Import 대상 실파일을 Gate로 확인한다.

## 4. Design System

공통 요소:

- Typography
- Spacing
- Form
- Button
- Table
- Badge
- Alert
- Dialog
- Drawer
- Empty/Error State
- Skeleton
- Icon

외부 Runtime CDN, Font, Script에 의존하지 않는다.

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
- Validation 오류
- 401
- 403
- Timeout
- Server 오류
- Stale 응답
- 취소

검색 UX:

- Enter 검색
- Reset
- URL Query 동기화
- 저장 조건
- 기간 기본값
- Page 변경
- Sort
- 결과 건수
- 마지막 조회시각

## 7. Paging

- Server Page 또는 Cursor
- Page Size 상한
- Stable Order
- 로딩 중 중복 호출 방지
- Filter 변경 시 첫 Page
- Count 실패와 목록 실패 구분
- 대량 Export는 비동기 Job

## 8. 상세 화면

상세는 다음을 구조화한다.

- 기본 정보
- 상태
- Owner
- Version
- 관계
- Timeline
- 오류
- Audit
- 가능한 Action

Raw JSON만 표시하지 않는다. JSON Viewer는 진단용 보조 영역으로 둔다.

## 9. Form

- Label과 설명
- 필수 표시
- Client Validation
- Server Validation Mapping
- 필드 단위 오류
- Form 상단 오류 요약
- 변경 감지
- 이탈 확인
- 저장 중 비활성
- Double Submit 방지
- 성공 후 최신 재조회

## 10. 상태

Status Badge는 Catalog를 사용한다.

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

UI가 숨겼다고 권한이 보장되는 것은 아니다.

READ 사용자는 변경 버튼을 볼 수 없다. 위험 Action은 별도 Permission을 사용한다.

## 12. 위험 조치

Dialog 필수 항목:

- 대상
- 현재 상태
- 영향
- 사유
- Approval 필요 여부
- Version
- 최종 확인 문구
- 실행 후 결과

고위험 조치는 단순 `confirm()`을 사용하지 않는다.

## 13. 민감정보

기본은 Masking이다.

원문 보기:

- Permission
- Reason
- 재인증
- 표시 시간 제한
- Clipboard 통제
- Audit
- 화면 이탈 시 제거

## 14. 오류 처리

표준 오류별 UX:

| 오류 | UX |
|---|---|
| 400 Validation | 필드 오류와 요약 |
| 401 | Session 정리 후 로그인 |
| 403 | 권한 부족 설명 |
| 404 | 대상 변경/삭제 안내 |
| 409 | 최신 데이터 비교와 재조회 |
| 429 | 재시도 시각 표시 |
| Timeout | 결과 불명 가능성 안내 |
| 500 | transactionId 제공 |
| Network | 연결 복구 후 재시도 |

Command Timeout은 곧바로 실패로 단정하지 않고 결과 조회 링크를 제공한다.

## 15. Stale Response

검색 조건이 빠르게 바뀌면 이전 응답이 최신 화면을 덮지 않도록 Request Token 또는 Abort Controller를 사용한다.

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
- 작은 화면: Card/List + Detail Page
- 고정 폭 Raw JSON 금지
- 긴 ID는 Copy와 줄바꿈

## 18. 성능

- Route 단위 Code Split
- 대형 Table Virtualization
- 검색 Debounce
- API Cache 정책
- 중복 호출 제거
- 긴 Timeline Cursor
- Bundle Size Gate

## 19. Test

### Component

- Render
- Validation
- Permission
- Status
- Dialog

### Integration

- API 성공/오류
- 401/403/409
- Stale Response
- Double Click
- Cursor

### Browser

- 실제 Route
- Keyboard
- 권한별 메뉴
- 위험 Action
- Audit 결과
- Console Error 0
- 외부 Runtime URL 0

## 20. 완료 체크리스트

- [ ] 기능별 Directory와 Route다.
- [ ] Raw JSON 중심이 아니다.
- [ ] 검색·Paging·상세가 있다.
- [ ] 모든 오류 상태가 있다.
- [ ] Permission이 UI와 서버에 적용된다.
- [ ] 위험 조치에 사유·확인·승인이 있다.
- [ ] 접근성과 반응형을 지원한다.
- [ ] 외부 CDN/Font/Script에 의존하지 않는다.
