# WP-R03.05 Harness Evidence Identity — Canonical Registry 변조 Root Cause 종결

## 1. 원 Finding

`validate_development_harness.py` 실행 시 다음 FAIL 이 발생했다.

```
FAIL LEGACY_EVIDENCE_REGISTRY Expecting property name enclosed in double quotes: line 1 column 2 (char 1)
```

위 오류는 canonical governance registry
`cpf-docs/governance/development-harness/current/LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl` 가
JSONL(줄당 1 JSON) 계약을 위반해 파싱조차 되지 않는다는 의미다.

이는 WP-R03.05 의 기존 관측 *"Source State After PASS 이나 Managed State After FAIL"* 과 동일한
Root Cause 이므로 별도 WP 를 만들지 않고 WP-R03.05 에 병합했다.

## 2. 실측 상태

| 항목 | HEAD | 발견 당시 Working Tree |
| --- | --- | --- |
| 형식 | JSONL (줄당 1 JSON) | pretty-printed 단일 JSON |
| 줄 수 | 144 | 169 |
| 최상위 구조 | legacy evidence 레코드 144건 | `requirementId: NXT2-REDIS-001` 검증 리포트 dict 1건 |

**형식 위반이 아니라 데이터 자체가 전량 대체된 상태였다.** legacy evidence 매핑 144 레코드가 소실되고
Redis/Valkey provider 검증 리포트가 그 자리를 덮고 있었다.

## 3. Root Cause

verification gate 4종이 **canonical governance registry 를 자신의 evidence 출력 기본값**으로 사용하고
있었다. `--evidence` 없이 단독 실행하면 tracked 정본을 `json.dumps(..., indent=2)` 로 덮어쓴다.

| 파일 | 위반 지점 |
| --- | --- |
| `cpf-tools/verification/nxt3/verify_annotation_runtime_consumer.py` | `out=... if ns.evidence else root/'<canonical registry>'` |
| `cpf-tools/verification/nxt3/verify_redis_valkey_provider_currentization.py` | 동일 |
| `cpf-tools/verification/verify_business_framework_crosscut.py` | `EVIDENCE = ... if _args.evidence else ROOT / "<canonical registry>"` |
| `cpf-tools/verification/nxt3/run_nxt3_final_all.py` | `ap.add_argument('--evidence', default='<canonical registry>')` |

이 4개 gate 는 서로 다른 산출물을 같은 파일에 쓰므로 마지막 실행자가 이긴다. 발견 시점에는
`verify_redis_valkey_provider_currentization.py` 의 결과가 남아 있었다.

`run_nxt3_final_all.py` 는 이미 올바른 격리 설계를 갖고 있었다.

> Verification mode must be read-only for tracked repository Evidence. Child gates that can emit
> evidence are redirected to an external temporary directory so a clean checkout remains
> byte-for-byte unchanged after NXT3 execution.

즉 runner 경유 실행은 안전했고, **단독 실행 경로만 정본을 파괴**했다. 이것이 Managed State After 가
FAIL 하던 실제 경로다.

## 4. 부가 결함 — 격리 test 의 false-green

`cpf-tools/verification/tests/test_nxt3_local_build_isolation.py` 는 이 격리를 검증하고 있었으나

```python
ANNOTATION = ROOT / '...LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl'
REDIS      = ROOT / '...LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl'   # 동일 경로
...
before = {ANNOTATION: _sha_or_missing(ANNOTATION), REDIS: _sha_or_missing(REDIS)}
```

두 상수가 같은 경로라 dict 가 1개 항목으로 축약되어 **실질적으로 1개만 검사**했고, 더 중요하게는
`--evidence` 를 항상 지정해 실행했기 때문에 **이번 Root Cause 인 단독 실행 경로를 전혀 검사하지 않았다.**

## 5. 수정 내용

1. **Writer Root Cause 제거** — gate 4종이 canonical registry 를 기본 출력으로 삼지 않도록 수정.
   - 3종(`verify_annotation_runtime_consumer`, `verify_redis_valkey_provider_currentization`,
     `verify_business_framework_crosscut`)은 `--evidence` 가 주어진 경우에만 파일을 기록한다
     (read-only 기본 동작).
   - `run_nxt3_final_all.py` 는 자신의 종합 결과를 쓰는 것이 목적이므로 기본값을
     `cpf-docs/work/evidence/current/NXT3_FINAL_ALL.json` 으로 이동했다(기존 `--log` 기본값과 동일 계열 경로).
