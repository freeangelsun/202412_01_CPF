<div align="center">

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-hero-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-hero-desktop.png" alt="Core Platform Framework — 시스템 구축과 운영을 위한 하나의 기준" width="100%">
</picture>

<br>

**설계, 개발, 실행, 운영과 변화 대응을 하나의 기준 Source로 연결합니다.**

동일 JVM · 분리 WAS · 모듈형 단일 애플리케이션 · 마이크로서비스 · 다중 인스턴스 · 정기·대량 처리 · 비동기 메시지 처리 · 운영 통제

[프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) · [개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) · [배치 개발](cpf-docs/guides/02_배치개발매뉴얼.md) · [ADM 연동 개발](cpf-docs/guides/03_ADM개발자매뉴얼.md) · [ADM 운영](cpf-docs/guides/04_ADM운영자매뉴얼.md) · [플랫폼 운영](cpf-docs/guides/05_플랫폼운영매뉴얼.md)

</div>

---

## 처음 방문했다면 여기서 시작합니다

![CPF 문서 시작 경로](cpf-docs/assets/manuals/cpf-reader-start.svg)

| 지금 알고 싶은 것 | 먼저 읽을 문서 | 첫 번째 과제 |
|---|---|---|
| CPF가 어떤 시스템에 맞는가 | [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) | 적용 사례를 고르고 Profile·Topology 선택표 작성 |
| 온라인·연계 업무를 만드는 방법 | [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) | PAY 예제로 조회·상태 변경·응답 유실 복구 수행 |
| 정기·대량 처리를 만드는 방법 | [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.md) | Job을 실행하고 Process Kill 뒤 Restart·대사 |
| 고객 업무를 ADM에 연결하는 방법 | [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.md) | Owner Port에서 Route·화면·Browser Test까지 연결 |
| 화면에서 장애를 판단하고 복구하는 방법 | [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.md) | `UNKNOWN_RESULT` 사례를 Operation과 Owner 원장으로 종결 |
| 설치·설정·배포·백업 방법 | [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md) | Artifact 검증부터 Health·Rollback까지 수행 |
| 조직·사용자·권한·결재 적용 방법 | [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.md) | 조직 기준일과 Effective Permission을 업무에 연결 |
| 외부 API 공개와 게시 복구 방법 | [91 Gateway 매뉴얼](cpf-docs/guides/91_Gateway매뉴얼.md) | Route Draft를 게시하고 NACK 뒤 LKG로 복구 |

전문 용어를 모르는 독자도 각 매뉴얼의 **용어 준비 → 따라 하기 → 오류 재현 → 복구 → 운영 인계** 순서로 읽을 수 있습니다. 이미 CPF를 사용하는 독자는 각 장의 판단표와 Source·API·SQL·화면 경로를 바로 참조합니다.

---

## 시스템의 전체 생명주기를 하나의 구조로

CPF는 조회·등록·상태 변경 API, 정기·대량 처리, 비동기 메시지, 파일·외부기관 연계, 권한·감사, 운영 화면과 장애 정상화를 같은 제품 계약으로 구성하는 Business Platform Framework입니다.

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-architecture-overview-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-architecture-overview-desktop.png" alt="CPF 전체 구조" width="100%">
</picture>

- 신규 업무 Domain을 생성하고 API·DB·Test·운영 인계 항목을 같은 Manifest로 관리합니다.
- 같은 업무 계약을 동일 JVM Local Facade와 분리 서비스 Remote Facade에서 유지합니다.
- 중복 요청, 동시성 충돌, 시간초과, 응답 유실과 결과 불명을 상태와 대사 절차로 관리합니다.
- Kafka·RabbitMQ·JMS·IBM MQ, HTTP·TCP·전문, Attachment·SFTP 기능을 업무 Source와 Provider 구현 사이의 계약으로 분리합니다.
- Spring Batch 기반 Job·Step·Chunk·Partition·Center-Cut·Scheduler·Worker를 중지·재시작·재처리·대사 흐름과 연결합니다.
- ADM에서 상태·로그·배치·설정·복구·승인·감사를 조회하고 허용된 조치를 Owner 계약으로 전달합니다.
- BZA는 조직·사용자·권한·결재를, Gateway는 외부 API의 인증·라우팅·적용 상태를 담당합니다.

