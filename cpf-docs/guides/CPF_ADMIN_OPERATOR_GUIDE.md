# CPF 플랫폼 운영자 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 플랫폼 운영자, 관제 담당자, 승인자, 사고 대응자
> **목적**: 서비스·거래·배치·설정·보안 상태를 조회하고 안전하게 조치한다.
> **관련 문서**: [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md) · [보안·재해복구·데이터 보존](CPF_SECURITY_DR_RETENTION_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-admin` |
| 이 문서로 완료하는 일 | 서비스·Gateway·Batch·Log·설정·사고 상태를 연결해 조회하고, 권한·사유·승인·감사와 함께 안전한 운영 명령을 실행한다. |
| 적용 범위 | ADM 조회·제어 API, 운영 Frontend, 승인·감사, 원격 Owner Port |
| 주요 독자 | 플랫폼 운영자, 관제 담당자, 승인자, 사고 대응자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---



<picture>
  <source media="(max-width: 720px)" srcset="../assets/readme/cpf-operations-mobile.png">
  <img src="../assets/readme/cpf-operations-desktop.png" alt="CPF 플랫폼 운영과 업무 관리" width="100%">
</picture>

## 1. 대상과 목적

이 문서는 `cpf-admin`을 사용하는 운영자, 장애 대응자, 승인자, 보안 담당자와 감사 담당자를 위한 운영 절차를 정의한다.

ADM은 단순 조회 화면이 아니다. 운영자가 다음을 수행할 수 있어야 한다.

- 서비스와 인스턴스 상태 파악
- 거래와 실패 구간 추적
- 게이트웨이와 배치 운영
- 결과 불명 거래 대사
- 위험 조치 승인·실행·감사
- 설정과 로그 정책 변경
- 사고와 운영 절차서 관리
- 검증 증적 확보

## 2. 운영자 역할

| 역할 | 대표 권한 |
|---|---|
| 조회 운영자 | 상태, 거래, 로그, 이력 조회 |
| 서비스 운영자 | Drain, Resume, 재시도, 상태 대사 |
| 배치 운영자 | 실행, Stop, Restart, Reprocess |
| 게이트웨이 운영자 | 바인딩, 적용, 연결시험 |
| 보안 운영자 | 비밀값 메타데이터, 인증서, 내려받기 통제 |
| 승인자 | 위험 조치 승인 |
| 감사자 | 변경·승인·실행 이력 조회 |
| 비상 운영자 | 제한된 Break-glass |

한 사용자가 모든 권한을 기본 보유하지 않는다.

## 3. 로그인과 세션

- 인증 실패 시 보호 API 접근 거부
- Access Token은 제한된 세션 범위
- Logout/401 후 브라우저 상태 제거
- 권한 변경 후 세션 재평가
- 위험 조치 재인증
- Refresh Token 재사용 탐지
- 다중 Device 세션 조회와 폐기

## 4. 화면 공통 사용법

### 참조형 매개변수

서비스, 서버 그룹, 경로, 작업정의, 비밀값 참조처럼 다른 관리 대상의 식별자를 입력하는 필드는 자유 입력 대신 참조 Catalog를 사용한다. 상위 필드가 바뀌면 하위 선택값을 초기화하고 다시 조회하며, 사용할 수 없는 항목은 비활성 사유를 표시한다. Catalog 조회 실패를 빈 목록으로 위장하지 않는다.


모든 운영 목록은 다음을 제공한다.

- 검색 조건
- 기간
- 상태
- 소유자/모듈/인스턴스
- 페이징 또는 Cursor
- 안정적인 정렬
- 상세 화면
- 오류와 재시도
- 저장 검색 조건
- 권한 있는 반출
- URL Deep Link

Raw JSON은 보조 진단으로만 제공하고 기본 화면은 구조화한다.

## 5. 서비스 등록부

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

### 5.2 인스턴스 상세

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
→ Local DB, 리스너, 필수 Dependency 확인

Registry Stale
→ Heartbeat 지연, Network, Instance 종료 확인
```

## 6. 구성 관계

구성 관계 화면은 서비스, 인스턴스, 엔드포인트, 의존 대상, 소유자와 DB를 연결한다.

운영자는 다음 질문에 답할 수 있어야 한다.

- 이 서비스가 누구를 호출하는가
- 장애 대상의 영향 서비스는 무엇인가
- 어느 인스턴스가 같은 셀에 있는가
- 게이트웨이 경로가 어떤 바인딩을 사용하는가
- 배치 작업자가 어떤 작업 묶음을 실행하는가
- 소유자 명령이 어느 실행 환경으로 전달되는가

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

시간선:

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

## 8. 로그와 추적

### 감사된 로그 반출

로그 상세를 클립보드로 복사하거나 파일로 내려받는 동작은 브라우저가 조회 응답을 그대로 저장하지 않는다.

1. 운영자가 대상 로그와 반출 방식을 선택한다.
2. 서버가 권한과 사유를 검증한다.
3. 로그 수집 보호 정책에 따라 다시 마스킹하고 최대 크기를 적용한다.
4. `exportId`, 행위자, 대상, 사유, 결과와 만료 시각을 감사한다.
5. 클립보드 방식은 마스킹된 본문만 반환한다.
6. 내려받기 방식은 만료되는 주소와 파일명을 반환한다.

쿼리, 요청·응답 헤더, 요청·응답 본문과 오류 스택은 각각 `NONE`, `METADATA_ONLY`, `ALLOWLIST`, `MASKED` 등 승인된 수집 모드와 크기 제한을 적용한다. 원문 전체 수집을 기본값으로 두지 않는다.


로그 조회 기준:

- Environment
- 모듈
- 인스턴스
- transactionId
- executionId
- 작업/작업자
- Level
- Logger
- 시간

원문 내려받기나 클립보드 반출은 별도 권한, 사유와 감사가 필요하다.

추적 Boost 또는 동적 로그 Level은:

- 대상 범위
- 최대 기간
- 자동 만료
- 허용 Level
- 민감정보 보호
- 변경 전후
- 감사

를 갖춘다.

## 9. 게이트웨이 운영

ADM은 게이트웨이 테이블을 직접 수정하지 않고 게이트웨이 소유 제어 API를 호출한다. 분리 실행 환경에서는 요청 서명과 재전송 방지를 적용하고, 게시·적용·실패·되돌리기 상태는 실시간 운영 스트림과 조회 API 양쪽에서 확인한다. 각 인스턴스의 기대 버전, 실제 적용 버전, 체크섬과 확인 응답을 비교한다.


운영 흐름:

1. 등록부와 서버 그룹 확인
2. 바인딩 상세
3. 정책 검증
4. 연결시험
5. 승인
6. 게시
7. 인스턴스 적용
8. ACK
9. 구성 불일치 확인
10. 대사 또는 되돌리기

상세는 게이트웨이 가이드를 참고한다.

## 10. 배치 운영

작업정의 작성·검증·승인·게시 명령은 배치 제어 서버의 소유 제어 포트로 전달한다. 게시된 정의는 실행 투영으로 변환되고 일정관리기와 작업자가 이 투영을 소비한다. 운영자는 실행 자체뿐 아니라 각 시도의 시작·종료·오류·재시도 판단과 선택된 실행기까지 조회한다.


운영자는 다음 개념을 구분한다.

- 작업정의
- 일정
- 트리거
- CPF 실행
- Spring Batch JobInstance
- 작업자 임대
- 에이전트
- Restart
- Reprocess

위험 명령:

- Run
- Stop
- Restart
- Reprocess
- Skip/Manual Confirm
- 작업자 Drain
- 에이전트 Maintenance
- 실행 소유권 상실 상태 대사

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

확정 전 동일 명령을 무조건 재실행하지 않는다.

## 12. 사고

사고 생명주기:

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

사고는 관련 서비스, transactionId, 배포, 설정 Change와 연결한다.

## 13. 위험 명령 공통 절차

필수 입력:

- 대상
- 현재 상태
- 권한
- 사유
- expectedVersion
- operationId
- 승인
- Confirmation
- 실행 결과
- 감사

동일 명령의 Double Click은 operationId로 중복 실행을 막는다.

## 14. 승인

작성자와 승인자를 분리한다.

지원 정책:

- ALL
- ANY
- N_OF_M
- 역할/조직 대상
- 만료
- 대리
- 비상 승인
- 정책 버전 스냅샷

승인 후 대상 상태가 바뀌면 expectedVersion 불일치로 실행을 거부하고 재승인을 요구한다.

## 15. Break-glass

비상 권한은 다음을 요구한다.

- 비상 사유
- 대상 범위
- 자동 만료
- 재인증
- 사후 승인
- 즉시 경보
- 모든 조회·명령 감사

일반 운영 편의를 위해 사용하지 않는다.

## 16. 비밀값 Center

표시 가능:

- 공급자
- 참조
- 버전
- 만료
- 교체 가능 여부
- 상태

표시 금지:

- 비밀값 원문
- 복호화 값
- 전체 인증정보
- 개인 키

교체는 공급자가 지원하고 권한·사유·승인이 있을 때 수행한다.

## 17. 설정과 실행 정책

변경 절차:

1. 현재 버전 조회
2. 대상 범위 확인
3. 변경안 검증
4. 영향도와 Preview
5. 승인
6. 적용 사건 생성
7. 인스턴스 ACK
8. Partial Failure 확인
9. 재시도/상태 대사
10. 되돌리기

## 18. 캐시

캐시 운영:

- 네임스페이스
- Key 형식
- Size
- Hit/Miss
- TTL
- 소유자
- Clear Preview
- 제한된 무효화
- 전체 Clear 별도 권한

업무 원장 대체로 캐시를 사용하지 않는다.

## 19. 내려받기와 원문 조회

- 별도 권한
- 사유
- 최대 건수/크기
- 마스킹 기본값
- 워터마크 또는 감사 ID
- 파일 체크섬
- 만료 URL
- 재다운로드 이력
- 민감도 분류

## 20. 백업·복원·재해복구

운영자는 백업 파일과 명세서를 함께 보존한다.

복원 절차:

1. 격리 환경
2. 공급자/DB/체크섬 확인
3. 복구
4. 스키마 Verify
5. 애플리케이션 기본 동작
6. 거래·배치 대사
7. RPO/RTO 기록
8. 운영 전환 승인

## 21. 장애 대응 표준 순서

1. 사고 시각과 영향도 확인
2. transactionId 또는 실행 ID 확보
3. 등록부와 구성 관계 확인
4. 상태 점검과 최근 배포/설정 확인
5. 시간선/로그/추적 확인
6. 결과 불명 여부 확인
7. 자동 복구 상태 확인
8. 위험 조치 전 멱등성과 Downstream 상태 확인
9. 조치
10. 복구와 재발 방지 확인
11. 검증 증적과 사후 분석

## 22. 운영 검증 증적

- HEAD SHA
- 환경/프로필
- Operator
- 권한
- 사유
- 승인
- 대상 스냅샷
- 정확한 명령/API
- 시작·종료
- 결과
- Failure Code
- 관련 사고
- 민감정보 제거

## 23. 운영 체크리스트

- [ ] 조회와 변경 권한이 분리됐다.
- [ ] 위험 조치에 사유가 있다.
- [ ] 작성자·승인자가 분리됐다.
- [ ] expectedVersion과 operationId를 사용한다.
- [ ] 결과 불명을 확정 실패로 바꾸지 않는다.
- [ ] 원문 내려받기가 감사된다.
- [ ] 조치 결과와 대사가 연결된다.
- [ ] 운영 화면 오류가 원 업무를 오염시키지 않는다.

## 부록 A. 사고 초기 15분

1. 사고 번호와 담당자를 지정한다.
2. 사용자 영향, 시작 시각, 대상 환경과 업무영역을 기록한다.
3. 최근 배포·설정·DB 변경·인증서 교체를 확인한다.
4. 서비스 등록부와 인스턴스 준비 상태를 확인한다.
5. 대표 거래 식별자로 타임라인·로그·추적을 연결한다.
6. 확산을 막기 위한 배수, 경로 차단, 재시도 제한을 검토한다.
7. 결과 불명 거래와 진행 중 배치의 범위를 산정한다.
8. 복구 조치 전 현재 상태와 판단 근거를 보존한다.

## 부록 B. 위험 명령 확인 화면

확인 화면은 단순 “실행하시겠습니까?”로 끝나지 않는다.

- 대상 환경·서비스·인스턴스·버전
- 현재 상태와 마지막 확인 시각
- 예상 영향과 중단 조건
- 관련 사고·변경 번호
- 사유와 유효기간
- 필요 승인과 승인자
- 기대 버전·멱등 식별자
- 되돌리기 가능 여부와 절차

## 부록 C. 대표 운영 시나리오

### 준비 상태 저하

`의존성 상세 → 최근 변경 → 동일 영역 인스턴스 비교 → 신규 요청 배정 중단 → 원인 복구 → 상태 점검 → 배수 해제`

### 설정 일부 적용

`기대·적용 버전 비교 → 실패 인스턴스 배수 → 실패 단계 확인 → 재적용 또는 되돌리기 → 실제 체크섬 확인 → 감사`

### 결과 불명 거래

`거래 타임라인 → 대상 호출 시도 → 업무 원장·상태 조회 → 최종 상태 확정 → 재처리·보상 → 사용자 통지 → 감사`

## 27. 공통 운영 명령 절차

모든 위험 조치는 같은 순서를 사용한다.

1. 대상과 현재 Version을 다시 조회한다.
2. 거래·호출·Batch·배포 영향과 대체 용량을 확인한다.
3. 필요한 Permission과 작성자·승인자 분리 여부를 확인한다.
4. 구체적인 사유와 Incident·Change ID를 입력한다.
5. 상태 Snapshot과 기대 Version으로 명령을 제출한다.
6. ADM 수신 성공이 아니라 Owner 실행 환경의 처리 결과를 확인한다.
7. 일부 실패, 결과 불명 또는 오래된 응답을 별도 상태로 분류한다.
8. 실제 거래와 상태를 대사하고 되돌리기 가능 여부를 확인한다.
9. 행위자·대상·전후·사유·승인·결과·오류·시각을 감사한다.

## 28. 대표 운영 시나리오

### 28.1 서비스 Instance 배수

`상태 점검과 서비스 등록부` 가이드의 API로 Instance를 조회하고 `DRAIN`을 요청한다. 신규 배정 중단, 실행 중 요청 종료, 결과 불명 대사, 배포·점검, Readiness 확인과 `RESUME`까지 하나의 작업으로 처리한다.

### 28.2 Gateway 일부 적용

Binding의 기대·적용 Version과 인스턴스별 오류를 조회한다. 확대를 중단하고 실패 인스턴스를 배수한 뒤 정책·Secret·TLS·파일 문제를 복구한다. 재적용·연결시험·거래 대사를 수행하고 Stale ACK를 제외한다.

### 28.3 Batch Worker 인계

Execution·Attempt·Worker·Lease·Fencing을 조회한다. Lease가 살아 있으면 중복 Claim을 금지하고, 만료 뒤 새 Worker가 인계한다. 과거 Worker의 늦은 완료는 차단하며 업무 결과가 불명확하면 재실행 전에 대사한다.

### 28.4 로그 반출

원문을 Browser에서 직접 복사하지 않는다. `/adm/api/log-exports`에 대상 Log ID, 동작과 사유를 제출하고 서버가 마스킹한 Clipboard 내용 또는 만료 Artifact만 사용한다. 반출과 다운로드를 감사한다.

## 29. 운영 화면 오류 표시 기준

| 상태 | 화면 표시 | 허용 조치 |
|---|---|---|
| `401` | Session 만료·재인증 필요 | 재로그인. 입력한 위험 조치 자동 재전송 금지 |
| `403` | 필요한 Permission과 요청 대상 | 권한 신청·승인 경로 안내 |
| `409` | 현재 Version·요청 Version·변경자 | 새 상태 비교 후 재작성 |
| `429` | 제한 정책과 재시도 가능 시각 | 자동 폭주 방지 |
| `503` | Owner 실행 환경 미설치·연결 불가 | Local 성공처럼 표시 금지 |
| `PARTIAL` | 성공·실패 Instance와 원인 | 확대 중단·재적용·되돌리기 |
| `UNKNOWN_RESULT` | 대사 필요와 조회 Key | 무조건 재시도 금지 |

Loading, Empty, Error와 권한 없음 상태를 같은 빈 화면으로 표현하지 않는다. 오래된 응답이 최신 검색 결과나 명령 결과를 덮어쓰지 못하게 요청 Sequence를 확인한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| ADM API | `cpf-admin/src/main/java/com/cpf/admin/opr/` | 운영 Controller·Service·Adapter |
| Gateway Remote Port | `cpf-admin/src/main/java/com/cpf/admin/opr/gateway/RemoteCpfGatewayRegistryAdapter.java` | ADM에서 Gateway Owner 제어 |
| Batch Remote Port | `cpf-admin/src/main/java/com/cpf/admin/opr/batch/RemoteBatchJobDefinitionControlAdapter.java` | ADM에서 BAT Owner 제어 |
| 운영 Frontend | `cpf-admin/frontend/src/features/` | 조회·제어·승인·오류 UI |
| 감사 | `AdmAuditLogService` 사용 지점 | 행위자·대상·사유·전후·결과 기록 |

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
