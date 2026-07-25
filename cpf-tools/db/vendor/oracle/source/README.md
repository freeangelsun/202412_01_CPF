# CPF Platform ORACLE Source

Status: `not-implemented`

이 디렉터리는 `oracle`용 Platform DB SQL의 사람이 수정하는 Vendor 정본 경계다.
모든 Vendor는 `cpf-tools/db/vendor/<vendor>/source` 동일 경로 계약을 사용한다.

Platform DDL/Runtime Pack은 아직 구현되지 않았다.
MariaDB SQL을 복사하거나 fallback하지 않는다. 해당 Vendor 구현이 완료되기 전 installer/runtime은 fail-closed해야 한다.
Generated Domain의 Vendor template 지원과 Platform full pack 지원을 혼동하지 않는다.
