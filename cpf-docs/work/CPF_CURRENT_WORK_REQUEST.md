# CPF Current Work Request

> Baseline input ZIP SHA-256: `b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850`
> Baseline input files: `8,440`
> Source identity (Git-independent, evidence-metadata excluded): `762343a5d08d11a7cfc9990236761f5a380e4f92e0f4bfd54a98d52095da2a64`
> Desired-state files: `8,399`
> Git exact SHA: `UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT`
> Development/static/package status: **CURRENT PASS — LOCAL RUNTIME REVALIDATION REMAINS**
> Environment-dependent runtime verification: **미검증 / 재실행 필요**

## 현재 요청 상태

최신 Steering과 QA 재개발 요구를 현재 Source에 반영했다. BZA는 CPF 내부 Optional Prebuilt Business Administration Domain(`cpf-biz-admin`)과 외부 DB-less Pure Spring Boot Channel(`cpf-biz-channel`), 외부 Reference Frontend(`cpf-biz-frontend`)로 분리한다. Education은 20 Online/15 Batch feature-role 구조를 사용하고, Common은 cpfDB 단일 Runtime Owner를 사용한다. Public Distribution은 default-deny staging 및 fail-closed publish driver를 제공한다.

삭제 후보는 `cpf-docs/deliverables/DELETE_MANIFEST.csv`에만 기록하며 실제 Source에서는 삭제하지 않았다. desired-state 검증 Snapshot은 해당 후보를 제외해 중복/구 경로 제거 상태를 검증한다. 최종 static rerun에서 NXT3 23/23, focused Python 22/22, Java syntax 2,820/0, ADM route 68/329, generated OpenAPI 337, Public staging 116을 확인했다.

## 남은 완료 조건

- Java 25 Root Gradle build/test/publication/SBOM
- Live Oracle/PostgreSQL/MariaDB lifecycle
- Redis/Valkey live reconnect/failover
- Multi-WAS/process-kill/restart/redeploy
- 외부 BZA Channel + Frontend live/browser E2E
- Public Git 실제 clone/commit/push는 모든 Gate PASS 후 사용자 로컬에서 실행

위 미실행 항목은 PASS로 기록하지 않는다.
