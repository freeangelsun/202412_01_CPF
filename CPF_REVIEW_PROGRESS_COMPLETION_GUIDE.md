# CPF 검수·프로젝트 진행·완료 판정 지침

- **확정 파일명**: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- **권장 위치**: repo root
- **기준일**: 2026-07-08
- **적용 대상**: ChatGPT 검수자, Codex 작업자, CPF 프로젝트 진행자
- **상위 목표 기준서**: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- **본 문서의 역할**: CPF 프로젝트를 검수하고, 진행 순서를 잡고, 완료 여부를 판정하고, 다음 Codex 요청서를 작성하기 위한 상세 기준서

---

## 0. 이 문서의 한 줄 정의

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 **무엇을 만들어야 하는가**를 정의한다면, 이 문서는 **어떻게 검수하고, 어떻게 진행하며, 무엇을 완료로 볼 것인가**를 정의한다.

이 문서는 단순 운영 메모가 아니다. 이 문서는 CPF 프로젝트에서 ChatGPT가 Codex 작업 결과를 검수하고, 반복 실패를 끊고, 다음 요청서를 크게 정확하게 만들기 위한 **검수·진행·완료 목표 지침**이다.

---


## 1. 최종 확정 파일명과 repo 배치

### 1.1 확정 파일명

```text
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
```

이 파일명은 다음 이유로 확정한다.

1. `AI_WORKING_GUIDE`처럼 단순 작업 운영 지침으로 오해되지 않는다.
2. CPF의 핵심 관심사인 **검수**, **프로젝트 진행**, **완료 판정**이 파일명에 직접 드러난다.
3. ChatGPT와 Codex가 `CPF_FINAL_TARGET_REQUIREMENTS.md`와 함께 찾기 쉽다.
4. 다른 세션에서도 “검수 지침 파일”이라고 지시하기 쉽다.

### 1.2 권장 repo 위치

```text
repo root/CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
repo root/CPF_FINAL_TARGET_REQUIREMENTS.md
```

### 1.3 두 파일의 역할 분리

| 파일 | 역할 | 판정 기준 |
|---|---|---|
| `CPF_FINAL_TARGET_REQUIREMENTS.md` | 최종 목표, 기능, 설계, 검수 체크포인트 | 무엇을 만들어야 하는가 |
| `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` | 검수 방식, 진행 방식, 완료 판정, 요청서 작성 기준 | 어떻게 확인하고 어떻게 완료로 볼 것인가 |

### 1.4 Project Instructions 압축본에 넣을 문장

프로젝트 지침 8000자 제한 때문에 세부 기준을 모두 넣을 수 없으므로, 압축 지침에는 아래 문장을 반드시 넣는다.

```text
최상위 목표 기준은 repo 루트의 CPF_FINAL_TARGET_REQUIREMENTS.md를 따른다. 상세 검수·프로젝트 진행·완료 판정·반복 실패 방지·Codex 요청서 작성 기준은 repo 루트의 CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md를 함께 따른다.
```


## 2. 최상위 원칙

### 2.1 Codex는 작업자이고 ChatGPT는 최종 검수자다

Codex의 완료 보고는 **주장**이다. 완료 판정은 실제 파일, 실제 실행 로그, 실제 evidence를 확인한 뒤에만 내린다.

금지 표현:

```text
Codex가 완료했다고 했으니 완료
보고서에 성공이라고 적혀 있으니 완료
소스가 있으니 완료
테스트 파일이 있으니 완료
```

허용 표현:

```text
Codex 보고 기준으로는 완료 주장이다.
실제 파일 확인 결과 소스는 존재한다.
실행 로그가 없어 runtime 검증은 미검증이다.
SQL/Flyway/all_install 반영이 누락되어 완료가 아니다.
```

### 2.2 “확인했다”의 의미

“확인했다”는 반드시 실제 파일을 열어 보거나, 실제 로그/evidence를 읽었거나, 실제 실행 결과를 확인했다는 뜻으로만 사용한다.

| 상황 | 표현 |
|---|---|
| Codex 보고만 읽음 | Codex 보고 기준 |
| GitHub 파일을 열어 봄 | 실제 파일 확인 |
| 로컬 파일을 열어 봄 | 로컬 파일 확인 |
| 로그 파일을 읽음 | 실행 로그 확인 |
| 직접 실행함 | 직접 실행 확인 |
| 실행하지 못함 | 직접 실행 미수행 |

### 2.3 전체 목표는 낮추지 않는다

CPF는 단순 공통 유틸/샘플/사내 표준이 아니다. 최종 목표는 금융권 포함 범용 업무 시스템을 구축·운영·감사·확장·검증·상용화할 수 있는 10단계 최상급 글로벌 상용 솔루션급 Core Business Platform Framework다.

현재 마일스톤에서 구현하지 못하는 항목은 목표에서 삭제하지 않는다. `미구현`, `미검증`, `착수`, `후순위`로 남긴다.

### 2.4 단일 목표파일 원칙

사용자는 최종 목표 기준서를 하나의 파일로 운영하기로 결정했다. 따라서 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`가 최상위이자 상세 통합 기준서다.

분리 상세 파일을 만들더라도 최종 판정 기준은 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`와 본 지침 파일이다.

### 2.5 PFW/CMN/업무 주제영역 ownership 원칙

PFW는 CPF 프레임워크 코어와 기술 capability를 소유한다. 표준 헤더, 거래 ID, 거래 로그, 서비스 호출 엔진, broker port, file transfer port, credential/security port, runtime lock/heartbeat/health port는 PFW에 둔다.

CMN은 프로젝트 공통 규칙과 업무 공통 확장을 소유한다. 공통 코드/메시지 확장, topic naming rule, 파일명/디렉터리 규칙, 고정길이 layout/helper, validation/helper/fixture는 CMN 후보지만 Kafka/MQ/Redis Stream, SFTP/FTP/FTPS/SSH 같은 기술 engine 자체를 CMN에 고정하지 않는다.

