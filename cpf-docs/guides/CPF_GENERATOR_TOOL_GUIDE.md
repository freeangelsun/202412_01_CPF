# CPF 업무영역 생성기 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무영역 설계자, 생성기 운영자, 플랫폼 개발자
> **목적**: 충돌 없이 업무영역을 생성하고 사용자 코드 보호·업그레이드·제거까지 관리한다.
> **관련 문서**: [공개 API와 생성 업무영역](CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md) · [데이터베이스 프로필과 업무영역 DB](DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md)

---


<picture>
  <source media="(max-width: 720px)" srcset="../assets/readme/cpf-domain-journey-mobile.png">
  <img src="../assets/readme/cpf-domain-journey-desktop.png" alt="CPF 업무영역 생성과 검증 흐름" width="100%">
</picture>

## 1. 목적

CPF 생성기는 신규 업무영역을 CPF 제품 표준과 동일한 구조로 생성한다. 특정 업무명에 종속된 복사본을 만들지 않고 하나의 기준 템플릿을 사용한다.

정본 명령:

```powershell
cpf-tools/generator/create-domain.ps1
```

## 2. 생성 원칙

- `DomainName`과 `SystemCode`를 분리한다.
- 공식 모듈은 `cpf-` 접두사를 사용한다.
- Java Root 패키지는 `com.cpf.<domain>`을 사용한다.
- 업무영역은 자신의 API, 애플리케이션, 업무영역, 어댑터와 DB를 소유한다.
- 공개 API/SPI만 사용한다.
- Oracle, PostgreSQL, MariaDB를 같은 제품 규칙으로 생성한다.
- 사용자 수정 영역을 덮어쓰지 않는다.
- 생성 계획과 충돌을 적용 전에 확인한다.

## 3. 입력

### 3.1 식별

| 입력 | 설명 | 예 |
|---|---|---|
| `DomainName` | 사람이 읽는 업무명 | `payment` |
| `SystemCode` | 3자리 대문자 내부 코드 | `PAY` |
| `ModuleName` | Gradle 모듈 | `cpf-payment` |
| `PackageName` | Java 패키지 | `com.cpf.payment` |
| `SchemaName` | 논리 스키마 | `payDB` |
| `TablePrefix` | Table Prefix | `pay_` |
| `Port` | 독립 실행 환경 Port | `18080` |

### 3.2 데이터베이스

- `DatabaseVendor`
- 호스트 / Port
- 데이터베이스 / 스키마
- Admin / 이관 / 실행 환경 User
- 비밀값 참조
- Provision 여부
- Product / Optional / 테스트 Seed 정책

### 3.3 Capability

- `database`
- `remote-call`
- `messaging`
- `file`
- `external`
- `batch`
- `center-cut`
- `security-audit`
- `ui`
- `bza-menu`

실제 매개변수 이름과 기본값은 스크립트 도움말을 정본으로 한다.

## 4. 사전 계획

첫 실행은 반드시 사전 계획으로 수행한다.

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "postgresql" `
  -Capabilities "database,remote-call,messaging" `
  -DryRun
```

사전 계획 결과:

- 생성 파일 목록
- 변경 파일 목록
- 충돌
- 예약 식별자
- Port와 경로
- DB와 스키마
- 생성기 소유 영역
- 적용 순서
- 삭제 예정 파일
- 예상 Build Project

## 5. 적용

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "postgresql" `
  -Capabilities "database,remote-call,messaging" `
  -Apply
```

적용은 사전 계획과 같은 입력으로 수행한다. Plan 해시를 지원하는 경우 동일 Plan임을 확인한다.

## 6. 생성 구조

```text
cpf-payment/
├─ build.gradle
├─ README.md
├─ deploy/
│  ├─ database/
│  ├─ runtime/
│  └─ manifests/
├─ src/main/java/com/cpf/payment/
│  ├─ api/
│  ├─ application/
│  ├─ domain/
│  ├─ adapter/
│  └─ config/
├─ src/main/resources/
│  ├─ application.yml
│  ├─ db/
│  └─ mybatis/
└─ src/test/
```

## 7. 기본 업무 예제

DB Capability는 `${TablePrefix}sample_item`을 사용한 최소 CRUD/Search/페이징/검증 예제를 생성한다.

