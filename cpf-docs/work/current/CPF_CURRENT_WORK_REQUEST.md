# CPF Current Work Request — R13 제품 품질 하드닝 이후

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- R13 시작 Commit: `9b12ba025a0c6f2df59589681a862959232be16f`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 입력: R12 잔여 통합검증 + QA 품질개선 15개 후보

## 2. R13에서 닫은 Source Gap

최신 master를 직접 재확인한 결과 QA 1~10은 실제 Source Gap이었다. 이번 변경은 다음을 구현했다.

1. Generator version gate가 canonical `cpf-tools/generator/create-domain.ps1`를 사용하고 old path는 thin compatibility launcher만 허용한다.
2. Release Manifest/SBOM/Provenance를 서로 다른 schema/payload로 분리하고 실제 artifact SHA-256 검증을 추가했다. License 확정과 승인된 서명/attestation은 아직 부분 구현이다.
3. ADM/BAT liveness/readiness를 분리하고 필수 dependency 장애를 HTTP 503으로 처리한다. runtime harness도 readiness endpoint를 probe한다.
4. ADM 거래로그 상세의 `SELECT *`, raw details 반환을 제거하고 Backend 최종 응답에 recursive masking을 적용한다.
5. MBR는 `EmptyResultDataAccessException`만 NotFound로 처리하고 DB/SQL/무결성 오류는 DATABASE_ERROR로 분리한다.
6. 회원번호를 DB AUTO_INCREMENT sequence 기반으로 발급하며 AUTO/MANUAL 발급 이력을 저장하고 MBR Owner Query + ADM 조회 API를 제공한다.
7. 회원/상태/권한 변경에 `version_no` CAS와 권한 변경 idempotency key를 적용한다.
8. ADM 회원 상세의 거래/감사 read-model 장애를 0건으로 위장하지 않고 section `AVAILABLE/FAILED`, errorCode, transactionId를 반환한다.
9. Response Code/Message/Config/Code cache를 DB-read-first + explicit snapshot population + commit-after cache replace 방식으로 정리했다. 전체/개별 key를 분리하고 refresh event 저장은 실제 `REQUIRES_NEW` bean으로 분리했으며 bounded retry/status와 consumer retry 상태를 ADM summary에 노출한다.
10. CSV Header/String의 spreadsheet formula injection을 차단하고 Audit에 `CPF-CSV-1` 정책 Version을 기록한다.

동일 Owner 범위에서 추가로 발견한 MBR Runtime SQL/DB baseline column drift, role history 필수 column 누락, canonical fresh schema의 MBR 운영 테이블 누락, BAT/ADM cache controller의 `core.common.*`/actor fallback 회귀도 함께 정합화했다.

## 3. 제품 확장 — 의도적으로 제한한 범위

- **OpenTelemetry**: CPF vendor-neutral Telemetry API + 선택형 OTLP trace adapter + `@CpfOnlineTransaction` 실제 consumer. Remote/Messaging/Batch/Center-Cut 전체 propagation/metric/log/Collector Evidence는 부분 구현/미검증이다.
- **Feature Flag**: safe default, kill switch, context targeting, deterministic percentage rollout, Provider SPI. 대형 A/B 플랫폼/별도 운영제품은 제외했다.
- **Contract Compatibility**: REST/Shared API/Event/Fixed-Length/File/Batch breaking-change engine + schema + CI gate. 영속 Registry/환경 배포 Matrix는 부분 구현이다.
- **Fault Injection**: production 기본 비활성, test/verification/chaos profile + allowlist + 최대 지연 제한의 거래 경계 injector. 대형 Chaos 운영제품은 제외했다.
- **Golden Path**: 별도 Developer Portal을 만들지 않고 canonical Generator DryRun/충돌검사/Ownership/결정성 기준을 Gate로 보호한다.

## 4. DB/Migration

- `V52__qa_product_quality_hardening.sql`
- `R52__qa_product_quality_hardening.sql`
- canonical/runtime V52 byte parity 및 checksum 일치
- `30_adm_schema.sql`, `40_business_modules_schema.sql`, `00_empty_install.sql` 동기화
- 역사 Migration을 수정하지 않고 V52로 Upgrade한다.

## 5. 현재 남은 작업

R13 Source 구현을 CPF Release 완료로 판정하지 않는다. 아래는 최신 적용 Commit에서 실제 검증해야 한다.

- 전체 Gradle `clean test assemble qualityGate`
- `validateReleaseMetadata` 실제 Artifact/SBOM/Provenance 검증 및 License/서명 정책 마감
- MariaDB fresh install, V51→V52 upgrade, R52 rollback/re-apply, checksum/drift
- MBR 단일/다중 인스턴스 동시 회원등록, 동일 Version 동시 수정, idempotency replay
- ADM/BAT DB/Owner down readiness 503와 LB/배포 Script 연계
- ADM 로그 API 민감정보 payload 및 Browser 검증
- 회원 상세 부분장애 UI에서 0건/실패 구분
- CSV Excel/LibreOffice 실제 formula 실행 방지 검증
- CMN cache startup/rollback/multi-instance/event DB-down/retry/process-restart 시나리오
- OTel Collector local/remote/async/batch trace 및 exporter down 비오염
- Feature Flag 외부 Provider/OpenFeature adapter 필요성 판단과 실제 연계 Evidence
- Contract 공식 snapshot/Registry/can-deploy 확장 필요 범위
- Fault Injection timeout/DB/message/worker/UNKNOWN_RESULT verification harness
- Generator create/build/test/bootstrap/remove/regenerate full lifecycle
- R12 Audit/Ghost/Calendar/Gateway/Browser/Multi-instance 통합검증

실행하지 않은 항목은 `미검증`이며 정적 검색만으로 PASS 처리하지 않는다.
