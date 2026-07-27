# CPF 20260727_04 Root Patch 적용 가이드

## 기준

- 적용 기준 master: `702bf83580b9c4db2dbba6482ece233e00842f1b` (`20260727_03`)
- Change Set: `STACK / ARTIFACT / BASELINE SAFETY`
- 형식: Repository Root 상대경로 전체 파일
- 삭제 목록: 없음
- ChatGPT Commit/Push/Branch/Tag/Release: 없음

## 적용

ZIP 내부의 `build.gradle`, `settings.gradle`, `cpf-*`, `cpf-tools`, `cpf-docs`, `gradle` 경로를 CPF Repository Root에 그대로 덮어쓴다.

적용 전 최신 master SHA가 기준과 다른 경우 무조건 덮어쓰지 말고 최신 Diff와 충돌/Side Effect를 먼저 재검토한다.

## 적용 후 우선 확인

현재 Patch는 정적 검토만 수행했으며 다음은 실행 미검증이다.

1. Java 25 / Gradle 9.1 configuration
2. `checkCpfStackSupport`
3. `aggregateQualityBuild`
4. Included BOM/Convention Plugin build
5. `publishCpfVerifiedLocalPlatformArtifacts`
6. `verifyCpfLocalArtifactPropagation`
7. Generated standalone Domain LOCAL_DEV/OFFLINE
8. bootJar/bootWar
9. Remote Registry

다만 Codex를 즉시 투입하지 않고 다음 ChatGPT Change Set을 계속 진행할 경우,
후속 변경 영향까지 누적한 뒤 검증 범위를 다시 계산한다.

## 금지

- 이 Patch 적용만으로 `검증 완료` 처리 금지
- 사용자 승인 없는 Commit/Push 금지
- `REMOTE/OFFLINE`에서 개발자 Local Maven fallback 금지
- 범용 Gradle `publish`를 공식 Artifact 배포 Entry로 사용 금지
