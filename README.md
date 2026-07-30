<div align="center">

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-hero-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-hero-desktop.png" alt="Core Platform Framework — 업무 시스템을 위한 하나의 실행·운영 기준" width="100%">
</picture>

<br>

**설계에서 운영까지, 업무 시스템이 따라야 할 기준을 하나로 연결합니다.**

동일 JVM · 분리 WAS · 모듈형 단일체 · 마이크로서비스 · 다중 인스턴스 · 배치 · 외부 연계 · 운영 통제

[문서 홈](cpf-docs/guides/README.md) · [구조와 배포](cpf-docs/guides/CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [개발 시작](cpf-docs/guides/CPF_DEVELOPER_GUIDE.md) · [운영 시작](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md) · [설치와 업그레이드](cpf-docs/guides/CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

</div>

---

## 프로젝트가 커질수록 필요한 것은 기능보다 기준입니다

업무 시스템은 화면과 API를 만드는 것만으로 끝나지 않습니다. 배포 방식이 달라져도 호출 계약이 유지돼야 하고, 다중 인스턴스에서 중복 실행을 막아야 하며, 장애 뒤에는 실제 처리 결과를 확인하고 복구할 수 있어야 합니다. 운영자는 서비스·거래·로그·배치·설정의 상태를 연결해 보고, 위험한 조치는 권한·사유·승인·감사와 함께 수행해야 합니다.

CPF는 이러한 요구를 프로젝트마다 다시 조립하지 않도록 **공개 계약, 실행 규칙, 운영 제어, 데이터베이스 생명주기, 업무영역 생성과 검증 기준**으로 제공합니다.

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-product-map-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-product-map-desktop.png" alt="CPF 제품 지도" width="100%">
</picture>

### 같은 계약으로 연결합니다

동일 JVM에서는 로컬 어댑터로, 분리된 실행 환경에서는 원격 어댑터로 호출합니다. 경로가 바뀌어도 표준 헤더, 인증·권한 문맥, 오류 분류, 시간 제한, 멱등성, 추적 정보의 의미는 유지됩니다.

### 실패를 정상 설계에 포함합니다

재시도, 회로 차단, 결과 불명, 상태 대사, 재처리와 보상은 장애가 난 뒤 덧붙이는 예외 처리가 아닙니다. 온라인 거래, 비동기 메시지, 파일 연계와 배치가 같은 식별자와 상태 원칙을 사용합니다.

### 운영 명령이 실제 실행과 연결됩니다

운영 화면에서 정의와 상태만 보여주는 데 그치지 않습니다. 서비스 등록부 제어, Gateway 적용 확인, Batch 실행 투영, 로그 수집 정책과 감사된 반출이 각 소유 실행 환경의 공개 제어 계약으로 연결됩니다.

### 새 업무영역도 같은 품질로 시작합니다

생성기는 모듈, 패키지, 공개 API, 데이터베이스, 테스트, 설정, 문서와 배포 정보를 함께 만들고 충돌을 사전에 검사합니다. 생성기 소유 영역과 고객 수정 영역을 분리해 재실행 시 사용자 코드를 보호합니다.

---

## 배포가 달라도 업무 계약은 같습니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-topology-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-topology-desktop.png" alt="CPF 배포 구성과 호출 경로" width="100%">
</picture>

- 업무영역으로 직접 진입하거나, 공통 진입 정책이 필요할 때만 Gateway를 선택합니다.
- 같은 JVM에서는 공개 계약을 메모리 안에서 호출하고, 분리 WAS에서는 같은 계약을 원격 호출로 전달합니다.
- 모듈형 단일체에서 시작해 업무영역별 마이크로서비스로 분리해도 호출자의 업무 코드는 유지됩니다.
- 다중 인스턴스에서는 임대(Lease), 점유(Claim), 세대 토큰(Fencing Token), 버전과 낙관적 잠금으로 오래된 실행자의 늦은 결과를 차단합니다.
- Batch의 제어 서버, Scheduler, Worker, Center-Cut Runner와 Host Agent는 독립 실행 프로세스로 확장할 수 있습니다.

[구조와 배포 구성 상세 보기 →](cpf-docs/guides/CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md)

---

## 실패를 숨기지 않고 복구 가능한 상태로 남깁니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-execution-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-execution-desktop.png" alt="CPF 실행과 복구의 공통 흐름" width="100%">
</picture>

상대 시스템에 요청을 보낸 뒤 응답이 유실되면 성공이나 실패로 단정하지 않습니다. CPF는 이를 `UNKNOWN_RESULT`로 보존하고 거래 식별자, 호출 시도, 상대 원장과 대사 자료를 이용해 실제 결과를 확정합니다.

확정된 결과에 따라 재처리, 보상 또는 운영 확정을 수행하고, 판단 근거와 조치 결과를 감사 및 검증 증적에 남깁니다. 이 흐름은 온라인 호출, 비동기 메시지, 외부 기관 연계, 파일 처리와 Batch 실행에 같은 원칙으로 적용됩니다.

[비동기·메시징·보상 상세 보기 →](cpf-docs/guides/CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md) · [장애대응과 복구 상세 보기 →](cpf-docs/guides/CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)

---

## 운영 제어가 실제 실행과 연결됩니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-operations-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-operations-desktop.png" alt="CPF 플랫폼 운영과 업무 관리" width="100%">
</picture>

### 서비스와 인스턴스

조회 계약과 제어 계약을 분리합니다. 등록·수정·점검·배수·점검모드 명령은 소유자의 제어 포트로 전달하고, 운영 화면은 기대 상태와 실제 상태를 함께 표시합니다.

### Gateway

ADM은 서명된 제어 요청으로 Gateway 소유 실행 환경을 호출합니다. 경로와 대상군은 버전 단위로 동기화되고, 인스턴스별 적용 결과와 확인 응답(ACK), 연결시험, 상태 점검, 구성 불일치와 거래 원장이 연결됩니다. 운영 화면은 적용 상태 변화를 실시간으로 구독할 수 있습니다.

### Batch

작업정의는 작성·검증·승인·게시 절차를 거쳐 실행 투영으로 배포됩니다. Scheduler는 승인된 투영을 일정과 동기화하고, Worker는 실행기 Registry를 통해 작업을 선택합니다. 각 실행 시도는 별도 원장에 남고, 승인된 파일 산출물은 무결성을 확인한 뒤 원격으로 전달·실행합니다.

### 로그와 민감정보

쿼리, 헤더, 요청·응답 본문과 오류 Stack은 항목별 수집 모드, 허용 목록, 최대 크기와 마스킹 정책을 적용합니다. Clipboard 복사와 파일 내려받기는 서버가 마스킹된 산출물을 생성하고 사유·행위자·대상·결과를 감사한 뒤, 만료되는 내려받기 주소로 제공합니다.

[플랫폼 운영자 가이드 →](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md) · [Gateway 운영 가이드 →](cpf-docs/guides/CPF_GATEWAY_OPERATIONS_GUIDE.md) · [Batch 실행 환경 가이드 →](cpf-docs/guides/CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md)

---

## 새 업무영역도 같은 구조로 확장합니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-domain-journey-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-domain-journey-desktop.png" alt="CPF 업무영역 생성과 검증 흐름" width="100%">
</picture>

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName payment `
  -SystemCode PAY `
  -DatabaseVendor postgresql `
  -DryRun
```

사전 계획에서는 모듈명, 패키지, 시스템 코드, 포트, 경로, 데이터베이스와 서비스 등록 정보의 충돌을 검사합니다. 적용 후에는 공개 API와 SPI, 계층 구조, 로컬·원격 호출, 세 데이터베이스 공급자 산출물, 테스트, OpenAPI, JavaDoc, 교육 예제와 실행 설정을 함께 관리합니다.

[업무영역 생성기 가이드 →](cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md)

---

## 역할에 맞는 문서에서 시작하세요

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-guide-map-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-guide-map-desktop.png" alt="CPF 역할별 문서 체계" width="100%">
</picture>

- **구조를 설계한다면** — [구조와 배포](cpf-docs/guides/CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) → [공개 API와 생성 업무영역](cpf-docs/guides/CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md)
- **업무를 개발한다면** — [개발자 가이드](cpf-docs/guides/CPF_DEVELOPER_GUIDE.md) → [기반 API](cpf-docs/guides/CPF_FOUNDATION_API_GUIDE.md) → [생성기](cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md)
- **플랫폼을 운영한다면** — [플랫폼 운영자](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md) → [관측·장애대응](cpf-docs/guides/CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)
- **설치·DB·배포를 담당한다면** — [설치·업그레이드](cpf-docs/guides/CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md) → [데이터베이스 도구](cpf-docs/guides/CPF_DATABASE_TOOL_GUIDE.md) → [산출물 공급](cpf-docs/guides/CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md)

전체 문서의 역할과 추천 순서는 [CPF 문서 홈](cpf-docs/guides/README.md)에 정리돼 있습니다.

---

<details>
<summary><strong>5분 시작 명령 보기</strong></summary>

### 준비 환경

JDK 25, Git, Gradle Wrapper, PowerShell 7, Node.js와 npm, Oracle·PostgreSQL·MariaDB 중 사용할 데이터베이스가 필요합니다.

### 전체 빌드

```powershell
.\gradlew.bat clean build
```

```bash
./gradlew clean build
```

### 데이터베이스 설치와 검증

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

### 로컬 통합 실행 환경

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

### 로컬 분산 Batch 실행 환경

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

실제 계정, 포트, 비밀값 참조, 데이터베이스 권한, 실행 Profile과 검증 절차는 [설치·업그레이드·되돌리기 가이드](cpf-docs/guides/CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)를 따릅니다.

</details>

---

## 지원 범위

**배포** — 동일 JVM, 분리 WAS, 모듈형 단일체, 마이크로서비스, 내장 실행 JAR, 외부 WAS용 WAR, 다중 인스턴스, 순차 교체·선행 배포·이중 환경 전환·재해복구 구성

**데이터베이스** — Oracle, PostgreSQL, MariaDB의 설치, 초기 자료, 검증, 이관, 업그레이드, 되돌리기, 재적용, 백업과 복구

**실행** — 온라인 서비스 호출, 비동기 이벤트와 메시징, 외부 시스템 연계, 파일·첨부·전문, Batch·Scheduler·Worker·Host Agent·Center-Cut, 운영 조회·제어·승인·감사

> CPF는 기능 목록이 아니라, 업무 시스템이 오래 유지되도록 만드는 공통 실행·운영 기준입니다.
