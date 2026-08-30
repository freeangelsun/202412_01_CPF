> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `../CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF 최종 전달·명령·인수인계 표준

## 1. 최종 전달 필수 구성
개발/검수/Harness 변경이 끝난 세션은 최종 응답 전에 다음을 모두 실제 생성·검증한다. 하나라도 없으면 최종 완료를 선언하지 않는다.

- Current-only Root-relative Overlay ZIP
- ZIP SHA-256 sidecar 및 ZIP 재추출 검증 Evidence
- Source Identity / Package Manifest / Change Manifest / Delete Manifest / SHA256SUMS
- 상세 `FINAL_REVIEW.md`, `TEST_AND_EVIDENCE.md`, `OPEN_ISSUES.md`, `HANDOVER.md`
- Windows/Linux 적용 명령
- 기존 정본·가비지·삭제 후 빈 폴더 정리 명령
- 저비용 검증 명령
- Windows/Linux 최고강도 Runtime 검증 명령
- Git read-only status 명령
- Codex/Claude 독립 검수 요청 경로

## 2. 명령 형식
사용자에게 전달하는 Apply/Delete/Verify/Runtime/Git 명령은 **각각 독립적인 한 줄 명령**이어야 한다. 여러 단계 수동 복사나 여러 줄 실행 순서를 필수 전제로 하지 않는다. Windows PowerShell과 Linux shell은 동일 의미를 가져야 한다.

Runtime/Verify/Release 명령의 prerequisite version/range는 과거 대화에서 하드코딩해 재사용하지 않는다. Current Source의 canonical verifier/bootstrap/toolchain/package metadata에서 required 값을 다시 확인하고, 명령 또는 사전검증에서 `required/actual`을 명확히 표시한다. 사용자 PC actual 값에 맞춰 Product Contract를 낮추는 명령을 제공하지 않는다. Host 도구는 capability-first 정책을 적용해 호환 가능한 설치 버전을 최대한 재사용하고, 불필요한 exact patch/minor 강제는 전달 명령에 넣지 않는다.

## 3. 삭제와 빈 폴더
삭제는 `DELETE_MANIFEST.csv`의 typed exact allowlist만 사용한다. Canonical/legacy Source는 `FILE_SHA256`로 적용 직전 hash를 검증한다. 실행 때마다 내용이 달라지는 build/cache/generated garbage는 `GENERATED_ROOT`로 분리하고, Current Garbage Decision에서 generated ownership이 확인된 exact root-relative directory만 허용한다. `GENERATED_ROOT`는 hash 불일치를 이유로 실패시키지 않는 대신 Repository containment, symlink/reparse-point 금지, protected/current Harness 경로 금지, 허용 generated leaf 검증을 모두 통과한 뒤 그 exact root만 재귀 삭제한다. 경로에서 선행 `.`을 제거하거나 normalize 과정에서 파일명을 바꾸는 Manifest 생성은 금지한다. 삭제 후에는 허용된 부모 경로를 아래에서 위로 검사해 **비어 있는 디렉터리만** 제거한다. Repository Root, 보호 경로, Current Development Harness, Documentation Harness, Guides/Deliverables 보호경로는 삭제하지 않는다. wildcard/broad repository recursive delete, `git clean`, `reset --hard`, `restore .`를 사용하지 않는다. 결과에는 `EMPTY_DIRS_DELETED=<n>`을 남긴다.

## 4. 로컬 Runtime
환경 문제로 로컬 Runtime을 못 돌려도 범위를 축소하지 않는다. Windows/Linux 각각 최고강도 명령, prerequisite, PASS/FAIL 기준, 로그 경로를 남기고 `BLOCKED_EXTERNAL/NOT_EXECUTED`로 관리한다. 실제 PASS Evidence 전에는 완료가 아니다.

## 5. 최종 Handover
다음 세션은 별도 개발 기본지침을 요구하지 않고 Harness 하나에서 시작할 수 있어야 한다. Handover는 Source Identity, Current Work Item 전체 상태, 역할별 상태, Product Gap, 마지막 Gate, Runtime 미검증, 재실행 명령, Delete 상태, Codex/Claude 요청, 다음 정확한 시작점을 포함한다. 리뷰 요청 시 전체 Work Item을 1:1 상세 출력할 수 있어야 한다.

## 6. Session/Merge Handover 필수 규격

모든 세션 Handover는 `CPF_WORK_ITEM_SESSION_MERGE_AND_REPORT_STANDARD.md`를 따르며 최소 다음을 포함한다.

- 현재 sessionKey / 역할 / Source Identity
- Current Merge Control State 전체
- 이번 세션에서 Merge한 sessionKey
- 발견했지만 아직 Merge되지 않은 sessionKey
- MERGE_CONFLICT sessionKey와 정확한 이유
- 이번 세션에서 실제 작업한 Work Item ID와 각 상태
- Work Item별 Evidence 경로
- mandatory 미실행/실패/환경 Blocker와 재실행 조건
- 다음 세션이 **가장 먼저 실행할 Merge Preflight**와 그 뒤의 정확한 Work Item

Handover는 `여기까지 완료`, `나머지 다음 세션` 같은 요약만으로 대체하지 않는다. 다음 작업자가 Harness와 Handover만 읽고 미Merge 항목을 자동 발견·통합할 수 있어야 한다.

## 7. Final Self Review

최종 전달 직전에는 Current Registry의 모든 Mandatory Work Item을 한 건씩 리뷰한다. 각 항목의 Requirement, Root Cause, Source, Consumer, Test, Runtime, Regression, Evidence SHA, Source Identity, 역할별 상태, 완료/미완료 사유를 확인한다. 개별 근거가 없는 일괄 완료/일괄 SKIP/집계 PASS는 최종 전달 근거가 아니다.