포함 범위:

- Create
- Detail
- Search
- 페이지
- Update with `expectedVersion`
- Duplicate 오류
- 검증 오류
- 트랜잭션 되돌리기
- 공급자별 SQL
- OpenAPI
- 단위/통합 테스트

이 예제는 Golden 구조 검증용이며 고객 업무를 추정하지 않는다.

## 8. 공개 Boundary

생성 소스는 다음만 사용한다.

```text
com.cpf.core.api.*
com.cpf.core.spi.*
com.cpf.common.api.*
```

금지:

```text
com.cpf.core.internal.*
com.cpf.core.common.*
다른 업무 Domain의 persistence package
```

## 9. 데이터베이스 산출물

각 공급자에 대해 생성한다.

```text
deploy/database/
├─ mariadb/
│  ├─ install
│  ├─ migration
│  ├─ rollback
│  └─ verify
├─ postgresql/
└─ oracle/
```

생성 항목:

- Provision
- Empty Install
- Product Seed
- Optional/테스트 Seed
- 이관
- 되돌리기
- Verify
- 스키마 명세서
- Checksums
- 공급자별 MyBatis Mapper

## 10. 실행 환경 프로필

Generated 실행 환경은 환경별 프로필을 분리한다.

- local
- test
- development
- staging
- production

Production 프로필에는 다음 원칙을 적용한다.

- Memory 어댑터 금지
- 비밀값 참조
- 외부 공개 기본 거부
- Secure 헤더
- DB Pool 상한
- 시간 제한
- 상태 점검
- 구조화 로그
- Management 엔드포인트 최소화

## 11. 등록부와 엔드포인트

생성기는 등록부 등록 후보를 생성한다.

- serviceId
- systemCode
- moduleId
- 엔드포인트
- 상태 점검
- Protocol
- visibility
- gatewayAllowed
- Zone/셀
- 버전

내부 엔드포인트는 외부 게이트웨이 바인딩 후보에 자동 포함하지 않는다.

## 12. Messaging Capability

생성 항목:

- 사건 DTO
- 스키마 버전
- Publisher 서비스
- 송신함 저장소
- 소비자
- 수신함/Idempotency
- DLQ 처리 Port
- 테스트 Fixture

## 13. 파일 Capability

생성 항목:

- 파일 요청/결과
- 엔드포인트 메타데이터
- 인증정보 참조
- 체크섬
- 상태와 이력
- 실패·결과 불명
- 테스트

## 14. 배치 Capability

업무 작업 묶음 SPI를 생성한다.

- 작업 ID
- 버전
- 매개변수 스키마
- 작업/Step 구성
- 재시도/시간 제한
- 체크포인트
- Testkit
- BAT 등록부 메타데이터

## 15. 대량 실행 Capability

- 대상 공급자
- Item Handler
- Partition Key
- 점유/Fencing DTO
- 내부 구현 엔드포인트
- 결과 상태
- 재처리 테스트

## 16. 화면과 BZA Menu

화면 Capability는 독립 Feature Directory를 만든다.

```text
features/payment/
├─ api
├─ model
├─ state
├─ components
├─ pages
└─ tests
```

BZA Menu Seed는 중복 경로/권한을 검사한다. 프런트엔드 경로와 백엔드 권한을 함께 생성한다.

## 17. 사용자 수정 보호

`generator-ownership.json`은 다음을 기록한다.

- 생성기 버전
- Template 버전
- 파일 경로
- 소유 유형
- 생성 당시 체크섬
- 재생성 정책

재실행 동작:

| 상태 | 처리 |
|---|---|
| 생성기 소유 + 미수정 | 안전 갱신 |
| 생성기 소유 + 사용자 수정 | 충돌 보고 |
| 사용자 소유 | 변경 금지 |
| 삭제된 Template 파일 | 삭제 Plan에 표시 |
| 신규 Template 파일 | 생성 Plan에 표시 |

