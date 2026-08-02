# CPF QA38 전수검수·보완 개발·최종 봉인 요청서

## 1. 기준

- Repository: `C:\dev\projects\jck\202412_01_CPF`
- Branch: `master`
- Review baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- Docker: `C:\dev\Docker\CPF`
- Official DB: Oracle, PostgreSQL, MariaDB
- Codex role: independent verifier and source remediator
- Git write: prohibited

작업 시작 시 actual HEAD와 origin/master를 확인한다. 다르면 actual clean remote HEAD를 기준으로 하고 이유를 기록한다.

## 2. QA37 이력 사용

QA37 focused/static PASS는 Root Cause 반복 분석을 줄이는 참고 자료다.
다음 조건을 모두 만족할 때만 Stage skip이 가능하다.

- current gitHead 동일
- command hash 동일
- relevant source/config/sql/profile 동일
- environment/vendor 동일
- exit 0
- log and log hash 존재
- artifact stage는 artifact hash 존재

그 외에는 미검증이다.

## 3. 검수만 하고 끝내지 않기

Source Defect 발견 시:

```text
root cause
→ owner/consumer/impact
→ source/sql/test/config/generator fix
→ targeted verification
→ upper lifecycle once
→ matrix/evidence/history
```

부분 구현, marker-only, consumerless interface, one-vendor-only fix를 남기지 않는다.

## 4. Stage order

`STAGE_PLAN.csv` 순서대로 진행한다.
앞 Stage 실패 시 뒤 비싼 Stage를 실행하지 않는다.
동일 Root Cause 실패는 defect ledger 한 건으로 묶는다.

### Core·Starter

- Core published POM and runtime classpath inventory
- non-Boot Core consumer
- AutoConfiguration and concrete adapter migration
- all real consumers
- generator profiles and resolved lock
- aggregate starter no-bean rule
- provider conflict negative matrix
- footprint budget

### Messaging·TCP

- Kafka actual runtime
- JMS common adapter
- IBM MQ provider
- RabbitMQ provider
- persistent TCP
- ACK/transaction/order/redelivery/DLQ/outage/recovery/unknown/multi-instance
- TLS/secret rotation/masking/readiness/operations

### Fresh DB

Before any DB start:

1. Run canonical/generator/vendor static sync.
2. Confirm official reset/provision path.
3. Snapshot initial container state.
4. Start only one Vendor.
5. Use dedicated QA database/schema.
6. Prove CPF object count is zero.

Then run Fresh Install, metadata/seed, arbitrary generated Domain, runtime query, upgrade, rollback, reapply, different-hash conflict, optional pack off/on, drift and cleanup.

If any official path is missing, implement it before manual execution. Manual SQL is prohibited.

### Final

After Source stable:

- Java 25 fresh lifecycle
- ADM/BZA clean verify
- 3DB actual lifecycle
- runtime/fault/multi-instance
- Playwright Chromium/Firefox/WebKit
- Trivy, secret, SBOM, ORT, license
- final exact-SHA evidence

## 5. History

Continuously update:

- `C:\dev\Docker\CPF\output\codex\qa38\execution-ledger.csv`
- `C:\dev\Docker\CPF\output\codex\qa38\defect-ledger.csv`
- repository `VERIFICATION_HISTORY.csv`
- `CPF_CODEX_CONTINUITY_STATE.md`
- `CPF_CODEX_DECISION_LOG.md`

Repository history contains sanitized summaries and hashes, not secrets or oversized raw logs.

## 6. End states

If source changes are made:

- remediation development: completed only after targeted and upper tests
- final exact-SHA seal: waiting for user commit/push
- overall: `재확인 필요`

After user push, verify manifest equality and run final canonical plan once.
Only then may overall state become `완료`.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.
