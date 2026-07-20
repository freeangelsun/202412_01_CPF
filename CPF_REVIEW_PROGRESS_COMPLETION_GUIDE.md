# 2026-07-20 검수 운영 보정

# 2026-07-20 운영·아키텍처 보정 규칙

이 보정은 기존 문서의 상충 문구보다 우선한다.

- 현재 개발 버전은 `1.0.0-SNAPSHOT`, 1차 공식 완성은 `1.0.0`
- MBR·ACC·BZA·XYZ는 동급 업무 주제영역
- BZA는 독립 Business Backoffice Subject Area
- ACC의 generator reference는 개발 검증 역할이며 runtime 계층이 아님
- XYZ module/artifact/topology에는 EDU를 사용하지 않음
- CMN은 shared common contract/utility이며 runtime 거래 hub가 아님
- 외부 요청은 필요 시 PFW Gateway, 내부 주제영역 호출은 동일 facade contract의 local/remote adapter
- Codex는 작업 전 최신 origin/master 확인
- 작업 완료는 검증·commit·master merge·origin/master push·원격 SHA 확인까지
- 과거 `commit/push/branch 금지`, `사용자가 직접 commit/push` 지시는 폐기
- force가 불가피하면 원격 SHA와 제3자 commit을 확인하고 `--force-with-lease`를 사용
- 신규 요구 때문에 기반을 바꾸기 전에 ownership/API/SQL/migration/consumer/evidence/회귀 영향을 분석
- 문서는 기존 정본에 흡수하며 완료된 요청서·분석서·보정서는 삭제


# 0. CPF 최상위 목표

CPF(CoreFlow Platform Framework)는 단순 공통 유틸, 샘플 프로젝트, 개발 표준 문서가 아니다.

CPF의 최상위 목표는 금융권을 포함한 범용 업무 시스템을 구축·운영·감사·확장·검증·상용화할 수 있는 **상용 솔루션급 Core Business Platform Framework**를 만드는 것이다.

CPF는 아래 품질을 목표로 한다.

```text
1. 업무 시스템 구축 표준
   - 여러 업무 주제영역을 독립 모듈 또는 독립 서비스로 구축할 수 있어야 한다.
   - 같은 JVM 구조와 MSA 구조를 모두 지원해야 한다.
   - 업무 코드는 URL 직접 조합, Controller 직접 호출, 타 업무 DB 직접 접근 없이 표준 Facade/Port/Service Call 구조를 사용해야 한다.

2. 운영 가능한 플랫폼
   - 서비스/엔드포인트/인스턴스 registry
   - health check
   - heartbeat
   - ghost instance 판단
   - scheduler/worker control
   - batch execution 관리
   - ADM 운영 조회/제어
   - runtime 상태 관제
   - 장애 추적과 복구 가능성

3. 금융권 수준의 감사·추적·보안
   - transactionGlobalId
   - segment/timeline
   - 표준/확장 header propagation
   - selectedInstanceId logging
   - audit log
   - masking
   - 권한/메뉴/API/button/download 권한
   - 다운로드 감사
   - 민감정보 원문 기록 금지
   - credential/key/cert/secret provider port

4. 분산/연계/비동기 처리 표준
   - Service Call Engine
   - timeout/retry/circuit breaker/failover
   - idempotency
   - outbox/inbox
   - broker publish/consume 표준 port
   - DLQ/replay
   - unknown result 표준 모델
   - reconciliation 표준 port
   - file transfer 표준 port
   - archive/compression 표준 port

5. 설치·배포·검증 가능성
   - split SQL
   - Flyway migration
   - 00_all_install.sql
   - 00_all_install_and_smoke.sql
   - 99_smoke_check.sql
   - 신규 빈 MariaDB full install 검증
   - local/dev/stg/prod profile
   - bootJar/java -jar 기본 실행
   - remote deploy dry-run/real deploy 구분
   - evidence 기반 완료 판정

6. 개발자 사용성
   - 기능별 EDU 샘플
   - EDU 샘플 테스트
   - Swagger/OpenAPI 계약
   - sample coverage matrix
   - 기능 매트릭스
   - 검증 가능한 예제
   - 향후 개발자 가이드 정본화 가능성

7. 상용화 수준의 품질 관리
   - qualityGate
   - report/matrix/evidence 정합성
   - 허용 상태값 통제
   - stale evidence 차단
   - 실행하지 않은 검증 완료 기록 금지
   - 반복 실패 원인 분류와 재발 방지
```

현재 마일스톤에서 모든 목표를 한 번에 완성하지 못할 수 있다.

그러나 최종 목표는 낮추지 않는다. 현재 못 하는 항목은 삭제하거나 축소하지 않고 아래 상태로 남긴다.

```text
미구현
부분 구현
미검증
재확인 필요
후순위
gap
```

CPF의 목표는 “작동하는 샘플 몇 개”가 아니라, **업무 시스템을 실제로 만들고 운영하고 검증할 수 있는 플랫폼 프레임워크**다.

---

# 1. 기준 repository와 기준 파일

CPF 기준 GitHub repository는 아래다.

```text
repository: https://github.com/freeangelsun/202412_01_CPF
branch: master
```

