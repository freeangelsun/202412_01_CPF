# Codex 독립 검수 요청 — CPF 20260801_02

기준 SHA는 `19dd72b5978f2a3c630943c0fff05bee2d2fed34`이다. README와 README 연결 Manual·Guide는 검수·수정 범위에서 제외하고 완료 근거로 사용하지 않는다.

한 번만 다음 순서로 검수한다.

1. `git diff --check`
2. `python cpf-tools/scripts/verify-cpf-controller-permission-contract.py --root . --strict`
3. `python -m unittest discover -s cpf-tools/scripts/tests -p "test_*.py" -q`
4. ADM/BZA 각각 clean `npm ci` 성공 후 `node cpf-tools/scripts/verify-cpf-frontend-source-syntax.cjs .`
5. Java 25 환경에서 Build·Test

완료 처리 금지 조건: Operation ID 누락, Python Test Error/Failure, 암묵적 CP949 의존, Frontend compiler 미설치 상태의 성공 처리, 실행하지 않은 Runtime 검증의 성공 기록.
