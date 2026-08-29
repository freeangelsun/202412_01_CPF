# CPF Documentation False-Green Prevention Standard — Harness 2.15.1

1. 사용자 Finding은 자동/수동 PASS보다 우선한다.
2. 사용자 Finding 발생 시 이전 Acceptance와 Review Evidence를 자동 무효화한다.
3. Rule 문구만 보강하고 Validator/Fixture/Required Gate가 없으면 Harness 보강 완료가 아니다.
4. README Coverage는 Keyword 존재가 아니라 설명 깊이와 독자 행동 완결성으로 판정한다.
5. Visual 품질은 파일 존재·해상도·Contact Sheet로 대체하지 않는다.
6. Manual Evidence는 현재 파일 SHA와 일치해야 한다.
7. Source Identity와 CLI Surface는 실행 시 Source에서 읽으며 Validator 코드에 SHA/Command Set을 하드코딩하지 않는다.
8. Harness가 결함을 놓친 경우 산출물을 탓하지 않고 Harness 자체를 FAIL로 판정한 뒤 먼저 보강한다.
