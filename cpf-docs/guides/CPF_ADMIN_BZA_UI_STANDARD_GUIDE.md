# CPF ADM / BZA UI Standard Guide

## 1. 권한과 메뉴

메뉴 노출과 API 권한 검사는 서로 대체하지 않는다. UI는 세션의 effective permission으로 메뉴/버튼을 숨기고, 서버 Controller/Service가 다시 권한을 검증한다. 위험조치는 reason/approval/reference/audit 결과를 남긴다.

## 2. 공통 입력 Component

ADM/BZA 양쪽에 다음 공통 Component를 둔다.

- `CpfCodeSelect.vue`: CPF 코드 API에서 받은 option을 표준 select로 표시
- `CpfDateRange.vue`: 시작/종료일 역전 방지, native keyboard/date 접근성 유지
- `CpfIcon.vue`: 외부 CDN/font icon 의존 없이 제품 내 아이콘 사용

`CpfCodeSelect`가 DB를 직접 알거나 임의 enum을 하드코딩하지 않는다. Feature API layer가 CPF 코드 데이터를 읽어 `{value,label,disabled}`로 전달한다.

## 3. 조회 화면 기본

목록 기능 성격에 따라 다음을 사용한다.

- 검색조건 + Reset/Search
- `CpfPage` offset 또는 HMAC cursor keyset
- Loading/Empty/Error/403/409/Timeout/Unknown 상태
- 일관된 status badge
- 상세 drawer/page
- 대량 데이터는 export/virtualization 또는 cursor
- 기간 조회는 `CpfDateRange`

## 4. 위험조치

서비스 차단, Routing 강제변경, Batch/Center-Cut 재처리, DLQ Replay, Download/Unmask, Config/Secret 변경은 일반 CRUD 버튼과 동일 취급하지 않는다. 최소 reason, permission, confirmation, approval/break-glass 정책, audit result를 연결한다.
