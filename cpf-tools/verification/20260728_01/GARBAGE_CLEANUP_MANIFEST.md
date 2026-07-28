# CPF 20260728_01 Garbage / Stale Artifact Manifest

## 자동 삭제 대상

아래 3개는 BZA login exact replay 구현으로 Consumer/Query contract에서 제거된 stale runtime SQL이며 cleanup script가 삭제한다.

- `cpf-tools/db/vendor/mariadb/runtime/bza/repository/auth-revoke-refresh-by-login-operation.sql`
- `cpf-tools/db/vendor/postgresql/runtime/bza/repository/auth-revoke-refresh-by-login-operation.sql`
- `cpf-tools/db/vendor/oracle/runtime/bza/repository/auth-revoke-refresh-by-login-operation.sql`

`sync-platform-runtime-query-packs.ps1`도 canonical contract에 없는 generated runtime SQL을 non-Check sync 시 제거하도록 보강했다.

## Codex 검토 후 삭제 대상

다음 패턴은 무조건 삭제하지 않고 Git ownership/Evidence 유효성을 확인한 후 삭제한다.

- `*.tmp`, `*.bak`, `*.orig`, `*.rej`, 작업용 `*.patch`
- `build/`, `.gradle/`, frontend `node_modules/`, dist/build 결과
- 실행 중 생성된 logs/tmp
- 기준 SHA가 오래된 stale Evidence
- duplicate Guide/Request/Handover
- Consumer 없는 legacy implementation
- Generated artifact인데 canonical source와 drift 난 복사본

## 보호 대상

- 현재 Commit 검증에 필요한 실제 Evidence 원본
- canonical DB source와 generator template
- install/upgrade/rollback/recovery script
- Public API/SPI와 실제 Consumer
- BAT standalone artifact source
- QA/Handover/validation 정본

최종 cleanup 후 `check-repository-hygiene.ps1`와 `git status --short`를 Evidence로 남긴다.
