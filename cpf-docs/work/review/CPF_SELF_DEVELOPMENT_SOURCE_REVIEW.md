# CPF 자체 개발 Source 검토

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Baseline SHA: `95e592c05fc457301efdb13ee50e0d7453325806`
- 목적: 개발 주체가 실제 Source 검토로 발견한 누락·구조 부채·운영 완성도 결함을 독립 Backlog로 관리한다.

## 정본 분리

이 문서와 연계 Matrix는 개발 주체의 자체 검토 결과만 포함한다.

- 외부 검수 조직이 제공하는 Requirement·Defect ID를 가져오지 않는다.
- 외부 검수 문서를 수정하거나 재분류하지 않는다.
- 외부 검수 회차명을 파일명·ID·상태에 사용하지 않는다.
- 이후 별도 검수 목록이 전달되더라도 원본 ID와 정본을 유지하고 실행 계획에서만 선행 의존성을 조정한다.

## 확인된 반복 원인

1. File·Menu·Script 존재를 기능 완결로 오판
2. 실제 Consumer와 Runtime Evidence 없는 완료 선언
3. Bulk-ID Evidence와 기계적 Scenario
4. Push 후 SHA·Generated Artifact·정본 불일치
5. 공통 조회 Wrapper를 상용 운영화면으로 오판
6. Menu·Route·Permission·API Registry 분리
7. Batch와 Online 식별자는 있으나 운영 Timeline 미완결
8. Frontend OpenAPI·Generated Client와 실제 Consumer Drift
9. 네트워크·DB·다중 인스턴스 Runtime 검증 부족
10. Source 수정과 Evidence 생성이 서로 SHA를 변경하는 구조

## 보호 대상

- Batch Job Pack·Version·Approval Governance
- Transaction Group 기반 통합 추적
- Break-glass와 위험조치 Audit
- Gateway Default Deny와 Network Identity
- 공식 DB Vendor 3종 정책
- 기존 정상 기능과 Public API/SPI 경계