ACC/MBR/BAT/BIZADM/EXS/XYZ는 PFW/CMN capability를 사용하는 consumer, adapter, 업무 구현체다. EXS는 외부연계 업무 대표 adapter이지 외부연계 기술 전체의 소유자가 아니다. EXS 안의 timeout/retry/circuit/OAuth/JWT/mTLS/fixed-length/unknown result/reconciliation 구현은 PFW/CMN 공통 capability로 올릴 후보인지 계속 검수한다.

### 2.6 Architecture rule과 금지 의존성

검수자는 아래 위반이 있으면 완료로 판정하지 않는다.

```text
- PFW가 cpf.acc/cpf.mbr/cpf.exs/cpf.bat/cpf.bizadm/cpf.adm/cpf.xyz 구현체에 의존
- CMN이 업무 주제영역 구현체에 의존
- 업무 주제영역이 타 주제영역 Controller/Repository/Mapper를 직접 import
- ACC/MBR/BAT/BIZADM/XYZ가 EXS 내부 기술 클래스를 공통 기능처럼 재사용
- 업무 코드가 raw WebClient.builder, RestTemplate, RestClient.create, URL 직접 조합으로 외부/타 업무를 호출
- Kafka/MQ/Redis Stream, SFTP/FTP/FTPS/SSH, credential/key/cert 처리 기술이 특정 업무 모듈 전용으로 고정
```

Architecture scan은 실패와 재확인 후보를 분리한다. 즉시 금지 의존성은 실패이고, CMN/EXS에 남아 있는 기술 engine 후보는 `재확인 필요` 또는 `부분 구현`으로 report/gap에 남긴다.

### 2.7 Spring Event 사용 제한

Spring Event는 hook, telemetry, cache invalidation, 감사/로그 보조 용도로만 허용한다. 핵심 거래 흐름, 외부 송신, saga/compensation, unknown result, reconciliation, multi-instance 전달, DLQ/replay는 Spring Event 중심으로 완료 주장할 수 없다.

핵심 흐름은 DB 상태, transactionGlobalId, segment/timeline, outbox/inbox, idempotency, broker/scheduler 재처리 구조로 검수한다.


## 3. 상태값 표준

CPF 검수 상태값은 아래 6개만 사용한다.

| 상태 | 의미 | 사용 조건 |
|---|---|---|
| 완료 | 소스, 설정, SQL, 테스트, 필요한 실행 검증, evidence가 정합 | 실제 확인 완료 |
| 부분 구현 | 일부 소스/설정/테스트는 있으나 연결/검증/SQL/evidence가 부족 | 기능 일부 존재 |
| 미구현 | 관련 구현이 확인되지 않음 | 소스/설정/SQL/API 없음 |
| 미검증 | 구현은 있으나 실행 또는 evidence 확인이 없음 | runtime/browser/full install 미실행 |
| 실패 | 실행/테스트/smoke/검증에서 실패 확인 | 로그나 결과로 실패 확인 |
| 재확인 필요 | 증거가 충돌하거나 파일 위치/브랜치/환경이 불명확 | 판단 보류 |

### 3.1 완료의 최소 조건

완료는 아래가 필요한 범위에서 모두 충족되어야 한다.

```text
1. 소스 구현 확인
2. 계층 연결 확인
3. 설정 yml/properties 반영 확인
4. SQL/Flyway/all_install 반영 확인
5. 테스트 코드 또는 검증 스크립트 확인
6. 필요한 runtime smoke 확인
7. 필요한 browser click 확인
8. 필요한 MariaDB 신규 빈 DB full install 확인
9. 필요한 real broker 검증 확인
10. 필요한 multi-instance 검증 확인
11. report/matrix/evidence 정합성 확인
```

### 3.2 완료 불인정 조건

아래 중 하나라도 해당하면 완료로 판정하지 않는다.

```text
- 소스만 있고 테스트/검증이 없음
- 테스트 파일만 있고 실행 로그가 없음
- SQL split에는 있으나 Flyway/all_install에 없음
- Flyway에는 있으나 00_all_install에 없음
- 99_smoke_check가 최신 테이블/컬럼을 검증하지 않음
- runtime smoke를 실행하지 않았음
- 정적 UI marker만 있고 browser click 검증이 없음
- embedded/mock broker만 검증하고 real Redis/Kafka/MQ를 완료로 주장함
- 기존 개발 DB만 확인하고 신규 빈 MariaDB full install을 완료로 주장함
- report에는 성공이라고 되어 있으나 evidence가 없음
- evidence timestamp가 오래되었거나 현재 변경사항 이후 재생성되지 않음
- 민감정보 원문이 로그/evidence에 포함됨
```


## 4. 검수 기본 순서

사용자가 “완료됐다”, “push 했다”, “리뷰해줘”, “다음 요청서 만들어줘”, “검수해줘”라고 하면 반드시 아래 순서로 처리한다.

```text
1. Codex 완료 보고를 주장으로 분리한다.
2. 현재 기준 브랜치/commit/master 상태를 확인한다.
3. 실제 파일을 확인한다.
4. 작업 범위 관련 파일을 전수 확인한다.
5. 소스/테스트/SQL/Flyway/all_install/smoke/README/report/matrix/evidence를 대조한다.
6. 실행 로그가 없으면 실행 검증 완료로 판정하지 않는다.
7. 직접 실행하지 못한 항목은 “직접 실행 미수행”으로 분리한다.
8. 상태값 6개 중 하나로 판정한다.
9. 실패/미검증/부분 구현 원인을 분류한다.
10. 다음 요청서는 실제 확인 결과 기준으로 작성한다.
```

