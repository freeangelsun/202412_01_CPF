# 0. 이 문서의 역할

이 문서는 CPF 프로젝트 진행 중 ChatGPT가 항상 우선 참고해야 하는 기준이다.

ChatGPT는 CPF 작업 검수, Codex 요청서 작성, 완료 판정, gap 분석, 다음 작업 범위 정리 시 이 문서를 기준으로 판단한다.

이 문서는 아래를 목적으로 한다.

```text
1. CPF 최종 목표를 낮추지 않는다.
2. Codex 보고를 실제 완료로 오인하지 않는다.
3. GitHub master 또는 사용자가 제공한 로컬 파일/evidence/log 기준으로만 완료 판정한다.
4. 기능 개발 시 기능 소스, EDU 샘플, Swagger/OpenAPI, 테스트, evidence, report가 함께 가도록 한다.
5. PFW/CMN/업무 주제영역 ownership을 지킨다.
6. Spring Event를 핵심 거래 흐름 중심 기술로 남용하지 않는다.
7. SQL/Flyway/all_install/smoke/evidence 정합성을 엄격히 본다.
8. 반복 실패 시 원인을 분류하고 같은 방식으로 재요청하지 않는다.
9. 요청서는 크고 실질적으로 작성하되 완료 판정은 엄격하게 한다.
```

---

# 1. Repository 기준

CPF 기준 GitHub repository는 아래다.

```text
https://github.com/freeangelsun/202412_01_CPF
branch: master
```

CPF 검수, 요청서 작성, 완료 판정, gap 분석 시 가능하면 GitHub master 또는 사용자가 제공한 로컬 파일에서 아래 파일을 먼저 확인한다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.html
specs/sample-coverage-matrix.md
```

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 최상위·상세 통합 목표 기준서다.

주의:

```text
- GitHub master에 약 23.9MB 대용량 최종 목표파일로 존재할 수 있다.
- 브라우저/RAW가 전체를 안정적으로 보여주지 못할 수 있다.
- 짧은 기존 파일, 미반영, 삭제됨으로 단정하지 않는다.
- GitHub master 또는 로컬 파일을 직접 확인하지 못한 경우 “확인했다”고 말하지 않는다.
```

응답 시 확인 수준은 반드시 분리한다.

```text
- GitHub master 직접 확인
- 로컬 파일 직접 확인
- 실행 로그 직접 확인
- Codex 보고 기준
- 추정
- 미확인
```

Codex 보고는 주장일 뿐이다. 완료 판정은 실제 GitHub master 또는 사용자가 제공한 로컬 파일, 실행 로그, evidence 확인 기준으로만 한다.

---

# 1.1 최종 목표파일 필수 참조 기준

CPF 검수, Codex 요청서 작성, 완료 판정, gap 분석, 다음 작업 범위 선정 시 최우선 기준 파일은 아래다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
```

ChatGPT는 CPF 관련 요청을 처리할 때 아래 원칙을 따른다.

```text
1. 최종 목표 판단은 CPF_FINAL_TARGET_REQUIREMENTS.md를 기준으로 한다.
2. 기능이 목표에 포함되는지 불명확하면 목표파일 검색 또는 사용자가 제공한 로컬 파일 기준 확인을 우선한다.
3. 목표파일을 직접 확인하지 못한 경우 “목표파일 직접 확인 미수행”이라고 명시한다.
4. Codex 보고서, matrix, report에 완료라고 적혀 있어도 목표파일 기준과 충돌하면 완료로 판정하지 않는다.
5. 현재 마일스톤에서 구현하지 못한 목표는 삭제하지 않고 미구현/미검증/후순위/gap으로 남긴다.
6. 요청서 작성 시 Codex에게 로컬 checkout 기준으로 목표파일의 관련 REQ-ID, 키워드, 섹션을 검색하게 한다.
7. 목표파일 검색 없이 임의 판단으로 기능을 축소하거나 완료 처리하지 않는다.
```

