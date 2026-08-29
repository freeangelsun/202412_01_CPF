# CPF 개발·독립검수·QA 역할 및 권한 표준

## 역할
### DevGPT / Developer
개발, QA 결과 반영, Root Cause 보완, 자체검수, 개발 Evidence 작성 책임. `DevGPT_*` 영역만 소유한다. 개발 변경으로 재검수가 필요한 경우 새 검수 요청을 생성하지만 Codex/Claude/QA의 과거 판정을 임의 PASS로 변경하지 않는다.

### Codex / Claude = INDEPENDENT_REVIEWER
Codex와 Claude는 동일 역할로 취급한다. 둘 사이 완료 정의·검수 강도·권한 차이는 없다. DevGPT 구현을 독립적으로 검수하고 필요한 범위에서 보완하며 `IndependentReviewer_*` 및 독립 Evidence를 소유한다. 실제 재검증 없이 과거 PASS를 승계하지 않는다.

#### Source 수정 시 Fresh VS Code Zero-Diagnostic Gate
Codex/Claude가 Source를 한 줄이라도 수정하면 그 시점 이전의 VS Code PASS Evidence는 해당 영향범위에 대해 무효다. 수정된 Source와 upstream/downstream Consumer가 걸친 **모든 영향 Domain/Module**을 Fresh Java25/Gradle import/reload 기준으로 다시 검사하고 `Error=0 AND Warning=0`을 실제 Problems JSON으로 증명한다. 오류/경고가 하나라도 생기면 현재 WP에서 즉시 Root Cause를 찾아 동일 원인 전체를 수정하고 Build/Test/Consumer/Generator/Config 영향과 함께 재검증한다. suppression/waiver/expected 변경/검사 제외로 0을 만드는 False Green은 금지한다. 환경상 실행 불가하면 PASS가 아니라 `BLOCKED_EXTERNAL`/`VERIFICATION_PENDING`이며 Windows에서 Fresh Problems JSON을 생성·검증하는 명령과 재실행 조건을 남긴다.

Independent Reviewer가 Source 수정 후 PASS하려면 Role Ledger의 `source_modified=true`, `vscode_fresh_import=true`, `vscode_scope`, `vscode_problems_json`, `vscode_error_count=0`, `vscode_warning_count=0`이 모두 현재 Source Identity와 연결되어야 한다.

### QA
최종 Acceptance Owner. 전체 상태, 재개발/재검수 요청, 최종 PASS 여부를 판정한다. QA만 최종 완료를 확정한다.

## 역할별 기록 필수 필드
각 Work Item/Role은 `수행여부, 역할상태, 개발/검수내용, 완료사유, 미완료사유, 검증명령, 환경, 시작/종료, ExitCode, SourceIdentity, Evidence, 영향도검토, 회귀검토, Runtime결과, 다음조치`를 가진다.

## PASS 금지
`완료/CLOSED/PASS`인데 완료사유·명령·환경·exit code·Evidence·SourceIdentity·impact/regression 결과가 빠진 경우 Validator FAIL. 테스트를 실행하지 않은 경우 `NOT_EXECUTED`; 환경 부족은 `BLOCKED_EXTERNAL`; Source만 고친 경우 `SOURCE_FIXED`; 검증 대기는 `VERIFICATION_PENDING`이다.

## 상세 리뷰
사용자가 리뷰를 요청하면 모든 Work Item을 원순서대로 1:1 출력한다. 상위 WP만 요약하거나 완료 행을 생략하지 않는다. 각 행은 원요구, 실제 변경이유, 변경 Source/Consumer, 구현내용, 추가 발견, 사전/사후 영향도, Targeted/Regression/Runtime, Evidence, 역할별 상태, 완료/미완료 사유를 포함한다.