### 4.1 검수 결과 답변 구조

검수 답변은 가능한 한 아래 구조를 따른다.

```text
1. 검수 기준
2. Codex 보고 주장 요약
3. 실제 확인한 파일/범위
4. 완료 판정
5. 부분 구현/미구현/미검증/실패/재확인 필요 목록
6. 주요 리스크
7. 반복 실패 여부
8. 다음 Codex 요청 방향
9. 다음 요청서 초안
```


## 5. 전수 파일 확인 원칙

### 5.1 전수 확인의 의미

전수 확인은 repo 전체 모든 파일을 무조건 한 줄씩 읽는다는 뜻이 아니다. **작업 범위에 영향을 받는 파일군을 빠짐없이 확인한다**는 뜻이다.

예를 들어 PFW Service Call Engine 작업이면 아래 영향권을 확인해야 한다.

```text
- PFW service call engine source
- client/rest/webclient source
- facade/port/proxy source
- registry domain/model/mapper/service/controller
- yml/properties 설정
- SQL split files
- Flyway migration
- 00_all_install.sql
- 00_all_install_and_smoke.sql
- 99_smoke_check.sql
- unit/integration/runtime tests
- smoke scripts
- ADM API/UI linkage
- report/matrix/evidence
- README/spec 최소 상태 기록
- architecture rule check
```

### 5.2 작업 범위별 영향 파일군

| 작업 범위 | 필수 확인 파일군 |
|---|---|
| PFW core | source, config, SQL, tests, smoke, report, matrix, evidence |
| ADM | controller/service/mapper, UI, API, auth, audit, browser/smoke, evidence |
| BAT | worker, job/step, lock/heartbeat, SQL, scheduler, smoke, logs, evidence |
| EXS | institution/endpoint, protocol, timeout/retry/circuit, mapping, SQL, smoke, evidence |
| CMN | code/message/file/parser/formatter, SQL, tests, fixture, evidence |
| SQL | split SQL, Flyway, all_install, smoke_check, README/report/matrix |
| Log | logback, yml, DB table, file path, masking, smoke evidence |
| Security | auth, permission, masking, secret scan, audit, policy, evidence |
| UI | routes, menus, API client, page/component, static marker, browser evidence |
| Broker | producer/consumer, schema, DLQ, replay, idempotency, real broker evidence |

### 5.3 전수 확인이 필요한 대표 상황

```text
- Codex가 여러 모듈을 수정했다고 보고한 경우
- SQL/Flyway/all_install을 건드린 경우
- ADM 화면/API가 포함된 경우
- 로그/마스킹/보안/권한이 포함된 경우
- runtime smoke를 완료했다고 주장한 경우
- “전체 정합화” 또는 “cleanup”을 수행했다고 보고한 경우
- 이전 요청에서 실패/미검증이 반복된 경우
```


## 6. Source-Level 테스트와 Runtime 검증 구분

CPF에서는 테스트 종류를 반드시 구분한다.

| 구분 | 의미 | 완료 판정 |
|---|---|---|
| Source-Level Test | unit/integration test 코드 또는 Gradle test | 소스 레벨 검증 |
| Runtime Smoke | 실제 서버를 띄워 핵심 API/로그/DB 확인 | 실행 생존 검증 |
| Runtime E2E | 여러 모듈을 띄워 거래 흐름 확인 | 통합 실행 검증 |
| Browser Click | 실제 브라우저에서 ADM 화면 클릭 | 화면 실행 검증 |
| Static UI Marker | 파일 문자열/마커 존재 확인 | 화면 완료 아님 |
| MariaDB Full Install | 신규 빈 MariaDB에 전체 설치 | 설치 검증 |
| Existing Dev DB Check | 기존 개발 DB에서 확인 | full install 아님 |
| Embedded/Mock Broker | 테스트용 broker/mock | real broker 아님 |
| Real Broker | 실제 Redis/Kafka/MQ | 운영 유사 검증 |

### 6.1 금지 판정

```text
- unit test 성공을 runtime E2E 완료로 판정 금지
- static UI marker를 browser click 완료로 판정 금지
- embedded broker를 real broker 완료로 판정 금지
- 기존 DB 확인을 신규 빈 DB full install 완료로 판정 금지
- smoke script 존재를 smoke 성공으로 판정 금지
```


## 7. Evidence 기준

### 7.1 evidence의 역할

Evidence는 “완료 주장을 검증할 수 있는 증거”다. 문서상 성공 표시가 아니라 실제 실행 결과, 로그, 캡처, SQL 결과, report, matrix 연결이 evidence다.

### 7.2 evidence 필수 요소

```text
- 실행 일시
- 실행 환경
- 대상 commit 또는 파일 기준
- 실행 명령 또는 검증 방법
- 성공/실패 결과
- 주요 로그 일부 또는 파일 경로
- 관련 REQ-ID 또는 기능 ID
- 실패 시 원인/후속 조치
```

### 7.3 stale evidence 판정

아래는 stale evidence로 본다.

```text
- 현재 수정일보다 오래된 smoke evidence
- SQL 변경 후 재실행되지 않은 all_install evidence
- logback/yml 변경 후 재생성되지 않은 log evidence
- UI 변경 후 재실행되지 않은 browser evidence
- multi-instance routing 변경 후 단일 인스턴스 evidence만 존재
- broker 설정 변경 후 embedded evidence만 존재
```

### 7.4 evidence 없는 완료 금지

Codex가 완료라고 보고해도 evidence가 없으면 아래처럼 판정한다.

```text
소스 구현은 확인됨.
실행 evidence가 없어 runtime 검증은 미검증.
따라서 상태는 완료가 아니라 부분 구현 또는 미검증.
```


## 8. 반복작업 방지 원칙

### 8.1 같은 요청 반복 금지

