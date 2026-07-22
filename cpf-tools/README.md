# CPF Tools

`cpf-tools`는 CPF 제품 사용자가 직접 호출하는 생성·검증·이관 도구의 공개 진입점입니다.
실제 구현은 저장소 `scripts`에서 관리하고, 이 디렉터리의 명령은 안정적인 사용자 경로를
제공합니다.

## Generator Lifecycle

| 단계 | Windows | Linux/macOS |
|---|---|---|
| 생성 | `generator/create-domain.ps1` | `generator/create-domain.sh` |
| DB 초기화 | `generator/initialize-domain-database.ps1` | `generator/initialize-domain-database.sh` |
| 검증 | `generator/verify-domain.ps1` | `generator/verify-domain.sh` |
| 제거 | `generator/remove-domain.ps1` | `generator/remove-domain.sh` |

입력 규격과 전체 예시는 [Generator Guide](../cpf-docs/development/GENERATOR_GUIDE.md)를
참고합니다. DB 비밀번호는 명령행에 직접 적지 않고 `CPF_DOMAIN_DB_PASSWORD` 환경변수로
전달합니다.
