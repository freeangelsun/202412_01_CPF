# 운영/보안 가이드

> 이 문서는 deprecated 보조 문서입니다. 정본은 `specs/관리자_가이드.html`, `specs/프레임워크_구성_가이드.html`, `specs/기능_구현_매트릭스.md`를 참조하세요.

## Secret

- prod profile에는 DB password, JWT secret, crypto key 기본값을 두지 않습니다.
- 로컬 예시 값은 운영에서 사용하지 않습니다.
- 운영에서는 환경변수, Vault, KMS, Secret Manager 중 하나로 secret을 주입합니다.

## DB 권한

- migration 계정만 DDL 권한을 가집니다.
- app 계정은 DML 권한만 가집니다.
- smoke 검증에는 app 계정이 DDL을 수행하지 못하는지 확인하는 절차를 포함합니다.

## 시큐어코딩 기준

- SQL은 parameter binding을 사용합니다.
- 파일 경로는 base directory 밖으로 벗어나지 않도록 정규화합니다.
- 로그에는 password, token, 주민번호, 계좌번호 원문을 남기지 않습니다.
- CORS/CSRF는 외부 공개 서비스 기준으로 profile별 명시 설정합니다.
- 내부 오류 stack trace는 응답에 노출하지 않고 trace id만 반환합니다.

## ADM 운영 보강

- 비밀번호 초기화, 잠금 해제, 권한 변경, 배치 실행/재수행/중지는 감사 사유를 필수로 받습니다.
- ADM API 필터는 메뉴 권한과 버튼/행위 권한을 모두 확인합니다.
- IP allowlist와 MFA 상태는 ADM 보안 운영 API에서 관리합니다.
- 만료/폐기 세션은 세션 관리 API와 scheduler 후보로 정리합니다.

## 로그/장애 대응

- 거래 로그는 `pfw_transaction_log`, 상세 로그는 `pfw_transaction_log_detail`에 남깁니다.
- 운영 행위는 `adm_audit_log`와 기능별 operation log에 남깁니다.
- 민감정보는 마스킹된 값만 조회/다운로드할 수 있게 합니다.
- 장애 복구 시 DB 백업, 설치 SQL 버전, Flyway migration 버전을 함께 확인합니다.