목표파일은 대용량일 수 있으므로 아래를 주의한다.

```text
- 브라우저/RAW에서 일부만 보인다고 짧은 파일로 단정하지 않는다.
- 대용량 로딩 실패를 삭제/미반영으로 단정하지 않는다.
- 원본 목표파일을 임의로 축약/삭제/덮어쓰기 하지 않는다.
- 필요 시 size/hash/marker evidence를 남기고 split 문서화를 별도 작업으로 진행한다.
```

---

# 1.2 CPF 개발·검수·관리 Lifecycle 기준

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
   - Git commit/push/branch 금지 포함

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

CPF 개발 관리는 아래 산출물을 함께 본다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.html
specs/sample-coverage-matrix.md
specs/evidence/<작업일자>/*
```

위 파일들 사이의 상태값, evidence 경로, 완료/미검증 기록이 불일치하면 완료가 아니라 `실패` 또는 `재확인 필요`로 판정한다.

---

# 2. CPF 최종 목표

CPF(CoreFlow Platform Framework)는 단순 공통 유틸, 샘플, 사내 개발 표준이 아니다.

CPF는 금융권 포함 범용 업무 시스템을 구축·운영·감사·확장·검증·상용화할 수 있는 **상용 솔루션급 Core Business Platform Framework**를 목표로 한다.

CPF 최종 목표의 성격은 아래와 같다.

```text
- 공통 유틸 모음이 아님
- 단순 샘플 프로젝트가 아님
- 단순 사내 개발 표준 문서가 아님
- 업무 시스템 생성·구축·운영·감사·확장·검증을 지원하는 Core Business Platform Framework
- 금융권 수준의 보안·감사·추적·검증·운영·장애 대응·설치 검증을 고려
- MSA-first, modular-monolith-compatible 구조 지향
- 상용 솔루션 수준의 framework capability 지향
```

구현은 단계적으로 나누되 최종 목표를 낮추지 않는다.

현재 마일스톤에서 못 하는 항목은 아래 중 하나로 남긴다.

```text
미구현
후순위
착수
미검증
재확인 필요
```

목표 기준서에서 기능을 삭제하거나 목표를 낮춰서 완료처럼 보이게 하지 않는다.

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

## 3.1 상태값 의미

### 완료

완료는 아래가 실제 확인된 상태다.

```text
- 소스 구현
- 계층 연결
- SQL/Flyway/all_install 반영 필요 시 반영
- 테스트 존재 및 실행 evidence 존재
- 필요한 smoke/runtime/browser/MariaDB/broker/multi-instance 검증 수행
- report/matrix/evidence 정합성 확인
- 없는 evidence 참조 없음
- 실행하지 않은 검증을 완료로 기록하지 않음
```

### 부분 구현

소스 또는 구조가 일부 있으나 완료 조건을 충족하지 못한 상태다.

예:

```text
- 소스만 있고 테스트/evidence 없음
- source/contract만 있고 runtime 미검증
- EDU 샘플은 있으나 테스트 없음
- Swagger annotation 일부만 있음
- SQL split은 있으나 Flyway/all_install 미반영
- matrix/report 반영이 불완전함
```

### 미구현

해당 기능, 파일, 테스트, 설정, SQL, 샘플 등이 존재하지 않거나 실질 구현이 없는 상태다.

### 미검증

구현 또는 파일은 있으나 실제 검증을 수행하지 않았거나 evidence가 없는 상태다.

예:

```text
- 실제 MariaDB 신규 빈 DB full install 미실행
- 실제 broker runtime 미실행
- 실제 SFTP/SSH runtime 미실행
- 실제 browser click 미실행
- 실제 remote deploy 미실행
- 실행 로그 없음
```

### 실패

실행 결과 실패, 품질 게이트 실패, ownership 위반, evidence 불일치 등 명확한 문제 상태다.

### 재확인 필요

GitHub view/raw/evidence가 충돌하거나, 로컬에서만 확인 가능한 사안이거나, 사용자 확인이 필요한 상태다.

---

# 4. 기본 검수 원칙

사용자가 아래와 같이 말하면 검수 기준으로 처리한다.

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
2. GitHub master 또는 로컬 파일을 직접 확인한다.
3. 기준 파일을 확인한다.
4. 작업 범위 관련 파일을 확인한다.
5. 소스, 테스트, SQL, Flyway, all_install, smoke, README, report, matrix, evidence를 대조한다.
6. 기술 ownership 위반 여부를 확인한다.
7. 금지 의존성 여부를 확인한다.
8. Spring Event 남용 여부를 확인한다.
9. 실행 로그가 없으면 실행 검증 완료로 판정하지 않는다.
10. 직접 실행하지 못한 항목은 “직접 실행 미수행”으로 분리한다.
11. 검수 결과 기준으로 다음 요청서를 작성한다.
```

주의:

```text
- 정적 UI marker smoke는 browser click 완료가 아니다.
- embedded/mock broker는 real broker 완료가 아니다.
- 기존 개발 DB 확인은 신규 빈 MariaDB full install 완료가 아니다.
- dry-run은 real remote deploy 완료가 아니다.
- source smoke는 runtime smoke 완료가 아니다.
```

---

# 5. 중간 문서 운영 기준

CPF 개발 중간 단계에서는 문서 정본화를 하지 않는다.

개발이 계속 바뀌므로 문서 작업은 작업 진행, 검수, 후속 개발에 지장 없을 정도의 최소 기록만 남긴다.

중간 단계에서 필요한 기록은 아래로 제한한다.

```text
- 상태값
- 핵심 변경 소스 경로
- SQL/Flyway/all_install/smoke 반영 여부
- 실제 실행한 검증 명령
- evidence 경로
- 실행하지 못한 검증과 사유
- 남은 gap
```

아래 문서는 검수 가능한 최소 정합성만 맞춘다.

```text
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.html
specs/sample-coverage-matrix.md
```

금지 또는 지양:

```text
- 장문 설명으로 문서량 채우기
- 문구 다듬기 중심 정본화
- 상세 가이드 정본화
- PDF/HTML 정본화
- 신규 HTML 작성/수정 남발
```

신규 가이드 문서는 Markdown을 우선한다.

기존 HTML 파일은 qualityGate, 기능 매트릭스, ADM 정적 화면 smoke 등 현재 구조 유지에 필요한 최소 수정만 허용한다.

PDF 생성은 최종 정본화 단계에서만 수행한다.

---

# 6. 목표파일 대용량 처리 기준

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 대용량 단일 파일이므로 AI/검수/요청서 작성에 불리할 수 있다.

그러나 임의로 삭제, 축약, 덮어쓰기 하지 않는다.

요청서 작성 시 Codex에게 로컬 checkout 기준으로 아래를 검색하게 한다.

```text
- 목표파일 최상단 요청서 작성용 인덱스
- 관련 키워드
- REQ-ID prefix
- 관련 섹션
```

목표파일 검색 없이 임의 판단으로 구현하지 않게 한다.

향후 별도 작업으로 목표파일을 아래 구조로 split하는 것은 권장한다.

```text
docs/requirements/
REQ_MANIFEST.json
```

단, 원본 대용량 파일은 size/hash/marker evidence를 남기고 보존한 뒤 진행한다.

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
secret provider port
credential reference
token provider port
key/cert provider port
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

`exs`, `acc`, `mbr`, `bat`, `bizadm`, `adm`, `xyz` 등 업무 주제영역은 PFW/CMN capability를 사용하는 consumer, adapter, 업무 설정, 업무 구현체 역할을 담당한다.

EXS는 외부연계 기술의 소유자가 아니다.

EXS 책임 후보:

```text
대외기관 업무 설정
기관별 endpoint 업무 설정
대외 송수신 원장
기관별 adapter
대외 전문 송수신 업무 구현체
대외 unknown result 처리 구현체
대외 reconciliation 구현체
대외 파일 송수신 업무 adapter
```

ACC/MBR/BAT/BIZADM 등 다른 주제영역도 EXS를 거치지 않고 PFW/CMN capability를 직접 또는 facade/port를 통해 사용할 수 있어야 한다.

## 7.4 금지 구조

```text
PFW가 EXS/ACC/MBR/BAT/BIZADM에 의존
CMN이 업무 주제영역 구현체에 의존
ACC/MBR/BAT가 EXS 내부 기술 클래스를 공통 기능처럼 직접 사용
업무 주제영역 간 내부 기술 클래스 재사용
fixed-length parser/formatter가 EXS 전용
timeout/retry/circuit/failover가 EXS 전용
OAuth/JWT/mTLS가 EXS 전용
unknown result/reconciliation 표준 모델이 EXS 전용
broker/filetransfer/archive 기술 engine이 CMN 소유처럼 구현
```

---

# 8. Service Call / MSA 기준

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

앞으로 CPF 기능을 신규 구현하거나 수정할 때는 기능 소스만 수정하지 않는다.

해당 기능의 개발자 참조 EDU 샘플, EDU 테스트, 기능 테스트, Swagger/OpenAPI, evidence, report를 함께 갱신한다.

## 10.1 기능 완료 후보 조건

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

기능 소스만 있고 EDU/Swagger/Test/Evidence가 없으면 완료가 아니라 `부분 구현` 또는 `미검증`으로 판정한다.

## 10.2 기능 완료 판정

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

## 10.3 완료 불인정

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

앞으로 기능 검수는 “소스 구현 여부”만 보지 않는다.

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

기능 소스만 보고 완료 판정하지 않는다.

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

## 12.3 EXS EDU

EXS는 외부연계 기술 소유자가 아니라 대외업무 adapter/구현체 샘플을 가진다.

```text
exs/src/main/java/cpf/exs/edu/fixedlength
exs/src/main/java/cpf/exs/edu/endpoint
exs/src/main/java/cpf/exs/edu/auth
exs/src/main/java/cpf/exs/edu/servicecall
exs/src/main/java/cpf/exs/edu/retry
exs/src/main/java/cpf/exs/edu/unknown
exs/src/main/java/cpf/exs/edu/reconciliation
exs/src/main/java/cpf/exs/edu/ledger
exs/src/main/java/cpf/exs/edu/filetransfer
exs/src/main/java/cpf/exs/edu/archive
```

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

## 12.5 ADM/BIZADM EDU

```text
adm/src/main/java/cpf/adm/edu/operation
adm/src/main/java/cpf/adm/edu/registry
adm/src/main/java/cpf/adm/edu/servicecall
adm/src/main/java/cpf/adm/edu/batch
adm/src/main/java/cpf/adm/edu/filetransfer
adm/src/main/java/cpf/adm/edu/broker
adm/src/main/java/cpf/adm/edu/runtime
adm/src/main/java/cpf/adm/edu/scheduler

bizadm/src/main/java/cpf/bizadm/edu/auth
bizadm/src/main/java/cpf/bizadm/edu/menu
bizadm/src/main/java/cpf/bizadm/edu/api
bizadm/src/main/java/cpf/bizadm/edu/button
bizadm/src/main/java/cpf/bizadm/edu/audit
bizadm/src/main/java/cpf/bizadm/edu/download
bizadm/src/main/java/cpf/bizadm/edu/masking
```

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

## 12.7 완료 불인정

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
BIZADM 업무관리 API
ACC 업무 API
MBR 업무 API
EXS 대외연계 API
BAT 배치 실행/관리 API
XYZ 업무/EDU API
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

## 14.4 완료 불인정

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

# 16. 요청서 작성 기준

앞으로 Codex 요청서에서 기능을 신규 구현하거나 수정하라고 할 때는 아래 항목을 기본 포함한다.

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

즉, “기능 구현” 요청은 기본적으로 EDU/Swagger/Test/Evidence 동시 구현 요청으로 해석한다.

요청서에는 항상 아래 금지를 포함한다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서량 채우기식 문서화 금지
신규 HTML 작성/수정 지양
PDF 생성은 최종 정본화 단계에서만 수행
```

---

# 17. SQL / 설치 / evidence 기준

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

qualityGate는 아래를 잡아야 한다.

```text
report/matrix/evidence 불일치
없는 evidence
허용되지 않은 상태값
SKIPPED 완료 기록
sample coverage matrix 누락
CoverageCatalog-only 완료 처리
OpenAPI 누락
EDU 샘플 누락
```

---

# 18. Java/profile/deploy 기준

CPF profile 표준은 아래다.

```text
local
dev
stg
prod
```

`prd`는 표준으로 쓰지 않는다.

PFW/CMN은 실행 주체가 아니지만 공통 기본값과 zone별 override를 위해 아래와 같은 파일을 둘 수 있다.

```text
application-pfw-local/dev/stg/prod.yml
application-cmn-local/dev/stg/prod.yml
```

단, PFW/CMN은 실행 주체 설정을 강제하지 않는다.

PFW/CMN 금지:

```text
server.port 강제
datasource 강제
module-id 강제
실행 주체 설정 강제
```

실행 주체 설정은 각 업무 서비스 yml 또는 deploy env가 최종 결정한다.

실행 주체:

```text
ACC
MBR
EXS
ADM
BAT
BIZADM
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

# 19. 가비지 파일 / 빈 폴더 기준

가비지 정리 시 파일뿐 아니라 빈 폴더도 확인한다.

Git은 빈 폴더를 추적하지 않으므로 GitHub tree만으로는 확인할 수 없다.

Codex 완료 후 로컬 checkout 기준 empty directory scan evidence가 필요하다.

검사 대상:

```text
scripts/deploy
deploy/env
deploy/inventory
src/main/java
src/test/java
src/main/resources
src/test/resources
specs/evidence
각 edu package
```

가비지 삭제 기준:

```text
git grep 참조 없음
Gradle build/test 참조 없음
script 호출 없음
README/report/matrix/evidence 참조 없음
qualityGate/check script 참조 없음
자동생성 템플릿 참조 없음
같은 목적의 신규 표준 파일 존재
```

삭제 금지 또는 보류:

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_NEW_REQUEST.md
사용자가 명시한 기준 파일
직접 확인 안 된 파일
대용량 목표파일 원본
후속 필요성이 있으나 근거 부족한 파일
```

애매하면 삭제하지 말고 `재확인 필요`로 남긴다.

---

# 20. OpenAPI/Swagger 검수 기준

Swagger/OpenAPI는 전체 REST 거래/API 계약 관리 기준이다.

검수 시 아래를 확인한다.

```text
모든 Controller가 OpenAPI path로 노출되는지
operationId 중복 없는지
request/response schema 있는지
표준 오류 response 있는지
transactionGlobalId/header 설명 있는지
권한/마스킹/감사 설명 있는지
sampleId와 Swagger operation 매핑이 있는지
openApiSmoke 또는 OpenAPI coverage 검증 evidence가 있는지
qualityGate에 연결되어 있는지
```

Swagger UI 접근만으로 완료 판정하지 않는다.

---

# 21. qualityGate 기준

qualityGate는 단순 빌드 성공이 아니라 report/matrix/evidence/coverage 정합성까지 잡아야 한다.

qualityGate 필수 후보:

```text
check-feature-evidence
check-html-docs
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

# 22. 검수 답변 기준

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
| EDU 샘플 | 완료/부분 구현/... | 경로/evidence | ... |
| Swagger/OpenAPI | 완료/부분 구현/... | 경로/evidence | ... |
| 테스트 | 완료/부분 구현/... | 로그/evidence | ... |
| Evidence 정합성 | 완료/실패/... | 경로 | ... |
| Runtime 검증 | 미검증/완료 | 로그 | ... |

---

# 23. 반복 실패 대응 기준

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

# 24. 마지막 원칙

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
