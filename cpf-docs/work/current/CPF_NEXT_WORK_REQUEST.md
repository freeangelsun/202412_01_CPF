# CPF Next Work Request — R13 적용 후 통합검증 및 잔존 Gap 제거

## 1. 목표

R13 overlay가 반영된 최신 `master` SHA 하나를 기준으로 R11~R13 기능을 Build/DB/Runtime/Browser/Multi-instance에서 검증하고, 실패가 나오면 동일 Owner 범위의 Source/SQL/Test/Guide/Evidence까지 함께 수정한다. 과거 완료 문구나 정적 검색만으로 완료 판정하지 않는다.

## 2. 시작 전 확인

- 최신 `master` SHA와 worktree
- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `CPF_CURRENT_WORK_REQUEST.md`
- `CPF_R13_HANDOVER.md`
- `20260726_02/CPF_R13_IMPLEMENTATION_REPORT.md`
- 실제 Source/SQL/API/Test/Script와 현재 Commit Evidence

## 3. 우선 Gate

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-r13-overlay.ps1 -StaticOnly
.\gradlew.bat verifyVersionConsistency checkContractCompatibility checkR13ProductHardening --no-daemon
.\gradlew.bat clean test assemble --no-daemon
.\gradlew.bat qualityGate --no-daemon
.\gradlew.bat validateReleaseMetadata --no-daemon
```

## 4. MariaDB V52

- Empty Install에서 ADM download audit + MBR member/role/sequence/issue/idempotency table 확인
- R51 DB → V52 upgrade
- migration checksum/drift
- R52 rollback → V52 재적용
- 존재하지 않는 column/index/FK 참조 0
- Generator/DB metadata 영향과 canonical parity

## 5. QA 1~10 Runtime/E2E

- Generator canonical version gate + compatibility launcher
- Manifest/SBOM/Provenance가 실제로 다른 payload이며 Jar/War hash 변조 시 Gate 실패
- ADM/BAT liveness 200, required dependency down readiness 503
- ADM Log Password/Authorization/Cookie/JWT/전화/이메일/회원·고객·계좌 원문 0
- MBR not-found / DB-down / schema-error / duplicate-result 오류 분리
- 2개 이상 MBR 인스턴스 동시 memberNo 중복 0 + 발급 이력 조회
- 동일 expectedVersion 동시 요청 하나만 성공
- 권한 idempotency replay/key misuse conflict
- 회원 상세 `AVAILABLE/FAILED`와 Browser의 0건/실패 구분
- ResponseCode/Message/Config/Code cache preload first-hit, rollback cache 유지, multi-instance event, event DB-down retry/status
- CSV 위험 payload가 Excel/LibreOffice에서 formula로 실행되지 않음

## 6. 제품 확장 잔존 범위

대형 신규 제품으로 확장하지 않는다.

- OTel: W3C context + local/remote/service-call/message/batch/center-cut trace, exporter 상태/비오염이 실제 상품 가치가 있는 범위까지
- Feature Flag: CPF SPI가 충분한지 먼저 검증하고 외부 OpenFeature adapter가 실익이 있을 때만 추가. A/B 제품 제외
- Contract: 공식 API/Event/전문/File/Batch snapshot과 실제 배포 Gate에 필요한 최소 Registry만
- Fault Injection: `TEST-FAULT` 실검증 harness 우선. 운영 Chaos 제품 제외
- Generator: CLI Golden Path가 정본. 별도 Portal은 필요성이 객관적으로 확인되기 전 생성 금지

## 7. 보호할 성공 기능

- Generated Domain `com.cpf.core.common.*` import 0
- `cpf-common.utils` consumer 0
- `cpf-common` business-common dependency
- BAT/Center-Cut ownership
- ADM→BAT/MBR Owner Port
- UNKNOWN_RESULT/recovery
- mandatory durable Audit + verified actor
- Calendar product/DB-less + optimistic conflict
- ADM/BZA permission fail-closed
- canonical Generator 단일 정본
- MariaDB canonical lifecycle + unsupported vendor fail-closed
- verification read-only

## 8. Evidence/완료 금지

Evidence: 기준 SHA, 명령, profile/env, 시작/종료, Requirement/QA ID, 실제 결과, sanitized raw log.

완료 금지:
- 실행하지 않은 Test/Runtime/Browser를 PASS 처리
- HTTP 503 대신 body 문자열만 DOWN
- DB 장애를 empty/not-found로 변환
- 민감 원문을 API/CSV/Evidence에 보존
- multi-instance/동시성 검증 없이 분산 안전 완료 선언
- Migration만 수정하고 canonical/install/rollback 영향 누락
- OTel/Flag/Contract/Fault Interface만 존재하고 전체 제품 기능 완료 선언
