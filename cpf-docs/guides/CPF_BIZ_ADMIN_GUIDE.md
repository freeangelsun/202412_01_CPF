# CPF Biz Admin Guide

## 1. 목적
BZA는 고객 업무 조직·사용자·Role·Permission·결재·첨부·알림·감사를 운영하는 Backoffice다. 단순 DB CRUD가 아니라 권한, 동시성, 이력, 감사, 업무 안전성을 보장해야 한다.

## 2. User와 Role
사용자 계정의 legacy `roleCode`는 호환 표시용이며 Role 변경 정본은 `bza_user_role` 이력이다. 기존 사용자의 Role을 User edit 화면에서 직접 변경하지 말고 User Role 화면에서 grant/revoke한다.

Role 이력 원칙:
- surrogate `user_role_id`로 재부여 이력을 보존.
- `operationId`로 재시도 멱등성을 보장.
- 동일 operationId를 다른 요청에 재사용하면 거부.
- primary role 변경은 user row lock으로 직렬화.
- 새 primary 반영 시 legacy roleCode도 호환 목적에 한해 동기화.
- Role history가 한 번이라도 존재한 계정은 active history가 0건이어도 legacy roleCode로 되살리지 않는다.

## 3. Permission
실효 권한은 active user-role, active role, active menu, environment scope를 모두 평가한다. 다중 Role의 deny 정책을 명확히 적용한다. environment/domain/dataScope/httpMethod/apiPattern 전체 통합 evaluator는 Runtime 검증을 거쳐야 하며 부분 구현을 완료로 간주하지 않는다.

## 4. 조직/직원/Assignment
Employee의 조직/직위/직책/유효기간/primary 관계 정본은 Assignment다. 제거된 과거 delegated/absence column을 Runtime SQL에서 사용하지 않는다. 조직 변경 시 parent cycle을 차단한다. 주요 기준정보는 `versionNo` CAS로 동시 수정 충돌을 409로 드러낸다.

## 5. Server Paging
대량 목록은 `/page` API를 사용한다. `CrudTable`은 서버에서 page/size/total을 받고 stable ordering을 전제로 한다. 편의상 전체 list를 Browser에 내려 client slice하는 방식은 운영 대용량 화면에서 사용하지 않는다.

## 6. Approval
Requester는 request body 값이 아니라 인증 operator와 employee mapping에서 도출한다. 대리결재는 유효한 delegation을 검증한다. 사용된 Approval Policy version은 immutable이며 수정이 필요하면 새 version을 만든다.

Legacy direct approval mutation endpoint/service는 정책 엔진을 우회하지 못하도록 차단한다. 결재 전에는 payload/policy/step/history/attachment와 available action을 확인해야 한다.

## 7. Attachment
표준 scan lifecycle:
`PENDING -> CLEAN | INFECTED | FAILED | QUARANTINED`
다운로드는 CLEAN이면서 quarantine이 아닌 파일만 허용한다. 분류는 PUBLIC/INTERNAL/CONFIDENTIAL/RESTRICTED를 사용한다. 운영자는 recheck/mark clean/quarantine/disable/retention 변경 시 reason과 audit를 남긴다.

외부 Malware Scanner adapter와 실제 파일 sandbox E2E가 없으면 해당 부분은 `미검증/부분 구현`이다.

## 8. Notification
알림은 unread 필터, 개별 read, 전체 read, reference deep-link를 제공한다. 알림 자체가 업무 조치를 대신하지 않으며 target 업무 화면과 권한이 다시 검증되어야 한다.

## 9. Audit Hash Chain
관리 변경 Audit은 canonical JSON 기반 record hash와 previous hash로 연결한다. chain lock row를 `FOR UPDATE`로 잡아 다중 인스턴스 writer를 직렬화한다. verify 결과:
- `VALID`: chain과 head 일치.
- `PARTIAL_LEGACY`: hash 이전 legacy row가 존재하지만 이후 chain은 정합.
- `BROKEN`: content/link/head 불일치.

## 10. 동시성 오류 처리
`expectedVersion` mismatch는 자동 overwrite하지 않는다. 사용자는 최신 row를 재조회해 변경 내용을 비교하고 재시도한다. UI에서 versionNo가 있는 row를 수정할 때 expectedVersion을 함께 전송한다.

## 11. 인증 정책
강제 비밀번호 변경/만료 계정은 Frontend만 막지 않고 Backend authorize에서 업무 API를 차단한다. 동시 401 refresh는 single-flight로 직렬화하고 최종 refresh 실패 시 session을 제거한다.

## 12. 운영 검증
Role별로 Dashboard/Users/Roles/Permissions/Organizations/Employees/Assignments/Approvals/Attachments/Audits/Notifications를 실제 Browser에서 검증한다. 401/403/409/500과 stale response, double-click, keyboard 접근성, console error도 포함한다.