[제품 범위와 선택 절차 →](cpf-docs/guides/00_프레임워크안내.md)

---

## 책임이 분명한 제품 구성

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-product-map-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-product-map-desktop.png" alt="CPF 기본 플랫폼과 선택 제품" width="100%">
</picture>

### 기본 플랫폼

- `cpf-core` — Topology 독립 Public API·SPI, 표준 Context·Error·식별자
- `cpf-common` — 고객 공통 정책과 명시된 업무 공통 기능
- `cpf-starters` — 선택 기술 Runtime과 AutoConfiguration
- `cpf-batch` — Batch Contract·Execution·Control·Scheduler·Worker·Runner·Agent
- `cpf-admin` — 플랫폼 운영 Control Plane
- `cpf-tools` — Generator·Build Plugin·BOM·DB Pack·검증·환경 도구
- `cpf-member`, `cpf-reference` — 생성 Domain 기준과 교육·복구 예제

### 선택 제품

- `cpf-biz-admin` — 조직·직원·사용자·역할·권한·결재가 필요한 업무
- `cpf-gateway` — 외부 Client와 내부 API 사이의 인증·라우팅·제한·적용 통제가 필요한 업무

고객 개발자가 선택하는 공개면은 `minimal-domain`, `web-api`, `secure-api`, `browser-bff`, `event-service`, `batch-service`의 6개 Profile입니다. 내부 Provider는 Capability Group에서 Generator Binding과 `resolved-starter-lock.json`으로 결정합니다.

---

## 배포 구성이 달라도 같은 계약

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-topology-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-topology-desktop.png" alt="동일 JVM과 분리 WAS에서 유지되는 CPF 계약" width="100%">
</picture>

Modular Monolith에서는 Local Facade가, Microservice와 분리 WAS에서는 Remote Facade가 같은 요청·응답 DTO, Validation, Error Taxonomy, Idempotency, Timeout Budget, Audit와 Trace 의미를 실행합니다. 배포 위치 때문에 업무 Source를 다시 작성하지 않습니다.

다중 인스턴스에서는 Lease·Claim·Fencing Token·Expected Version을 사용해 이전 실행자의 늦은 갱신과 중복 부작용을 차단합니다.

[온라인·비동기·외부 연계 개발 절차 →](cpf-docs/guides/01_개발자매뉴얼.md)

---

## 처리 상태와 판정 근거를 이어갑니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-execution-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-execution-desktop.png" alt="요청부터 상태 판정과 운영 정상화까지 이어지는 CPF 실행 흐름" width="100%">
</picture>

요청을 보낸 뒤 응답을 받지 못한 경우 실제 부작용이 발생했는지 확인하기 전까지 성공이나 실패로 확정하지 않습니다. Transaction ID, Idempotency Key, Request Hash, Attempt Ledger, Provider Tracking ID와 업무 원장을 연결해 결과를 대사한 뒤 재처리·보상·운영 확정을 수행합니다.

---

## 정기·대량 업무를 같은 기준으로 구성

CPF 배치는 Tasklet·Chunk·File·Partition·Remote Worker·Center-Cut·Scheduler를 Spring Batch 메타데이터와 운영 상태로 연결합니다. 운영자는 대상 Preview, 승인, 실행, 진행, Stop·Restart·Abandon·Reprocess, 결과 불명 대사와 업무 합계를 같은 Execution ID로 추적합니다.

[배치 개발과 운영 절차 →](cpf-docs/guides/02_배치개발매뉴얼.md)

---

## 실제 실행과 연결되는 운영

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-operations-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-operations-desktop.png" alt="ADM 화면 운영과 플랫폼 실행환경 운영" width="100%">
</picture>

업무 개발자는 Owner Query·Command를 ADM에 연결하고, 운영자는 Route별 권한·Data Scope·Reason·Approval·Expected Version에 따라 조회와 조치를 수행합니다. 플랫폼 운영자는 Artifact·Config·Secret·DB·Broker·배포·관측·Backup·DR을 관리합니다.

