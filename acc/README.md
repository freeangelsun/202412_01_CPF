# Account 주제영역 골격

이 디렉터리는 scripts/create-domain.ps1로 생성한 신규 업무 모듈 후보입니다.

- 실제 반영 전 settings.gradle, specs/sql, ADM 메뉴/API/버튼 seed, OpenAPI 문서를 함께 검토합니다.
- Controller, Facade, Service, Repository, DTO, Mapper XML, SQL의 모듈 코드와 테이블 prefix를 일치시킵니다.
- 운영 로그는 ${Dollar}{CPF_LOG_ROOT}/{environment}/{moduleCode}/{instanceId}/{category}/cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log 규칙을 사용합니다.