# CPF CoreFlow Platform Framework

CPF는 금융권을 포함한 복잡한 업무 시스템을 일관된 표준으로
구축·운영·감사·확장·배포하기 위한 Java 기반 Core Business Platform Framework입니다.

단순 공통 라이브러리가 아니라 온라인 거래, 내부 서비스 호출,
배치·대량처리, 외부 시스템 연계, 메시징, 파일 전송,
보안, 감사, 운영 관제와 장애 복구를 하나의 제품 계약으로 제공합니다.

## 제품 목표

- MSA와 modular monolith 동시 지원
- 동일 JVM과 분리 WAS의 동일 typed contract
- 다중 인스턴스, timeout, retry와 recovery
- 금융권 수준 보안·권한·감사·마스킹
- 멱등성·비동기·재처리·결과 불명 거래 복구
- 외부연계·파일·첨부·압축·메시징
- Batch Agent·Scheduler·Center-Cut
- ADM/BZA 운영 조회·제어·감사
- 신규 업무 domain generator와 EDU
- install·migration·upgrade·rollback·release
- source·SQL·API·test·document·evidence 정합성

## 모듈

- `pfw`: 기술 공통 contract, runtime capability와 extension API
- `pfw-gateway-runtime`: 선택적 외부 진입 runtime
- `cmn`: 여러 업무가 공유하는 프로젝트 업무 공통
- `adm`: 운영 control plane
- `bat`: batch control plane과 agent/worker runtime
- `mbr`, `acc`, `bza`: 독립 업무 주제영역
- `xyz`: EDU/reference 업무 주제영역
- 생성 domain: generator로 생성되는 신규 업무 주제영역
- `exs`: 필요 시 일반 generator로 생성하는 외부연계 업무 주제영역

## Architecture

```text
Client / Channel
      |
PFW Gateway Runtime
      |
Business Domain ── typed local/remote contract ── Business Domain
      |                                             |
      └──────────── PFW common capabilities ────────┘
                            |
              BAT Control Plane / Worker
                            |
                           ADM
```

## 개발자 확장 모델

1. 안전한 framework default
2. type-safe property, environment와 secret
3. 요청·feature 단위 typed option, strategy와 SPI

TLS 검증, 필수 masking, 감사, endpoint allowlist,
secret 보호와 최대 timeout·retry 같은 보안 불변조건은 override할 수 없습니다.

## Package 표준

```text
cpf.<domain>.<feature>/
├─ controller/
├─ application/
├─ domain/
├─ dto/
├─ port/
├─ adapter/
├─ repository/
├─ mapper/
├─ validation/
└─ config/
```

## Generator

ACC는 업무 domain generator의 정식 검증 대상입니다.

```text
ACC snapshot
→ delete
→ residual scan
→ generate
→ clean tree review
→ compile/test/package/OpenAPI
→ DB/runtime
→ local/remote
→ generated/business diff
→ regenerate parity
```

## Frontend

ADM과 BZA는 Vue 3 SFC·TypeScript·feature architecture를 사용합니다.

- package lock
- lint·typecheck
- unit·component·API mock
- production build
- Gradle/JAR/WAR integration
- browser E2E
- accessibility
- CSP/XSS/CSRF
- source/dist 분리

## DB·Release

- canonical SQL single source
- MariaDB runtime 검증
- Oracle·PostgreSQL·SQL Server 구조
- Flyway
- install·rerun·upgrade·rollback·reset·uninstall
- JAR/WAR·sources·JavaDoc
- checksum·SBOM·provenance

## 빠른 시작

필수 도구:

- JDK 25
- Gradle Wrapper
- MariaDB 10.6+
- PowerShell
- Node.js와 package manager: frontend build 시

```powershell
.\gradlew.bat test
.\gradlew.bat qualityGate -PcpfResultDir=specs/evidence/local
```

## 문서

- `CPF_FINAL_TARGET_REQUIREMENTS.md`: 최종 제품 요구사항
- `CPF_CURRENT_WORK_REQUEST.md`: 현재 전체 작업 요청
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`: 검수·완료 판정
- `CPF_STABILIZATION_REPORT.md`: 최신 검증 결과
- `CPF_GAP_MATRIX.md`: 목표와 현재 차이
- `CPF_EVIDENCE_INDEX.md`: evidence 위치와 유효성
- `specs/기능_구현_매트릭스.md`: 기능별 상태
- `specs/sample-coverage-matrix.md`: EDU coverage

README에는 작업 일지, commit, 진행률과 남은 gap을 기록하지 않습니다.

## License

개인의 비상업적 학습·연구·실험 목적 사용은 허용됩니다.
그 외 사용은 Team Pixel의 사전 승인 또는 별도 계약이 필요합니다.

**Team Pixel**  
freeangelsun@gmail.com
