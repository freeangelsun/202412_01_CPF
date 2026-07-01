CPF_M0_M1_REQUEST_20260630_07 — 차단 오류 정리 및 표준 헤더 Source-Level 검증 게이트 완료 요청

## 1. 작업 목적

이번 작업의 목적은 반복 검수 루프를 끊기 위해 현재 남아 있는 차단 오류를 먼저 닫고, 표준 헤더 Source-Level 검증 상태를 실제 파일/테스트/evidence/리포트/기능 매트릭스 기준으로 일치시키는 것이다.

이번 작업은 문서 꾸미기, 포맷 정리, 대규모 개발가이드 작성, DB 전체 설치 검증이 아니다.

이번 작업의 핵심은 아래다.

1. Codex 완료 보고와 실제 파일 상태 불일치 제거
2. `CpfStandardHeaderE2eTest.java`의 존재와 검증 범위를 evidence에 반영
3. 표준 헤더 Source-Level 검증과 Runtime E2E를 명확히 분리
4. 리포트와 기능 매트릭스 상태값 일치
5. 실행하지 않은 ADM runtime/OpenAPI/browser/DB/broker 검증을 완료로 기록하지 않음
6. 다음 마일스톤인 ADM runtime/OpenAPI smoke로 넘어갈 수 있는 상태 만들기

## 2. 이번 범위

이번 범위는 아래 4개로 제한한다.

### 2.1 차단 오류 정리

아래는 반드시 닫는다.

* Codex 보고와 실제 파일 상태 불일치
* 리포트와 기능 매트릭스 상태 불일치
* 표준 헤더 테스트가 있는데 evidence/check script가 못 잡는 문제
* Source-Level 테스트를 Runtime E2E 완료로 과장하는 문제
* 실행하지 않은 검증을 완료로 기록하는 문제
* `CPF_STABILIZATION_CHANGED_FILES.txt` 같은 불필요 산출물 생성
* old fixture path 또는 legacy evidence 잔존
* 허용 상태값 외 표현 사용

### 2.2 표준 헤더 Source-Level 검증 게이트 정리

대상:

```text
pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java
pfw/src/main/java/cpf/pfw/common/header/**
scripts/check-feature-evidence.ps1
CPF_STABILIZATION_REPORT.html
specs/기능_구현_매트릭스.html
```

### 2.3 ADM runtime/OpenAPI smoke 진입 준비

이번 작업에서 ADM runtime을 무조건 완료로 만들지 않는다.
다음 작업에서 실제 runtime smoke를 실행할 수 있도록 Controller/API/script 대상만 정리한다.

### 2.4 문서 최소 반영

문서는 아래만 수정한다.

* 상태값 불일치
* evidence 경로 불일치
* 표준 헤더 Source-Level 검증 반영
* Runtime E2E 미검증 분리
* 다음 ADM runtime smoke 대상 목록

## 3. 제외 범위

이번 작업에서는 아래를 하지 않는다.

* Git commit
* Git push
* branch 생성
* 문서 포맷/디자인/CSS 개선
* HTML 레이아웃 정리
* 개발가이드/운영가이드 대규모 상세화
* Markdown/AsciiDoc/YAML 전환
* MariaDB 신규 빈 DB 전체 설치
* split SQL/Flyway/all_install 최종 검증
* ADM 브라우저 클릭 검증
* Redis/Kafka/MQ 실 broker 검증
* BAT ghost/center-cut 구현
* 대규모 리팩토링

문서 품질 고도화와 가이드 정본화는 최종 마무리 단계로 이관한다.
단, 오류/불일치/상태값 과장은 절대 뒤로 미루지 않는다.

## 4. 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

주의:

* `CPF_NEW_REQUEST.md`가 수정 상태라면 사용자 요청 원본으로 보고 작업 대상에서 제외한다.
* commit/push/branch 생성은 하지 않는다.
* 로컬 실행 결과와 GitHub master에서 ChatGPT가 확인 가능한 상태를 구분한다.

## 5. 차단 오류 / 병행 보강 / 최종 정본화 구분

이번 작업부터 모든 항목을 아래 3개로 분류한다.

### 5.1 차단 오류

아래는 반드시 이번 작업에서 닫는다.

```text
Codex 보고와 실제 파일 불일치
리포트와 기능 매트릭스 상태 불일치
완료라고 기록했으나 실제는 미검증
테스트/evidence 누락
check script가 잡아야 할 오류를 못 잡음
보안/민감정보/표준 헤더 의미 오류
빌드/test/qualityGate 실패
```

### 5.2 병행 보강

아래는 이번 기능 작업에 필요한 만큼만 보강한다.

```text
테스트 주석 부족
코드 가독성 부족
문서 설명 부족
smoke script 설명 부족
```

단, 검증을 방해하면 차단 오류로 올린다.

### 5.3 최종 정본화

아래는 이번 작업에서 하지 않는다.

```text
개발가이드 수십 페이지화
운영가이드 문장 품질 고도화
HTML 디자인/포맷 정리
문서 통합/분리
목차 재설계
```

## 6. `CpfStandardHeaderE2eTest.java` 정리 기준