- [ADM 연동 개발자 매뉴얼 →](cpf-docs/guides/03_ADM개발자매뉴얼.md)
- [ADM 운영자 매뉴얼 →](cpf-docs/guides/04_ADM운영자매뉴얼.md)
- [플랫폼 운영 매뉴얼 →](cpf-docs/guides/05_플랫폼운영매뉴얼.md)

---

## 새 서비스도 같은 품질 기준으로 시작

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-domain-journey-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-domain-journey-desktop.png" alt="CPF 생성 도구를 이용한 서비스 생성과 검증" width="100%">
</picture>

```powershell
$repo='D:\WORK_CPF\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -DryRun
```

Dry Run에서 Module·Package·System Code·Profile·Provider Binding·DB Vendor·충돌 경로를 확인합니다. 적용 후 Domain Manifest, Resolved Starter Lock, DB Pack, OpenAPI, Test와 운영 인계표를 함께 검토합니다.

---

## 역할에 맞는 공식 매뉴얼

![CPF 역할별 매뉴얼 지도](cpf-docs/guides/png/cpf-guide-map.png)

| 문서 | 주 독자와 완료 결과 |
|---|---|
| [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) | 도입 검토자·아키텍트가 기능 범위·Profile·Topology·도입 순서를 결정 |
| [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) | 온라인·비동기·파일·외부 연계 업무를 설계·구현·시험·운영 인계 |
| [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.md) | 배치를 개발하고 실행·중지·재시작·재처리·대사 |
| [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.md) | 고객 업무의 조회·조치·승인·복구 계약을 ADM에 연결 |
| [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.md) | 실제 Route에서 조회·조치·승인·대사·Rollback 수행 |
| [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md) | 설치·설정·DB·배포·관측·Backup·DR·장애 정상화 |
| [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.md) | 조직·사용자·권한·결재·첨부·감사 운영 |
| [91 Gateway 매뉴얼](cpf-docs/guides/91_Gateway매뉴얼.md) | API Route·보안·게시·ACK/NACK·Drift·Rollback 운영 |

---


## 처음 접하는 사용자의 책형 학습 경로

CPF 문서는 기능 목록만 읽는 방식이 아니라 **하나의 업무를 설계·개발·실행·장애 재현·복구·운영 인계하는 순서**로 사용합니다.

1. [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md)에서 적용 시나리오와 Profile·Topology를 고릅니다.
2. [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md)의 PAY Golden Journey와 45개 EDU로 온라인·연계 업무를 만듭니다.
3. [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.md)에서 Process Kill·Restart·대사를 수행합니다.
4. [03/04 ADM 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.md)에서 업무 계약을 화면에 연결하고 Route별 운영 판단을 연습합니다.
5. [05 플랫폼 운영](cpf-docs/guides/05_플랫폼운영매뉴얼.md), [90 BZA](cpf-docs/guides/90_BZA매뉴얼.md), [91 Gateway](cpf-docs/guides/91_Gateway매뉴얼.md)에서 설치·권한·게시·복구를 역할별로 완료합니다.

![CPF 문서 품질 Gate](cpf-docs/assets/manuals/cpf-document-quality-r10.svg)

---
## 제품 요구사항과 설계 산출물

- [산출물 목록](cpf-docs/deliverables/산출물목록.md)
- [아키텍처 설계서](cpf-docs/deliverables/아키텍처설계서.md)
- [기술 사양서](cpf-docs/deliverables/기술사양서.md)
- [기술 표준서](cpf-docs/deliverables/기술표준서.md)
- [데이터베이스 표준서](cpf-docs/deliverables/데이터베이스표준서.md)
- [최상위 제품 요구사항](cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md)

---

<details>
<summary><strong>시작 명령 보기</strong></summary>

```powershell
$repo='D:\WORK_CPF\202412_01_CPF'
git -C $repo fetch origin
git -C $repo rev-parse origin/master
git -C $repo status --short
java -version
& (Join-Path $repo 'gradlew.bat') clean test assemble --no-daemon --max-workers=1 --stacktrace
```

설치·기동·종료·DB·검증·되돌리기 절차는 [플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md)을 사용합니다.

</details>
