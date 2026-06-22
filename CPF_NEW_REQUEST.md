# CPF_REQUEST_004: BAT 문서 정본화 / 배치 개발 가이드 별도 생성 / 운영 매뉴얼 프로세스 구동 절차 보강

## 0. 이번 작업 범위 고정

이번 작업은 BAT 독립 실행 모듈 생성 이후 깨진 문서 정합성을 바로잡는 작업이다.

이번 작업에서 수행할 범위는 아래로 고정한다.

```text id="kyv9c3"
선택 범위:
- specs/배치_개발_가이드.html 별도 문서 생성 또는 정본화
- specs/관리자_가이드.html 운영 매뉴얼 섹션 보강
- 필요 시 specs/운영_매뉴얼.html 신규 생성
- specs/index.html 문서 링크 정리
- README.md 문서 링크와 BAT 구동 요약 정리
- specs/프레임워크_구성_가이드.html의 PFW/BAT/ADM 책임 경계 현행화
- specs/SQL_가이드.html의 BAT 관련 SQL/JobRepository/운영 메타 설명 보강
- specs/기능_구현_매트릭스.html의 BAT/문서 상태 현행화
- CPF_STABILIZATION_REPORT.html에 실제 문서 정본화 결과 기록
```

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text id="thmktq"
이번 실행 제외:
- BAT 코드 신규 기능 추가
- Batch listener heartbeat / 처리율 갱신 고도화
- ghost 자동 감지 scheduler / 알림 연동
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- 센터컷 모수/item/result 테이블 구현
- 업무별 커스텀 센터컷 adapter 구현
- 온라인/배치 로그 정책 + 거래 메타 자동 등록
- ADM 브라우저 실제 클릭 자동화
- Redis/Kafka/MQ 실 broker 검증
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 현재 문제 인식

직전 작업에서 BAT 독립 실행 모듈은 추가되었다.
그러나 문서 정합성은 아직 완성되지 않았다.

현재 반드시 확인해야 할 문제는 아래와 같다.

```text id="ymh2ak"
확인 필요:
- specs/배치_개발_가이드.html 별도 문서가 없거나 부족함
- specs/프레임워크_구성_가이드.html에 “별도 bat 모듈을 만들지 않는다” 같은 과거 문구가 남아 있을 수 있음
- README.md와 specs 문서의 BAT 설명이 서로 다를 수 있음
- specs/index.html에서 배치 개발 가이드 링크가 누락되었을 수 있음
- specs/관리자_가이드.html에 ADM/BAT/MBR/BIZADM/EXS 프로세스 구동/중지/재기동/검증 방법이 부족함
- BAT standalone jar 구동, profile, health, smoke, 장애 후 조치 기준이 운영 매뉴얼 수준으로 부족함
- 기능 구현 매트릭스가 실제 BAT 상태와 문서 상태를 충분히 반영하지 않을 수 있음
```

Codex 완료 메시지나 과거 리포트만 믿지 말고, 실제 GitHub 파일 기준으로 현재 상태를 먼저 판정한다.

---

## 2. 상위 아키텍처 전제

이번 작업은 문서 정본화 작업이지만, 아래 아키텍처 전제를 반드시 문서에 반영한다.

```text id="rrfu9b"
PFW:
- 공통 framework/library 성격
- 독립 프로세스가 아님
- Batch 공통 API, 공통 launcher, transactionGlobalId, 표준 상태/오류/로그/감사 기준 제공
- Spring Batch JobRepository와 CPF 운영 메타 연결 표준 제공

BAT:
- 독립 실행 가능한 Spring Boot batch worker/application
- executable jar 형태로 standalone 구동 가능
- 필요 시 WAS/컨테이너 환경에도 배포 가능
- PFW Batch 공통 API를 사용해 Job/Step 실행
- Spring Batch 표준 JobRepository 사용
- 향후 worker heartbeat/listener/retry/recovery/온디맨드/센터컷 실행 주체

ADM:
- 배치 실행 로직을 직접 소유하지 않음
- Job/Execution/Step/Worker/Lock/Ghost/Log 상태 관제
- 수동 실행/중지/재실행 요청과 운영 조치 담당

업무 모듈:
- BAT가 업무 DB를 임의로 직접 수정하지 않음
- 업무 처리는 각 주제영역 Handler/Service/Facade를 호출하는 구조
```

