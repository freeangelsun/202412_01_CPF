# CPF GAP 매트릭스

작성 시각: 2026-07-10 10:00:22 +09:00

| 영역 | 상태 | 남은 작업 | 근거 |
| --- | --- | --- | --- |
| PFW EDU 샘플 커버리지 | 완료 | catalog-only였던 PFW call/broker/file/runtime/security 항목을 실제 샘플과 테스트로 전환 | `specs/evidence/20260709_03/sample-coverage-result.sanitized.json` |
| Java 25 Gradle 테스트 | 재확인 필요 | Gradle 8.11.1과 Java 25 test task 호환성 확인 또는 Gradle 업그레이드 검토 | `specs/evidence/20260709_03/java25-compatibility-result.sanitized.json` |
| 외부 runtime 검증 | 미검증 | Kafka/MQ, SFTP/FTP/SCP/SSH, Vault/KMS, 다중 WAS runtime 검증 필요 | 없음 |
| Architecture ownership | 재확인 필요 | CMN 파일/메시지 기술 구현을 PFW port adapter 후보로 추적 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` |
| Archive TAR | 부분 구현 | Java 표준 라이브러리 외부 TAR 라이브러리 도입 여부 결정 필요 | `specs/evidence/20260709_03/archive-advanced-test.sanitized.json` |
