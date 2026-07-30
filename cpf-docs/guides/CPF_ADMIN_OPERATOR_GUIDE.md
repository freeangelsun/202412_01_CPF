# CPF 플랫폼 운영자 가이드

## 1. 대상과 목적

이 문서는 `cpf-admin`을 사용하는 운영자, 장애 대응자, 승인자, 보안 담당자와 감사 담당자를 위한 운영 절차를 정의한다.

ADM은 단순 조회 화면이 아니다. 운영자가 다음을 수행할 수 있어야 한다.

- 서비스와 인스턴스 상태 파악
- 거래와 실패 구간 추적
- Gateway와 Batch 운영
- 결과 불명 거래 대사
- 위험 조치 승인·실행·감사
- 설정과 Log 정책 변경
- Incident와 Runbook 관리
- Evidence 확보

## 2. 운영자 역할

| 역할 | 대표 권한 |
|---|---|
| 조회 운영자 | 상태, 거래, Log, 이력 조회 |
| 서비스 운영자 | Drain, Resume, Retry, Reconcile |
| Batch 운영자 | 실행, Stop, Restart, Reprocess |
| Gateway 운영자 | Binding, Apply, Connection Test |
| 보안 운영자 | Secret Metadata, 인증서, Download 통제 |
| 승인자 | 위험 조치 승인 |
| 감사자 | 변경·승인·실행 이력 조회 |
| 비상 운영자 | 제한된 Break-glass |

한 사용자가 모든 권한을 기본 보유하지 않는다.

## 3. 로그인과 Session

- 인증 실패 시 보호 API 접근 거부
- Access Token은 제한된 Session 범위
- Logout/401 후 Browser 상태 제거
- 권한 변경 후 Session 재평가
- 위험 조치 재인증
- Refresh Token 재사용 탐지
- 다중 Device Session 조회와 폐기

## 4. 화면 공통 사용법

모든 운영 목록은 다음을 제공한다.

- 검색 조건
- 기간
- 상태
- Owner/Module/Instance
- Paging 또는 Cursor
- 안정적인 정렬
- 상세 화면
- Error와 Retry
- 저장 검색 조건
- 권한 있는 Export
- URL Deep Link

Raw JSON은 보조 진단으로만 제공하고 기본 화면은 구조화한다.

## 5. Service Registry

### 5.1 서비스 목록

확인 항목:

- serviceId
- systemCode
- moduleId
- version
- endpoint
- protocol
- zone/cell
- instance count
- healthy count
- circuit state
- maintenance state

### 5.2 Instance 상세

- serverInstanceId
- host
- processId
- startedAt
- heartbeat
- liveness
- readiness
- active profile
- drain
- maintenance
- capacity
- current load
- last error

### 5.3 판단

```text
Liveness DOWN
→ Process/Host 장애 확인

Liveness UP + Readiness DOWN
→ Local DB, Listener, 필수 Dependency 확인

Registry Stale
→ Heartbeat 지연, Network, Instance 종료 확인
```

## 6. Topology

Topology는 Service, Instance, Endpoint, Dependency, Owner와 DB를 연결한다.

운영자는 다음 질문에 답할 수 있어야 한다.

- 이 서비스가 누구를 호출하는가
- 장애 대상의 영향 서비스는 무엇인가
- 어느 Instance가 같은 Cell에 있는가
- Gateway Route가 어떤 Binding을 사용하는가
- Batch Worker가 어떤 Job Pack을 실행하는가
- Owner Command가 어느 Runtime으로 전달되는가

## 7. 거래 조회

검색 키:

- transactionId
- traceId
- operationId
- customer/business key의 마스킹 값
- systemCode
- instanceId
- status
- 시간
- failureCode

Timeline:

```text
IN
→ AUTH
→ APPLICATION
→ LOCAL/REMOTE CALL
→ RETRY ATTEMPT
→ OUT
→ RESULT
→ AUDIT
```

결과 불명은 최종 실패와 구분한다.

## 8. Log와 Trace

Log 조회 기준:

- Environment
- Module
- Instance
- transactionId
- executionId
- Job/Worker
- Level
- Logger
- 시간

원문 Download나 Clipboard 반출은 별도 권한, 사유와 감사가 필요하다.

Trace Boost 또는 동적 Log Level은:

- 대상 Scope
- 최대 기간
- 자동 만료
- 허용 Level
- 민감정보 보호
- 변경 전후
- 감사

를 갖춘다.

## 9. Gateway 운영

운영 흐름:

1. Registry와 Server Group 확인
2. Binding 상세
3. Policy 검증
4. 연결시험
5. 승인
6. Publish
7. Instance Apply
8. ACK
9. 구성 불일치 확인
10. 대사 또는 되돌리기

상세는 Gateway 가이드를 참고한다.

## 10. Batch 운영

운영자는 다음 개념을 구분한다.

