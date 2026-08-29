# CPF Development Harness Current-only·Garbage 관리 표준

1. `cpf-docs/governance/development-harness/` 하나만 개발 진행 정본이다.
2. `development-harness-v*`, `_old`, `_backup`, `_history`, `_session`, `_checkpoint`, `rerun-*`, 날짜 복제 Harness를 금지한다.
3. Harness 변경은 기존 현행 파일을 갱신하고 `PACKAGE_MANIFEST.json`, `SHA256SUMS.txt`, `SOURCE_IDENTITY.json`, Evidence를 다시 만든다.
4. 과거 개발 정본/원장 경로는 Migration Map에서 `MAPPED/MERGED/SUPERSEDED`와 새 경로가 증명된 뒤 Delete Manifest로 제거한다.
5. `UNMAPPED>0`이면 삭제와 Harness 전환을 중단한다.
6. 제품 동작에 필요한 immutable DB migration/release history는 garbage가 아니다.
7. 삭제는 exact root-relative file allowlist만 허용한다. wildcard, directory recursive delete, `git clean/reset/restore` 금지.
8. 삭제 후 empty directory는 허용된 old root 아래에서 **빈 디렉터리만** 제거한다.
9. Fresh Replay에서 **활성 Source/Consumer/Config/Script의 old canonical path reference 0**, current Harness 1, missing Consumer 0을 확인한다. Migration Map, exact Delete Manifest, Current Garbage Decision, historical Evidence provenance, 그리고 해당 legacy path를 탐지하는 Validator 내부 literal은 추적·검증 목적의 허용 참조이며 active stale reference로 계산하지 않는다. 허용 영역 밖에서 legacy canonical path가 발견되면 FAIL한다.