센터컷 전제도 문서에 남긴다.

```text id="o2g1yx"
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않음
- PFW는 표준 인터페이스, 상태, 오류, 로그, 감사, transactionGlobalId 기준만 제공
- BAT는 향후 기본 센터컷 모수/item/result 테이블과 기본 구현체 제공
- 개발자는 필요 시 업무별 커스텀 모수/item/result 테이블 추가 가능
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동 가능
```

이번 작업에서 센터컷을 구현하지는 않는다.
단, 향후 이 구조를 방해하는 문서 표현은 남기지 않는다.

---

## 3. 먼저 현재 상태 판정

작업 시작 전 실제 파일 기준으로 아래를 먼저 확인하고 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```text id="u4qumk"
현재 상태 판정 항목:
- specs/배치_개발_가이드.html 존재 여부
- specs/index.html의 배치 개발 가이드 링크 여부
- README.md의 BAT 안내 상태
- specs/프레임워크_구성_가이드.html의 BAT 책임 경계 상태
- specs/관리자_가이드.html의 운영 절차 상태
- specs/SQL_가이드.html의 Spring Batch / CPF 운영 메타 설명 상태
- specs/기능_구현_매트릭스.html의 BAT/문서 상태
- CPF_STABILIZATION_REPORT.html의 직전 BAT 검증 결과
- “별도 bat 모듈 없음” 같은 오래된 문구 존재 여부
- PFW/ADM/BAT 책임 경계 충돌 문구 존재 여부
```

상태값은 아래만 사용한다.

