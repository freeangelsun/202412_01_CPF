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

## 3. 삭제와 빈 폴더
삭제는 `DELETE_MANIFEST.csv` exact file allowlist만 사용한다. 삭제 후에는 삭제된 파일의 부모 경로를 아래에서 위로 검사해 **비어 있는 디렉터리만** 제거한다. Repository Root, 보호 경로, Current Development Harness, Documentation Harness, Guides/Deliverables 보호경로는 삭제하지 않는다. wildcard/broad recursive delete, `git clean`, `reset --hard`, `restore .`를 사용하지 않는다. 결과에는 `EMPTY_DIRS_DELETED=<n>`을 남긴다.

## 4. 로컬 Runtime
환경 문제로 로컬 Runtime을 못 돌려도 범위를 축소하지 않는다. Windows/Linux 각각 최고강도 명령, prerequisite, PASS/FAIL 기준, 로그 경로를 남기고 `BLOCKED_EXTERNAL/NOT_EXECUTED`로 관리한다. 실제 PASS Evidence 전에는 완료가 아니다.

## 5. 최종 Handover
다음 세션은 별도 개발 기본지침을 요구하지 않고 Harness 하나에서 시작할 수 있어야 한다. Handover는 Source Identity, Current Work Item 전체 상태, 역할별 상태, Product Gap, 마지막 Gate, Runtime 미검증, 재실행 명령, Delete 상태, Codex/Claude 요청, 다음 정확한 시작점을 포함한다. 리뷰 요청 시 전체 Work Item을 1:1 상세 출력할 수 있어야 한다.
