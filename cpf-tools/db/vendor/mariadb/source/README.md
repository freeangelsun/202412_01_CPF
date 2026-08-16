# CPF Platform MARIADB Source

Status: `implemented`

이 디렉터리는 `mariadb`용 Platform DB SQL의 사람이 수정하는 Vendor 정본 경계다.
모든 Vendor는 `cpf-tools/db/vendor/<vendor>/source` 동일 경로 계약을 사용한다.

독립 `cpf-tools/db/source/mariadb` 경로는 폐기되었으며 복구하지 않는다.
수정 후 `cpf-tools/db/tools/sync-database-artifacts.ps1`을 실행하여 install/seed/migration/verify/manifest를 동기화한다.
