# CPF 20260729_04 Codex 최종 검수 전용 요청서

## 역할 고정

Codex는 검수자다. 신규 기능, Source, SQL, Frontend, Architecture를 구현하지 않는다.

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay 개발 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227`
- 적용 후 반드시 새로운 exact HEAD SHA를 기록한다.
- 최종 목표: 162개
- QA Requirement 개발 Closure: 816개
- 실행 Scenario: 387개

## 실행 순서

1. Clean Worktree와 exact SHA 확인
2. Root Hygiene와 Local Runtime 물리 이관 확인 (`cpf-tools/runtime/*`, Root 중복 0)
3. Java 25·Gradle 9.1 전체 Build/Test/Assemble
4. ADM/BZA Frontend Test·Build·Browser E2E
5. 3개 DB Lifecycle
6. Redis·File Job·Runtime Control·Notification·Batch·Gateway·External Fault
7. Generator 임의 Domain Lifecycle 및 멱등 원장 재호출/Hash 충돌/3 Vendor Rollback
8. 387개 Scenario Evidence 연결

## 필수 명령 시작점

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-20260729-final-overlay.ps1 -ProjectRoot . -ExpectedBaseSha b8941577b99535ff3e64a4fad99b74bafa544227
python .\cpf-tools\verification\20260729_04\check_final_source_closure.py .
python .\cpf-tools\verification\20260729_04\check_generator_idempotency_templates.py .
python .\cpf-tools\verification\20260729_04\check_generator_java_template_compile.py .
python .\cpf-tools\verification\20260729_04\check_java_syntax.py .
```

```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon --no-build-cache
```

## 결함 보고 형식

- Scenario/Requirement ID
- exact SHA
- 명령과 환경
- Expected
- Actual
- 최초 실패 위치
- 회귀 범위
- Sanitized Evidence 경로

Codex는 결함을 직접 고치지 않고 ChatGPT 개발 세션에 반환한다.
