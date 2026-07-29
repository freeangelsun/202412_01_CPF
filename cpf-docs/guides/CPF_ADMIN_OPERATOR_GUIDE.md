# CPF Admin Operator Guide

## 1. 대상
`cpf-admin`을 이용해 플랫폼을 운영하는 운영자, 장애 대응자, 승인자, 보안 담당자를 위한 가이드다. ADM은 단순 CRUD 화면이 아니라 장애 분석과 위험조치 통제를 위한 운영 도구다.

## 2. 로그인과 세션
- 인증 실패 후 보호 API를 호출할 수 없어야 한다.
- logout/401 이후 이전 운영자의 로그·감사·검색·보안 결과가 Browser state에 남지 않아야 한다.
- access token은 장기 persistent storage보다 제한된 session scope를 사용한다.
- 민감 조작은 필요 시 재인증/승인 정책을 추가한다.

## 3. Health / Service Registry
인스턴스 상태는 다음 식별자를 함께 본다.
`moduleId`, `wasId`, `serverInstanceId`, `host`, `processId`, `profiles`, `checkedAt`.

판단 기준:
- liveness DOWN: 프로세스 자체 문제.
- liveness UP + readiness DOWN: 로컬 필수 DB/Runtime dependency 문제.
- 원격 Owner diagnostics DOWN: 해당 Owner 호출 영향은 있으나 ADM 인스턴스 전체 readiness를 기본적으로 오염시키지 않는다.
- 다중 인스턴스 전체 상태는 Service Registry에서 service/instance별로 비교한다.

## 4. 거래/로그 조회
거래 식별자, 실행 Module, 서버 인스턴스, 시작/종료 시각, 상태, 소요시간, 실패구간을 조합해 조회한다. 원문 로그/JSON/Clipboard 반출은 서버 권한·사유·감사 없이 Browser-only 기능으로 우회하지 않는다.

## 5. Generated Domain 운영
ADM은 MBR/ACC/EXS 같은 Generated Domain 업무 API나 DB에 종속되지 않는다. 운영 대상은
Generator Manifest와 Runtime Registry에 등록된 임의 Domain으로 발견하며, Domain별 업무 운영은
해당 Owner API가 제공한다. Owner가 삭제·재생성되거나 부분 장애인 경우에도 ADM은 고정 경로를
호출하거나 오류를 정상 0건으로 위장하지 않는다.

## 6. Operator 운영
운영자 관리에서 지원해야 할 핵심 조치:
- 계정/Role 조회
- 잠금 해제
- 비밀번호 Reset
- 세션 조회/폐기
- 변경 actor와 reason 감사
위험한 변경은 body의 requester를 믿지 않고 인증 principal을 사용한다.

## 7. Secret Center
Secrets 화면은 Provider/Reference/Metadata만 조회한다. 원문 Secret 값은 API로 반환하지 않는다. ENV Provider는 metadata 조회용 bootstrap provider이고 rotate를 지원하지 않는다. 실제 Rotate 가능 Provider는 권한과 사유를 요구하고 감사한다.

## 8. Config
`encryptedYn` 또는 secret reference 성격 설정을 일반 Text로 노출하지 않는다. 읽기 시 mask/reference, 쓰기 시 write-only를 기본으로 한다. 운영 환경 변경 전 대상 scope와 영향도를 확인한다.

## 9. 위험 Command 공통 원칙
Drain/Disable/Resume, Batch Run/Retry/Stop, Lock release, Ghost recovery, Replay/Purge와 같은 명령은 최소 다음을 갖춘다.
- 대상 Snapshot/현재 상태
- 권한
- reason
- expectedVersion 또는 최신 상태 확인
- idempotency/operation id
- 확인 Dialog
- 실행 Audit
- 결과/reconcile 상태

위험 Command는 Runtime Control의 preview/approval/apply/reconcile 및 불변 감사 계약을 사용하며,
각 기능은 이 공통 계약을 우회하는 별도 실행 경로를 제공하지 않는다.

## 10. Retention
`cpf.retention.execute-enabled=false`가 기본이다. Dry-run/Legal Hold는 실제 삭제를 하지 않는다. 실제 ARCHIVE/PURGE는 cutoff가 필요하고 kill-switch가 ON이어야 한다. Archive 후 삭제 순서와 실패 시 transaction을 확인한다.

## 11. Audit 검증
BZA audit chain이 적용된 경우 VALID/PARTIAL_LEGACY/BROKEN 상태를 구분한다. 운영 검증 시 임의 row 변경뿐 아니라 마지막 row 삭제 후 chain-head mismatch도 BROKEN인지 확인한다. Audit 원문에서 개인정보/Secret이 노출되지 않는지도 별도 점검한다.

## 12. Backup / Restore / DR
운영 Backup은 민감 데이터로 분류한다. Manifest와 SHA-256을 함께 보존한다. Restore는 대상 DB명/Vendor/Hash 불일치 시 중단한다. DR 검증은 격리 DB에서 수행하고 운영 DB를 검증용으로 덮어쓰지 않는다.

## 13. 장애 대응 순서
1. Incident 시각과 transactionGlobalId 확보.
2. Service Registry에서 관련 instance readiness/heartbeat 확인.
3. 거래 Timeline/로그/Service Call segment 확인.
4. timeout이면 UNKNOWN_RESULT 등록 여부와 reconciliation 상태 확인.
5. Replay/Retry 전에 멱등성 및 downstream 최종상태 확인.
6. 조치 reason/audit/evidence 남김.

## 14. Evidence
운영 검증 Evidence에는 HEAD SHA, command, profile, 시작/종료, 실제 결과, 관련 Requirement/QA ID, 원본 log/DB query, 민감정보 제거 여부가 있어야 한다. 화면 캡처만으로 완료 처리하지 않는다.