CPF 검수, 요청서 작성, 완료 판정, gap 분석 시 우선 확인해야 할 기준 파일은 아래다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.json
specs/기능_구현_매트릭스.md
specs/sample-coverage-matrix.md
```

## 1.1 최상위 목표 기준 파일

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 CPF의 최상위·상세 통합 목표 기준서다.

이 파일은 대용량일 수 있다. GitHub browser 또는 raw view에서 전체가 안정적으로 표시되지 않을 수 있다.

따라서 아래를 금지한다.

```text
- 일부만 보였다고 “짧은 기존 파일”로 단정
- raw 로딩 실패를 “삭제됨”으로 단정
- 일부 검색 실패를 “미반영”으로 단정
- 원본 목표파일을 임의로 축약/삭제/덮어쓰기
```

목표파일을 직접 확인하지 못한 경우에는 반드시 아래처럼 분리해서 말한다.

```text
GitHub master 목표파일 직접 확인 미수행
로컬 목표파일 직접 확인 미수행
Codex 보고 기준
추정
재확인 필요
```

## 1.2 목표파일 참조 원칙

CPF 관련 검수·요청서 작성·gap 분석 시 최종 목표 판단은 `CPF_FINAL_TARGET_REQUIREMENTS.md` 기준으로 한다.

원칙:

```text
1. 기능이 CPF 목표에 포함되는지 판단할 때 목표파일을 우선한다.
2. 목표파일을 직접 확인하지 못했으면 확인했다고 말하지 않는다.
3. Codex 보고, report, matrix가 완료라고 해도 목표파일 기준과 충돌하면 완료로 판정하지 않는다.
4. 목표파일에서 요구한 기능을 현재 마일스톤에서 못 하면 삭제하지 않고 gap으로 남긴다.
5. Codex 요청서에는 로컬 checkout 기준으로 목표파일 관련 키워드, REQ-ID prefix, 관련 섹션을 검색하라는 지시를 넣는다.
6. 목표파일 검색 없이 임의 판단으로 구현 범위를 축소하지 않는다.
```

---

# 2. 이 문서의 역할

`CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`는 CPF 검수, 진행 순서, 완료 판정, 다음 Codex 요청서 작성 기준 문서다.

이 문서는 Codex에게 매번 그대로 붙여넣는 작업지시서가 아니다.

이 문서는 ChatGPT가 아래 작업을 할 때 기준으로 사용한다.

```text
- Codex 완료 보고 검수
- GitHub master 또는 로컬 파일 확인
- 소스/test/sql/smoke/evidence/report/matrix 대조
- 완료/부분 구현/미구현/미검증/실패/재확인 필요 판정
- gap 분석
- 다음 Codex 요청서 작성
- 반복 실패 원인 분류
- 프로젝트 목표 대비 진행률 판단
- 후순위/제외 범위 분리
```

Codex에게 전달할 요청서는 이 문서의 기준을 바탕으로 별도 작성한다.

---

# 3. 상태값 표준

상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

다른 상태값을 임의로 만들지 않는다.

## 3.1 완료

완료는 아래가 실제 확인된 상태다.

```text
- 목표파일 기준 해당 기능 범위 확인
- 소스 구현
- 계층 연결
- 필요한 SQL/Flyway/all_install 반영
- 기능 테스트 존재
- 기능 테스트 실행 evidence 존재
- 필요한 EDU 샘플 존재
- EDU 샘플 테스트 존재
- REST API 기능이면 Swagger/OpenAPI 반영
- sample coverage matrix 반영
- 기능 매트릭스 반영
- report/evidence index/gap matrix 정합
- 필요한 runtime/browser/MariaDB/broker/multi-instance 검증 수행
- 직접 실행하지 않은 항목을 완료로 기록하지 않음
```

## 3.2 부분 구현

아래는 부분 구현이다.

```text
- 소스만 있음
- 계층 연결 일부 누락
- 테스트는 있으나 evidence 없음
- 기능 테스트는 있으나 EDU 샘플 없음
- EDU 샘플은 있으나 EDU 테스트 없음
- Swagger annotation 일부만 있음
- matrix/report 반영 일부 누락
- source/contract는 있으나 runtime 검증 없음
```

## 3.3 미구현

실질 구현이 없거나 필요한 파일/기능/테스트/설정이 없는 상태다.

## 3.4 미검증

구현은 있으나 검증을 수행하지 않았거나 evidence가 없는 상태다.

예:

```text
- 실제 MariaDB 신규 빈 DB full install 미실행
- 실제 broker runtime 미실행
- 실제 SFTP/SSH runtime 미실행
- 실제 browser click 미실행
- 실제 remote deploy 미실행
- java -version/gradle --version evidence 없음
```

## 3.5 실패

실행 실패, 품질게이트 실패, evidence 불일치, ownership 위반, 금지 구조 확인 등 명확한 문제 상태다.

## 3.6 재확인 필요

GitHub view/raw/evidence가 충돌하거나, 로컬에서만 확인 가능한 사안이거나, 사용자 확인이 필요한 상태다.

---

# 4. 기본 검수 순서

사용자가 아래처럼 말하면 검수 기준으로 처리한다.

```text
완료됐다
push 했다
리뷰해줘
검수해줘
다음 요청서 만들어줘
```

처리 순서:

```text
1. Codex 보고와 완료 리포트를 주장으로 분리한다.
2. GitHub master 또는 사용자가 제공한 로컬 파일을 직접 확인한다.
3. CPF_FINAL_TARGET_REQUIREMENTS.md 확인 가능 여부를 분리한다.
4. CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md 기준을 확인한다.
5. 작업 범위 관련 파일을 확인한다.
6. 소스, 테스트, SQL, Flyway, all_install, smoke, README, report, matrix, evidence를 대조한다.
7. 기능별 EDU 샘플과 EDU 테스트를 확인한다.
8. REST API 기능이면 Swagger/OpenAPI를 확인한다.
9. sample coverage matrix와 evidencePath를 확인한다.
10. 기술 ownership 위반 여부를 확인한다.
11. 금지 의존성 여부를 확인한다.
12. Spring Event 남용 여부를 확인한다.
13. 실행 로그가 없으면 실행 검증 완료로 판정하지 않는다.
14. 직접 실행하지 못한 항목은 직접 실행 미수행으로 분리한다.
15. 검수 결과 기준으로 다음 요청서를 작성한다.
```

---

# 5. Codex 보고 처리 원칙

Codex 보고는 완료 근거가 아니라 검수 대상이다.

Codex가 아래처럼 보고해도 그대로 완료로 판정하지 않는다.

```text
작업 완료
테스트 성공
qualityGate 성공
evidence 생성
리포트 갱신
push 완료
```

반드시 실제 파일과 evidence를 확인한다.

답변 시 아래를 분리한다.

```text
검증 사실
Codex 주장
추정
미확인 사항
직접 실행 미수행
```

---

# 6. CPF 개발·검수·관리 Lifecycle

ChatGPT는 CPF 진행을 아래 lifecycle로 관리한다.

```text
1. 목표 확인
   - CPF_FINAL_TARGET_REQUIREMENTS.md
   - CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
   - 관련 matrix/report/evidence 확인

2. 현재 상태 확인
   - GitHub master 또는 사용자가 제공한 로컬 파일 확인
   - Codex 보고는 주장으로 분리
   - 소스/test/sql/smoke/evidence/report/matrix 대조

3. gap 분류
   - 완료
   - 부분 구현
   - 미구현
   - 미검증
   - 실패
   - 재확인 필요

4. 요청서 작성
   - 목표파일 기준
   - 실제 검수 결과 기준
   - ownership 기준
   - Spring Event 제한 기준
   - EDU/Swagger/Test/Evidence 동시 구현 기준
   - SQL/Flyway/all_install/evidence 기준

5. Codex 완료 보고 검수
   - 완료 주장과 실제 파일 분리
   - 실행 로그/evidence 확인
   - report/matrix/evidence 정합성 확인
   - 직접 실행하지 않은 항목은 미검증으로 분리

6. 다음 작업 선정
   - 목표파일 기준 남은 gap 우선
   - 반복 실패 원인 분류
   - 같은 방식 재요청 금지
   - 요청 단위는 너무 작게 쪼개지 않되, 전체 최종 목표를 한 번에 완료하라고 하지 않음
