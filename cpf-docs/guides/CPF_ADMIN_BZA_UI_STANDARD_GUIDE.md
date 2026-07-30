# CPF ADM·BZA 화면 표준 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 화면 설계자, 프런트엔드 개발자, 접근성 검수자
> **목적**: ADM·BZA 화면을 일관된 탐색·상태·오류·권한 기준으로 구현한다.
> **관련 문서**: [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md) · [업무 관리자](CPF_BIZ_ADMIN_GUIDE.md)

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
