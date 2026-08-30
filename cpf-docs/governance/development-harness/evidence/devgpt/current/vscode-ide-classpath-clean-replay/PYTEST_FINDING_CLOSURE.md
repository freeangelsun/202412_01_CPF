# pytest 28 FAIL — 전건 1:1 매핑 및 종결

## 1. 개요

세션 시작 시점 `python -m pytest -c cpf-tools/testing/config/pytest.ini cpf-tools -q` 결과는
**28 failed / 994 passed / 3 skipped** 였다. 28건 전부를 Root Cause 단위로 분류하고 Current Harness
WP 에 1:1 매핑하여 종결했다. "기존 실패"를 이유로 남겨둔 항목은 없다.

## 2. 1:1 매핑표

| # | 실패 test | 건수 | Root Cause | Mandatory | WP | 조치 | 상태 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `test_cpf_tool_entrypoint_inventory` | 1 | canonical entrypoint inventory CSV 가 stale (실존 entrypoint 15건 미등록) | true | WP-R07.14 | `build-cpf-tool-entrypoint-inventory.py` 로 재생성 (entries=1021, duplicate 0, dead 0) | **CLOSED** |
| 2 | `test_verify_cpf_javadoc_coverage` | 2 | verifier 가 Public Java publication policy 를 요구하도록 변경됐으나 fixture 가 tmp root 에 policy 를 넣지 않음. negative test 는 javadoc 누락이 아니라 policy 부재로 통과하던 **false-green** | true | WP-R07.13 | fixture 가 canonical policy 를 복사하도록 교정 | **CLOSED** |
| 3 | `test_cpf_unified_cli_contract` (owner/catalog) | 1 | `pytest-basetemp` / `generated/pytest` 에 누적된 `CpfCli.java` 사본 6개가 canonical owner 스캔을 오염 | true | WP-R07.14 | untracked garbage 16MB 제거, repository 내 `CpfCli.java` 1개로 정규화 | **CLOSED** |
| 4 | `test_cpf_unified_cli_contract` (jar identity) | 1 | bootstrap jar 내장 `sourceIdentitySha256` 이 현재 Source Identity 와 불일치 (jar 는 identity 계산에서 제외되어 non-circular) | true | WP-R07.14 | `build-cpf-cli.py` 로 재빌드 | **CLOSED** |
| 5 | `test_cpf_unified_cli_contract` (cross-platform) + generator 8건 | 9 | **`cpf.cmd` 의 `for /f` 가 따옴표로 시작하는 명령을 `cmd /c` 로 실행해 Java 25 탐지가 항상 실패** (`CPF-CLI-JAVA-VERSION`, exit 69) | true | WP-R07.13 | `call` 접두로 교정 | **CLOSED** |
| 6 | `test_release_target_trust` | 7 | openssl 이 시스템에 존재하나(Git 3.5.4) 실행 PATH 에 없음 | true | WP-R15.02 | PATH prerequisite 확정 후 전건 PASS 실측 | **CLOSED** |
| 7 | `test_cpf_backup_crypto` | 5 | `cpf-backup-crypto.py` 의 `cryptography` 의존성이 **어떤 requirements 에도 선언되지 않음** | true | WP-R10.12 | `cpf-tools/db/tools/requirements.txt` 로 선언 후 설치(50.0.1) | **CLOSED** |
| 8 | `test_admin_data_safety_gate_contract` | 1 | 폐기된 refDB lineage 의 **빈 디렉터리 6개(untracked)** 가 postgresql/oracle 을 `incomplete lifecycle` 로 오판정 | true | WP-R10.12 | 빈 refDB 디렉터리 제거 | **CLOSED** |
| 9 | `test_cmn_code_message_durable_cache` | 1 | `JdbcCpfCodeService` 에 cache refresh 계약(`refresh()`) 미구현 | true | WP-R10.12 | fail-closed `refresh()` 추가 | **CLOSED** |
| 합계 | | **28** | | | | | **전건 CLOSED** |

## 3. 주요 Root Cause 상세

### 3.1 `cpf.cmd` Windows CLI 전면 장애 (9건 연쇄)

가장 영향이 큰 결함이다. `cpf.ps1` 은 `Join-Path` 로 정상 동작했으나 `cpf.cmd` 는 항상 실패했다.

```
CPF_CLI=FAIL code=CPF-CLI-JAVA-VERSION message=Java_25_required actual=
The filename, directory name, or volume label syntax is incorrect.
```

격리 실험으로 원인을 확정했다.

| 구문 | 결과 |
| --- | --- |
| `JAVA_BIN` 경로 자체 | `EXISTS=YES` |
| `for /f ... in ('"%JAVA_BIN%" -version ...')` | 빈 결과 + syntax error |
| `for /f ... in ('call "%JAVA_BIN%" -version ...')` | `25.0.3` 정상 |

