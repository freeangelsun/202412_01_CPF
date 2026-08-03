# CPF QA39 OPEN_ISSUES

## 미검증 항목

1. 전체 Java Build·Test·Check
2. ADM/BZA Frontend 및 3 Browser Playwright
3. Oracle·PostgreSQL·MariaDB 실제 Lifecycle
4. 외부 연계 장애·복구, UNKNOWN, Reconcile, 다중 인스턴스 Runtime
5. Publication·SBOM·Artifact Repository
6. GitHub Advanced Security 기반 Secret Scan

## 현재 확인된 Source 결함

최종 정적·구조 Fixture 기준으로 알려진 미해결 Source 결함은 없다. 다만 위 Runtime 검증을 수행하지 않았으므로 전체 완료 또는 QA 통과로 판정하지 않는다.

## Cleanup 범위

`CPF_QA39_FINAL_CLEANUP.ps1`은 `DELETE_MANIFEST.csv`에 기록된 정확한 경로만 처리한다. 보호 경로, 사용자 기존 파일, 다른 GPT 산출물, Manifest에 없는 경로는 삭제하지 않는다.
