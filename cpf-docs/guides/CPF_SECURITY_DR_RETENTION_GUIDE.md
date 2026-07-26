# CPF Security / DR / Retention Guide

## 1. Secret Lifecycle
Secret 원문을 Config 조회/API/로그/Audit/Trace/Evidence에 반환하지 않는다. 애플리케이션은 `CpfSecretReference`와 `CpfSecretProvider`를 통해 값을 얻는다. 값이 꼭 필요한 구간만 `CpfSecretValue`를 열고 사용 후 close한다.

ENV Provider는 초기 bootstrap용 기본 구현이다. 환경변수 이름과 metadata는 볼 수 있어도 원문은 API에 노출하지 않는다. 실제 상용 Rotate는 Vault/KMS/HSM adapter가 `CpfRotatableSecretProvider`를 구현하도록 한다.

## 2. ADM Secret API
- Provider 목록: capability/상태만 표시.
- Metadata: reference, version/만료 등 비민감 정보만 반환.
- Rotate: provider가 지원하고 운영자 권한/사유가 있을 때만 실행.
- ENV provider rotate는 unsupported로 거부.
- Secret value를 response DTO나 exception message에 포함하지 않는다.

## 3. Certificate
`check-certificate-expiry.ps1`는 공개 인증서의 Subject/Issuer/Serial/Thumbprint/NotBefore/NotAfter/잔여일을 검사한다. Private key를 읽거나 Evidence에 내보내지 않는다. WarnDays 기준 이하이면 운영 경고로 처리한다.

## 4. Session
BZA는 비밀번호 강제 변경/만료 상태를 Backend에서 집행한다. Refresh rotation은 동시 401에서 single-flight로 직렬화하고 최종 refresh 실패 시 Browser session을 제거한다.

Access token 즉시 폐기, refresh family reuse detection, device/session metadata, KMS key ring/kid rotation은 전체 상용 Security Center 관점에서 아직 별도 검증/보완 대상이다.

## 5. Permission
실효 권한은 active user-role/role/menu와 환경 scope를 평가한다. Role history가 존재하는 계정에서 만료/회수된 Role을 legacy roleCode fallback이 되살리면 안 된다. deny precedence와 API server enforcement를 Browser 표시보다 우선한다.

## 6. Audit Hash Chain
BZA 감사 writer는 chain lock을 row-lock하여 다중 인스턴스 순서를 만든다. 각 row는 previous hash + canonical content로 record hash를 계산한다. Verify는 row mutation, 중간 link 손상, 마지막 row 삭제 후 persisted chain-head 불일치를 모두 탐지해야 한다.

`PARTIAL_LEGACY`는 이전 hash 도입 전 row가 존재함을 의미하며 `VALID`과 동일하게 취급하지 않는다.

## 7. Sensitive Audit
Hash chain은 기밀성을 제공하지 않는다. before/after/exception/context는 별도로 recursive masking하고 대용량 원문은 안전한 archive/reference 전략을 사용한다. 개인식별정보와 Credential이 chain에 원문으로 들어가면 안 된다.

## 8. Retention Policy
`CpfRetentionPolicy`는 retention 기간/행위/legal hold 정책을 표현한다. 실행은 `CpfRetentionCommand`로 target/cutoff/dryRun/reason을 전달한다.

## 9. BAT Retention Baseline
R14 concrete target은 `BAT_OPERATION_LOG`다.
- `KEEP`: 변경 없음.
- `dryRun=true`: 대상 계산만 하고 변경 없음.
- `legalHold=true`: destructive action 금지.
- `ARCHIVE`: archive table insert 후 원본 delete를 한 transaction으로 수행.
- `PURGE`: 정책과 kill-switch가 허용할 때 delete.

## 10. Destructive Kill Switch
기본값 `cpf.retention.execute-enabled=false`. 실제 ARCHIVE/PURGE에는 cutoff가 필수다. Kill switch가 OFF면 운영자의 요청이 있어도 403/fail-closed한다. 실제 실행 시 권한, reason, audit를 함께 검증한다.

## 11. Rollback Safety
Retention archive에 데이터가 있는 상태에서 R54가 table을 DROP하면 데이터 손실이다. 따라서 R54 rollback은 archive row가 있으면 SIGNAL로 실패한다. 운영자는 먼저 보존/이관 결정을 내려야 한다.

## 12. Backup
DB backup은 본질적으로 개인정보/업무데이터를 포함할 수 있으므로 `containsSensitiveData=true`로 취급한다. Script argument에 password를 받지 않고 client credential mechanism을 사용한다. Backup과 SHA-256 manifest를 분리 보존한다.

## 13. Restore
Restore는 `-ConfirmRestore`가 없으면 실행하지 않는다. Manifest가 있으면 hash/vendor/database가 모두 맞아야 한다. DB명이 다른 backup을 잘못 restore하지 않도록 fail-closed한다. Missing manifest 허용은 legacy 예외다.

## 14. DR Verify
DR 복구는 운영 원본이 아닌 격리 환경에서 수행한다. 단일 DB만 복구했다면 connection/table baseline 또는 DB별 `VerifySql`을 사용한다. 모든 CPF logical DB를 함께 복구했을 때만 `RunPlatformVerify`로 canonical full verify를 실행한다.

Evidence에는 실제 시작/종료시각, duration, 기준 SHA, DB(민감 host는 scrub), command, result를 남긴다.

## 15. RPO/RTO
Framework는 복구 도구와 Evidence 표준을 제공하지만 고객별 RPO/RTO 값은 운영 정책에서 정한다. Backup 주기, offsite/immutable storage, 복구순서, DNS/LB 전환, 재처리 전략을 실제 장애훈련으로 검증한다.

## 16. Environment Promotion
변경 promotion manifest는 source/target environment, base commit, reason, 대상 파일과 SHA를 기록한다. 검증 시 current commit과 파일 hash가 달라지면 실패한다. 실제 조직 승인/서명/CD 연동은 별도 adapter가 필요하며 단순 manifest 존재를 승인 완료로 간주하지 않는다.

## 17. 완료 금지
- Secret Provider interface만 있고 운영 adapter 없음.
- Backup 파일 생성만 하고 restore test 없음.
- Retention SQL만 있고 legal hold/kill-switch/rollback safety 없음.
- Audit hash 컬럼만 있고 tamper test 없음.
위 상태는 `부분 구현`이다.