## 18. 기존 업무영역 동기화

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database -Apply
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope AllGeneratorOwned -Apply
```

적용 전에 사용자 수정 충돌을 확인한다.

## 19. 충돌 검사

- DomainName
- SystemCode
- 모듈
- 패키지
- Port
- 경로
- 실행 ID
- DB/스키마
- Table Prefix
- 설정 Key
- 큐/Topic
- 캐시 네임스페이스
- 로그 Directory
- BZA Menu
- 권한 Code

## 20. 예약 코드

CPF 공식 SystemCode와 충돌하는 코드는 거부한다.

- CPF
- CMN
- ADM
- BZA
- BAT
- GWY
- REF
- MBR

`AllowReserved`는 제품 내부 관리 작업 외에는 사용하지 않는다.

## 21. 검증 시나리오

최소 두 개의 임의 업무영역을 사용한다.

1. 서로 다른 업무영역 생성
2. Build/테스트
3. 3 공급자 DB 산출물 비교
4. 공개 Boundary 검사
5. 경로/Port 충돌
6. SystemCode 중복
7. 같은 입력 재실행
8. 사용자 수정 파일 보호
9. Remove
10. 재생성
11. 생성 업무영역 간 구조 정규화 비교
12. 실행 환경 프로필과 등록부 메타데이터 확인

## 22. 제거

제거는 명세서를 기준으로 수행한다.

- Gradle Include
- 모듈 Directory
- 로컬 Federation
- 실행 환경 등록부 후보
- Generated Seed
- DB 산출물
- Guide Link
- 테스트 Fixture

운영 DB와 고객 데이터는 제거 명령이 자동 삭제하지 않는다.

## 23. 검증 증적

- 기준 Commit
- 생성기/Template 버전
- 정확한 명령
- 인증정보를 제외한 매개변수
- 사전 계획 Plan
- 생성·변경·삭제 파일
- 충돌 테스트
- Build/테스트
- DB 산출물 해시
- 사용자 수정 보호 결과

## 24. 완료 체크리스트

- [ ] 하나의 기준 템플릿을 사용한다.
- [ ] 특정 업무명 예외가 없다.
- [ ] 공개 API/SPI만 사용한다.
- [ ] 3개 공급자 산출물을 생성한다.
- [ ] 외부 공개 기본 거부다.
- [ ] 사용자 소스를 덮어쓰지 않는다.
- [ ] 충돌을 생성 전에 검출한다.
- [ ] 제거·재생성이 가능하다.
- [ ] README/OpenAPI/테스트/검증 증적이 생성 결과와 일치한다.

## 부록 A. 핵심 매개변수

| 매개변수 | 필수 | 예 | 검사 |
|---|---|---|---|
| `DomainName` | 예 | `payment` | 영문 소문자, 예약어·기존 모듈 충돌 |
| `SystemCode` | 예 | `PAY` | 3자리 대문자, 전체 저장소 유일 |
| `BasePackage` | 기본값 | `com.cpf.payment` | 금지 패키지·기존 소유권 충돌 |
| `DatabaseVendor` | 예 | `postgresql` | `oracle`, `postgresql`, `mariadb` |
| `Port` | 환경별 | `18081` | 다른 실행 환경·문서·경로 충돌 |
| `Capabilities` | 선택 | `api,messaging,file` | 의존성·설정·DB 산출물 일관성 |
| `DryRun` | 권장 | 스위치 | 파일 변경 없이 계획 출력 |
| `Apply` | 적용 시 | 스위치 | 계획 해시와 확인 |

실제 전체 옵션은 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 따른다.

## 부록 B. 재실행 정책

재실행 시 파일을 `변경 없음`, `생성`, `정본 변경`, `사용자 변경`, `충돌`, `제거 후보`로 분류한다. 사용자 변경과 정본 변경이 같은 영역에 겹치면 자동 병합하지 않고 차이와 선택지를 제시한다.

## 부록 C. 이름 변경과 제거

업무명·시스템 코드 변경은 단순 문자열 치환이 아니다. 모듈, 패키지, API 경로, 서비스 등록, 권한, DB 소유권, 메시지 유형, 배치 작업과 검증 증적의 호환·이관 계획이 필요하다.

제거는 `사용 중인 공개 API → 이벤트 소비자 → DB 자료 → 운영 경로 → 배치·설정 → 산출물` 순으로 영향도를 확인하고 되돌리기 계획을 만든다.
