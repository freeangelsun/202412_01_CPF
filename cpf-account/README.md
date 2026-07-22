# ACC 생성기 기준 주제영역

`acc`는 CPF 신규 업무 모듈 생성 결과를 지속 검증하는 reference domain입니다. 독립 제품이나 기본 업무 배포 대상이 아니며, 생성기가 제공해야 하는 온라인 API, 공유 API, 배치, 다중 DataSource, SQL/Flyway, 배포 profile과 테스트 계약을 실제 컴파일 가능한 형태로 유지합니다.

## 구조

- `com.cpf.account.account`: validation, 낙관적 버전, 논리 삭제와 변경 감사를 포함한 대표 계정 CRUD
- `com.cpf.account.reference`: 생성기 기준 조회, local adapter와 remote proxy를 포함한 reference 기능
- `com.cpf.account.config`: ACC 업무 DB와 CPF 배치 메타 DB 설정
- `manifest/standard-execution-catalog.json`: ACC O/S/B 표준 실행 카탈로그

업무 데이터는 `accDB.acc_account*`가 소유하고 Spring Batch 원천 메타는 `cpfDB.BATCH_*`를 사용합니다. 다른 주제영역은 ACC Repository나 Mapper에 직접 접근하지 않고 CMN facade contract와 CPF Service Call Engine을 사용합니다.

## 빌드와 실행

```powershell
.\gradlew.bat :cpf-account:test :cpf-account:bootJar :cpf-account:bootWar --no-daemon
.\gradlew.bat :cpf-account:bootRun --args='--spring.profiles.active=local'
```

신규 주제영역 생성과 제거는 루트의 `scripts/create-domain.ps1`, `scripts/remove-domain.ps1`을 사용합니다. 생성 결과를 제품 구조에 반영할 때는 SQL/Flyway, 표준 실행 카탈로그, ADM/BZA seed, profile, 배포 inventory와 OpenAPI를 함께 검토합니다.
