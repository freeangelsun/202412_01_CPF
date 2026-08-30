# CPF 산출물 내용 구조화 표준 — Harness v2.15.4

## 1. 상위 원칙

여기서 “압축”은 페이지 수를 줄이는 뜻이 아니라 **중복과 불필요한 일반론을 제거하고 정보 구조를 명확히 하는 것**이다. 독자가 업무를 수행하는 데 필요한 Source-backed 정보에는 총 분량 상한을 두지 않는다. 페이지 수·기능명 개수·설명 길이는 품질 지표가 아니다.

## 2. 작성 순서

1. 결론 또는 선택을 먼저 제시한다.
2. 왜 그 선택인지 핵심 근거만 적는다.
3. 실제 개발/운영에 필요한 옵션·예를 짧게 제공한다.
4. 실패·주의 사항은 실제로 중요한 것만 남긴다.
5. 상세 계약/Source는 Specification 또는 Source 경로로 연결한다.

## 3. 삭제 대상

- 같은 내용을 표현만 바꿔 반복한 문장
- 모든 장에서 반복되는 일반론적 정상/실패/복구 문구
- 실제 선택에 영향을 주지 않는 내부 구현 설명
- 표에 이미 있는 내용을 다시 풀어쓴 문단
- 기능명만 길게 나열한 목록
- 페이지를 채우기 위한 배경/장점 홍보 문구

## 4. 개발자 문서

`하려는 일 → 기능/API → 핵심 옵션 → 선택 기준 → 3~8줄 예 → 주의/실패 → 확인` 순서로 쓴다. 장문의 기능 설명보다 개발자가 무엇을 골라 어떻게 쓰는지가 먼저 보여야 한다.

## 5. 운영 문서

`상황 → 확인/판단 → 조치 → 금지/승인 → 완료 기준` 순서로 쓴다. 메뉴 설명보다 판단과 안전한 조치를 우선한다.


## 6. 사용빈도 우선 개발자 가이드

개발자 가이드는 기능 Catalog나 교과서가 아니다. 실제 구현 중 자주 반복해서 찾는 거래 패턴, 호출 방식, API/Annotation, 핵심 옵션, 선택 기준, 오류·복구·검증을 우선한다.

- 거래/호출 절은 `하려는 일 → 패턴 → API/Annotation → 옵션 → 언제 선택 → 피해야 할 경우 → 오류/실패 → Retry/Idempotency/Reconcile/Compensation → 검증` 흐름을 우선한다.
- Same JVM과 Remote Domain Invocation은 같은 업무 계약의 Topology 차이로 설명한다. 내부 Domain 간 호출을 Gateway 경유로 설명하지 않는다.
- 저빈도 기능이나 내부 구현 상세는 본문을 늘리지 말고 1~2문장 요약 후 Specification/Source/EDU로 연결한다.
- 총 페이지 Budget은 두지 않는다. 반복·중복·독자 작업에 불필요한 설명만 제거하고, 필요한 Source-backed Coverage는 길이와 무관하게 유지한다.

## 7. 시각적 구조와 분량 균형

복잡한 거래·호출·오류·복구 설명은 같은 밀도의 장문 문단을 연속 배치하지 않는다. Decision Table, 비교도, Flow, State/Recovery Map 중 실제 이해를 빠르게 하는 표현을 먼저 선택하고 그림/표 아래 한국어 설명은 1~2문장으로 끝낸다. 그림과 글은 같은 내용을 중복하지 않고 서로 보완한다.

## 8. 경로와 Harness 가비지

- 사용자 Windows Repository Root `C:\dev\projects\jck\202412_01_CPF`를 포함한 생성 파일 절대경로는 150자를 초과하지 않는다.
- Repository에는 현행 `cpf-docs/governance/documentation-harness/**` 한 세트만 유지한다. 과거 Harness 버전, 날짜/R/REV/SESSION suffix, ZIP 해제본, 세션 백업, Validator 임시 출력은 삭제 대상이다.
- 목차는 실제 H1/H2가 화면에 보여야 하며 빈 TOC Field 페이지만 남는 산출물은 FAIL이다.


## v2.15.4 강제 보강

- README와 모든 공식 DOCX/PDF에는 총 파일 크기·페이지·문자·단어·Section/Figure 수 상한을 두지 않는다.
- 국소 Density/Paragraph/Table Threshold는 **재구성 Trigger**이며 정보 삭제 근거가 아니다.
- 길이 때문에 Source-backed Coverage 또는 Reader Task를 줄이면 FAIL한다.
- README는 브로셔형 Hero/시각 Story를 유지하고 모든 의미 Figure에 Alt Text + 바로 아래 간략 한글 설명을 제공한다.
- 작성자는 `DOCUMENT_DESIGN_PLAYBOOK.md`, `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md`, `README_BROCHURE_AND_AI_TEXT_STANDARD.md`, `AUTHORING_EXECUTION_PROTOCOL.md`를 따라야 한다.
- 최종 시각검수는 전페이지 Scan pass + Detail pass 두 번을 모두 수행하고 Evidence를 남긴다.