```

---

# 7. 기술 ownership 기준

모든 기술 capability는 업무 주제영역에 종속시키지 않는다.

## 7.1 PFW 책임

기본 프레임워크 기술 기능은 `pfw`가 소유한다.

PFW 책임 후보:

```text
Service Call Engine
CpfWebClient/CpfRestClient
timeout/retry/circuit breaker/failover
service/endpoint/instance registry
instance health/heartbeat/ghost 판단
transactionGlobalId/TransactionContext
segment/timeline
selectedInstanceId logging
표준/확장 헤더 전파
masking/security/audit 기본
idempotency 표준
outbox/inbox 표준
unknown result 표준 모델
reconciliation 표준 port
broker publish/consume 표준 port
file transfer 표준 port
archive/compression 표준 port
SFTP/FTP/SCP/SSH request/result/plan
credential/key/cert/secret provider port
runtime heartbeat
distributed lock
architecture rule check
```

## 7.2 CMN 책임

프로젝트 공통 커스텀과 업무 공통 확장은 `cmn`이 소유한다.

CMN 책임 후보:

```text
공통 코드
공통 메시지
프로젝트 표준 오류/상태 코드 확장
고정길이 전문 layout/parser/formatter
전문 field spec
전문 fixture/helper
파일명/디렉터리 규칙 helper
validation/converter/helper
프로젝트별 adapter base
업무 공통 helper
```

## 7.3 업무 주제영역 책임

`mbr`, `bza`와 생성기로 추가한 업무 주제영역은 PFW/CMN capability를 사용하는 consumer, adapter, 업무 설정, 업무 구현체 역할을 담당한다. `adm`은 프레임워크 운영, `xyz`와 `bat`는 각각 온라인·배치 교육과 검증을 담당한다.

신규 업무 주제영역 책임 후보:

```text
업무 aggregate와 업무 규칙
자기 주제영역 DB와 Repository/Mapper
PFW public port를 구현하는 local adapter
분리 배포를 위한 remote proxy
업무 API와 표준 실행 ID
업무 감사와 권한 연결
업무 SQL, 테스트, OpenAPI
```

## 7.4 금지 구조

```text
PFW가 MBR/BAT/BZA 또는 생성 업무 모듈에 의존
CMN이 업무 주제영역 구현체에 의존
업무 모듈이 다른 업무 모듈의 Repository/Mapper를 직접 사용
업무 주제영역 간 내부 기술 클래스 재사용
fixed-length parser/formatter가 특정 업무 모듈 전용 기술 엔진으로 고정
timeout/retry/circuit/failover가 특정 업무 모듈 전용
OAuth/JWT/mTLS 공통 엔진이 특정 업무 모듈 전용
unknown result/reconciliation 표준 모델이 특정 업무 모듈 전용
broker/filetransfer/archive 기술 engine이 CMN 소유처럼 구현
```

---

# 8. MSA / Service Call 기준

CPF는 MSA-first, modular-monolith-compatible 구조를 기준으로 한다.

모든 주제영역은 아래 방식으로 배포될 수 있어야 한다.

```text
같은 JVM
별도 WAS
1대 인스턴스
2대 인스턴스
3대 이상 다중 인스턴스
```

PFW는 모든 주제영역 호출에 대해 아래를 공통 제공해야 한다.

```text
service/endpoint/instance registry
health check
client-side load balancing
LB endpoint mode
direct instance mode
failover
timeout
retry
circuit breaker
selected instance logging
remote instance response header
ADM 관제
```

Facade는 같은 JVM 전용 개념이 아니다.

```text
같은 JVM:
- Local Facade 사용

MSA:
- Facade Contract/Port를 Remote Facade Proxy가 구현
- CpfWebClient/CpfRestClient를 통해 대상 API 호출
```

업무 코드 금지:

```text
URL 직접 조합
Controller 직접 호출
타 주제영역 DB 직접 접근
타 주제영역 Repository/Mapper 직접 참조
```

---

# 9. Spring Event 사용 기준

Spring Event는 핵심 거래 흐름의 중심 기술로 남용하지 않는다.

허용:

```text
같은 JVM 내부 보조 hook
감사/알림/cache invalidation/telemetry
이미 DB 상태가 저장된 뒤의 부가 후처리
유실되어도 재계산 또는 재처리 가능한 이벤트
```

제한 또는 금지:

```text
핵심 거래 상태 전이를 ApplicationEvent/EventListener 체인에 의존
외부 송신을 outbox 없이 EventListener에서 직접 처리
Saga/compensation을 Spring Event만으로 처리
unknown result/reconciliation을 이벤트 체인에만 의존
multi-instance 전달을 Spring Event로 완료 주장
broker DLQ/replay를 Spring Event로 대체
```

핵심 흐름은 아래를 우선한다.

```text
DB 상태
transactionGlobalId
segment/timeline
outbox/inbox
idempotency
broker 또는 scheduler 기반 재처리 구조
```

---

# 10. 기능 개발 시 EDU/Swagger/Test/Evidence 동시 구현 원칙

CPF 기능을 신규 구현하거나 수정할 때는 기능 소스만 수정하지 않는다.

해당 기능의 개발자 참조 EDU 샘플, EDU 테스트, 기능 테스트, Swagger/OpenAPI, evidence, report를 함께 갱신한다.

기능 완료 후보가 되려면 필요한 범위에서 아래가 함께 확인되어야 한다.

```text
1. 기능 소스 구현
2. 기능 자체 테스트 구현
3. 기능별 EDU 샘플 소스 구현
4. EDU 샘플 테스트 구현
5. REST API 기능이면 Swagger/OpenAPI 반영
6. OpenAPI smoke 또는 coverage 검증
7. sample coverage matrix 반영
8. 기능 매트릭스 반영
9. evidence 생성
10. CPF_STABILIZATION_REPORT.md 반영
11. CPF_EVIDENCE_INDEX.md 반영
12. CPF_GAP_MATRIX.md 반영
```

판정 기준:

```text
기능 소스만 있음
→ 부분 구현

기능 소스 + 기능 테스트만 있음
→ 부분 구현 또는 미검증

기능 소스 + 기능 테스트 + Swagger만 있음
→ 부분 구현

기능 소스 + 기능 테스트 + EDU 샘플 + EDU 테스트 + Swagger + evidence + report까지 있음
→ 완료 후보