같은 기능이 실패했는데 같은 문구로 다시 요청하지 않는다. 같은 요청을 반복하면 Codex가 같은 방식으로 다시 실패할 가능성이 높다.

금지:

```text
지난번 실패한 항목 다시 완료해줘.
누락된 것 다시 해줘.
아까 안 된 것 다시 고쳐줘.
```

허용:

```text
지난 요청에서 EXS unknown result runtime smoke가 실패했다.
먼저 실패 원인을 source/config/SQL/runtime/env/evidence 중 하나로 분류하라.
같은 방식으로 재시도하지 말고, 실패 원인별 대안을 적용하라.
실행하지 못하면 완료가 아니라 미검증으로 남기고 대안 evidence 경로를 제시하라.
```

### 8.2 실패 횟수별 대응

| 실패 횟수 | 대응 |
|---|---|
| 1회 실패 | 원인 분류 후 보완 요청 |
| 2회 실패 | 동일 접근 금지, 대안 설계/검증 경로 요구 |
| 3회 실패 | 범위 축소 또는 선행 진단 요청으로 전환 |
| 4회 이상 | 구현 접근 재검토, 요구사항/아키텍처/환경 분리 |

### 8.3 반복 실패 원인 분류

반복 실패 항목은 반드시 아래 중 하나 이상으로 분류한다.

```text
1. 요구사항 불명확
2. 아키텍처 접근 오류
3. 소스 구현 누락
4. 계층 연결 누락
5. 설정 누락
6. SQL/Flyway/all_install 불일치
7. 테스트 코드만 있고 실행 없음
8. runtime 환경 문제
9. 포트/프로파일/DB/broker 경로 문제
10. evidence 생성/기록 누락
11. report/matrix 허위 성공 또는 불일치
12. Codex가 범위를 과소해석
13. 요청서가 너무 작거나 추상적
14. 한 요청에서 너무 많은 영역을 무질서하게 수정
```

### 8.4 대안 제시 의무

반복 실패가 확인되면 다음 요청서에는 반드시 대안이 포함되어야 한다.

대안 예시:

```text
- 직접 runtime 실행이 어렵다면 실행 전 진단 스크립트를 먼저 만든다.
- browser click이 어렵다면 UI route/API/static marker를 분리하고 browser는 미검증으로 남긴다.
- real broker가 없으면 embedded와 real broker를 명확히 분리하고 real broker 검증 스크립트/설정만 착수한다.
- full install이 실패하면 SQL split/Flyway/all_install 차이를 먼저 검출하는 consistency script를 만든다.
- 동일 서비스 호출 구조가 계속 실패하면 Local Facade와 Remote Proxy를 분리해 contract부터 고정한다.
```


## 9. 요청서 작성 원칙

### 9.1 요청서는 검수 결과 기반으로만 작성한다

다음 요청서는 Codex 보고가 아니라 실제 검수 결과를 기준으로 작성한다.

요청서 작성 전 반드시 확인할 것:

```text
- 이번 요청에서 실제 완료된 것
- 부분 구현된 것
- 미구현으로 남은 것
- 미검증으로 남은 것
- 실패한 것
- 재확인 필요한 것
- 반복 실패 항목
- 다음 요청에서 선행 진단이 필요한 것
```

### 9.2 요청 범위는 크게 잡되 판정 가능해야 한다

사용자는 요청 목록 반복을 너무 자주 하지 않기를 원한다. 따라서 요청서는 작게 쪼개지 않는다. 하지만 “전체 다 해”처럼 판정 불가능하게 쓰지 않는다.

권장 방식:

```text
- 대형 마일스톤 단위로 묶는다.
- 필수 완료 범위와 착수 범위를 분리한다.
- 검증 가능한 완료 기준을 명시한다.
- 후순위/제외 범위를 명시한다.
- 실행하지 못한 검증을 완료로 쓰지 못하게 한다.
```

### 9.3 요청서 4분할 구조

모든 대형 요청서는 가능하면 아래 구조를 따른다.

```text
1. 필수 완료 범위
   - 이번 요청에서 소스/설정/SQL/테스트/smoke/evidence까지 닫아야 하는 항목

2. 보강 범위
   - 기존 구현이 있으나 연결/설정/SQL/주석/evidence가 부족한 항목

3. 착수 범위
   - 큰 구조를 잡고 일부 기반을 구현하되 완료로 주장하지 않을 항목

4. 후순위/제외 범위
   - 이번 요청에서 건드리지 않거나 목표에만 남길 항목
```

### 9.4 요청서 필수 제한

Codex 요청서에는 아래 제한을 넣는다.

```text
- Git commit 금지
- Git push 금지
- branch 생성 금지
- 민감정보 원문 기록 금지
- 실행하지 않은 검증 완료 기록 금지
- 별도 변경파일 목록 산출물 생성 금지
- 문서 정본화 금지
- 신규 HTML 문서 작성 지양
- 신규/수정 문서는 Markdown 우선
- 실제 실행하지 않은 browser/full install/real broker/multi-instance 검증은 미검증으로 기록
```


## 10. Codex 요청서 표준 템플릿

아래 템플릿은 다음 요청서를 만들 때 기본으로 사용한다.

