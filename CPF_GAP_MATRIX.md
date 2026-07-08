# CPF 남은 Gap 매트릭스

## 목적

`CPF_FINAL_TARGET_REQUIREMENTS.md` 기준으로 이번 작업 이후에도 완료로 볼 수 없는 항목을 추적합니다. 완료 증적이 없는 작업은 완료로 표시하지 않습니다.

| 우선순위 | 영역 | 상태 | 남은 작업 | 완료 기준 |
| --- | --- | --- | --- | --- |
| 1 | PFW Service Call Engine | 부분 구현 | 다중 서비스 실기동 HTTP runtime, timeout budget 전파, health scheduler 주기 실행 검증 | 실제 ACC/MBR/EXS/ADM 동시 기동 후 성공/실패/timeout/retry/circuit/call-history 검증 |
| 1 | ADM Service Registry | 미검증 | `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime`로 실제 API 호출 검증 | ADM 서버 기동 후 services/endpoints/instances/health/routing/circuit/call-history API 통과 |
| 1 | report/matrix/evidence gate | 완료 | report, evidence index, feature matrix 상태 정합성 유지 | `scripts/check-report-matrix-evidence-consistency.ps1` 통과 |
| 2 | ADM browser click | 미검증 | Playwright/브라우저 클릭 검증 환경 준비와 실제 클릭 증적 추가 | `adm-ui-browser-smoke-result.sanitized.json`이 SKIPPED가 아닌 성공 상태 |
| 2 | Redis/Kafka/MQ broker | 미검증 | 실제 broker pub/sub, fallback, 장애 복구, DLQ/replay 검증 | broker runtime smoke JSON 생성 |
| 2 | PFW broker capability | 부분 구현 | Kafka/MQ/Redis Stream 실제 adapter, outbox/inbox, DLQ/replay, ordering/idempotency runtime 구현 | PFW port 기반 실제 broker runtime smoke와 장애 시나리오 통과 |
| 2 | PFW file transfer capability | 부분 구현 | SFTP/FTP/FTPS/SSH adapter, credential, checksum, temp/rename/archive, 전송 이력 runtime 구현 | 실제 파일 전송 서버 대상 송수신 smoke와 이력 검증 통과 |
| 2 | PFW security/credential capability | 부분 구현 | Vault/KMS/HSM/secret manager, key/cert rotation, mTLS/JWT/OAuth 실연동 | 원문 secret 미노출 상태의 credential runtime smoke 통과 |
| 2 | PFW runtime control capability | 부분 구현 | 분산 lock/heartbeat/ghost detector 저장소 구현과 다중 인스턴스 검증 | 다중 worker 환경에서 lock/heartbeat/ghost smoke 통과 |
| 2 | CMN 기술 engine ownership | 재확인 필요 | CMN KafkaTemplate, 파일전송 protocol 처리 후보가 helper인지 engine인지 재분류 | 기술 engine은 PFW port로 이동하고 CMN은 프로젝트 공통 규칙/helper로 정리 |
| 2 | Service Registry UI | 부분 구현 | 실제 브라우저 클릭 검증과 운영자 UX 세부 개선 | 정적 UI marker 통과 후 브라우저 클릭 증적 추가 |
| 3 | 문서 mojibake 정리 | 재확인 필요 | 오래된 HTML/Java 주석의 깨진 문자열 전수 정리 | 강화된 UTF-8/mojibake gate 통과 |
