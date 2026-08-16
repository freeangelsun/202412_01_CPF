# CPF Documentation Continuity

기준 Source SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

다음 작업은 공식 문서 본문을 다시 임의 축약하는 것이 아니라, Source 변경 또는 개발 검토 결과가 실제로 확정됐을 때 영향 문서만 다시 Source와 대조하고 동일한 지침 Gate를 재실행하는 방식으로 이어간다.

## 재검수 Trigger

- Generated Domain / Batch Generator 계약 변경
- Public Profile/Starter/Provider Catalog 변경
- Public API/SPI/Annotation/Config/State/Error 계약 변경
- ADM/BZA/Gateway/Batch 운영 API 또는 UI 변경
- Oracle/PostgreSQL/MariaDB DB lifecycle 변경
- 공식 문서 작성 지침 변경

Trigger가 발생하면 영향 문서 작성 → 문서별 지침 Gate → Source Fact Check → DOCX render → 전 페이지 Visual QA → PDF 재렌더 → Manifest/SHA 순으로 다시 검수한다. 중간 Gate FAIL은 완료로 승계하지 않는다.
