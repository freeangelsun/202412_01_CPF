# Test and Evidence

## 실제 실행 환경

- Java: OpenJDK 21.0.10
- Node: 22.16.0
- Python: 3.13.5
- PowerShell: 현재 컨테이너 미설치
- GitHub Connector: exact SHA Source 조회 가능
- fresh clone/archive: DNS 해석 실패

## 실환경 시도

- `git clone --no-checkout ...` → Exit 128, `Could not resolve host: github.com`
- exact SHA archive `curl` → Exit 6, `Could not resolve host: github.com`

환경 부재로 검수를 생략하지 않고 exact-SHA Source 조회, Java21 실제 제품 Source Harness, Python Semantic Gate, SQL lifecycle 의미 검증, Frontend/Controller/Owner 호출 경로 검수로 대체했다.

## 대체검증 결과

- 전체 QA 보완 통합 회귀: **52 passed**
- Python Compileall: PASS
- V100 Lifecycle 수정 전: FAIL / 수정 후: PASS
- BAT post-side-effect UNKNOWN 수정 전: FAIL / 수정 후 Java21 Harness PASS
- R4 source-head 검증 수정 전: FAIL / 수정 후 PASS
- Owner Boundary 수정 전 false-negative 재현 / 수정 후 PASS
- Spring Request Mapping Gate: 단위 8/8 PASS, 최신 Controller 조합 중복 7건 재현
- Full QA Completion Gate: 6/6 PASS
- Full QA Ledger Builder: 6/6 PASS
- V100/R100 Lifecycle Gate: 6/6 PASS
- Exact full validation runner contract: PASS

## 검증 수준

- 실제 제품 Java Source 사용: BAT Risk Coordinator Harness에서 사용
- Mock/Stub: 외부 Framework 의존 최소 stub 사용, 핵심 Coordinator Source는 실제 제품 Source
- 실제 Spring Context: 미실행. 대신 중복 Mapping을 Source semantic Gate로 재현
- 실제 Vendor DB: 미실행. Parser/Semantic/Lifecycle 순서와 Rollback fail-closed 검증 수행
- 실제 Browser: 미실행. Frontend API→Controller→Owner 신뢰 경로 Source 검수 수행
- 실제 Multi-instance: 기존 Script 계약 검수 및 exact runner에 필수 단계로 고정

## 전체 완료 차단

`run-cpf-full-qa-validation.ps1`은 다음 모든 단계를 통과해야 마지막 71,321행 `product-pass`를 실행한다.

- exact master/HEAD/origin/clean tree
- Java25 Root clean/test/assemble
- Architecture/Security/Approval/Mapping/DB/Generator Gate
- ADM/BZA npm ci/unit/build/browser E2E
- Oracle/PostgreSQL/MariaDB Fresh/Upgrade/RollbackReapply
- Spring 다중 인스턴스 Process Kill/Restart Audit
- Requirement 30,558 + Scenario 40,763 개별 Current 원장 exact-SHA Evidence

현재 제품은 확인된 P0 결함 때문에 위 Gate에서 실패하는 것이 정상이다.