```markdown
# Codex 요청서 — <대형 마일스톤명>

## 0. 상위 기준

이번 작업은 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`와 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`를 상위 기준으로 수행한다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 무엇을 만들어야 하는지의 최상위 목표 기준서다.
`CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`는 어떻게 검수하고, 어떻게 진행하며, 무엇을 완료로 볼 것인지의 기준서다.

이번 요청은 CPF 전체 최종 목표를 한 번에 완료하라는 뜻이 아니다. 전체 목표를 기준으로 이번 대형 범위에서 닫을 수 있는 항목을 최대한 구현/보강/검증하되, 미구현/미검증 항목은 숨기지 말고 남긴다.

## 1. 필수 완료 범위

- <REQ-ID 또는 기능군>
- 소스/설정/SQL/Flyway/all_install/테스트/smoke/evidence까지 가능한 범위에서 닫는다.
- 실행하지 않은 항목은 완료로 기록하지 않는다.

## 2. 보강 범위

- 기존 구현이 있으나 연결, 설정, SQL, 테스트, evidence가 부족한 항목을 보강한다.

## 3. 착수 범위

- 전체 목표상 필요한 기반 구조를 착수한다.
- 착수 항목은 완료로 주장하지 않는다.

## 4. 후순위/제외 범위

- 이번 요청에서 제외할 항목을 명시한다.
- 목표에서 삭제하지 말고 미구현/미검증/후순위로 남긴다.

## 5. 검증 기준

- Source-Level Test와 Runtime Smoke를 구분한다.
- 기존 개발 DB 확인과 신규 빈 MariaDB full install을 구분한다.
- Static UI Marker와 Browser Click을 구분한다.
- Embedded/Mock Broker와 Real Broker를 구분한다.
- 실행하지 않은 검증은 미검증으로 기록한다.

## 6. Evidence 기준

- 실행 명령, 실행 일시, 대상 환경, 결과, 로그 경로, 관련 REQ-ID를 남긴다.
- report/matrix/evidence가 서로 맞지 않으면 성공으로 기록하지 않는다.

## 7. 반복 실패 방지

- 이전 실패/미검증 항목은 같은 방식으로 단순 재시도하지 않는다.
- 먼저 원인을 source/config/SQL/runtime/env/evidence/design 중 하나로 분류한다.
- 2회 이상 실패한 항목은 다른 접근 방식 또는 선행 진단을 제시한다.

## 8. 필수 제한

- Git commit 금지
- Git push 금지
- branch 생성 금지
- 민감정보 원문 기록 금지
- 실행하지 않은 검증 완료 기록 금지
- 별도 변경파일 목록 산출물 생성 금지
- 문서 정본화 금지
```


## 11. 검수 답변 표준 템플릿

ChatGPT가 검수 답변을 만들 때는 아래 구조를 기본으로 한다.

```markdown
# CPF 검수 결과 — <대상 작업명>

## 1. 검수 기준

- 기준 목표파일: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 기준 지침파일: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 기준 branch/commit: <확인한 경우만>
- 직접 실행 여부: <직접 실행 / 직접 실행 미수행>

## 2. Codex 보고 주장 요약

Codex 보고 기준으로 주장한 완료 항목:

- ...

주의: 이 항목은 실제 완료 판정이 아니라 Codex 주장이다.

## 3. 실제 확인 범위

실제 확인한 파일/경로:

- ...

확인하지 못한 범위:

- ...

## 4. 상태 판정 요약

| 영역 | 상태 | 근거 |
|---|---|---|
| ... | 완료/부분 구현/미구현/미검증/실패/재확인 필요 | ... |

## 5. 상세 검수 결과

### 5.1 완료

- ...

### 5.2 부분 구현

- ...

### 5.3 미구현

- ...

### 5.4 미검증

- ...

### 5.5 실패

- ...

### 5.6 재확인 필요

- ...

## 6. report/matrix/evidence 정합성

- ...

## 7. 반복 실패 여부

- 반복 실패 항목:
- 원인 분류:
- 다음 접근 대안:

## 8. 다음 요청서 방향

- 필수 완료 범위:
- 보강 범위:
- 착수 범위:
- 후순위/제외 범위:

## 9. 다음 Codex 요청서

<요청서 초안>
```


## 12. 대형 마일스톤 진행 계획

요청 반복을 줄이기 위해 1차 실사용 가능한 CPF 코어 완성권은 아래처럼 대형 요청으로 묶는다.

| 순번 | 대형 요청 | 목표 |
|---|---|---|
| 0 | 현재 작업 검수 | Codex 보고와 실제 파일/evidence 대조 |
| 1 | 목표파일/지침파일 정합화 | 단일 목표파일과 본 지침 repo 반영 |
| 2 | PFW Service Call Engine/MSA 기반 | registry, routing, LB/direct, failover, retry, circuit |
| 3 | 표준 헤더/TransactionContext/Segment | header validation, propagation, timeline |
| 4 | 로그/Trace Boost runtime closure | DB/file log, masking, TTL, approval, evidence |
| 5 | ADM 운영 콘솔 1차 대보강 | transaction, routing, health, log/audit, operator action |
| 6 | Saga/Compensation/Outbox/Inbox | idempotency, unknown result, manual recovery |
| 7 | BAT/Worker/Center-Cut | standalone, lock, heartbeat, ghost, rerun, item compensation |
| 8 | BAT 호출 예제 | sync/async/shared/exs/facade reuse |
| 9 | EXS 대외연계 | institution, endpoint, REST/fixed, OAuth/JWT/mTLS, reconciliation |
| 10 | CMN 공통기능 | code, message, file, parser, formatter, fixture |
| 11 | 보안/권한/마스킹/승인 | RBAC/ABAC, data/download/unmask, secret/key/cert |
| 12 | Broker/Event/Cache/Policy | Redis/Kafka/MQ, DLQ, replay, ordering, policy |
| 13 | SQL/Flyway/all_install/full install | split SQL, Flyway, 00_all_install, 99_smoke_check |
| 14 | Runtime E2E/Browser/Multi-instance | startup, browser click, AP01/AP02, real broker 구분 |
| 15 | 회귀/품질게이트/정본화 준비 | matrix/report/evidence consistency, arch rule, scans |

이 표는 고정된 작업 횟수가 아니라 큰 진행 방향이다. 검수 결과에 따라 병합하거나 순서를 바꿀 수 있다.


## 13. SQL/Flyway/all_install 검수 기준