대상:

```text
pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java
```

요구사항:

* 파일이 정상적인 Java multi-line 포맷인지 확인한다.
* 신규 테스트이므로 class 또는 test method에 한글 주석을 둔다.
* 주석은 단순 동작 반복이 아니라 운영 의미와 검증 의도를 설명한다.
* 테스트가 검증하는 범위를 과장하지 않는다.

이 테스트가 검증해야 하는 항목:

```text
필수 표준 헤더 검증
transaction context 생성
trusted proxy 기반 client IP 산정
X-User-Id / X-Customer-No / X-Member-No / X-Operator-Id 의미 분리
X-Original-Channel-Code / X-Channel-Code 구분
Authorization / X-Api-Key / token류 원문 로그 금지
민감 헤더 마스킹
outbound 허용 헤더만 전파
```

이 테스트가 검증하지 않은 항목도 명확히 기록한다.

```text
실제 ADM runtime 거래 로그 조회
실제 DB 로그 저장 결과
실제 OpenAPI runtime 접근
실제 브라우저 클릭
실제 WebClient/RestClient로 하위 mock 서버 수신 확인
Redis/Kafka/MQ broker 전파
```

표현 기준:

* 허용: `PFW 표준 헤더 Source-Level 흐름 테스트`
* 허용: `표준 헤더 단위/통합 성격 테스트`
* 금지: `표준 헤더 Runtime E2E 완료`
* 금지: `ADM 로그 조회까지 완료`
* 금지: `outbound 실제 HTTP 전파 완료`

## 7. Evidence gate 반영

대상:

```text
scripts/check-feature-evidence.ps1
```

요구사항:

`check-feature-evidence.ps1`는 아래를 확인해야 한다.

```text
CpfStandardHeaderE2eTest.java 존재
CpfHeaderNames 존재
CpfHeaderSpecs 존재
CpfInboundHeaderValidator 존재
CpfHeaderExtractor 존재
CpfHeaderMasker 존재
CpfHeaderPropagator 존재
CpfTrustedProxyPolicy 존재
CpfHeaderMutator 또는 동등한 보정 수단 존재
```

가능하면 아래도 evidence로 확인한다.

```text
CpfWebClient 또는 CpfRestClient 또는 outbound header 전파 wrapper/interceptor
TransactionContext
TransactionHeader
```

완료 불인정:

* 표준 헤더 테스트 파일이 있는데 evidence script가 확인하지 않음
* 문서에는 있다고 쓰고 소스 evidence가 없음
* evidence script 성공을 Runtime E2E 성공으로 기록

## 8. 리포트 반영 기준

대상:

```text
CPF_STABILIZATION_REPORT.html
```

리포트에는 아래를 반드시 구분한다.

### 8.1 검증한 것

```text
PFW 표준 헤더 Source-Level 흐름 테스트
필수 헤더 검증
transaction context 생성
trusted proxy client IP 산정
user/customer/member/operator 의미 분리
original/current channel 구분
민감 헤더 마스킹
outbound 허용 헤더 전파
```

### 8.2 검증하지 않은 것

```text
ADM runtime 기동
OpenAPI runtime 접근
ADM 거래 로그 조회
DB 로그 저장 결과
ADM 브라우저 클릭
실제 HTTP client outbound 수신
Redis/Kafka/MQ broker 전파
MariaDB 전체 신규 설치
```

### 8.3 상태값

권장 상태:

```text
PFW 표준 헤더 Source-Level 흐름 테스트: 완료
표준 헤더 Runtime E2E: 미검증
ADM 로그 연계 검증: 미검증
실제 outbound HTTP 수신 검증: 미검증 또는 재확인 필요
```

주의:

* Codex 로컬 테스트 성공은 기록 가능하다.
* ChatGPT가 직접 실행하지 못한 Gradle/JUnit 결과는 “Codex 로컬 실행 결과”로 기록한다.
* 실행하지 않은 runtime 검증은 완료로 쓰지 않는다.

## 9. 기능 매트릭스 반영 기준

대상:

```text
specs/기능_구현_매트릭스.html
```

기능 매트릭스에는 Source-Level 검증과 Runtime E2E를 분리한다.

권장 구조:

```text
PFW 표준 헤더 Source-Level 흐름 테스트: 완료
표준 헤더 Runtime E2E: 미검증
ADM 거래 로그 연계: 미검증
outbound 실제 HTTP 전파 검증: 미검증 또는 재확인 필요
```

기존 `표준 헤더 E2E` 항목이 하나로만 되어 있다면, 과장 방지를 위해 분리한다.

상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

완료 불인정:

* Source-Level 테스트 추가를 Runtime E2E 완료로 기록
* 리포트와 매트릭스 상태 불일치
* 허용되지 않은 상태값 사용
* check script 결과와 문서 상태 불일치

## 10. ADM runtime/OpenAPI smoke 준비

이번 작업의 다음 진도를 위해 ADM runtime smoke 대상만 정리한다.
가능하면 실제 Controller를 열어 path를 확인한다.

확인 대상 후보:

