# CPF 다음 개발GPT 공통 실행정책 — Post-R6I

- 현재 master reference: `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- 실제 다음 개발 시작 시 latest master exact SHA 재확인
- 이 문서는 다음 QA A/B 결과를 중앙 통합한 뒤 생성될 **구체 재개발 Requirement**에 공통 적용한다.

## 1. 개발 범위

개발GPT는 QA가 명시한 Finding만 국소 수정하지 않는다.
분석·개발·Test 과정에서 추가 문제를 발견하면 동일 Root Cause의 잠복 결함을 Repository 전체에서 찾아 Source/Test/SQL/API/Config/Frontend/Generator/Evidence까지 함께 보완한다.

## 2. 중앙 의견 승계

다음 개발 요청에는:
- Developer previous opinion
- QA A opinion
- QA B opinion
- Central review
- disagreement
- architecture decision
- additional QA/development
를 포함한다.

어느 하나의 의견도 자동 정답이 아니다. 최상위 Requirement와 actual Source를 따른다.

## 3. ADM / EDU

- ADM은 CPF 완제품이다. 도입 개발자가 ADM 본체를 다시 개발하지 않는다.
- EDU는 Public API/SPI/Extension/Integration 교육 예제다.
- QA가 EDU-ADM/EDU135를 재분류한 뒤 최상위 Requirement/Catalog/Generator/Manual/Test를 동시에 변경한다.
- QA 판정 전 개발GPT가 임의로 EDU ID를 삭제하지 않는다.

## 4. 거래·로그 P0 개발기준

QA에서 부족하다고 판정되면 다음을 하나의 개발단위로 닫는다.

- end-to-end transactionId lineage
- segment/parent/attempt
- Local/Remote/Async/Message/File/Batch propagation
- ADM transactionId one-shot timeline/tree
- DB transaction log canonical schema/index/retention
- structured file log
- rotation/compression/retention
- async queue/backpressure/disk full/process kill
- spool/replay/duplicate/loss alert
- Batch job/step/partition/worker correlation
- external call attempt/timeout/error
- masking/raw permission/audit
- Runtime/Test/Evidence

## 5. Evidence

실행하지 않은 Test는 PASS 금지.
result SHA, command, exit code, tool version, actual stdout/stderr hash, artifact hash를 남긴다.

## 6. 결과 전달

GPT는 Commit/Push하지 않는다.
Root-relative ZIP + SHA-256 + Handover를 사용자에게 제공한다.
사용자 Push 후 새 master에서 QA가 다시 검증한다.