```text id="y9n4jv"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

---

## 4. 배치 개발 가이드 별도 문서 필수

`specs/배치_개발_가이드.html`은 별도 문서로 반드시 존재해야 한다.
README나 프레임워크 구성 가이드의 일부 섹션으로 대체하지 않는다.

배치 개발자는 이 문서만 보고 BAT 모듈 구조, Job/Step 작성, 실행, 검증, ADM 관제 연결 기준을 이해할 수 있어야 한다.

필수 포함 내용:

```text id="nq3vnh"
- CPF 배치 아키텍처 개요
- PFW / BAT / ADM 책임 경계
- BAT 독립 실행 모듈 구조
- BAT standalone jar 구동 방법
- BAT profile 기준: bat-local, bat-worker, scheduler-off 등
- Spring Batch JobRepository 기준
- BATCH_* 테이블과 CPF pfw_batch_* 운영 메타 연결 기준
- JobExecutionId / StepExecutionId / transactionGlobalId 연결 기준
- 최소 Job/Step 작성 예제 위치
- :bat:test 실행 방법
- :bat:bootJar 실행 방법
- scripts/smoke-bat-runtime.ps1 실행 방법
- ADM 배치 관제와 연결되는 항목
- 실패 Job / 실패 Step / 재실행 / 중지 / ghost recovery의 향후 확장 기준
- 이번 작업에서 제외되는 온디맨드/센터컷/heartbeat 고도화/ghost 자동 감지 항목
- 다음 보강 후보
```

배치 개발 가이드에는 실제 파일 경로를 포함한다.

```text id="l28qj3"
예:
- bat/src/main/java/cpf/bat/BatApplication.java
- bat/src/main/java/cpf/bat/job/BatSmokeJobConfig.java
- bat/src/main/java/cpf/bat/operation/BatHealthController.java
- scripts/smoke-bat-runtime.ps1
```

---

## 5. 운영 매뉴얼 / 관리자 가이드 보강

`specs/관리자_가이드.html`에는 운영자가 각 프로세스를 상황별로 어떻게 구동, 중지, 재기동, 검증하는지 확인할 수 있는 운영 매뉴얼 수준의 절차를 추가한다.

필요하면 `specs/운영_매뉴얼.html`을 별도 신규 문서로 생성하고, `specs/index.html`과 README에서 링크한다.
별도 문서를 만들지 않는 경우에는 `관리자_가이드.html` 안에 “운영 매뉴얼” 섹션을 명확히 둔다.

필수 포함 내용:

```text id="mege62"
프로세스/모듈별 구동 책임:
- PFW
- ADM
- BAT
- MBR
- BIZADM
- EXS
- CMN
- ACC
```

PFW는 독립 프로세스가 아니라 공통 framework/library임을 명확히 적는다.

BAT는 독립 실행 가능한 Spring Boot batch worker임을 명확히 적는다.

상황별 구동 시나리오:

```text id="gmcqer"
- 로컬 개발 환경 기동
- ADM 단독 기동
- BAT 단독 기동
- ADM + BAT 동시 기동
- 전체 모듈 기동
- 배포 후 재기동
- 장애 후 재기동
- BAT worker scale-out 기동
- scheduler 비활성 상태로 BAT 기동
- scheduler 활성 상태로 BAT 기동
- 점검 모드 기동
- smoke 검증용 기동
```

각 시나리오에는 가능한 명령 예시와 확인 기준을 적는다.

ADM 구동 예시:

```powershell id="j1e94k"
.\gradlew.bat :adm:bootJar --offline
java -jar adm\build\libs\<adm-jar-name>.jar --spring.profiles.active=adm-local
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
```

BAT 구동 예시:

```powershell id="uxz62r"
.\gradlew.bat :bat:bootJar --offline
java -jar bat\build\libs\<bat-jar-name>.jar --spring.profiles.active=bat-local
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1
```

구동 후 확인 기준:

```text id="cd12t4"
ADM:
- /adm/api/health
- OpenAPI 접근
- ADM 배치 API smoke
- ADM UI marker 또는 브라우저 검증

BAT:
- /bat/api/health 또는 actuator health
- workerId/serverInstanceId 확인
- JobRepository 연결 확인
- 정상 smoke Job 실행
- 실패 smoke Job 실행
- JobExecutionId / StepExecutionId / CPF executionId / transactionGlobalId 연결 확인

DB:
- MariaDB 접속
- pfwDB/admDB/mbrDB 등 schema 존재
- BATCH_* 테이블 존재
- pfw_batch_* 운영 메타 존재
```

중지/재기동 기준:

```text id="q80256"
정상 중지:
- 신규 Job 실행 차단
- 실행 중 Job 상태 확인
- 가능한 경우 graceful shutdown
- lock 해제
- worker heartbeat 종료 기록
- 종료 후 health 미응답 확인