`for /f` 는 명령을 `cmd /c` 로 실행하는데, 명령이 따옴표로 시작하면 `cmd /c` 의 따옴표 제거 규칙에
걸려 파싱이 깨진다. `call` 을 앞에 두어 해결했다. `JAVA_HOME` 트레일링 백슬래시는 원인이 아니었다
(제거해도 동일 실패 — 가설 기각 후 재조사).

### 3.2 false-green 2건

- `test_verify_cpf_javadoc_coverage::test_missing_rejected` — javadoc 누락이 아니라 policy 부재로
  `ValueError` 가 발생해 통과하고 있었다. fixture 교정 후 실제 계약을 검증한다.
- `test_nxt3_local_build_isolation` — 두 상수가 동일 경로라 dict 가 1개로 축약되었고,
  `--evidence` 를 항상 지정해 실행하여 **정본 오염 경로 자체를 검사하지 않았다**
  (`HARNESS_EVIDENCE_IDENTITY_REPAIR.md` 참조).

### 3.3 UTF-8 mojibake (generator 4건)

`subprocess.run(..., text=True)` 가 encoding 미지정이라 Windows 기본(cp949)으로 디코드하다 실패해
`cp.stderr` 가 `None` 이 되었고, 한국어 진단 메시지를 검증하는 negative test 가 `TypeError` 로 깨졌다.

```
UnicodeDecodeError: 'cp949' codec can't decode byte 0xbc in position 31: illegal multibyte sequence
TypeError: argument of type 'NoneType' is not iterable
```

canonical 패턴(`text=True, encoding='utf-8'`)에 맞춰 7개 호출을 교정했다.

## 4. refDB 종결

`refDB` 는 정본에 존재하지 않는 폐기된 lineage 다. `DELETE_MANIFEST.csv` 에 이미
`CURRENT-ONLY-LEGACY-ZERO: retired referenceFixture/refDB/current-history artifact removed after
current canonical/consumer migration; no active current consumer remains.` / `SATISFIED` 로
등록되어 있었다.

| 항목 | 결과 |
| --- | --- |
| refDB SQL/파일 잔재 | **0** (DELETE_MANIFEST 등록 항목 전건 삭제 완료 확인) |
| refDB 빈 디렉터리 | **0** (mariadb/postgresql/oracle × migration·rollback 6개 제거) |
| 제품 Source/Contract 내 refDB 참조 | **0** |
| governance 원장 내 참조 | 폐기 이력 기록으로 보존 (삭제 근거이므로 제거 대상 아님) |

빈 refDB 디렉터리는 git 미추적이었고, `check-official-db-vendor-readiness.ps1` 의 historical pack
검사에서 `.sql` 0개로 잡혀 postgresql/oracle 을 incomplete lifecycle 로 오판정하고 있었다.

## 5. 검증 결과

| Gate | 결과 |
| --- | --- |
| `check-official-db-vendor-readiness.ps1` | **exit 0** (vendors=3, tables=231, seeds=142) |
| `test_admin_data_safety_gate_contract` | 3 passed |
| `test_cmn_code_message_durable_cache` | 4 passed |
| `test_cpf_backup_crypto` | 5 passed, 1 skipped |
| `test_release_target_trust` | 7 passed |
| generator suite | 58 passed, 6 subtests |
| `test_cpf_unified_cli_contract` | 4 passed |
| `test_verify_cpf_javadoc_coverage` | 3 passed |
| `test_cpf_tool_entrypoint_inventory` | 2 passed |
| Java25 `gradlew compileJava --continue` | BUILD SUCCESSFUL, error/warning 0 |
| `validate_development_harness.py` | **PASS** (exit 0) |

## 6. 환경 prerequisite (확정)

| 항목 | 상태 |
| --- | --- |
| openssl | 시스템 보유(Git 3.5.4). 실행 PATH 에 포함 필요 |
| cryptography | `cpf-tools/db/tools/requirements.txt` 로 선언, 50.0.1 설치 완료 |
| JDK | 25.0.3 Temurin (`JAVA_HOME` 트레일링 백슬래시 허용 — cpf.cmd/ps1 양쪽 정상) |

## 7. Source Identity

| 항목 | 값 |
| --- | --- |
| productContentSha256 | `2d17ca4a3a9b5642bb9680b14f01facdefd1ef40561438f3bf75e0d6fdd24ea7` |
| productContentSha1 | `e41f875d3031fb964092778cc08fd145e4efc93b` |
| fileCount / totalBytes | 8454 / 46158489 |