실제 runtime이 필요한데 실행하지 않았음
→ source/contract 수준은 완료 후보가 될 수 있으나 runtime은 미검증
```

완료 불인정:

```text
기능 소스만 있고 EDU 샘플 없음
EDU 샘플은 있으나 EDU 테스트 없음
REST API인데 Swagger/OpenAPI 누락
Swagger UI만 열리고 schema/operation 검증 없음
sample coverage matrix 누락
CoverageCatalog만 있고 실제 sample class 없음
evidencePath가 실제 존재하지 않음
report에는 완료라고 되어 있으나 evidence가 없음
외부 runtime을 실행하지 않았는데 runtime 완료로 기록
```

---

# 11. 기능별 검수 단위

기능 검수는 “소스 구현 여부”만 보지 않는다.

기능별 검수 단위는 아래 묶음으로 본다.

```text
기능 소스
기능 테스트
EDU 샘플
EDU 샘플 테스트
Swagger/OpenAPI
OpenAPI 검증
sample coverage matrix
evidence
report/index/gap matrix
```

검수 답변에는 가능한 범위에서 아래 표를 포함한다.

| 기능 | 기능 소스 | 기능 테스트 | EDU 샘플 | EDU 테스트 | Swagger/OpenAPI | Evidence | 상태 |
|---|---|---|---|---|---|---|---|
| ... | 확인/누락 | 확인/누락 | 확인/누락 | 확인/누락 | 확인/누락 | 확인/누락 | 완료/부분 구현/미구현/미검증/실패/재확인 필요 |

---

# 12. EDU 샘플 기능별 패키지 기준

EDU 샘플은 한 패키지에 몰아넣지 않는다.

개발자가 기능별로 쉽게 찾고 참고할 수 있도록 `edu` 하위에 기능별 package를 나눠 구성한다.

`catalog` 패키지는 sampleId 목록 관리와 coverage tracking 용도로만 사용한다.

실제 개발자 참조 샘플은 기능별 package 하위에 둔다.

## 12.1 XYZ EDU

XYZ는 별도 EDU 서비스가 아니라 XYZ 내부 교육 샘플 패키지를 가진다.

```text
xyz/src/main/java/cpf/xyz/edu/crud
xyz/src/main/java/cpf/xyz/edu/query
xyz/src/main/java/cpf/xyz/edu/pagination
xyz/src/main/java/cpf/xyz/edu/detail
xyz/src/main/java/cpf/xyz/edu/transaction
xyz/src/main/java/cpf/xyz/edu/servicecall
xyz/src/main/java/cpf/xyz/edu/facade
xyz/src/main/java/cpf/xyz/edu/header
xyz/src/main/java/cpf/xyz/edu/idempotency
xyz/src/main/java/cpf/xyz/edu/failure
xyz/src/main/java/cpf/xyz/edu/security
xyz/src/main/java/cpf/xyz/edu/audit
xyz/src/main/java/cpf/xyz/edu/validation
xyz/src/main/java/cpf/xyz/edu/telegram
xyz/src/main/java/cpf/xyz/edu/messaging
xyz/src/main/java/cpf/xyz/edu/filetransfer
xyz/src/main/java/cpf/xyz/edu/archive
xyz/src/main/java/cpf/xyz/edu/operation
```

## 12.2 BAT EDU

BAT는 BAT 내부에 배치 교육 샘플 패키지를 가진다.

```text
bat/src/main/java/cpf/bat/edu/job
bat/src/main/java/cpf/bat/edu/chunk
bat/src/main/java/cpf/bat/edu/transaction
bat/src/main/java/cpf/bat/edu/servicecall
bat/src/main/java/cpf/bat/edu/centercut
bat/src/main/java/cpf/bat/edu/logging
bat/src/main/java/cpf/bat/edu/idempotency
bat/src/main/java/cpf/bat/edu/retry
bat/src/main/java/cpf/bat/edu/restart
bat/src/main/java/cpf/bat/edu/reconciliation
bat/src/main/java/cpf/bat/edu/archive
```

## 12.3 생성 업무 모듈 샘플

신규 업무 모듈은 별도 EDU 패키지를 복제하지 않는다. `scripts/create-domain.ps1`가 생성한 Controller, Facade, Port, local adapter, remote proxy, Repository, SQL과 테스트 골격을 사용하고 범용 학습은 XYZ/BAT EDU를 참조한다.

## 12.4 CMN EDU/Test-Support

CMN은 기술 engine 소유자가 아니라 공통 helper, parser, formatter, fixture, validation 역할을 가진다.

```text
cmn/src/main/java/cpf/cmn/edu/fixedlength
cmn/src/main/java/cpf/cmn/edu/validation
cmn/src/main/java/cpf/cmn/edu/converter
cmn/src/main/java/cpf/cmn/edu/message
cmn/src/main/java/cpf/cmn/edu/code
cmn/src/main/java/cpf/cmn/edu/fixture
cmn/src/main/java/cpf/cmn/edu/file
cmn/src/main/java/cpf/cmn/edu/masking
```

## 12.5 ADM/BZA reference

ADM과 BZA는 제품 운영 코드에 `edu` 패키지를 만들지 않는다. ADM 운영 API·UI와 BZA 업무 백오피스는 실제 reference 구현으로 유지하고, 동일 capability의 학습용 코드는 XYZ/BAT EDU에서 제공한다.

## 12.6 PFW capability sample/test-support

PFW는 독립 EDU 서비스가 아니라 capability별 package와 test-support를 통해 샘플을 제공한다.

```text
pfw/src/main/java/cpf/pfw/common/servicecall
pfw/src/main/java/cpf/pfw/common/broker
pfw/src/main/java/cpf/pfw/common/filetransfer
pfw/src/main/java/cpf/pfw/common/archive
pfw/src/main/java/cpf/pfw/common/security
pfw/src/main/java/cpf/pfw/common/runtime
pfw/src/main/java/cpf/pfw/common/admin
pfw/src/main/java/cpf/pfw/common/audit
pfw/src/main/java/cpf/pfw/common/masking
```

완료 불인정:

```text
EDU 실제 샘플이 catalog package에만 몰려 있음
기능별 package가 없음
하나의 controller에 모든 샘플을 몰아넣음
sampleId featureArea와 sourcePath package가 맞지 않음
CoverageCatalog만 만들고 sample 완료 처리
```

---

# 13. Sample Coverage Matrix 기준

`specs/sample-coverage-matrix.md`는 단순 샘플 목록이 아니라 기능 검증 coverage를 추적하는 기준 파일이다.

기능 개발 시 sample coverage matrix도 함께 갱신한다.

필수 컬럼:

```text
sampleId
module
package
featureArea
sampleName
sourcePath
testPath
evidencePath
validationLevel
runtimeRequired
runtimeExecuted
status
notes
swaggerTag
swaggerOperationId
openApiPath
httpMethod
openApiVerified
```

기준:

```text
sourcePath는 실제 샘플 class 또는 실제 기능 테스트여야 한다.
CoverageCatalog는 완료 근거로 사용하지 않는다.
testPath는 실제 기능/계약/fixture 테스트여야 한다.
완료 sample은 evidencePath가 실제 존재해야 한다.
REST API 샘플은 Swagger operation과 연결되어야 한다.
외부 runtime 필요 항목은 runtimeRequired=true, runtimeExecuted=false로 분리한다.
```

완료 불인정:

```text
sampleId만 있고 실제 sourcePath 없음
CoverageCatalog만 있고 실제 sample class 없음
CoverageCatalogTest를 기능 테스트처럼 완료 처리
evidencePath가 존재하지 않음
REST API sample인데 Swagger operation 연결 없음
```

---

# 14. 전체 거래/API Swagger/OpenAPI 표준 대응 기준

Swagger/OpenAPI는 EDU 샘플 API에 한정하지 않는다.

CPF의 모든 REST 거래/API는 구현과 동시에 OpenAPI 계약이 생성·관리·검증되어야 한다.

## 14.1 적용 대상

```text
ADM 운영 API
BZA 업무관리 API
MBR 업무 API
BAT 배치 실행/관리 API
XYZ 업무/EDU API
생성 업무 모듈 API
PFW runtime/admin/status API 후보
공통 인증/권한/메뉴/코드/메시지/감사/다운로드/마스킹 API
Service Call/Registry/Health API
File Transfer API
Broker/Outbox/Inbox API
Archive/Compression API
Fixed-Length 전문 API
Scheduler/Worker/Batch execution API
```

## 14.2 모든 REST Controller 기준

```text
@Tag 명시
@Operation 명시
operationId 중복 금지
summary/description 명시
@ApiResponses 명시
성공 response schema 명시
표준 오류 response schema 명시
request DTO schema 명시
response DTO schema 명시
필수 request header 명시
transactionGlobalId 관련 header 명시
moduleId/instanceId/selectedInstanceId 관련 header 또는 설명 명시
권한이 필요한 API는 권한 요구사항 설명
마스킹 대상 응답은 마스킹 설명
감사/다운로드 감사 대상 API는 감사 설명
외부 runtime 미검증 API는 runtime 미검증 여부 설명
```

## 14.3 OpenAPI 검증 기준

Swagger UI 접근만으로 완료 처리하지 않는다.

아래를 함께 검증한다.

```text
module별 /v3/api-docs 접근 가능
module별 /swagger-ui/index.html 접근 가능
모든 Controller가 OpenAPI path로 노출되는지 확인
Controller method 수와 OpenAPI operation 수 비교
operationId 중복 검사
path/method 중복 검사
@Tag 누락 검사
@Operation 누락 검사
request schema 누락 검사
response schema 누락 검사
standard error response 누락 검사
required header 설명 누락 검사
transactionGlobalId/header 설명 누락 검사
권한/마스킹/감사 설명 누락 검사
```

완료 불인정:

```text
EDU 샘플 API만 Swagger 대응하고 일반 거래 API는 누락
Swagger UI만 열리고 schema 품질 검증 없음
Controller가 있는데 OpenAPI operation 없음
@Tag 없음
@Operation 없음
operationId 중복
request/response schema 누락
표준 오류 response 누락
transactionGlobalId/header 설명 누락
권한/마스킹/감사 설명 누락
openApiSmoke가 qualityGate에 연결되지 않음
OpenAPI evidence 없이 Swagger 완료 기록
```

---

# 15. Java 25 LTS 표준 목표 기준

CPF의 Java 표준 목표 버전은 Java 25 LTS로 한다.

단, Java 25 전환은 단순 문서 표기가 아니라 Gradle toolchain, module compile, test, bootJar, qualityGate, CI/Jenkins 기준으로 검증해야 한다.

기준:

```text
Java target version: 25
Java toolchain: 25
JDK distribution candidate: Eclipse Temurin 25 LTS 또는 조직 표준 OpenJDK 25 LTS
```

완료 후보 조건:

```text
java -version evidence 존재
gradle --version evidence 존재
Gradle toolchain 25 설정
전체 모듈 compile/test 성공
실행 주체 모듈 bootJar 성공
qualityGate 성공
Java 17/21 하드코딩 잔존 여부 검사
Spring Boot/Spring Framework/Gradle wrapper 호환성 확인
```

완료 불인정:

```text
문서에만 Java 25라고 기록
toolchain 설정 없음
java -version evidence 없음
compile/test 미실행
bootJar 미검증
Java 25에서 실패했는데 완료 기록
```

Spring Boot major upgrade가 필요하면 무리하게 완료 처리하지 말고 `재확인 필요` 또는 `미구현`으로 gap에 남긴다.

---

# 16. SQL / 설치 / evidence 기준

SQL 검수 시 아래 정합성을 본다.

```text
split SQL
Flyway migration
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
```

MariaDB 신규 빈 DB 실실행 없이 전체 설치 성공이라고 말하지 않는다.

완료/부분 구현으로 기록한 항목은 실제 존재하는 evidence 경로가 있어야 한다.

금지:

```text
없는 evidence 파일을 완료 근거로 참조
stale evidence를 신규 완료 근거로 참조
다른 profile evidence를 완료 근거로 참조
다른 DB evidence를 완료 근거로 참조
다른 branch 산출물을 완료 근거로 참조
```

---

# 17. profile / deploy / EDU 기준

CPF profile 표준은 아래다.

```text
local
dev
stg
prod
```

`prd`는 표준으로 쓰지 않는다.

실행 주체:

```text
MBR
ADM
BAT
BZA
XYZ
```

실행 주체 아님:

```text
PFW
CMN
EDU
```

EDU는 별도 서비스가 아니라 XYZ/BAT 등 내부 교육 샘플 패키지다.

금지/점검 대상:

```text
EDU_SERVER_PORT
deploy/env/*-edu.env
deploy-edu.ps1
deploy-edu.sh
settings.gradle 별도 edu module
build.gradle EDU alias
inventory EDU 서비스 등록
-PcpfModule=EDU 성공 처리
```

기본 실행 표준:

```text
Spring Boot executable bootJar
embedded Tomcat
java -jar
module별 server.port
module별 datasource URL/env 기반 설정
```

외부 WAS/JNDI는 선택 호환 옵션이다. 실제 WAS/JNDI runtime 검증은 환경 없으면 미검증이다.

---

# 18. qualityGate 기준

qualityGate는 단순 빌드 성공이 아니라 report/matrix/evidence/coverage 정합성까지 잡아야 한다.

qualityGate 필수 후보:

```text
check-feature-evidence
check-docx-standard
check-report-matrix-evidence-consistency
check-evidence-path-existence
check-sample-coverage
check-sample-catalog-only
check-sample-source-duplication
check-sample-actual-implementation
check-sample-package-structure
check-openapi-controller-coverage
check-openapi-operation-id
check-openapi-schema
check-openapi-standard-error
check-openapi-headers
check-openapi-security-audit-masking
check-architecture-ownership
check-spring-event-usage
check-utf8-mojibake
check-empty-directories
check-garbage-files
check-local-port-duplicates
check-java25-toolchain
```

qualityGate가 통과했다고 해도, 실제 evidence 파일이 없거나 stale이면 완료 판정하지 않는다.

---

# 19. 요청서 작성 기준

Codex 요청서는 실제 검수 결과와 목표파일 검색 결과를 기준으로 작성한다.

작게 자주 쪼개지 말고 대형 마일스톤 단위로 작성하되, 한 요청에서 전체 최종 목표를 모두 완료하라고 시키지 않는다.

요청서는 아래로 나눈다.

```text
필수 완료 범위
보강 범위
착수 범위
후순위/제외 범위
```

기능을 신규 구현하거나 수정하는 요청에는 아래를 기본 포함한다.

```text
기능 소스 구현
기능 테스트 구현
기능별 EDU 샘플 구현
EDU 샘플 테스트 구현
REST API이면 Swagger/OpenAPI 반영
OpenAPI smoke 또는 coverage 검증
sample coverage matrix 반영
기능 매트릭스 반영
evidence 생성
CPF_STABILIZATION_REPORT.md 반영
CPF_EVIDENCE_INDEX.md 반영
CPF_GAP_MATRIX.md 반영
```

요청서에는 Codex 실행 제한을 포함한다.

```text
작업 전 최신 origin/master 확인 필수
검증 성공 후 commit·merge·origin/master push 필수
작업 branch만 push하고 종료 금지
원격 master SHA 재확인 필수
force 필요 시 --force-with-lease와 원격 변경 보존 필수
민감정보 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
문서량 채우기식 문서화 금지
신규 HTML 작성/수정 지양
PDF 생성은 최종 정본화 단계에서만 수행
```

위 제한은 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` 자체의 상단 지시가 아니라, **향후 Codex 요청서 작성 시 포함해야 할 제한사항**이다.

---

# 20. 검수 답변 기준

CPF 검수 답변은 아래를 분리한다.

```text
검증 사실
Codex 주장
추정
미확인 사항
직접 실행 미수행
```

직접 확인하지 않은 것은 완료라고 하지 않는다.

검수 답변에는 가능하면 아래 표를 포함한다.

| 영역 | 상태 | 근거 | 미확인/리스크 |
|---|---|---|---|
| 기능 소스 | 완료/부분 구현/... | 경로/evidence | ... |
| 기능 테스트 | 완료/부분 구현/... | 로그/evidence | ... |
| EDU 샘플 | 완료/부분 구현/... | 경로/evidence | ... |
| EDU 테스트 | 완료/부분 구현/... | 로그/evidence | ... |
| Swagger/OpenAPI | 완료/부분 구현/... | 경로/evidence | ... |
| Evidence 정합성 | 완료/실패/... | 경로 | ... |
| Runtime 검증 | 미검증/완료 | 로그 | ... |

---

# 21. 반복 실패 대응 기준

반복 실패 항목은 같은 방식으로 재요청하지 않는다.

실패 원인을 아래로 분류한다.

```text
요구사항 해석 문제
설계 방향 문제
소스 구현 누락
계층 연결 누락
SQL/Flyway/all_install 누락
설정/profile 문제
테스트 설계 문제
실행 환경 문제
DB/MariaDB 문제
broker 문제
browser/UI 검증 문제
evidence 부재 또는 불일치
Codex 보고와 실제 파일 불일치
ownership 위반
Spring Event 남용
Swagger/OpenAPI 누락
EDU 샘플 누락
sample coverage matrix 누락
qualityGate 설계 문제
```

같은 방식으로 안 되면 대안을 제시한다.

---


# 22. 요구사항 선제 발굴과 CPF 전체 공통기능 검수

## 22.0 작업 시작 전 최신 정본 preflight

모든 CPF 작업은 가장 먼저 최신 GitHub `master`와 `CPF_FINAL_TARGET_REQUIREMENTS.md` 전체를 확인한다.

필수 기록:

```text
repository
branch
start commit SHA
정본 blob SHA 또는 파일 식별값
확인 시각
확인 방식
```

검수자는 다음을 확인한다.

- 최신 `master`인지
- 로컬 checkout이 최신인지
- 정본이 작업 시작 이후 변경됐는지
- `CPF_NEW_REQUEST.md`가 최신 정본과 충돌하지 않는지
- 기존 report·gap·matrix·evidence가 정본 변경으로 stale해지지 않았는지
- 삭제된 분할 보조본을 참조하지 않는지

정본이 크면 구간 조회, 검색, 스크립트 비교를 사용할 수 있다. 그러나 일부 검색 결과만으로 전체 확인을 대신하지 않는다.

직접 확인하지 못한 경우:

- `GitHub 직접 확인 미수행`
- `로컬 직접 확인 미수행`
- `정본 전체 확인 미수행`
- `재확인 필요`

를 분리해 기록하고 완료 판정을 하지 않는다.

작업 중 `master` 또는 정본이 갱신되면 완료 검수 전에 반드시 다시 fetch/pull하고 변경 영향을 재검토한다.

## 22.0.1 정본 변경 영향도

정본이 갱신되면 최소 다음을 재검토한다.

- 현재 작업 범위
- 기존 완료 상태
- source ownership
- execution ID와 header 계약
- SQL/Flyway/all_install
- OpenAPI
- ADM/BAM/BZA
- EDU/sample coverage
- runtime/browser/MariaDB/broker/multi-instance evidence
- qualityGate
- report/gap/evidence consistency
- request protection baseline

정본 변경 자체를 구현 완료로 기록하지 않는다.

## 22.1 사용자의 질문을 신규 요구사항의 유일한 출발점으로 삼지 않는다

CPF 검수자와 요청서 작성자는 사용자가 개별 기능을 질문할 때마다 그 기능만 추가하는 방식으로 작업하지 않는다.

다음 전체 영역을 주기적으로 전수 검토한다.

```text
PFW
CMN
모든 업무 주제영역
BAT
BZA
ADM
BAM
Gateway
DB/SQL/Flyway
broker
file/attachment
scheduler/worker
security/audit
configuration/secret
observability/alert
deployment/DR
generator/EDU/OpenAPI
qualityGate/evidence/docs
```

상용 프레임워크에 일반적으로 필요하며 CPF 정본 목표와 일치하는 누락 기능은 사용자가 직접 지적하지 않았더라도 다음 요청서에 반영한다.

단, 임의의 유행 기능을 무조건 추가하지 않는다.

```text
CPF 최종 목표와의 관련성
재사용성
PFW/CMN ownership
금융권·운영 환경의 필요성
현재 구현과 연결 가능성
검증 가능성
```

을 판단한다.

## 22.2 새 요구사항 추가 시 기존 완료 상태 재판정

새 요구사항이 기존 기능의 계약·ID 형식·보안·운영·UI·완료 기준을 확장하면 과거 evidence는 새 범위의 완료 근거가 아니다.

예:

```text
기존 실행 ID 16자리 evidence
→ 신규 정본 10자리 요구
→ 기존 evidence는 역사적 근거
→ 신규 범위는 재확인 필요
```

요구사항만 추가하고 report/gap/evidence index를 그대로 두지 않는다.

최소 다음을 확인한다.

- 기존 완료 판정과 신규 요구 충돌
- 이전 evidence의 기준 commit
- source 변경 전후
- SQL 변경 전후
- OpenAPI/UI 변경 전후
- sample coverage 대상 capability 변경
- qualityGate 규칙 변경
- request protection hash 변경

신규 구현 전에는 완료로 올리지 않고 `재확인 필요`, `부분 구현`, `미구현`, `미검증`으로 기록한다.

## 22.3 ADM/BAM/BZA 전체 mutation inventory

ADM/BAM/BZA의 모든 등록·수정·삭제·상태 변경·운영 실행 API와 버튼을 inventory한다.

각 action에 다음을 기록한다.

```text
menu
API
action
target
riskLevel
approvalRequired
autoApprovalCondition
effectiveMode
applyHandler
rollbackHandler
audit
test
evidence
status
```

특정 메뉴에만 승인결재를 붙이지 않는다.

최소 검토 대상:

- channel
- execution policy
- service/instance
- route
- scheduler/batch/worker
- file
- broker/DLQ
- reconciliation/compensation
- error/message
- masking/audit
- permission/menu/role
- configuration/secret metadata
- data correction
- emergency control

## 22.4 자동승인 검수

자동승인은 다음이 모두 충족돼야 한다.

```text
허용 profile
AND auto-approval property
AND requestType 정책
AND 수정 권한
AND 승인 권한
AND 자동승인 사용 권한
AND self-approval 정책
AND 위험도 제한
```

`prod` 일반 자동승인은 기본 금지한다.

자동승인도 다음 상태와 증적을 남겨야 한다.

```text
REQUESTED
AUTO_APPROVED
SCHEDULED/APPLYING
APPLIED 또는 APPLY_FAILED
```

화면의 체크박스만 확인하지 않고 API, DB 상태, apply handler, audit를 확인한다.

## 22.5 적용 시점과 실행 snapshot 검수

승인된 변경의 적용 방식이 명시돼야 한다.

- 즉시
- 다음 실행
- 지정 일시
- 현재 실행 완료 후
- 재기동 후
- 신규 instance부터
- 수동 적용

batch, scheduler, worker, center-cut, broker, file, reconciliation, saga, 운영 재처리는 실행 시작 시 사용한 definition/parameter/policy/instance profile version과 approvalRequestId를 기록한다.

현재 실행 중인 작업의 설정이 승인 후 중간 변경되면 완료가 아니다.

## 22.6 채널·정책 성능 검수

거래별 채널 정책은 DB 정본으로 관리할 수 있으나 매 request마다 DB·Redis·ADM API를 호출하면 안 된다.

확인:

- immutable snapshot
- Map/bitmap lookup
- atomic swap
- loaded version/checksum
- last-known-good
- multi-instance drift
- load failure
- stale policy
- request당 query count

## 22.7 CPF 전체 횡단 기능 검수

기능별 검수 외에 다음 횡단 영역을 점검한다.

- configuration lifecycle
- secret/key/certificate rotation
- service identity
- data scope permission
- audit integrity
- metric/trace/alert/SLO
- resource protection
- cache
- time/business date
- API/event/file schema version
- DB migration/backfill
- retention/purge/privacy
- backup/restore/DR
- deployment/graceful shutdown/rollback
- failure injection/recovery
- supply-chain security
- performance/capacity
- accessibility/browser
- feature flag/kill switch
- support bundle
- docs/runbook

구현을 직접 확인하지 못하면 `재확인 필요` 또는 `미검증`이다.

## 22.8 문서 갱신 시점

요구사항 변경 시 즉시 갱신:

- `CPF_NEW_REQUEST.md`
- `CPF_FINAL_TARGET_REQUIREMENTS.md`의 정본 보강
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 필요 시 report/gap/evidence index의 재확인 상태

구현 후 실제 결과로 갱신:

- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- 기능 matrix
- sample coverage
- README
- OpenAPI
- SQL/Flyway/all_install
- runtime/browser evidence

DOCX/PDF 정본화는 기능과 구조가 안정된 최종 단계에서 수행한다.

---


# 23. 기존 구현 capability의 정본 역추적과 회귀 방지

## 23.1 source → 정본 검수

정본에서 기능을 검색하는 것만으로 충분하지 않다.

최신 source, test, SQL, config, API, UI, script, evidence에서 capability를 추출하고 정본 section과 연결한다.

확인 대상 예:

- PFW port/engine/annotation/autoconfiguration
- CMN parser/formatter/validator/helper
- ADM/BAM menu/controller/service
- BZA auth/organization/permission/approval
- BAT job/scheduler/worker/center-cut
- XYZ/BAT EDU
- generator template
- SQL table/seed
- qualityGate script

source capability가 정본에 없으면 요구사항 누락이다. 삭제 후보로 곧바로 분류하지 않는다.

## 23.2 정본 → source 검수

정본 requirement마다 owner, source, test, SQL/config, API, UI, EDU, runtime, evidence를 확인한다.

구현이 없거나 일부면 허용 상태값으로 남긴다. 정본 항목을 삭제하거나 설명을 줄여 완료처럼 만들지 않는다.

## 23.3 rename·cleanup 회귀 검수

rename, module 제거, package 이동 전에 기능 inventory를 작성한다.

최소 확인:

- source class
- test
- SQL/Flyway
- API/OpenAPI
- menu/permission
- EDU/sample
- script
- evidence
- consumer
- replacement location

삭제 후에는 이전 기능별 regression test와 대체 evidence를 확인한다.

## 23.4 권위 있는 충돌 해소

현재 권위 기준:

- 정본은 `CPF_FINAL_TARGET_REQUIREMENTS.md` 단일 파일
- 정식 backoffice 명칭은 BZA
- ACC는 generated verification/reference domain
- EXS 공통 engine 소유 금지, 기능 inventory 없는 삭제 금지
- 신규 실행 ID는 10자리 O/S/B
- 기존 16자리 ID는 migration/alias 대상
- PFW는 기술 core, CMN은 프로젝트 공통
- XYZ/BAT가 EDU 소유

오래된 report·README·DOCX·evidence가 위 기준과 다르면 과거 근거로만 사용하고 최신 상태를 재검증한다.

## 23.5 구현 capability baseline 검수

다음 기능은 repository에서 이미 구현 또는 부분 구현 이력이 있으므로 cleanup에서 반드시 역추적한다.

- BZA password hash, auth, refresh rotation, bootstrap, permission
- BZA organization/employee/approval/audit/operation support
- AI provider/embedding/vector와 XYZ EDU
- remote log local/multi-instance/bundle job
- attachment storage
- execution catalog/log propagation
- batch/scheduler/ghost
- create-domain
- standard header
- Service Call Engine
- broker/outbox/inbox/DLQ
- file transfer
- evidence sanitization
- request protection
- sample coverage

과거 report 상태를 그대로 복사하지 않고 최신 source와 evidence로 재판정한다.

## 23.6 완료 보고

최종 보고에 다음 표를 포함한다.

```text
capability
requirement section
source
owner
consumer
test
SQL/OpenAPI/UI/EDU
evidence
이전 상태
최신 상태
회귀 여부
남은 gap
```

source-to-target coverage가 확인되지 않으면 `full-capability-inventory` 또는 관련 항목을 완료로 기록하지 않는다.

---

# 24. 빠른 검수 체크리스트

```text
[ ] CPF_FINAL_TARGET_REQUIREMENTS.md 직접 확인 여부 분리
[ ] CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md 기준 확인
[ ] Codex 보고와 실제 파일 분리
[ ] 소스 구현 확인
[ ] 계층 연결 확인
[ ] 기능 테스트 확인
[ ] EDU 샘플 확인
[ ] EDU 샘플 테스트 확인
[ ] Swagger/OpenAPI 확인
[ ] operationId 중복 확인
[ ] request/response schema 확인
[ ] 표준 오류 response 확인
[ ] transactionGlobalId/header 설명 확인
[ ] 권한/마스킹/감사 설명 확인
[ ] sample coverage matrix 확인
[ ] evidencePath 실제 존재 확인
[ ] report/evidence index/gap matrix 정합성 확인
[ ] SQL/Flyway/all_install 정합성 확인
[ ] runtime 검증 여부 분리
[ ] ownership 위반 확인
[ ] Spring Event 남용 확인
[ ] Java 25 toolchain/evidence 확인
[ ] 신규 요구와 기존 완료 상태 충돌 확인
[ ] 기존 구현 capability의 정본 section 연결 확인
[ ] rename/cleanup 전후 기능 inventory와 대체 evidence 확인
[ ] BZA auth/권한/결재 기능 회귀 확인
[ ] AI/remote-log/attachment/batch/generator capability 회귀 확인
[ ] 신규 10자리 O/S/B와 legacy ID 사용처 확인
[ ] ACC reference domain·EXS 기능 이전 기준 확인
[ ] ADM/BAM/BZA mutation endpoint 전수 inventory
[ ] 승인 대상 API의 공통 approval orchestration 확인
[ ] 자동승인 profile/property/권한/self-approval 확인
[ ] 적용 시점·설정 version·실행 snapshot 확인
[ ] channel/client/service identity binding 확인
[ ] 정책 snapshot의 request당 DB/원격 조회 없음 확인
[ ] configuration/secret/observability/retention/DR 횡단 검토
[ ] API/event/file schema compatibility 확인
[ ] 성능·용량·공급망 보안 검증 여부 확인
```

---

# 25. 마지막 원칙

CPF 작업은 빠르게 가되 완료 판정은 엄격하게 한다.

```text
기능만 구현하고 완료라고 하지 않는다.
EDU 샘플만 있고 테스트 없으면 완료라고 하지 않는다.
Swagger UI만 열리면 완료라고 하지 않는다.
evidence 없는 완료는 완료가 아니다.
Codex 보고는 완료 근거가 아니라 검수 대상이다.
목표를 낮춰서 완료처럼 만들지 않는다.
```

앞으로 CPF 기능 개발은 아래를 한 묶음으로 본다.

```text
기능 구현
+ EDU 샘플
+ EDU 테스트
+ 기능 테스트
+ Swagger/OpenAPI
+ OpenAPI 검증
+ sample coverage
+ evidence
+ report/index/gap matrix
```

이 기준을 만족하지 못하면 `완료`가 아니라 `부분 구현`, `미검증`, `미구현`, `실패`, `재확인 필요` 중 하나로 판정한다.

---

## 2026-07-20 generator·Base 계층·logging·XYZ 추가 판정 기준

### Generator 완료 조건

`domain-generator=완료`는 다음을 모두 확인한 경우에만 가능하다.

- 기본 생성은 최소 module과 대표 feature 1개
- expected file manifest와 실제 파일 일치
- zero-byte/placeholder/빈 package/patch-candidates 없음
- platform version 상속
- canonical 실행 ID formatter 사용
- local profile 외 profile은 option
- batch/BZA/ADM/prod/deploy 후보는 option
- generator→build/test/runtime→remover→잔존 0건
- 사용자 작성 파일 보호 negative test
- ACC 자체 lifecycle은 disposable worktree에서 별도 검증
- modular-monolith 다중 DataSource/bean collision 검증
- DB vendor 하드코딩 없음

### BaseController/BaseService 완료 조건

- `CpfBaseController → ModuleBaseController → FeatureController`
- `CpfBaseService → ModuleBaseService → FeatureService`
- 모든 controller/service scanner와 명시적 예외 allowlist
- module base는 추상 class이며 route/bean을 만들지 않음
- DTO/entity/repository/mapper/port/adapter에는 상속 강제 금지
- Spring bean/AOP/transaction/OpenAPI/runtime 회귀 확인
- generator template과 기존 module source가 동일 기준

### Logging ownership 완료 조건

- 공통 logging `@Aspect`와 engine은 PFW에 단일 소유
- domain `@Aspect` 0건 또는 승인된 업무 이벤트 전용 예외
- domain은 PFW annotation/port/provider 사용
- 동일 join point 이중 로그 0건
- masking 이전 원문 기록 0건
- 비동기 queue failure와 fallback 검증
- 파일/DB 로그 transactionGlobalId·segment 일치

### XYZ 완료 조건

- 일반 업무 package 표준을 따름
- source/build/test/runtime sample은 유지
- production 기본 inventory·자동 배포·secret·DB·route·menu seed에서 제외
- 다른 production module의 XYZ dependency 0건
- 명시적 demo/reference option에서만 배포 가능

---

## 2026-07-20 Framework 사용성·Base Contract·JavaDoc 완료 기준

### 공통 capability 기본 API 완료

- 기본 사용은 업무 입력 + typed contract/거래 ID만 요구
- 1~3줄 기본 사용 예제
- timeout/retry/circuit/bulkhead 등은 중앙 policy default
- transport adapter, raw URI, HTTP method, framework target object를 업무 코드에 노출하지 않음
- local/remote 동일 typed client
- source module/trace/instance/channel 자동 파생
- raw override는 상한·권한·감사·production 제한
- basic/advanced sample 분리
- effective policy ADM 조회
- negative/timeout/target-down/unknown-result test

### 계층별 Base Contract 완료

- Controller와 Service는 PFW base→domain base→feature 3단 구조
- Facade, DAO/Repository, DTO, Model, Mapper, Client, Adapter, Batch에도 canonical contract/support 존재
- DTO/Mapper/Repository에는 무리한 concrete class 상속을 강제하지 않고 interface/capability/composition을 사용
- source scanner와 예외 allowlist
- generator와 기존 module 동시 보정
- Spring bean/AOP/transaction/serialization/OpenAPI 회귀

### JavaDoc 완료

- public/protected API·SPI 100%
- parameter/return/exception/nullability/thread-safety/idempotency/security/policy 설명
- meaningful comment; 기계적 한 줄 반복 금지
- doclint와 missing JavaDoc gate
- aggregate JavaDoc, sourcesJar, javadocJar
- sample snippet compile test
- offline JavaDoc distribution
- 생성 HTML repository commit 금지
- OpenAPI·JavaDoc·sample·transaction catalog 정합성

---

## 2026-07-20 3단 확장·Override·Property 완료 기준

### 3단 확장 체계

- 모든 개발 계층에 PFW Contract와 Domain Contract가 존재
- Feature 구현은 Domain Contract를 사용
- concrete inheritance가 부적합한 경우 interface/support composition 사용
- 3단계 초과 금지
- scanner와 예외 allowlist
- generator와 기존 source 일치
- EDU/reference sample 일치

### Overload·Override

- 기본 API는 업무 입력 중심
- 고급 기능은 typed options 또는 named policy
- overload ambiguity 없음
- raw Map/Object/varargs 금지
- 공통 template 순서 보호
- security/audit/transaction/masking 우회 불가
- hook JavaDoc와 test harness
- local/remote 동일 동작
- deprecated migration과 compile compatibility

### Property

- 변경 가능한 운영 default 전수 property화
- 거래 ID 문법·header·error schema·보안 invariant는 source contract 유지
- `@ConfigurationProperties`와 Bean Validation
- 한글 주석에 목적·기본값·단위·범위·운영 영향·reload·예시 포함
- profile은 차이만 override
- secret 원문 0건
- orphan/unused/undocumented/duplicate property 0건
- generated configuration metadata
- ADM effective config와 origin/version/audit
- invalid reload rollback과 multi-instance consistency