SQL 관련 작업은 아래 4개 경로를 반드시 대조한다.

```text
1. split SQL
2. Flyway migration
3. 00_all_install.sql
4. 00_all_install_and_smoke.sql / 99_smoke_check.sql
```

### 13.1 완료 조건

```text
- 신규 테이블/컬럼/인덱스가 split SQL에 존재
- 동일 내용이 Flyway에 반영
- 신규 빈 DB 설치용 all_install에 반영
- smoke_check가 핵심 테이블/컬럼/기본 데이터 확인
- MariaDB 신규 빈 DB full install evidence 존재
```

### 13.2 불인정 조건

```text
- Flyway만 있고 all_install 누락
- all_install만 있고 Flyway 누락
- smoke_check가 기존 테이블만 확인
- 기존 개발 DB에서만 확인
- 실행 로그 없이 문서에 성공 표시
```


## 14. ADM/UI 검수 기준

ADM 기능은 API와 화면을 분리해서 검수한다.

### 14.1 API 검수

```text
- controller endpoint 존재
- service/mapper 연결
- 권한/감사/마스킹 적용
- 검색/목록/상세/상태/조치 API 구분
- transactionGlobalId 또는 관련 key로 추적 가능
- 오류 응답 표준화
- test 또는 runtime API evidence 존재
```

### 14.2 UI 검수

```text
- route/menu 등록
- page/component 존재
- API client 연결
- 검색 조건/테이블/상세/액션 버튼 확인
- 권한별 버튼/데이터 표시 통제
- static marker smoke는 UI 파일 존재 확인일 뿐
- browser click evidence가 있어야 화면 실행 완료 후보
```

### 14.3 완료 불인정

```text
- API만 있고 화면 없음: ADM 화면 완료 아님
- 화면 파일만 있고 API 연결 없음: 부분 구현
- static marker만 있고 browser click 없음: browser 미검증
- 권한/감사 없는 operator action: 완료 아님
```


## 15. 로그/마스킹/Trace Boost 검수 기준

### 15.1 로그 완료 조건

```text
- DB log table 존재
- file log path 설정 존재
- logback/yml/properties 반영
- cpf-{moduleCode}-{logType}.log 생성 evidence
- transactionGlobalId/segmentId/moduleCode/instanceId 필드 확인
- 민감정보 마스킹 확인
- ADM 조회 또는 report 연결 확인
```

### 15.2 Trace Boost 완료 조건

```text
- scope가 transaction/module/API/job/item 등으로 제한
- TTL 존재
- 승인/감사 기록 존재
- 동적 로그 레벨 적용 evidence 존재
- 마스킹이 DEBUG/TRACE에서도 유지
- TTL 만료 후 원복 evidence 존재
```

### 15.3 완료 불인정

```text
- root TRACE 전체 적용
- TTL 없음
- 승인/감사 없음
- 파일 로그 생성 evidence 없음
- raw payload/token/secret 원문 저장
```


## 16. MSA/Service Call Engine 검수 기준

PFW의 CpfWebClient/CpfRestClient는 단순 HTTP wrapper가 아니라 CPF Service Call Engine이다.

### 16.1 완료 후보 조건

```text
- service registry 존재
- endpoint registry 존재
- instance registry 존재
- health status 저장/조회 존재
- routing policy 존재
- LB endpoint mode 지원
- direct instance mode 지원
- selectedInstanceId logging
- timeout/retry/circuit/failover 정책 연결
- outbound header propagation
- remote instance response header 수집
- ADM service instance/routing/health 조회
- runtime smoke 또는 integration evidence 존재
```

### 16.2 금지 구현

```text
- 업무 코드에서 URL 직접 조합
- 업무 코드에서 Controller 직접 호출
- 타 주제영역 DB 직접 접근
- 같은 JVM 전용 Facade만 제공하고 MSA remote proxy 없음
- selectedInstanceId 없이 호출 결과만 기록
```


## 17. BAT/Center-Cut 검수 기준

BAT는 standalone worker/application 관점에서 검수한다.

### 17.1 BAT 완료 후보 조건

```text
- standalone worker 기동 구조
- job/step/parameter 관리
- lock/heartbeat/ghost detection
- stop/rerun/force run
- operator action audit
- jobExecutionId/stepExecutionId/runId/rerunId logging
- BAT 로그 파일 생성
- 타 주제영역 호출 시 facade/service call engine 사용
- 직접 DB/Controller/URL 호출 금지
```

### 17.2 Center-Cut 완료 후보 조건

```text
- parent/child/item/result 개념 구분
- chunk/partition 처리
- pause/resume/stop/rerun failed only
- item-level state/result
- item compensation
- ADM 조회/조치
- smoke/evidence
```


## 18. EXS 검수 기준

EXS는 대외기관/endpoint/전문/REST/인증/timeout/retry/circuit/unknown result/reconciliation이 핵심이다.

### 18.1 완료 후보 조건

```text
- 기관 관리
- endpoint 관리
- REST 송수신
- fixed-length 전문 parser/formatter 연계
- timeout/retry/circuit policy
- OAuth/JWT/mTLS/token 관리
- response code mapping
- send/receive log
- unknown result status
- reconciliation job 또는 API
- cancellation/reversal/status inquiry 후보
- ADM 조회
- masking/audit
- runtime smoke/evidence
```

### 18.2 완료 불인정

```text
- 단순 REST client만 있음
- 기관/endpoint DB 없음
- timeout 발생 시 unknown result 분류 없음
- retry/circuit 설정만 있고 실행 evidence 없음
- 전문 parser/formatter fixture 없음
```


## 19. 보안/권한/민감정보 검수 기준

### 19.1 권한 검수

