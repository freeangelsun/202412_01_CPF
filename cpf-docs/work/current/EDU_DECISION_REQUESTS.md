# EDU Decision Requests

사용자 결정은 Source 계약에 반영됐다.

- EDU Module: `cpf-reference` 하나
- EDU DB: `refDB`
- 생성형 도메인 연결: 금지
- 제품 BZA 연결: 금지
- Package: 기능 중심, 숫자 Requirement Package 금지
- Batch: `com.cpf.reference.batch` 상위 아래 기능별 분리, 미사용 시 통째 제거
- Batch SQL: `CPF_REF_BAT_*` V94/U94 선택 Pack
- Operations·Backoffice·Gateway: Optional Package
- Query 변경: 3 Vendor와 Generator 제외 계약 동시 반영

남은 항목은 설계 의사결정이 아니라 merged Build·3DB·Runtime·Browser·Supply-chain 검증이다.
