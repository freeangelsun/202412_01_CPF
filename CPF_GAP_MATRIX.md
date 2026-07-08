# CPF 남은 Gap 매트릭스

## 목적

`CPF_FINAL_TARGET_REQUIREMENTS.md` 기준으로 아직 완료로 볼 수 없는 항목을 추적한다. 실행 증적이 없는 검증은 완료로 표시하지 않는다.

| 우선순위 | 영역 | 상태 | 남은 작업 | 완료 기준 |
| --- | --- | --- | --- | --- |
| 1 | PFW Service Call Engine | 부분 구현 | 다중 서비스 실제 HTTP runtime, timeout budget 전파, health scheduler 주기 실행 검증 | ACC/MBR/EXS/ADM 동시 기동 후 성공/실패/timeout/retry/circuit/call-history 증적 통과 |
| 1 | ADM Service Registry | 미검증 | `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime`로 실제 ADM API 호출 검증 | services/endpoints/instances/health/routing/circuit/call-history API runtime 통과 |
| 1 | report/matrix/evidence gate | 완료 | report, evidence index, feature matrix 상태 정합성 유지 | `scripts/check-report-matrix-evidence-consistency.ps1` 통과 |
| 1 | profile loading standard | 완료 | PFW/CMN/업무 서비스 profile import 표준 유지 | `scripts/check-profile-loading.ps1` 통과 |
| 1 | deploy dry-run standard | 부분 구현 | 실제 원격 전송/stop/start/health/rollback은 후속 | dry-run JSON과 checksum, env/inventory 검증 통과. real remote는 별도 증적 필요 |
| 2 | ADM browser click | 미검증 | Playwright 또는 브라우저 클릭 검증 환경 준비 | static smoke가 아닌 실제 click flow 성공 증적 |
| 2 | Redis/Kafka/MQ broker | 미검증 | 실제 broker pub/sub, fallback, 장애 복구, DLQ/replay 검증 | broker runtime smoke JSON 생성 |
| 2 | PFW broker capability | 부분 구현 | Kafka/MQ/Redis Stream adapter, outbox/inbox, DLQ/replay, ordering/idempotency runtime 구현 | PFW port 기반 실제 broker runtime smoke와 장애 시나리오 통과 |
| 2 | PFW file transfer capability | 부분 구현 | SFTP/FTP/FTPS/SSH adapter, credential, checksum, temp/rename/archive, 전송 이력 runtime 구현 | 실제 파일 전송 서버 대상 송수신 smoke와 이력 검증 통과 |
| 2 | PFW security/credential capability | 부분 구현 | Vault/KMS/HSM/secret manager, key/cert rotation, mTLS/JWT/OAuth 실연동 | 원문 secret 미노출 상태의 credential runtime smoke 통과 |
| 2 | PFW runtime control capability | 부분 구현 | 분산 lock/heartbeat/ghost detector 저장소 구현과 다중 인스턴스 검증 | 다중 worker 환경에서 lock/heartbeat/ghost smoke 통과 |
| 2 | CMN PFW port migration 후보 | 재확인 필요 | `CmnFileExchangeService`, `CmnMessageBridgeService`의 기술 engine 여부 재확인 | 기술 engine이면 PFW port로 이동하고 CMN은 프로젝트 공통 helper만 유지 |
| 2 | Service Registry UI | 부분 구현 | 실제 브라우저 클릭 검증과 운영자 UX 개선 | 정적 UI marker 통과 외 실제 click 증적 추가 |
| 3 | 문서 mojibake 정리 | 재확인 필요 | 오래된 HTML/Java 주석의 깨진 문자 전수 정리 | 강화된 UTF-8/mojibake gate 통과 |