2. **격리 test 강화** — 중복 상수를 `CANONICAL_REGISTRY` 로 통합하고,
   `--evidence` 를 주지 않은 단독 실행 후에도 registry sha 가 불변인지 검사하는 assert 를 추가했다.
   이것이 이번 Root Cause 의 직접적인 회귀 방어다.
3. **정본 복원** — canonical JSONL 144 레코드를 복원했다. `git restore`/`reset`/`clean` 은 사용하지 않았고
   해당 파일 하나만 최소 범위로 수정했다. 다른 작업자의 unrelated 변경은 건드리지 않았다.
4. **덮어쓴 데이터 보존** — 소실 방지를 위해 `OVERWRITTEN_NXT2_REDIS_001_REPORT.json` 으로 Evidence 에 보존했다.

## 6. Consumer / 호출경로 확인

| 대상 | 확인 결과 |
| --- | --- |
| `run_nxt3_final_all.py` child 호출 | `--evidence <tempdir>/...` 를 명시 전달 (변경 불필요, 기존 설계 유지) |
| `validate_development_harness.py` | registry 를 JSONL 로 파싱하고 `authoritative_for_current_pass`/`migration_disposition` 을 검증 — 복원 후 위반 0 |
| `contracts/current-authority-registry.json` | 이 registry 를 current authority 로 등록 — 정본 지위 확인 |
| `test_nxt3_local_build_isolation.py` | 격리 검증 소비자 — 강화 완료 |
| governance `current/` 를 출력 대상으로 쓰는 그 밖의 도구 | 전수 재검색 결과 **추가 사례 0** (test fixture 의 tmp root 쓰기와 read-only 인자만 존재) |
| Repository 전체 `.jsonl` | 1개뿐이며 형식 검증 invalid **0** |

## 7. 검증 결과

| Gate | 명령 | 결과 |
| --- | --- | --- |
| 격리 test | `pytest .../test_nxt3_local_build_isolation.py -q` | **2 passed** |
| verification + open-git 회귀 | `pytest cpf-tools/verification/tests/ cpf-tools/release/open-git/tests/ -q` | **148 passed, 1 skipped** |
| Harness self acceptance | `validate_development_harness.py` | **PASS** (exit 0) REQUIREMENTS=218 WORK_ITEMS=410 ROLE_ROWS=1230 MIGRATIONS=265 |
| registry 무결성 | JSONL 파싱 | records=**144**, invalid=**0**, authority violations=**0** |
| 문법 | `py_compile` 5개 파일 | 전부 OK |

수정 후 gate 를 단독 실행해도 registry sha 가 변하지 않음을 test 가 실제로 강제한다.

## 8. 함께 종결한 Harness self-acceptance FAIL

| FAIL | 원인 | 처리 |
| --- | --- | --- |
| `ROLE_LEDGER_COVERAGE missing=0 extra=1` | 이번 세션에서 role ledger 에 계약에 없는 `DEVELOPER` 행을 추가한 것 | 행 제거 후 canonical role(`DEVGPT`) 행을 갱신 |
| `CONTROL_CHAR ... OPEN_GIT_STAGE06_CONTRACT.md` | Evidence 작성 시 `\b` 가 백스페이스(0x08)로 기록됨 | 제어문자 제거 |
| `LEGACY_EVIDENCE_REGISTRY` | 본 문서의 Root Cause | 종결 |

## 9. Source Identity

| 항목 | 값 |
| --- | --- |
| productContentSha256 | `497f71c34307b4d3c39c6781b7c159dbf2c43b529ce70b408d7b8df1d408a9dd` |
| productContentSha1 | `7702b5941f3fd7787b0aa840d6ae811adab2b3cf` |
| fileCount / totalBytes | 8453 / 46145951 |

## 10. 상태

| 구분 | 상태 |
| --- | --- |
| Root Cause 확정 | 완료 |
| Source 수정 | 완료 (5개 파일) |
| Consumer / 호출경로 확인 | 완료 |
| Test 강화 및 실행 | **PASS** |
| Regression | **PASS** (148 passed / 1 skipped) |
| Harness validator | **PASS** |
| Evidence | 본 문서 + `OVERWRITTEN_NXT2_REDIS_001_REPORT.json` |
| Runtime | NOT_APPLICABLE (정적 governance 계약 결함) |

남은 확인은 동일 Source Identity 에서의 Managed State After 재실행이며, 이는 Full Runtime Validator
경로에서 수행된다.
