# CPF Documentation Hard Gate Policy — v2.10.0

## 1. 최종 PASS의 유일한 정의

최종 PASS는 `quality-acceptance.json`의 **모든 `required=true` Gate가 정확히 `PASS`**이고, 필수 Evidence가 존재하며, Critical Finding이 0건일 때만 가능하다. `AUTOMATED_PASS_ONLY`, `NOT_EXECUTED`, `BLOCKED`, `UNKNOWN`, `SKIPPED`, `PARTIAL`, `WAIVED`는 PASS가 아니다. 외부 환경 때문에 실행하지 못한 Gate가 있으면 최종 상태는 미완료다.

`validators/validate_final_acceptance.py` 또는 `validate_final_acceptance.ps1`이 최종 승인 권위자다. 개별 Validator PASS, 평균 점수, Contact Sheet, 이전 Evidence만으로 최종 PASS를 선언할 수 없다.

## 2. README False Green 금지

- `CPF를 적용하면 무엇이 달라지는가`, `핵심 장점`, `왜 좋은가`, `이 구조의 장점`, `핵심 해석`, `기반 기술`, `차별점`, `효익`, `좋아지는 점`, `편해지는 점` 같은 **장점 전용 Heading/Section을 금지**한다.
- CPF의 강점은 제품 정의, 전체 구조, 개발 흐름, 거래·호출·실패 처리, Batch·연계·운영 설명 속에서 **구조와 동작의 결과로 자연스럽게** 드러나야 한다.
- README는 상세 Reference가 아니다. 긴 API/옵션/실패 목록은 Guide로 이동한다.
- 긴 문단 3개 연속, 표 연속, 설명 없는 대형 Figure 연속, 한 Section 과밀을 FAIL 처리한다.
- 한 개 H1 제품 제목과 H2 최소 5개 권장, 상한 없음를 기본 범위로 하며 Heading 깊이는 H3까지로 제한한다.

## 3. 표 Width/Wrap Hard Gate

- 비대칭 표의 균등폭은 금지한다. 동일폭은 **대칭 비교 + 의미 역할 동등 + Content Demand 변동 12% 이하**일 때만 허용한다.
- ID/상태/필수/기본값/Code 열은 좁게, 설명/용도/선택/실패/복구/주의 열은 넓게 배정한다.
- Header Wrap 0, Short Token Wrap 0, Semantic Width Inversion 0, 반복 과도 개행 0이 필수다.
- OOXML `tblGrid`만 보고 PASS하지 않는다. 최종 Render Evidence에서 실제 개행을 확인한다.

## 4. 세로 호흡 Hard Gate

페이지 수를 줄이기 위해 여백을 줄이지 않는다. 기본 최소값은 H1 before 52pt, H2 28pt, H3 18pt, Body line 1.25, Body after 7.5pt, 의미 Block 전환 14pt다. 수치가 맞더라도 전페이지 Render에서 답답해 보이면 `HUMAN_VISUAL_PASS`는 FAIL이다.

## 5. Reader Task 완결성

Keyword 존재는 Coverage PASS가 아니다. 문서의 대상 독자는 적용 가능한 범위에서 다음을 완료할 수 있어야 한다: 목적/질문, 선택·사용 시점, 입력·옵션·선행조건, 정상 흐름, 실패·경계·UNKNOWN, 복구·Reconcile·다음 행동, 결과 검증, Source/Sample/상세 Reference. 하나라도 필요한데 빠지면 FAIL이다.

## 6. Visual Connector

Connector/Arrow는 대상 Box 외곽 Boundary에서 끝나야 한다. Target/Source Interior Penetration 0, Arrowhead Interior 0, Text/Label/다른 Node 교차 0이 필수다. 원본 PNG뿐 아니라 README/DOCX/PDF 삽입 Render에서도 재검증한다.

## 7. 사용자 Finding 재발 방지

사용자 Finding을 산출물만 수정하는 것은 금지한다. 동일 Finding을 Harness Rule + Profile + Negative Fixture + Validator + Final Gate에 반영해야 한다. Negative Fixture가 실제로 FAIL되지 않으면 Harness 자체가 FAIL이다.

## 8. Current-only

Repository에는 `cpf-docs/governance/documentation-harness/` 현행본 하나만 유지한다. versioned folder, `_old`, `_backup`, `_history`, `_session`, stale snapshot을 남기지 않는다. 삭제는 `DELETE_MANIFEST.txt`의 exact Root-relative 경로만 사용한다.


## v2.10.0 강제 보강

- README와 모든 공식 DOCX/PDF에는 총 파일 크기·페이지·문자·단어·Section/Figure 수 상한을 두지 않는다.
- 국소 Density/Paragraph/Table Threshold는 **재구성 Trigger**이며 정보 삭제 근거가 아니다.
- 길이 때문에 Source-backed Coverage 또는 Reader Task를 줄이면 FAIL한다.
- README는 브로셔형 Hero/시각 Story를 유지하고 모든 의미 Figure에 Alt Text + 바로 아래 간략 한글 설명을 제공한다.
- 작성자는 `DOCUMENT_DESIGN_PLAYBOOK.md`, `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md`, `README_BROCHURE_AND_AI_TEXT_STANDARD.md`, `AUTHORING_EXECUTION_PROTOCOL.md`를 따라야 한다.
- 최종 시각검수는 전페이지 Scan pass + Detail pass 두 번을 모두 수행하고 Evidence를 남긴다.

## Windows Root Containment Gate

- Overlay 적용/삭제 PowerShell은 `GetFullPath`로 대상 경로를 확정하고 repository root + `DirectorySeparatorChar` 경계 안인지 확인한다.
- Root containment prefix를 하드코딩된 backslash 문자열로 조립하여 정상 Root 상대경로를 `OUTSIDE ROOT`로 오판하는 구현을 금지한다.
- `APPLY.ps1`과 `DELETE_ONLY.ps1`에 `DirectorySeparatorChar` 기반 root prefix가 없거나 `TrimEnd('...')` 형태의 separator literal이 다시 들어오면 Harness 검증을 FAIL한다.



## Harness 2.10.0 Visual Quality Uplift

- 자동 Validator PASS보다 실제 사용자/육안 Finding을 우선한다. 표 Header 2줄, 답답한 문단 호흡, 저대비 Header, 저밀도 마지막 페이지가 보이면 자동 PASS라도 FAIL이다.
- 승인되거나 잘 된 현행본은 PATCH_FIRST로 보존하며 Finding 영향 밖의 구조·내용·Visual을 전면 재작성하지 않는다.
- README는 900/1200/1440px에서 대메뉴 전환 호흡과 Header 단일 행을 확인한다. 폭이 부족한 Markdown 표는 유지하지 않고 독자 흐름에 맞는 prose/list/card로 바꾼다.
- 진한 DOCX 표 Header는 텍스트 대비 4.5:1 이상이며 AUTO/검정 글자를 허용하지 않는다.
- 마지막 페이지의 제목+한 문장, Source-only, 표 꼬리 1~2행, 의미 없는 대형 공백은 금지한다. 페이지 수를 줄이기 위해 전체 Font/Margin/Line spacing을 축소하지 않고 내용·국소 배치를 보정한다.
- README Visual은 원본과 900/1200/1440px 삽입 Surface에서 text safe area·crop·boundary intrusion 0을 확인한다.
- Windows VS Code built-in Markdown Preview Runtime은 가능한 Windows 환경에서 별도 실행한다. 실행하지 못한 경우 미검증으로 기록한다.