강제 종료:
- shutdown hook 보장 불가
- RUNNING execution이 남을 수 있음
- heartbeat timeout 기준으로 ghost candidate 감지
- ADM에서 ghost 조치
- 조치 사유/조치자/감사 로그 기록
- 재실행 가능 여부 판단
```

---

## 6. 프레임워크 구성 가이드 현행화

`specs/프레임워크_구성_가이드.html`은 현재 실제 구조와 일치해야 한다.

반드시 수정해야 할 내용:

```text id="y0iz3v"
- “별도 bat 모듈을 만들지 않는다” 같은 과거 문구 제거
- BAT가 별도 모듈로 존재함을 명시
- settings.gradle에 bat가 포함된 현재 구조 반영
- PFW / BAT / ADM 책임 경계 반영
- BAT가 PFW Batch 공통 API를 사용하는 구조 설명
- ADM은 관제/조치 콘솔이고 실행 runtime이 아님을 명시
- 센터컷은 향후 BAT 기본 구현체와 업무별 커스텀 테이블 adapter 구조로 확장한다는 전제 반영
```

문서 내용이 실제 소스 구조와 다르면 완료로 기록하지 않는다.

---

## 7. README.md 현행화

README에는 아래를 짧고 명확하게 반영한다.

```text id="rxlx6p"
- BAT 독립 실행 모듈 존재
- BAT 구동/검증 명령
- 배치 개발 가이드 링크
- 관리자 가이드 또는 운영 매뉴얼 링크
- PFW/BAT/ADM 책임 경계 요약
- 현재 미검증 항목: 브라우저 실제 클릭, 장시간 heartbeat, ghost 자동 감지 등
```

README는 가능한 Markdown 가독성을 유지한다.
한 줄로 지나치게 압축된 형태라면 제목/목록/코드블록을 사용해 읽기 쉽게 정리한다.

---

## 8. SQL 가이드 보강

`specs/SQL_가이드.html`에는 BAT 독립 실행과 Spring Batch/CPF 운영 메타의 관계를 반영한다.

필수 포함 내용:

```text id="y049k5"
- Spring Batch BATCH_* 테이블은 표준 JobRepository가 관리
- CPF 운영 메타는 pfw_batch_* 테이블에서 관리
- JobExecutionId와 pfw_batch_execution 연결
- StepExecutionId와 pfw_batch_step_execution 연결
- transactionGlobalId 연결
- BAT 신규 모듈이 생겼지만 이번 문서 정본화 작업에서 신규 SQL 변경이 없다면 “SQL 변경 없음”으로 기록
- 센터컷 대량 모수/item/result 테이블은 PFW에 고정으로 추가하지 않는다는 기준
```

센터컷 관련 SQL은 이번 작업에서 만들지 않는다.

---

## 9. 기능 구현 매트릭스 현행화

`specs/기능_구현_매트릭스.html`에는 BAT와 문서 상태를 실제 기준으로 반영한다.

필수 반영:

```text id="irk8mi"
- BAT 독립 실행 모듈 상태
- BAT standalone 구동 검증 상태
- smoke-bat-runtime 검증 상태
- Spring Batch JobExecutionId / StepExecutionId / CPF 운영 메타 연결 상태
- 배치 개발 가이드 상태
- 운영 매뉴얼/관리자 가이드 상태
- 미검증 항목: 브라우저 실제 클릭, 장시간 heartbeat, ghost 자동 감지, 온디맨드, 센터컷
```

상태값은 아래만 사용한다.

```text id="ozsbeq"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

완료는 증거가 있을 때만 사용한다.

---

## 10. 검증 명령

이번 작업은 문서 정본화 중심이므로 아래 검증을 수행한다.

```powershell id="pqmcut"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1

.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
```

가능하면 기존 BAT/ADM smoke도 재실행해 문서에 적힌 명령과 실제 스크립트가 유효한지 확인한다.

```powershell id="cx3nim"
.\gradlew.bat :bat:test --offline
.\gradlew.bat :bat:bootJar --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
```

이번 작업에서 MariaDB 전체 설치 SQL 재실행은 필수 범위가 아니다.
실행하지 않았다면 미검증 또는 범위 제외로 기록한다.
실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 11. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text id="bq70he"
[BAT 문서 정본화 / 배치 개발 가이드 별도 생성 / 운영 매뉴얼 프로세스 구동 절차 보강]

현재 상태 판정:
- 배치 개발 가이드 존재 여부:
- 프레임워크 구성 가이드의 BAT 문구 상태:
- 관리자 가이드 운영 절차 상태:
- README BAT 안내 상태:
- SQL 가이드 BAT/JobRepository 설명 상태:
- 기능 구현 매트릭스 상태:

