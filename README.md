<div align="center">

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-hero-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-hero-desktop.png" alt="Core Platform Framework — 시스템 구축과 운영을 위한 하나의 기준" width="100%">
</picture>

<br>

**설계, 개발, 실행, 운영, 복구와 다음 변화를 하나의 제품 기준으로 연결합니다.**

동일 JVM · 분리 WAS · Modular Monolith · MSA · 다중 인스턴스 · Spring Batch · Kafka · 운영 통제

[프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) · [개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) · [배치 개발](cpf-docs/guides/02_배치개발매뉴얼.md) · [ADM 운영](cpf-docs/guides/04_ADM운영자매뉴얼.md) · [플랫폼 운영](cpf-docs/guides/05_플랫폼운영매뉴얼.md)

</div>

---

## 시스템의 전체 생명주기를 하나의 구조로

CPF는 공통 Library 묶음이나 특정 프로젝트용 예제가 아닙니다. 시스템의 요청이 온라인 서비스, 비동기 메시지, 파일·외부 연계, Spring Batch 실행, 운영 제어와 복구로 이어지는 전 과정을 일관된 제품 계약으로 관리합니다.

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-architecture-overview-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-architecture-overview-desktop.png" alt="CPF 전체 구조" width="100%">
</picture>

- `cpf-core`는 배포 구성에 독립적인 기술 계약과 공개 API·SPI를 제공합니다.
- `cpf-common`은 필요한 서비스가 선택해 사용하는 공통 기능을 제공합니다.
- 생성 서비스와 `cpf-admin`은 자신의 API·상태·데이터·Transaction을 소유합니다.
- `cpf-batch`는 Spring Batch 기반 Job·Step·Scheduler·Manager·Worker·Agent 실행 환경을 제공합니다.
- `cpf-gateway`와 `cpf-biz-admin`은 필요한 환경에서 선택하는 제품입니다.
- Generator, DB Vendor Pack, 설치·배포·검증 도구는 Source부터 운영 Evidence까지 연결합니다.

[제품 스펙과 책임 경계 자세히 보기 →](cpf-docs/guides/00_프레임워크안내.md)

---

## 책임이 분명한 제품 구성

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-product-map-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-product-map-desktop.png" alt="CPF 기본 플랫폼과 선택 제품" width="100%">
</picture>

### 기본 플랫폼

- `cpf-core` — 기술 계약과 확장 경계
- `cpf-common` — 선택형 공통 기능
- `cpf-admin` — 플랫폼 운영 Control Plane
- 생성 서비스 — 시스템별 기능·데이터 Owner
- `cpf-batch` — Spring Batch 기반 실행 제품

### 선택 제품

- `cpf-biz-admin` — 사용자·조직·권한·결재가 필요한 경우 선택
- `cpf-gateway` — 공통 진입 정책과 Route Control Plane이 필요한 경우 선택

선택하지 않은 제품이 필수 Dependency나 기동 조건으로 따라오지 않도록 Module과 Starter 경계를 유지합니다.

---

## 배포 구성이 달라도 같은 계약

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-topology-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-topology-desktop.png" alt="동일 JVM과 분리 WAS에서 유지되는 CPF 계약" width="100%">
</picture>

같은 JVM에서는 Local Adapter가, 분리된 실행 환경에서는 Remote Adapter가 같은 공개 계약을 실행합니다. 호출자는 배포 위치에 결합되지 않으며 표준 식별자, 인증·권한 문맥, 오류 분류, 시간 예산, 멱등성과 Trace의 의미가 유지됩니다.

다중 인스턴스 실행은 Lease, Claim, Fencing Token과 Version을 사용해 소유권을 잃은 과거 실행자가 현재 상태를 덮어쓰지 못하게 합니다.

[개발자가 따라 하는 Local·Remote 구현과 Transaction →](cpf-docs/guides/01_개발자매뉴얼.md)

---

## 처리 상태와 복구 근거를 이어갑니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-execution-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-execution-desktop.png" alt="요청부터 상태 확정과 복구까지 이어지는 CPF 실행 흐름" width="100%">
</picture>

온라인·비동기·배치·외부 연계 전 과정의 상태와 이력을 일관되게 관리해 중단 이후 재시작·대사·복구 절차와 판정 근거를 유지합니다.

대상에 요청을 보낸 뒤 응답을 받지 못한 경우에는 실제 처리 여부를 확인하기 전까지 결과를 임의로 확정하지 않습니다. 거래 식별자, 요청 Hash, 멱등성 Key, Attempt와 상대 상태 조회를 연결해 실제 결과를 확정한 뒤 재처리·보상·운영 확정을 수행합니다.

---

## Spring Batch를 배치 실행의 Primary Engine으로

