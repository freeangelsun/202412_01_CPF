# CPF Current Work Request

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 Overlay 기준 SHA: `0c502b917cd2185cf1ff097c5beac3e5aefb00ac`
- 개발 산출물: `CPF_20260730_QA30_FULL_COMPLETION_ROOT_OVERLAY.zip`

## 현재 해야 할 일

신규 개발 잔여 항목은 없다. 현재 작업은 Overlay 적용, 사용자 Commit·Push, 그리고 실행 환경이 필요한 exact-SHA 독립 검증이다.

1. Clean SHA `0c502b917cd2185cf1ff097c5beac3e5aefb00ac`에 Overlay 적용 스크립트를 실행한다.
2. 적용 스크립트가 구형 Gateway 병렬 모델 3개를 제거하고 `git diff --check`를 통과하는지 확인한다.
3. 변경 Source·SQL·Frontend·Evidence를 검토하고 사용자가 Commit·Push한다.
4. Push SHA에서 Java 25 전체 Gradle, ADM/BZA Frontend, Oracle·PostgreSQL·MariaDB Lifecycle을 실행한다.
5. Redis·Multi-instance·Gateway/Batch Runtime·Browser E2E·Failure Injection을 실행한다.
6. 민감정보를 제거한 Evidence를 `cpf-docs/evidence/20260730_qa30`에 exact SHA와 함께 저장한다.
7. `verify-cpf-qa30-full-completion.ps1`을 3개 DB Profile로 실행한다.
8. Codex는 변경 없이 독립 검수만 수행한다.

## 완료 처리 금지 조건

- 실행하지 않은 검증을 PASS로 기록
- Overlay 기준 SHA와 적용 대상 HEAD 불일치
- 구형 Gateway 병렬 모델이 남은 상태
- 3 Vendor 중 일부만 검증하고 DB 완료 처리
- Checksum Manifest를 검증 중 자동 갱신
- Runtime/Browser/Multi-instance 결과 없이 Full Product Gate PASS 처리

## 사용자 승인 경계

ChatGPT는 Commit, Push, Branch, Tag, PR을 생성하지 않았다.