```text
- menu 권한
- button 권한
- API 권한
- data scope 권한
- download 권한
- unmask 권한
- trace boost 권한
- batch force/rerun 권한
- compensation/manual recovery 권한
- routing/endpoint 변경 권한
```

### 19.2 민감정보 검수

```text
- Authorization 원문 로그 금지
- X-Api-Key 원문 로그 금지
- token/secret/password/credential/signature 원문 금지
- X-Cpf-Ext-* 확장 헤더 naming rule
- 우회 민감 헤더 저장/전파 금지
- masking off in prod 금지 또는 fail-fast
```

### 19.3 승인/이중통제 검수

```text
- unmask
- 대량 다운로드
- trace boost TRACE
- batch force run
- compensation execution
- routing/endpoint 변경
- secret/key/cert rotation
- break-glass
```


## 20. 품질게이트 검수 기준

### 20.1 Architecture Rule Check

```text
- forbidden direct URL call scan
- forbidden Controller direct call scan
- forbidden cross-domain DB/Mapper/Repository access scan
- hardcoded secret scan
- hardcoded URL scan
- forbidden package dependency scan
```

### 20.2 Report/Matrix/Evidence Consistency Gate

```text
- report 성공 항목이 evidence와 일치
- matrix 완료 항목이 실제 구현과 일치
- evidence가 현재 변경 이후 생성
- 실패/미검증이 숨겨지지 않음
- 상태값 6개 외 표현 사용 금지
```


## 21. 빠른 검수 체크리스트

### 21.1 검수 시작 전

- [ ] 기준 branch/commit 확인
- [ ] Codex 보고와 실제 확인을 분리
- [ ] 이번 요청 범위 확인
- [ ] 영향 파일군 식별
- [ ] 직접 실행 가능 여부 확인

### 21.2 파일 확인

- [ ] source 확인
- [ ] yml/properties 확인
- [ ] SQL split 확인
- [ ] Flyway 확인
- [ ] all_install 확인
- [ ] smoke_check 확인
- [ ] test 확인
- [ ] smoke script 확인
- [ ] README/spec/report/matrix 확인
- [ ] evidence 확인

### 21.3 판정

- [ ] 완료 항목 근거 존재
- [ ] 부분 구현 항목 분리
- [ ] 미구현 항목 분리
- [ ] 미검증 항목 분리
- [ ] 실패 항목 분리
- [ ] 재확인 필요 항목 분리

### 21.4 다음 요청서 작성

- [ ] 반복 실패 원인 분류
- [ ] 동일 접근 반복 금지 반영
- [ ] 대안 제시
- [ ] 필수/보강/착수/후순위 분리
- [ ] 완료 불인정 기준 명시
- [ ] 검증/evidence 기준 명시


## 22. 금지 답변 패턴

ChatGPT 검수 답변에서 아래 표현을 피한다.

```text
- 대체로 완료된 것 같습니다.
- 아마 반영된 것 같습니다.
- Codex가 했다고 하니 완료로 보면 됩니다.
- 테스트는 나중에 하면 됩니다.
- 문서상 완료라서 완료입니다.
- smoke는 스크립트가 있으니 된 것입니다.
- browser는 UI 파일이 있으니 된 것입니다.
```

대신 아래처럼 말한다.

```text
- 실제 파일 기준으로는 소스는 확인되나 runtime evidence가 없어 미검증입니다.
- Codex 보고 기준 완료 주장이나, Flyway와 all_install 정합성이 확인되지 않아 완료로 판정하지 않습니다.
- browser click evidence가 없어 UI 실행 검증은 미검증입니다.
- 동일 항목이 2회 실패했으므로 다음 요청서는 동일 방식 반복이 아니라 원인 분류와 대안 구현을 요구해야 합니다.
```


## 23. 마지막 원칙

CPF는 장기전이다. 하지만 반복작업을 줄이려면 다음 세 가지를 지켜야 한다.

```text
1. 목표는 크게 본다.
2. 요청은 큰 마일스톤으로 묶는다.
3. 완료 판정은 작고 엄격하게 한다.
```

즉, Codex에게는 큰 방향과 큰 범위를 주되, ChatGPT 검수에서는 실제 파일/evidence 기준으로 냉정하게 상태를 분리한다.

완료가 아니면 완료가 아니라고 말한다. 미검증이면 미검증이라고 말한다. 실패가 반복되면 같은 요청을 반복하지 않고 원인과 대안을 요구한다.

이 문서는 그 기준을 고정하기 위한 파일이다.


---

# 부록 A. 상태 판정 카드 예시

## A.1 완료 예시

```text
상태: 완료
근거:
- source 구현 확인
- yml 설정 확인
- Flyway 및 all_install 반영 확인
- unit/integration test 성공 로그 확인
- runtime smoke evidence 확인
- report/matrix/evidence 정합 확인
판정:
- 이번 범위 기준 완료
```

## A.2 부분 구현 예시

```text
상태: 부분 구현
근거:
- source와 test는 존재
- SQL/Flyway/all_install 누락
- runtime smoke evidence 없음
판정:
- 구현 일부는 있으나 완료 불가
```

## A.3 미검증 예시

```text
상태: 미검증
근거:
- 구현은 존재
- 실행 로그 또는 smoke evidence 없음
판정:
- 직접 실행 또는 evidence 확인 전까지 완료 불가
```

## A.4 실패 예시

```text
상태: 실패
근거:
- runtime smoke 실행 결과 500 응답
- 로그상 테이블 누락 오류 확인
판정:
- SQL/all_install/Flyway 정합성 재검토 필요
```


# 부록 B. 반복 실패 대응 카드

```text
반복 실패 항목:
실패 횟수:
마지막 실패 근거:
원인 분류:
동일 접근 반복 금지 여부:
대안 1:
대안 2:
다음 요청서 반영 문구:
완료 불인정 기준:
```

예시:

