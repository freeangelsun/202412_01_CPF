# Development Harness Evidence

- `devgpt/current/`: DevGPT 현재 실행 Evidence
- `independent-reviewer/current/`: Codex/Claude 공통 독립검수 Evidence
- `qa/current/`: QA 현재 Evidence
- `platform/current/`: 역할 중립 Current verification Evidence
- `generated/current/`: Harness/Generator가 생성한 현재 Evidence

과거 Source Identity의 PASS는 Current PASS로 승계하지 않는다. Evidence 파일명에 RERUN 번호를 누적해 history를 만들지 않고 current identity 기준 의미 있는 ID를 사용한다.