- 작업정의
- Schedule
- Trigger
- CPF Execution
- Spring Batch JobInstance
- Worker Lease
- Agent
- Restart
- Reprocess

위험 명령:

- Run
- Stop
- Restart
- Reprocess
- Skip/Manual Confirm
- Worker Drain
- Agent Maintenance
- Lost Execution Reconcile

## 11. 결과 불명과 복구

```text
UNKNOWN_RESULT 조회
→ 대상과 마지막 Attempt 확인
→ Downstream 상태 조회
→ 자동 대사
→ 운영자 확인
→ 최종 성공/실패 확정
→ 재처리 또는 보상
```

확정 전 동일 Command를 무조건 재실행하지 않는다.

## 12. Incident

Incident 생명주기:

```text
Alert
→ Incident 생성
→ 영향도 분류
→ 담당자 지정
→ Timeline과 Evidence
→ Runbook 실행
→ 임시 조치
→ 근본 원인
→ 복구 확인
→ 종료
→ 사후 분석
```

Incident는 관련 Service, transactionId, Deployment, Config Change와 연결한다.

## 13. 위험 Command 공통 절차

필수 입력:

- Target
- 현재 상태
- Permission
- Reason
- expectedVersion
- operationId
- Approval
- Confirmation
- 실행 결과
- Audit

동일 Command의 Double Click은 operationId로 중복 실행을 막는다.

## 14. 승인

작성자와 승인자를 분리한다.

지원 정책:

- ALL
- ANY
- N_OF_M
- Role/Organization Target
- 만료
- 대리
- 비상 승인
- Policy Version Snapshot

승인 후 대상 상태가 바뀌면 expectedVersion 불일치로 실행을 거부하고 재승인을 요구한다.

## 15. Break-glass

비상 권한은 다음을 요구한다.

- 비상 사유
- 대상 Scope
- 자동 만료
- 재인증
- 사후 승인
- 즉시 Alert
- 모든 조회·명령 감사

일반 운영 편의를 위해 사용하지 않는다.

## 16. Secret Center

표시 가능:

- Provider
- Reference
- Version
- 만료
- Rotate 가능 여부
- 상태

표시 금지:

- Secret 원문
- 복호화 값
- 전체 Credential
- Private Key

Rotate는 Provider가 지원하고 권한·사유·승인이 있을 때 수행한다.

## 17. Config와 Runtime Policy

변경 절차:

1. 현재 Version 조회
2. 대상 Scope 확인
3. 변경안 검증
4. 영향도와 Preview
5. 승인
6. Apply Event 생성
7. Instance ACK
8. Partial Failure 확인
9. Retry/Reconcile
10. Rollback

## 18. Cache

Cache 운영:

- Namespace
- Key Pattern
- Size
- Hit/Miss
- TTL
- Owner
- Clear Preview
- 제한된 Invalidate
- 전체 Clear 별도 권한

업무 원장 대체로 Cache를 사용하지 않는다.

## 19. Download와 원문 조회

- 별도 Permission
- Reason
- 최대 건수/크기
- Masking Default
- Watermark 또는 Audit ID
- File Checksum
- 만료 URL
- 재다운로드 이력
- 민감도 분류

## 20. Backup·Restore·DR

운영자는 Backup 파일과 Manifest를 함께 보존한다.

Restore 절차:

1. 격리 환경
2. Vendor/DB/Checksum 확인
3. 복구
4. Schema Verify
5. Application Smoke
6. 거래·Batch 대사
7. RPO/RTO 기록
8. 운영 전환 승인

## 21. 장애 대응 표준 순서

1. Incident 시각과 영향도 확인
2. transactionId 또는 실행 ID 확보
3. Registry/Topology 확인
4. Health와 최근 Deployment/Config 확인
5. Timeline/Log/Trace 확인
6. 결과 불명 여부 확인
7. 자동 복구 상태 확인
8. 위험 조치 전 멱등성과 Downstream 상태 확인
9. 조치
10. 복구와 재발 방지 확인
11. Evidence와 사후 분석

## 22. 운영 Evidence

- HEAD SHA
- 환경/Profile
- Operator
- Permission
- Reason
- Approval
- 대상 Snapshot
- 정확한 Command/API
- 시작·종료
- 결과
- Failure Code
- 관련 Incident
- 민감정보 제거

## 23. 운영 체크리스트

- [ ] 조회와 변경 권한이 분리됐다.
- [ ] 위험 조치에 사유가 있다.
- [ ] 작성자·승인자가 분리됐다.
- [ ] expectedVersion과 operationId를 사용한다.
- [ ] 결과 불명을 확정 실패로 바꾸지 않는다.
- [ ] 원문 Download가 감사된다.
- [ ] 조치 결과와 대사가 연결된다.
- [ ] 운영 화면 오류가 원 업무를 오염시키지 않는다.