CPF Batch는 Job·Step·Tasklet·Chunk, JobRepository·ExecutionContext, Checkpoint·Restart, Parallel·Partition과 원격 Worker 실행을 Spring Batch 표준 생명주기로 통합합니다.

CPF는 실행 엔진을 중복 개발하지 않고 정의·Version·승인·Topology·Artifact·Agent 보안·Fencing·대사·감사와 ADM 운영 경험을 제공합니다.

[Spring Batch Job부터 Center-Cut·Worker 복구까지 따라 하기 →](cpf-docs/guides/02_배치개발매뉴얼.md)

---

## 실제 실행과 연결되는 운영

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-operations-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-operations-desktop.png" alt="ADM과 플랫폼 Runtime 운영" width="100%">
</picture>

ADM은 서비스·인스턴스·거래·로그·Trace·Batch·설정·배포 상태를 조회하고, 위험 조치를 권한·사유·승인·Version·감사와 함께 실제 Owner Runtime에 전달합니다.

플랫폼 운영자는 Profile·Property·Secret, DB, Artifact, Process, 배포, 관측, Backup·Restore와 DR을 관리합니다. 화면 조작과 Runtime 운영을 한 문서에 섞지 않고 역할별 절차로 분리했습니다.

- [ADM 기능을 개발하는 사람 →](cpf-docs/guides/03_ADM개발자매뉴얼.md)
- [ADM 화면을 사용하는 운영관리자 →](cpf-docs/guides/04_ADM운영자매뉴얼.md)
- [설정·DB·배포·기동·복구 담당자 →](cpf-docs/guides/05_플랫폼운영매뉴얼.md)

---

## 새 서비스도 같은 품질 기준으로 시작

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-domain-journey-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-domain-journey-desktop.png" alt="CPF Generator를 이용한 서비스 생성과 검증" width="100%">
</picture>

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName payment `
  -SystemCode PAY `
  -DatabaseVendor postgresql `
  -DryRun
```

계획 단계에서 Module, Package, SystemCode, Port, Route와 DB 충돌을 검사합니다. 생성 뒤에는 API·SPI, Local·Remote 호출, Oracle·PostgreSQL·MariaDB, Test, OpenAPI, JavaDoc, 설정과 운영 등록을 함께 확인합니다.

[Generator를 포함한 전체 개발 절차 →](cpf-docs/guides/01_개발자매뉴얼.md)

---

## 역할에 맞는 매뉴얼

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-guide-map-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-guide-map-desktop.png" alt="CPF 역할별 매뉴얼 8종" width="100%">
</picture>

| 문서 | 대상 |
|---|---|
| [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) | 제품 계약과 전체 구조를 먼저 이해할 때 |
| [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) | 온라인·연계·Transaction·Kafka·File 기능을 개발할 때 |
| [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.md) | Spring Batch·Scheduler·Worker·Agent를 개발할 때 |
| [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.md) | ADM Backend·Frontend·권한·승인을 개발할 때 |
| [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.md) | ADM 메뉴로 조회·제어·대사를 수행할 때 |
| [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md) | Profile·Property·DB·배포·관측·복구를 담당할 때 |
| [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.md) | 선택형 업무 관리자 제품을 사용할 때 |
| [91 Gateway 매뉴얼](cpf-docs/guides/91_Gateway매뉴얼.md) | 선택형 Gateway를 개발·운영할 때 |

[문서 역할과 읽는 순서 →](cpf-docs/guides/00_프레임워크안내.md#18-문서-지도와-읽는-순서)

---

<details>
<summary><strong>시작 명령 보기</strong></summary>

### Repository와 환경

```powershell
git rev-parse HEAD
git status --short
java -version
pwsh --version
```

### 전체 Build

```powershell
.\gradlew.bat clean build
```

```bash
./gradlew clean build
```

### 데이터베이스

```powershell
Get-Help .\cpf-tools\scripts\initialize-cpf-database.ps1 -Full
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

### 로컬 실행

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

명령의 실제 Parameter와 지원 환경은 최신 `master`의 `Get-Help`와 [플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md)을 확인합니다.

</details>

---

## 매뉴얼을 이용한 업무 수행

README는 제품 구조와 공식 매뉴얼 진입점을 제공합니다. 실제 개발·운영 작업은 역할별 매뉴얼의 선행 조건, 단계별 절차, 정상 결과, 오류·복구, 감사 확인 순서로 수행합니다.

- 신규 업무 기능 개발: [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md)
- Batch 개발·실행·복구: [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.md)
- ADM 기능 개발: [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.md)
- ADM 전체 화면 운영: [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.md)
- 설치·설정·배포·복구: [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md)
- BZA 전체 기능: [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.md)
- Gateway 전체 기능: [91 Gateway 매뉴얼](cpf-docs/guides/91_Gateway매뉴얼.md)