문서 반영:
- specs/배치_개발_가이드.html:
- specs/관리자_가이드.html 또는 specs/운영_매뉴얼.html:
- specs/index.html:
- README.md:
- specs/프레임워크_구성_가이드.html:
- specs/SQL_가이드.html:
- specs/기능_구현_매트릭스.html:

검증:
- check-html-docs:
- check-utf8:
- check-feature-evidence:
- test:
- qualityGate:
- smoke-bat-runtime:
- smoke-adm-runtime:

미검증:
- 미검증 항목:
- 미검증 사유:
- 다음 조치:

아키텍처 정합성:
- PFW 책임:
- BAT 책임:
- ADM 책임:
- 센터컷 확장성 기준:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

문서만 수정한 경우에도 실제 검증 명령 결과를 기록한다.
검증하지 않은 항목은 성공으로 기록하지 않는다.

---

## 12. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 기록한다.

```text id="q1gju9"
- specs/배치_개발_가이드.html 별도 문서 존재
- specs/index.html에서 배치 개발 가이드로 이동 가능
- README.md에서 배치 개발 가이드로 이동 가능
- 프레임워크 구성 가이드에서 오래된 “bat 모듈 없음” 문구 제거
- PFW/BAT/ADM 책임 경계가 모든 관련 문서에서 일치
- 관리자 가이드 또는 운영 매뉴얼에 프로세스별 구동/중지/재기동/검증 방법 존재
- ADM/BAT 구동 명령과 smoke 명령이 문서에 존재
- BAT scheduler on/off, worker profile, local profile 기준이 문서에 존재
- 장애 후 ghost/lock 조치 기준이 문서에 존재
- SQL 가이드에 Spring Batch JobRepository와 CPF 운영 메타 연결 기준 존재
- 기능 구현 매트릭스에 BAT와 문서 상태가 실제 기준으로 반영
- check-html-docs / check-utf8 / qualityGate 검증 결과가 리포트에 기록
- 실패/미검증 항목을 성공으로 기록하지 않음
```

---

## 13. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text id="ncy5wy"
- specs/배치_개발_가이드.html 없음
- README 일부 설명만 있고 배치 개발 가이드를 별도 문서로 만들지 않음
- 프레임워크 구성 가이드에 “bat 모듈 없음” 또는 동등한 과거 문구가 남아 있음
- PFW/BAT/ADM 책임 경계가 문서마다 다름
- BAT 구동 방법이 문서에 없음
- ADM/BAT 프로세스별 구동/중지/재기동 기준 없음
- scheduler on/off 구동 기준 없음
- worker scale-out 구동 기준 없음
- 장애 후 ghost/lock 조치 기준 없음
- SQL 가이드에 JobRepository와 CPF 운영 메타 연결 기준 없음
- 기능 구현 매트릭스가 실제 상태와 다름
- 문서에는 검증 성공이라고 되어 있으나 리포트/실제 명령 결과가 없음
- 실행하지 않은 검증을 성공으로 기록
- 이번 요청 범위를 넘어 BAT 신규 기능, 온디맨드, 센터컷 구현을 시작함
```

---

## 14. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 아래 순서로 기록한다.

```text id="yxasxs"
1. BAT listener heartbeat / 처리율 갱신 / ghost 자동 감지
2. ADM 브라우저 실제 클릭 자동화
3. 온디맨드 배치
4. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
5. 온라인/배치 로그 정책 + 거래 메타 자동 등록
6. BAT EDU / XYZ EDU 배치 샘플 보강
7. 트랜잭션 가이드 정본화
```

센터컷 후속 작업 기준은 아래 방향을 유지한다.

```text id="wr4mlt"
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않음
- BAT는 기본 센터컷 모수/item/result 테이블과 기본 구현체 제공
- 업무 주제영역은 커스텀 모수/item/result 테이블을 추가할 수 있음
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동
- ADM은 기본 테이블/커스텀 테이블 여부와 관계없이 공통 관제 제공
```
