# CPF Guide R2 Handover

- Base SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e`
- Branch: `master`
- Package: `CPF_GUIDES_R2_EXPERT_20260731.zip`
- Git Write: 없음

## 적용 범위

- `README.md`
- `cpf-docs/guides/**` 정본 9개
- Review·Source Inventory·Manifest
- Guide Content Quality Gate
- 적용 Script

## 적용 원칙

1. 최신 master가 Base SHA의 descendant인지 확인한다.
2. 다른 작업자의 변경을 reset/clean하지 않는다.
3. Overlay를 Repository Root에 덮어쓴다.
4. Content Gate와 기존 Guide System Gate를 실행한다.
5. Runtime 검증은 별도 실제 환경에서 수행한다.
6. Commit·Push는 사용자 승인 후 수행한다.

## 남은 Runtime 검증

- Java 25 전체 Gradle
- ADM/BZA npm ci·Browser 3종
- MariaDB·PostgreSQL·Oracle Lifecycle
- Kafka Remote Batch
- Gateway Scale-out·Fault·Load
- Multi-instance·Process Kill·Response Loss
- Backup·Restore·DR
- ORT·Syft·Grype Final Artifact
