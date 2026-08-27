# CPF Documentation 다음 세션 인수인계 - Harness v2.5.0 / PATCH_FIRST

- 기준 Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_125420(1).zip`
- Source SHA-256: `96587736A2BDCA1CE11896982E8DE5A7432FAF0CDC560125528FE4A236A3ECF9`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Canonical Harness: `cpf-docs/governance/documentation-harness/` **v2.5.0 current-only**
- 공식 산출물: `README.md` 1 + DOCX 11 + PDF 11 + Product Visual PNG 8
- 최종 DOCX/PDF 페이지: 53/53

## 반드시 그대로 승계할 원칙
1. 산출물은 **PATCH_FIRST**다. 기존 PASS/좋은 디자인/좋은 내용은 유지하고 실패한 구조만 수정한다. 전면 Fresh rewrite/redesign은 사용자 명시 승인이 없으면 금지한다.
2. 사용자가 새로 지적하거나 추가한 품질 기준은 해당 산출물만 고치지 않는다. **Harness Common Rule/Profile/Token/Validator/Manual Hard Gate + Negative Fixture**까지 함께 보완해 같은 문제가 재발하지 않게 한다.
3. Harness와 산출물은 **현행본 하나만 유지**한다. `_old`, `_backup`, `_history`, `_session`, versioned harness copy, 과거 session garbage를 남기지 않는다.
4. 삭제 대상은 Root 상대경로 exact `DELETE_MANIFEST.txt`로 관리하고 사용자 실행용 삭제 명령을 항상 함께 전달한다. Wildcard/Root 밖 삭제는 금지한다.
5. 표는 실제 관계형/비교 데이터에만 쓴다. reader/purpose/basis, 단일 메시지, 단순 절차, 메뉴/TOC를 억지 표로 만들지 않는다.
6. 사용자 본문에는 Harness version/Source SHA/build baseline 같은 제작 provenance를 노출하지 않는다. Evidence/Manifest에만 둔다.
7. Visual은 원본 PNG만 보고 PASS하지 않는다. README/DOCX/PDF 삽입 상태에서 crop/overlap/safe margin/contrast/effective text size를 확인한다. Connector/arrow는 source/target Box 경계에서 시작/종료하며 Box 내부 침범 0을 요구한다.
8. DOCX는 11개 전페이지 Render + 구조 Validator + Accessibility를 실행하고, PDF는 최종 DOCX에서 Fresh Export 후 PDFium/Poppler/Preflight/Font/Glyph를 확인한다.
9. 진행률 보고는 중단점이 아니다. 사용자가 100% 완료를 요구하면 최종 ZIP/Hash/Clean Replay까지 연속 수행한다.
10. 다음 세션에 더 최신 Source ZIP이 오면 과거 Source SHA/Evidence를 승계하지 않고 새 ZIP SHA를 다시 기준선으로 확정한다.

## 이번 사용자 지적에서 고정된 재발 방지
- 화살표/Connector가 Box·Text·Label을 침범하면 FAIL. 대상 Box 외곽 Boundary까지만 표시한다.
- `누가 보는가 / 이 문서로 끝낼 일 / 기준` 같은 도입부 Meta 정보를 2열 표로 만들지 않는다.
- Harness/Source SHA 제작 정보를 일반 독자 첫 화면에 노출하지 않는다.
- 마지막 페이지에 표 꼬리 1~2행, 제목+한 문장만 남는 isolated trailing page를 허용하지 않는다. 실질적인 독립 결론/Decision Matrix인 경우에만 근거화한다.
- 동일 결함은 모든 공식 문서에 공통 적용해 전수검수한다.

## 다음 세션 시작 순서
최신 Source identity -> Harness current-only/lock -> Existing Artifact KEEP/PATCH audit -> Targeted patch -> Visual -> DOCX 전페이지 -> Accessibility -> PDF Fresh Export -> PDFium/Poppler -> Evidence/Manifest -> Delete Manifest -> Fresh Clean Replay -> Final ZIP.
