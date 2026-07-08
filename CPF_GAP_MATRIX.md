# CPF 남은 Gap 매트릭스

`CPF_FINAL_TARGET_REQUIREMENTS.md` 기준으로 아직 완료라고 보기 어려운 항목을 추적합니다. 실행 증적이 없는 검증은 완료로 표시하지 않습니다.

| 우선순위 | 영역 | 상태 | 남은 작업 | 완료 기준 |
| --- | --- | --- | --- | --- |
| 1 | report/matrix/evidence gate | 완료 | `CPF_STABILIZATION_REPORT.md`, `CPF_EVIDENCE_INDEX.md`, `specs/기능_구현_매트릭스.html` 상태 정합성 유지 | `scripts/check-report-matrix-evidence-consistency.ps1` 통과 |
| 1 | profile loading standard | 완료 | PFW/CMN/업무 모듈 profile import 표준 유지 | `scripts/check-profile-loading.ps1` 통과 |
| 1 | garbage file cleanup | 완료 | 레거시 profile 파일과 직접 배포 ps1/sh 제거, 잔존 파일 검사 | `scripts/check-runtime-config-standard.ps1` 통과 |
| 1 | deploy env/inventory standard | 완료 | local/dev/stg/prod env와 inventory 필수 key, runtimeMode, datasource mode 유지 | Gradle `checkDeployEnv`, `checkDeployInventory` 통과 |
| 1 | deploy dry-run standard | 부분 구현 | Gradle dry-run은 가능하지만 실제 원격 stop/start/health/rollback은 미검증 | `remoteDeployDryRun` 통과, 실제 원격은 별도 환경 증적 필요 |
| 1 | packaged dependency check | 완료 | bootJar 내부 PFW/CMN 포함 여부 검증 | Gradle `checkPackagedDependencies` 통과 |
| 1 | BAT EDU/log policy | 완료 | BAT EDU tasklet 샘플과 Job 실행 단위 로그 경로 정책 테스트 유지 | `:bat:test` 통과 |
| 1 | PFW Service Call Engine | 부분 구현 | 다중 서비스 실제 HTTP runtime, timeout budget 전파, health scheduler 주기 실행 검증 | ACC/MBR/EXS/ADM 동시 기동 후 성공/실패/timeout/retry/circuit/call-history 증적 통과 |
| 1 | ADM Service Registry | 미검증 | 실제 ADM API runtime 호출 검증 | `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime` 통과 |
| 2 | ADM browser click | 미검증 | 실제 브라우저 클릭 흐름 검증 | static smoke가 아닌 실제 click flow 증적 |
| 2 | Redis/Kafka/MQ broker | 미검증 | 실제 broker pub/sub, fallback, 장애 복구, DLQ/replay 검증 | broker runtime smoke JSON 생성 |
| 2 | PFW broker capability | 부분 구현 | Kafka/MQ/Redis Stream adapter, outbox/inbox, DLQ/replay runtime 구현 | 실제 broker runtime smoke와 장애 시나리오 통과 |
| 2 | PFW file transfer capability | 부분 구현 | SFTP/FTP/FTPS/SSH adapter, credential, checksum, temp/rename/archive runtime 구현 | 실제 파일 전송 서버 대상 송수신 smoke 통과 |
| 2 | PFW security/credential capability | 부분 구현 | Vault/KMS/HSM/secret manager, key/cert rotation, mTLS/JWT/OAuth 실연동 | 원문 secret 미노출 상태와 credential runtime smoke 통과 |
| 2 | PFW runtime control capability | 부분 구현 | 분산 lock/heartbeat/ghost detector 저장소 구현과 다중 instance 검증 | 다중 worker 환경에서 lock/heartbeat/ghost smoke 통과 |
| 2 | CMN PFW port migration 후보 | 재확인 필요 | `CmnFileExchangeService`, `CmnMessageBridgeService`가 기술 engine이면 PFW port로 이동 | CMN은 프로젝트 공통 helper만 유지하고 기술 engine은 PFW로 이동 |
| 3 | 기존 문서/주석 mojibake 정리 | 재확인 필요 | 오래된 HTML/Java/XML/YML 주석의 깨진 문자 전수 정리 | 강화된 UTF-8/mojibake gate 통과 |
