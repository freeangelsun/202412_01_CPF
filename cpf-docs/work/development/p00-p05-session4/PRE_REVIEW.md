# CPF P00-P05 Session 4 사전 리뷰

- 기준 Branch: `master`
- 최신 기준 SHA: `d2adc89f344fa1f93a2f9291f6576ce69be05239`
- 이전 개발 기준 SHA: `a6856e7557f586875796172ac6ebae22bb87958e`
- 범위: 논리 Execution Sequence 행 1~10,027, P00~P05
- Git 쓰기·삭제: 수행하지 않음
- 보호 경로 변경: 없음

## 확인된 초기 결함

1. 28개 Split Part의 Index `size_bytes`·`sha256`가 Git LF 정본이 아닌 Local CRLF 기준이었다.
2. 기존 Traceability 검증기는 단일 QA Round 파일과 고정 건수를 전제해 Split Canonical Master를 소비하지 못했다.
3. Starter Capability Profile의 SMS SPI 좌표와 Integration/Operations Runtime 조합이 Catalog와 어긋났다.
4. ADM/BZA Browser Actor 차단 목록과 Batch Owner 전달 Sanitizer가 `operatorId` 계열 및 중첩 Map/List를 완전히 차단하지 못했다.
5. 일부 P03 검증기는 현재 Owner 경로가 아니라 과거 `cpf-common` 경로를 참조했다.
6. 최초 Checkpoint는 Java 21 대체검증과 Browser 가용성을 충분히 활용하지 않았고, Codex 이관 필수 필드가 부족했다.
7. 작업 중 최신 `master`가 변경됐으나 새 Commit은 보호 문서 14개만 변경했다. Overlay 중첩 0건을 확인하고 최신 SHA로 재기준화했다.

완료 판정은 Source·실제 Consumer·Test·현재 환경 실행 Evidence가 연결된 항목에만 적용한다. 외부 Runtime만 없는 항목은 구현 완료와 `검증 이관`을 분리한다.
