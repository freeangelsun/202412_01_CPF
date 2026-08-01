# CPF QA37 Codex Resume and Rerun Policy

## 원장 위치

```text
C:\dev\Docker\CPF\output\codex\qa37\execution-ledger.csv
```

## 재개 절차

1. Preflight를 다시 실행해 `HEAD == origin/master`와 Working Tree를 확인한다.
2. 실행 원장에서 각 Stage의 마지막 결과만 본다.
3. 마지막 `PASS` 다음의 첫 미완료 Stage부터 재개한다.
4. 이미 `PASS`인 Stage는 재실행하지 않는다.
5. `FAIL` Stage는 Root Cause 수정 전에는 재실행하지 않는다.
6. 수정 후 영향 Test를 실행하고 해당 Stage에만 `-AllowRerun`을 사용한다.
7. 전체 상위 Lifecycle은 관련 수정이 모두 끝난 뒤 한 번만 실행한다.

## 원장 무효화 조건

다음 중 하나면 해당 Stage 이후 결과를 현재 성공으로 승계하지 않는다.

- `HEAD` 변경
- Stage 명령 변경
- Docker Image·Profile·Vendor 변경
- 관련 Source·SQL·Config 변경
- Evidence Log 또는 Artifact Hash 불일치

무관한 문서 변경만 있는 경우 Source·Runtime Stage를 자동 무효화하지 않는다.
영향 범위는 실제 변경 경로로 판단한다.

## 금지

- 크레딧이 남았다는 이유로 PASS Stage 재실행
- 실패 원인을 고치지 않고 같은 명령 반복
- 여러 Vendor를 동시에 실행
- 모든 Browser를 매 수정마다 반복
- Source 안정화 전 Supply-chain 반복
