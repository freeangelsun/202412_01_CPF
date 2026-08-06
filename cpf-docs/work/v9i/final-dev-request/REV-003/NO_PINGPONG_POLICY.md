# No-Ping-Pong Policy

## 목적

환경이 없는 역할에게 동일 실행을 반복 요청하지 않고 구현·대체검증·Target Runtime 검증을 분리한다.

## 규칙

1. Capability 확인 1회.
2. 동일 환경의 실제 실행 시도 1회.
3. 원인과 변경점 없는 재시도 금지.
4. 환경 부족이면 개발GPT는 대체검증과 실행 Package를 완성.
5. 개발GPT 역할 완료 후 실제 Runtime은 Codex로 이관.
6. Codex도 환경이 없으면 QA/담당 환경으로 이관.
7. 환경 부족은 개발GPT 재개발 사유가 아니다.
8. 실제 Runtime이 Source·Config·Script 결함을 증명한 경우에만 개발GPT를 다시 연다.
9. 외부 Runtime 대기 중 직접 수행 가능한 Requirement는 계속 완료한다.
10. 미실행을 PASS로 기록하지 않는다.

## 역할 상태와 전체 상태

대체검증을 모두 완료한 환경 의존 Requirement:

- 개발GPT_상태 = 완료
- 개발GPT_자체검수상태 = 완료
- development_status = 재확인 필요
- verification_status = 미검증
- QA_검수결과 = 공란 또는 미통과

실제 Runtime과 QA가 끝나기 전 전체 완료가 아니다.
