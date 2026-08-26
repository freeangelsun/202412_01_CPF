# CPF Documentation Final Gate — v2.1.0

최종 완료는 아래 Gate가 **모두** PASS해야 한다.

- [ ] HARNESS_SCHEMA_PASS
- [ ] ARTIFACT_STRUCTURE_PASS
- [ ] SOURCE_IDENTITY_PASS / SEMANTIC_PASS
- [ ] RENDER_TECHNICAL_PASS
- [ ] HUMAN_VISUAL_PASS — 모든 Dimension >=4, 평균 >=4.4, Critical 0
- [ ] REGRESSION_PASS — 승인 기준선 대비 퇴행 0
- [ ] CLICK_THROUGH_PASS — PDF/DOCX 실제 Target 확인
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
