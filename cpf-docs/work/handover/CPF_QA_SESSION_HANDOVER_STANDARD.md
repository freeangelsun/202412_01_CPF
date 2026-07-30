# CPF QA 세션 인수인계 표준

후속 ChatGPT·Codex·QA 세션은 아래 항목을 그대로 이어받는다.

## 필수 인수인계

- 최신 Repository/Branch/HEAD SHA
- 이전 검토 Base SHA
- Working Tree Clean 여부
- 현재 Development Batch와 Vertical Slice
- QA31 Defect/Requirement/Scenario 상태
- 완료 Evidence 경로와 sourceSha
- 실행하지 못한 Java25/DB/Redis/Multi-instance/Browser 항목
- 재개방 Root Cause
- 다음 실행 명령
- Commit/Push/Branch/Tag/PR 여부
- 사용자 관여 선호: 가능하면 ADM·BZA UI, EDU, 실제 기능 적합성 중심
- 역할은 고정 분리가 아니라는 점
- README·Guide 별도 AI 병행 작업과 제외 정책

## 다음 세션의 첫 행동

1. 최신 master SHA 확인
2. QA 원본 무결성 확인
3. 최신 Diff와 이전 Completion Report 대조
4. 미검증을 완료로 승격하지 않았는지 확인
5. 목록 재작성만 하지 말고 현재 P0 수직 Slice를 실제 수정·검증

## 지속 표준

- Interface/DTO/Table/UI/Test 존재만으로 완료 금지
- actual Product Consumer 확인
- Local/Remote/미설치/Owner Down/다중 인스턴스 확인
- 실패·복구·Unknown·Audit·Ledger 확인
- exact-SHA Evidence 확인
- 중복 결함은 Root Cause에 통합
- 신규 편의 기능은 사용자가 승인한 ADM·EDU 범위 외에는 자제
- 원본 요청서와 Matrix를 임의 변경하지 않음
