# CPF 20260801_02 Windows 검증 보정 완료 보고

- 기준 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 범위: Source 1개, Python Gate/Test 20개, Evidence·Manifest
- README·README 연결 Manual·Guide: 수정하지 않음

## 실제 결함과 조치

1. `AdmGatewayOperationsStreamController.stream`에 명시적 `admGatewayOperationsStream` Operation ID를 추가했다.
2. Windows 기본 CP949에 의존하던 Test의 `Path.read_text/write_text`를 UTF-8 명시형으로 변경했다.
3. Approved Baseline Test는 Working Tree 줄바꿈 Hash가 아니라 Commit Blob Hash를 사용하도록 변경했다.
4. 실제 Repository의 SSE Operation ID를 확인하는 회귀 Test를 추가했다.

## 검증

- Python Unit: 145/145 PASS
- C locale + `PYTHONUTF8=0`: 145/145 PASS
- `core.autocrlf=true` Baseline Test: 4/4 PASS
- Controller Permission Strict Gate: PASS
- Java Source Syntax: PASS
- 암묵적 pathlib Text I/O: 0건

## 미검증

Frontend TypeScript Syntax는 Source 실패가 아니라 `node_modules`에 TypeScript compiler가 없는 환경 차단이다. `npm ci` 성공 후 재실행해야 한다.