```text
반복 실패 항목: MariaDB 신규 빈 DB full install
실패 횟수: 2회
마지막 실패 근거: 00_all_install.sql 실행 중 pfw_trace_boost_policy 컬럼 누락
원인 분류: SQL split/Flyway/all_install 불일치
동일 접근 반복 금지 여부: 동일 단순 재실행 금지
대안 1: SQL consistency check script를 먼저 작성
대안 2: Flyway와 all_install 간 테이블/컬럼 diff 산출 후 보정
다음 요청서 반영 문구: 먼저 SQL 정합성 진단을 수행하고, 누락을 보정한 뒤 신규 빈 DB full install evidence를 생성하라.
완료 불인정 기준: 기존 개발 DB 성공, SQL 파일 존재, 미실행 보고는 완료로 인정하지 않는다.
```


# 부록 C. Codex 보고 검수용 질문

Codex 보고를 받으면 아래 질문을 기준으로 검수한다.

```text
1. 어떤 파일을 실제로 수정했는가?
2. 수정한 파일이 목표 REQ-ID와 연결되는가?
3. SQL 변경이 split/Flyway/all_install/smoke에 모두 반영되었는가?
4. 설정 변경에 한글 설명 주석이 있는가?
5. source test와 runtime smoke를 구분했는가?
6. evidence가 실제로 존재하는가?
7. evidence가 현재 변경 이후 생성되었는가?
8. report/matrix가 실제 상태와 일치하는가?
9. 실행하지 않은 검증을 완료로 적지 않았는가?
10. 민감정보 원문이 로그/evidence에 남지 않았는가?
11. 반복 실패 항목에 대해 원인 분류와 대안을 제시했는가?
12. 다음 요청에서 같은 방식 반복이 아닌가?
```


# 부록 D. 대형 요청서 제목 예시

```text
Codex 요청서 01 — CPF 목표파일/검수지침 반영 및 현재 상태 Gap 정합화
Codex 요청서 02 — PFW Service Call Engine/MSA Registry/Routing 기반 구현
Codex 요청서 03 — 표준 헤더/TransactionContext/Segment/Timeline Runtime Closure
Codex 요청서 04 — DB 로그/파일 로그/Trace Boost Runtime Evidence Closure
Codex 요청서 05 — ADM 거래/로그/서비스 인스턴스/라우팅 관제 대보강
Codex 요청서 06 — Saga/Compensation/Outbox/Inbox/Idempotency/Unknown Result
Codex 요청서 07 — BAT Standalone Worker/Lock/Heartbeat/Ghost/Center-Cut
Codex 요청서 08 — BAT 도메인 호출/SHARED/비동기 Event/Online Facade 재사용
Codex 요청서 09 — EXS 기관/Endpoint/전문/REST/Auth/Retry/Circuit/Reconciliation
Codex 요청서 10 — CMN 공통코드/메시지/파일/전문 Parser/Formatter/Fixture
Codex 요청서 11 — 보안/RBAC/ABAC/마스킹/승인/Secret/Certificate
Codex 요청서 12 — Real Broker/Event/DLQ/Replay/Ordering/Cache/Policy
Codex 요청서 13 — SQL/Flyway/all_install/MariaDB Full Install Closure
Codex 요청서 14 — Runtime E2E/Browser Click/Multi-instance/Failover Evidence
Codex 요청서 15 — 최종 회귀/품질게이트/Report Matrix Evidence Consistency
```


# 부록 E. 본 문서 갱신 원칙

이 문서는 검수 기준 문서다. 기능 목표를 확장하는 문서는 아니다. 기능 목표는 `CPF_FINAL_TARGET_REQUIREMENTS.md`에 반영한다.

본 문서에 반영할 수 있는 것:

```text
- 검수 방식
- 완료 판정 기준
- 요청서 작성 방식
- 반복 실패 방지 방식
- evidence 판단 기준
- 상태값 적용 기준
- 전수 파일 확인 방식
```

본 문서에 반영하지 않을 것:

```text
- 신규 기능 목표 상세 카드
- 특정 업무 기능 구현 상세
- 임시 작업 보고
- Codex 변경파일 목록
- 실행 결과 원문 전체
```
# Codex 공통 작업 지침 보강

- 작업 기준은 항상 `CPF_FINAL_TARGET_REQUIREMENTS.md`와 현재 요청서이다. 목표 파일은 읽기 전용 기준으로만 사용하고, 요청서가 별도 지시하지 않으면 수정하지 않는다.
- 완료 주장은 실제 파일, 실제 실행 로그, 실제 evidence가 있을 때만 사용한다. 실행하지 않은 검증은 반드시 미검증으로 남긴다.
- PFW는 프레임워크 코어 영역이며 표준 헤더, 거래 ID, 로그, 응답/오류, service call engine, PFW broker capability, file transfer capability, security/credential capability, runtime control capability를 소유한다.
- CMN은 프로젝트 공통 영역이며 업무 공통 규칙, helper, fixture, fixed-length helper, 프로젝트 공통 확장을 소유한다. Kafka/MQ/Redis Stream, SSH/SFTP/FTP/FTPS 같은 기술 engine은 PFW capability 또는 PFW port-adapter 구조로 검토한다.
- EXS는 외부연계 기술의 소유자가 아니다. EXS는 대외업무 adapter/구현체이며 외부 연계 기술 공통화가 필요하면 PFW/CMN capability로 올린다.
- Spring Event는 hook, telemetry, cache invalidation, 감사/로그 보조 용도로만 사용한다. 거래 흐름, saga, compensation, unknown result, reconciliation, DLQ/replay의 핵심 전달 수단으로 완료 처리하지 않는다.
- 요청서 템플릿 앞에는 이 공통 지침을 반복 반영하고, report/matrix/evidence 경로와 상태가 서로 맞는지 항상 검사한다.
