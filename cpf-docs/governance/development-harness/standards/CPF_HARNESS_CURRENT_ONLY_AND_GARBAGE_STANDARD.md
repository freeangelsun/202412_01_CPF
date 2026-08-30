# CPF Development Harness Current-only·Garbage 관리 표준

1. `cpf-docs/governance/development-harness/` 하나만 개발 진행 정본이다.
2. `development-harness-v*`, `_old`, `_backup`, `_history`, `_session`, `_checkpoint`, `rerun-*`, 날짜 복제 Harness를 금지한다.
3. Harness 변경은 기존 현행 파일을 갱신하고 `PACKAGE_MANIFEST.json`, `SHA256SUMS.txt`, `SOURCE_IDENTITY.json`, Evidence를 다시 만든다.
4. 과거 개발 정본/원장 경로는 Migration Map에서 `MAPPED/MERGED/SUPERSEDED`와 새 경로가 증명된 뒤 Delete Manifest로 제거한다.
5. `UNMAPPED>0`이면 삭제와 Harness 전환을 중단한다.
6. 제품 동작에 필요한 immutable DB migration/release history는 garbage가 아니다.
7. Canonical/legacy Source 삭제는 exact root-relative **file** allowlist + 적용 직전 SHA-256 검증을 기본으로 한다. wildcard, repository-wide recursive delete, `git clean/reset/restore`는 금지한다.
8. Build/cache/generated garbage는 실행마다 byte/SHA가 바뀌므로 canonical Source 삭제와 같은 immutable SHA 계약을 강제하지 않는다. 대신 Current Garbage Decision에서 generated ownership이 확인된 **exact root-relative generated directory**만 `GENERATED_ROOT`로 허용한다. 적용 명령은 Repository 내부 containment, 허용 generated leaf(`build`, `.gradle`, `__pycache__`, `.pytest_cache`, `node_modules` 등), symlink/reparse-point 금지, protected/current Harness 경로 금지를 먼저 확인한 뒤 해당 exact root 하나만 재귀 삭제한다. 경로 문자열 변환으로 선행 `.`을 제거하거나 이름을 재작성하는 Manifest 생성은 금지한다.
9. 삭제 후 empty directory는 허용된 old/generated root의 부모를 아래에서 위로 검사해 **빈 디렉터리만** 제거한다.
10. Fresh Replay에서 **활성 Source/Consumer/Config/Script의 old canonical path reference 0**, current Harness 1, missing Consumer 0을 확인한다. Migration Map, exact Delete Manifest, Current Garbage Decision, historical Evidence provenance, 그리고 해당 legacy path를 탐지하는 Validator 내부 literal은 추적·검증 목적의 허용 참조이며 active stale reference로 계산하지 않는다. 허용 영역 밖에서 legacy canonical path가 발견되면 FAIL한다.
