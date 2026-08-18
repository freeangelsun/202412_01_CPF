# CPF Documentation Continuity

다음 세션은 이 문서를 읽은 뒤 기존 공식 문서를 축약하거나 새 Guide를 무작정 추가하는 것으로 시작하지 않는다.

## 반드시 유지할 해석

- 사용자가 개발 진행정보·Steering·QA 역할을 주는 목적은 **최종 구현 완료 상태의 CPF를 사용자 문서에 정확하게 정리하기 위한 것**이다.
- QA는 결함 목록을 사용자 문서에 싣는 작업이 아니라 Source Truth를 확보해 개발자·운영자가 문서만 보고 이해하고 사용할 수 있게 만드는 과정이다.
- API/Class/Annotation/Header/Config/Generator가 바뀌면 README → Owner Guide → Specification → 설계 산출물 → 예제/표/도식 → Handover를 전수 currentize한다.
- 새 Requirement를 License 아래나 문서 마지막에 기계적으로 append하지 않는다.
- 기존의 유효한 상세 설명을 축약해 문서 품질을 떨어뜨리지 않는다.

## 레이아웃 영구 규칙

- 빈 공간 최소화 ≠ 문단 간격 제거.
- 본문과 중간 절에는 읽기 쉬운 여백을 유지한다.
- 실제 대장급만 새 페이지를 기본으로 하며 모든 Heading에 강제 Page Break를 넣지 않는다.
- 표 한 행/불릿 한 줄/목차 한 항목/제목 하나가 다음 페이지에 고립되지 않게 한다.
- 최종 판단은 페이지 전체의 균형, 정보 밀도, 표/그림/설명 결합을 보고 한다.
- DOCX는 반드시 전 페이지 render 후 시각검수하고 PDF TOC 내부 링크까지 실제 검증한다.

## 현재 Publication Surface

README 포함 사용자 공식 7종 + 별도 설계 산출물 5종.

## 최신 기능 정본

- Channel: 외부 필수 5 / 내부 Context 6 / Current Receiver-owned / systemCode 값 그대로 Channel Identity / 1~16자 / Policy=`operationId + callerChannel`.
- Subject Tracking: 값 Optional, Collector Mandatory, `Subject → transactionId → Timeline`, Late Enrichment, protected search key, Masking/RBAC/Audit/Retention.
- Central Server Registry: Managed Server + Runtime Instance + Capability exactly-one Master, 기능별 Server Master 금지, Dashboard/Gateway/Batch/Logging/Config/Health가 동일 Identity 재사용.

세부 내용과 QA 결과는 같은 경로의 `CPF_DOCUMENTATION_HANDOVER.md`와 `DOCUMENTATION_REVIEW.md`를 우선 읽는다.
