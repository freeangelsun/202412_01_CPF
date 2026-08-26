# CPF Documentation Final Gate — v2.3.0

최종 완료는 아래 Gate가 **모두** PASS해야 한다.

- [ ] HARNESS_SCHEMA_PASS
- [ ] ARTIFACT_STRUCTURE_PASS
- [ ] SOURCE_IDENTITY_PASS / SEMANTIC_PASS
- [ ] RENDER_TECHNICAL_PASS
- [ ] HUMAN_VISUAL_PASS — 모든 Dimension >=4, 평균 >=4.4, Critical 0
- [ ] REGRESSION_PASS — 승인 기준선 대비 퇴행 0
- [ ] CLICK_THROUGH_PASS — 사용자 Navigation의 PDF 실제 Target 확인 및 DOCX 링크 0 확인
- [ ] ACCESSIBILITY_PASS
- [ ] PDF_PREFLIGHT_PASS + Korean Font Embedded + 2 Renderer
- [ ] PATH_OVER_150=0
- [ ] OLD_HARNESS_FILE=0 / OLD_HARNESS_REFERENCE=0
- [ ] PACKAGE_REPLAY_PASS

`AUTOMATED_PASS_ONLY`는 최종 완료가 아니다.

## Fresh Rewrite 예외 Gate

전체 재생성/재디자인을 하기 전 아래 중 하나를 증명한다.

- 사용자가 명시적으로 전면 재작성을 요청
- 파일 손상으로 Patch 불가능
- Canonical Architecture/Profile 핵심 Outcome 변경으로 전체 구조 무효화
- 생성 도구 제약상 재생성 불가피 + 승인 Block 동일 재현 Regression Plan 존재

예외가 아니면 **항상 기존 산출물 보완 수정**이다.

## 최초 재구축 예외 Lifecycle

기존 공식 산출물이 사용자에 의해 `BASELINE_REJECTED`로 판정된 경우에만 사용자 명시 요청으로 최초 1회 `INITIAL_FRESH_REBUILD`를 수행할 수 있다. 결과는 `GOLDEN_BASELINE_CANDIDATE`이며, `USER_APPROVED` 또는 `VISUAL_QA_APPROVED` 이후에는 `PATCH_ONLY`로 전환한다. 승인 이후 관계없는 영역의 Fresh Rewrite는 Regression FAIL이다.


## v2.3.0 독자 니즈·실행형 매뉴얼 강제 규칙

- 모든 산출물은 `누가 → 왜 연다 → 무엇을 결정/개발/운영한다 → 완료 기준`을 먼저 정의한다. 파일/기능 분류를 위한 메뉴는 금지한다.
- Framework/Batch Developer Guide의 주요 기능 장은 `Task Summary → Public API → Option/기본값 → 선택 기준 → 실패·복구 → 최소 예 → 검증 → Source`를 첫 스캔에서 찾을 수 있어야 한다. 설명문만 있는 장은 FAIL이다.
- API/옵션/선택표가 핵심이고 Portrait에서 token 개행·과밀이 발생하면 Landscape를 사용한다. 문서 방향보다 독자의 스캔 속도를 우선한다.
- 일반 사용자 Navigation은 PDF만 제공한다. DOCX는 최종 ZIP에 포함하지만 README/산출물목록의 바로 열기 링크로 노출하지 않는다.
- Figure 설명 앞에 `그림 해석`, `그림 설명` 같은 중간 라벨을 두지 않는다. Figure 바로 아래 1~2문장으로 의미를 자연스럽게 고정한다.
- 모든 Product Figure는 geometry manifest를 가진다. 실제 canvas/frame/node/text/annotation bounding box가 safe area를 침범하거나 서로 겹치면 자동 Gate에서 FAIL해야 한다.
- Markdown Viewer의 Host Background는 README가 제어할 수 없다. 대신 CPF가 소유하는 Hero/주요 Content Surface는 Dark Brochure로 분명히 만들고 그 위 주요 Visual은 light/neutral canvas로 분리한다.


## v2.3.0 추가 Hard Gate

- [ ] TABLE_SEMANTIC_FIT_PASS — 표는 실제 행·열 관계 데이터에만 사용
- [ ] TABLE_HEADER_WRAP=0
- [ ] UNJUSTIFIED_EQUAL_WIDTH=0 / UNJUSTIFIED_FIXED_50_50=0
- [ ] REPEATED_VALUE_ONLY_COLUMN=0
- [ ] MAJOR_HEADING_ORPHAN_WRAP=0
- [ ] SECTION_TRANSITION_CROWDING=0
- [ ] EMBEDDED_FIGURE_BOUNDARY_INTRUSION=0 / EMBEDDED_FIGURE_CROP=0
- [ ] SEMANTICALLY_INCOMPLETE_VISUAL=0

균등폭 자체는 실패가 아니다. 대칭 비교처럼 균등폭이 정보 구조에 맞으면 PASS이며, 내용 길이와 역할이 다른데도 균등폭을 고집해 불필요한 공백/자동개행이 생길 때 FAIL이다.
