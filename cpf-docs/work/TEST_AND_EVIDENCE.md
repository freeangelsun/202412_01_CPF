# CPF Test and Evidence — Current

> Architecture currentization basis: `4c4248a12e699c07f9f5fb11fbb33b97ca04077d` (`07_16`)  
> 본 문서는 실행된 사실과 미실행 항목을 분리한다. 계획/READY/Script 존재를 PASS로 기록하지 않는다.

## 1. Currentization 자체 검증

이번 문서 currentization에서 확인한 사실:

- architecture currentization basis master: `4c4248a12e699c07f9f5fb11fbb33b97ca04077d`
- `07_15` commit stats: additions 5,914 / deletions 402 / total 6,316
- `cpf-starters/data/persistence-jpa` 존재
- `cpf-starters/data/transaction-jta` 존재
- `cpf-starters/security/oidc-login` 존재
- `cpf-starters/security/audit-jdbc` 존재
- `cpf-starters/integration/ai` 존재
- `cpf-starters/integration/soap` 존재
- Core `api/util` 17종이 현재 존재함
- Core transaction API에 `CpfTransactionIdGenerator`, TCC/XA contract가 존재함
- `cpf-tools/verification`에 dated/QA/final campaign 디렉터리와 Python helper가 혼재함
- `cpf-docs/work/v9i/**`에 과거 Session/Final/Checkpoint/Result 문서가 다수 남아 있음

위 확인은 신규 제품 Requirement의 구현 PASS를 의미하지 않는다.

## 2. 신규 Current Request 상태

`CPF_CURRENT_WORK_REQUEST.md`의 신규 Requirement는 모두 시작 상태:

- development: `미완료`
- verification: `미검증`
- prior PASS inheritance: 금지

Developer는 각 ID별 Source/Consumer/Test/Harness/Evidence를 채운다.


## 2.1 Core Architecture Optimization 추가 Acceptance — NOT_EXECUTED

`07_16` Source 직접 리뷰에서 기존 Requirement의 해석을 다음과 같이 강화했다. 이는 **문서 현행화이며 실행 PASS가 아니다.**

- Provider-neutral/interface/SPI/Port라는 이유만으로 Core KEEP 금지
- Owner-specific/Optional Capability API·SPI·DTO·Port는 해당 Owner/Capability로 이동
- `admin/batch/centercut/gateway` 전용 Contract Core pollution 0
- FixedLength/File/AI 등 선택 Capability Contract의 Core 중복 Ownership 0
- Core Logging Runtime/AutoConfiguration 0
- Core Dynamic Log Level/Remote Log Operations 0
- moved-source residue / old-new duplicate / stale metadata/reference / empty migrated directory 0

Developer 완료 시 다음 Closure Gate를 최신 exact SHA에서 직접 실행·기록한다.

```text
Core→Starter = 0
Core→Optional Provider = 0
Core→Owner-specific Module = 0
Core Owner-specific/Optional API·SPI pollution = 0
Core Logging Runtime = 0
Core Dynamic Log Level/Remote Log Operations = 0
old/new duplicate = 0
moved-source residue = 0
stale AutoConfiguration/BOM/catalog/publication/generator/doc reference = 0
empty migrated source/resource/package directory = 0
Delete Manifest unresolved = 0
```

현재 판정: `NOT_EXECUTED / 미검증`.

## 3. 직전 세션 Runtime-only

직전 개발에서 다음 10건은 미검증이었다.

1. Java25/Gradle9.1 fresh full build/test
2. XA DB+DB DB3
3. XA DB+JMS prepare-kill/recovery
4. Broker ACK loss/process kill/multi-instance
5. Saga/TCC process-kill/restart
6. Optional JPA DB3/JTA
7. OIDC live SSO
8. PKCS#11 KMS/HSM
9. SOAP live timeout/UNKNOWN
10. PowerShell Generator full execution

신규 변경이 영향을 주면 이전 Harness를 그대로 성공 근거로 사용하지 않고 재검증한다.

## 4. Evidence 기록 형식

각 실행은 최소 다음을 남긴다.

```text
requirement_id
exact_source_sha
environment
command
started_at
finished_at
exit_code
actual_result
expected_result
failure_stage
runtime_dependency
evidence_path
evidence_sha256
sanitization
judgement
```

민감정보 원문은 금지한다.

## 5. QA Evidence 규칙

QA A/B는 서로 다른 독립 Evidence를 남긴다.

`Requirement → Source/Symbol → Consumer → Call Path → Test/Harness → Execution → Evidence → Judgement`

generic evidence를 다수 Requirement에 복사해 완료 처리하지 않는다.

---

## 6. 직전 세션 17 Evidence 보존

아래 내용은 `07_15` 직전 Developer 자체검수의 역사적 Current 근거로 보존한다.
QA PASS로 승격하지 않는다.

# CPF Test and Evidence — Current

## Basis
- remote `master`: `9f16468cccae71523f65f0aefcd94322788c4dd0`
- request authoring source: `a570b366ef85b23863e41173c991025c072a2427`
- GitHub connector used read-only; no commit/push/delete.
- local full repository clone unavailable because external GitHub DNS/network access from the execution container failed.
- execution JVM: OpenJDK 21; canonical target from `gradle/cpf-stack.properties`: Java 25 / Gradle 9.1.0 / Spring Boot 4.1.0.

## Direct review gates
- Core Hardening direct Source review: **180/180** (`CPF_CORE_HARDENING_AUDIT.csv`)
- Fundamental Baseline Audit: **240/240** (`CPF_FUNDAMENTAL_BASELINE_AUDIT.csv`)
- Persistence detail: **35/35** (`CPF_PERSISTENCE_BASELINE_AUDIT.csv`)
- Consumer review: **180/180 checked** (actual consumer or explicit N/A boundary)
- Test/Harness review: **180/180 checked** (actual test/harness or explicit N/A; execution separated)
- developer-remediable gaps: **21 found / 21 remediated / 0 remaining**
- runtime-only verification: **10** (`RUNTIME_ONLY_VERIFICATION.csv`)

## Executed low-cost validations
1. `javac --release 21` for new/changed provider-neutral Core API + broker contracts: **EXIT 0**, 69 classes.
2. `javac --release 21` for pure durable TCC business/recovery classes: **EXIT 0**.
3. New Public Core API Korean JavaDoc gate: **0 missing**.
4. Starter catalog JSON parse/partition static check: 45 physical modules; 6 public profiles; 7 capability groups; new modules internal.
5. DB3 static parity: Oracle/PostgreSQL/MariaDB all contain inbox hardening, XA recovery, tamper audit, durable TCC in Source/Migration/Install/Verify/Rollback.
6. DB vendor policy scan: product SQL/evidence uses only Oracle, PostgreSQL, MariaDB; an unsupported-vendor rejection Negative Test remains outside product evidence.
7. Stale relocation parent review on successor: MyBatis old `com/cpf/core`/`com/cpf/common` and old `starter/persistence`; messaging direct `.../reliability/*` replaced by `.../reliability/jdbc`; session direct `.../security/*` replaced by `.../security/session`. Current delete target count = 0.
8. Overlay JSON/CSV/package/hash gates are executed again before packaging.

## Not executed / not claimed PASS
The 10 rows in `RUNTIME_ONLY_VERIFICATION.csv` require Java25/Gradle9.1, PowerShell, DB3, broker, IdP, HSM or external SOAP runtime. They remain `미검증`; no READY/PLANNED value is counted as PASS.

