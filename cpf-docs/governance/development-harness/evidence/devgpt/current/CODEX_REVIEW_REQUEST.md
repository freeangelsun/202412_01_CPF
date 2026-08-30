# CPF Independent Reviewer 요청 — Current Source

## Source / Authority

- Current Development Harness만 실행 정본으로 사용한다.
- Product Source Identity: `1289304269e6f684cb9c32414efadbcfa179b5f7208bcd42f6ff1d5dff15a87f` / **8,450 files**
- Current Registry: **410 = Tracking 394 + Execution 16**
- Session Merge: **3 merged / pending 0 / conflict 0**
- Git commit/push/reset/restore/stash/clean 금지. Source 수정이 필요하면 Working Tree만 보완하고 Evidence를 남긴다.

## 독립 검수 원칙

Developer PASS를 승계하지 말고 Current Source에서 독립 재현한다. Finding을 발견하면 Root Cause 기준 기존 WP에 병합하고 Source→Consumer→Test/Runtime→Evidence까지 직접 보완한다. DTO/Interface/Sample/Swagger 존재만으로 완료하지 않는다.

특히 다음을 공격 검증한다.

1. `WP-B02`: Windows Fresh VS Code/Buildship/JDT actual **Error 0 / Warning 0**. source-empty profile class folder가 clean/reimport 후에도 안정적인지 검증.
2. `WP-H02`: CR-22 capability-first toolchain이 Host exact patch/minor 또는 Java exact host-major pin으로 다시 축소되지 않는지 확인.
3. `WP-FE01`: ADM/Backoffice Fresh npm ci/lint/typecheck/test/build 및 generated marker/OpenAPI consumer.
4. `WP-CLI01/RL01/RL02`: Windows/Linux CLI, 한글 Release 콘솔 가독성, detailed log 보존, Public Binary/Sources/Javadoc/POM/SBOM/Checksum/Fresh Consumer/Leakage 0.
5. `WP-BAT01`: app/root DB secret 분리, Gateway failure-result schema, 2-worker kill/takeover/fencing/UNKNOWN/reconcile.
6. Docker 전체/증분 설치 Script의 Fresh 설치·재실행·기존 Secret/Volume 보존·실행 중 container fail-closed·cleanup 경계.
7. Same Source DB3/One-WAS/Logging/OpenAPI/Browser/Performance/Full Runtime Fresh Replay.

## Developer 실제 근거

- Verification **126 PASS / 0 FAIL**
- Affected **62 PASS / 1 SKIP / 0 FAIL**
- Generator **24 PASS / 1 SKIP / 0 FAIL**
- Harness Product/Strength/Authority/Migration/Split/Detailed Review PASS
- Negative Mutation **17+5+5+6 전부 PASS**

위 근거는 독립 Reviewer PASS를 대신하지 않는다. Source가 바뀌면 영향범위 Evidence를 다시 실행한다.