```text
adm/src/main/java/**/controller/**/*.java
adm/src/main/java/**/controller/*.java
scripts/smoke-adm-runtime.ps1
scripts/smoke-openapi.ps1
scripts/smoke-log-policy-runtime.ps1
```

해야 할 일:

1. ADM 주요 Controller 파일을 실제 확인한다.
2. smoke 대상 API path를 실제 Controller 기준으로 정리한다.
3. 기존 smoke script가 실제 path와 맞는지 확인한다.
4. path가 명백히 틀리면 최소 수정한다.
5. runtime 실행을 못 했으면 `미검증`으로 기록한다.

우선 smoke 후보:

```text
ADM health 또는 actuator
OpenAPI JSON
거래 로그 목록
거래 로그 상세
오류 로그 목록
감사 로그 목록
로그 정책 조회
배치 목록
캐시 상태 조회
```

완료 불인정:

* Controller를 열지 않고 추정 path 작성
* OpenAPI 설정 파일 존재만으로 runtime 성공 처리
* ADM 앱 기동 없이 runtime smoke 성공 처리
* 정적 UI marker를 브라우저 클릭 성공으로 기록

## 11. 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
```

실행하지 못한 명령은 성공으로 기록하지 않는다.

timeout 발생 시:

* timeout을 실패 또는 미검증으로 기록한다.
* 재실행했다면 재실행 명령과 성공 여부를 분리 기록한다.
* timeout 명령을 성공으로 포장하지 않는다.

## 12. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 본다.

* `CpfStandardHeaderE2eTest.java`가 정상 포맷과 한글 주석을 갖는다.
* 테스트가 검증한 항목과 검증하지 않은 항목이 리포트에 분리된다.
* 기능 매트릭스가 Source-Level 검증과 Runtime E2E를 분리한다.
* `check-feature-evidence.ps1`가 표준 헤더 핵심 source/evidence를 확인한다.
* 리포트와 기능 매트릭스 상태가 일치한다.
* 실행하지 않은 runtime/OpenAPI/browser/DB/broker 검증은 `미검증`으로 남긴다.
* ADM runtime smoke 대상 API 목록이 실제 Controller 기준으로 정리된다.
* 차단 오류가 0건이다.
* 문서 포맷/디자인/대규모 정본화는 하지 않는다.

## 13. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

* Codex 보고와 실제 파일 불일치
* 표준 헤더 Source-Level 테스트를 Runtime E2E 완료로 기록
* ADM 앱을 기동하지 않았는데 ADM runtime 완료로 기록
* OpenAPI JSON 접근 없이 OpenAPI runtime 완료로 기록
* 브라우저 클릭 없이 ADM browser click 완료로 기록
* DB 로그 저장을 확인하지 않고 로그 E2E 완료로 기록
* 실제 HTTP outbound 수신 확인 없이 outbound runtime 전파 완료로 기록
* 리포트와 기능 매트릭스 상태 불일치
* evidence script가 새 테스트를 확인하지 않음
* 허용되지 않은 상태값 사용
* 민감 헤더 원문을 로그/리포트에 기록
* `CPF_STABILIZATION_CHANGED_FILES.txt` 생성
* Git commit / push / branch 생성 수행

## 14. 완료 보고 양식

완료 보고는 반드시 아래 양식으로 작성한다.

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- GitHub master 확인 가능 여부:

2. 차단 오류 처리 결과
- Codex 보고/파일 불일치:
- 리포트/매트릭스 불일치:
- evidence 누락:
- 완료 과장 여부:
- 남은 차단 오류:

3. 표준 헤더 Source-Level 테스트
- 파일:
- 포맷/주석 정리 여부:
- 검증한 항목:
- 검증하지 않은 항목:
- 테스트 결과:

4. Evidence gate
- check-feature-evidence.ps1 반영 내용:
- 확인하는 파일:
- legacy/금지 기준:

5. 리포트 / 기능 매트릭스
- Source-Level 상태:
- Runtime E2E 상태:
- ADM 로그 연계 상태:
- outbound runtime 상태:
- 불일치 여부:

6. ADM runtime/OpenAPI smoke 준비
- 확인한 Controller:
- 확인한 API path:
- smoke script:
- path 일치 여부:
- 실행 여부:
- 미검증 사유:

7. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- timeout/재실행 여부:

8. 남은 미검증
- 대상:
- 사유:
- 완료로 기록하지 않은 이유:

9. 다음 추천 작업
- ADM runtime/OpenAPI smoke 실제 실행
- 표준 헤더 Runtime E2E
- ADM browser click
- BAT ghost/center-cut
```

## 15. 다음 마일스톤

이번 작업이 완료되면 다음 작업은 아래 순서로 진행한다.

1. ADM runtime/OpenAPI smoke 실제 실행
2. 표준 헤더 Runtime E2E
   실제 API 호출 → context 생성 → 로그 저장 → ADM 조회 → outbound 확인
3. ADM 브라우저 클릭 검증
4. BAT standalone / heartbeat / ghost
5. center-cut 기본 구현체와 EDU 샘플
6. EXS / CMN / BIZADM / MBR 보강
7. DB/Flyway/all_install 신규 DB 검증
8. 최종 문서 정본화
