# CPF 20260729_04 최종 Root Overlay 적용·검증 Guide

## 적용 전 기준

- Overlay 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227`
- 적용 대상: 사용자의 최신 `master` Working Tree
- Overlay는 CPF Project Root 상대경로를 보존한다.
- 사용자 기존 변경은 임의 삭제·Restore하지 않는다.

## 1. ZIP 적용

ZIP 내부 파일을 `C:\dev\projects\jck\202412_01_CPF`에 덮어쓴다. 별도 상위 Wrapper Directory가 없어야 한다.

## 2. Root 정본 이관과 즉시 검증

다음 한 줄은 Root 중복 Gradle/BOM과 Root Local Runtime Module을 각각 정본 위치로 이관하고 Source·Frontend·Topology Gate를 실행한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-20260729-final-overlay.ps1 -ProjectRoot . -ExpectedBaseSha b8941577b99535ff3e64a4fad99b74bafa544227
```

정본 위치는 다음과 같다.

- Gradle Plugin: `cpf-tools/build/gradle-plugin`
- Platform BOM: `cpf-tools/build/platform-bom`
- Deploy Assets: Root `deploy` 유지(제품 배포 자산 정본)
- Local Web Runtime Source: `cpf-tools/runtime/cpf-local-runtime`
- Local Batch Runtime Source: `cpf-tools/runtime/cpf-local-batch-runtime`

현재 SHA가 기준과 다르면 Script가 중단한다. 최신 master를 별도로 재대조한 경우에만 `-AllowDifferentBaseSha`를 사용한다.

## 3. 개별 정적 검증

```powershell
python .\cpf-tools\verification\20260729_04\check_final_source_closure.py .
python .\cpf-tools\verification\20260729_04\check_generator_idempotency_templates.py .
python .\cpf-tools\verification\20260729_04\check_generator_java_template_compile.py .
python .\cpf-tools\verification\20260729_04\check_java_syntax.py .
node .\cpf-tools\verification\20260729_04\check_frontend_syntax.cjs .
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-20260729-final-overlay.ps1 -ExpectedSha b8941577b99535ff3e64a4fad99b74bafa544227
```

## 4. DB 정본 동기화

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\generate-migration-checksums.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-db-vendor-pack-parity.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

## 5. Java 25·Gradle 9.1 전체 Build

```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon --no-build-cache
```

최초 실패부터 수정한다. 실행하지 않은 Task를 PASS로 기록하지 않는다.

## 6. Frontend

ADM/BZA 각각 `npm ci`, Test, Production Build를 수행한다. Browser에서는 최소 다음을 확인한다.

- BZA 조직 5단계 Tree
- 메뉴 Tree Cycle 차단
- 권한 Simulation 운영형 결과
- READ 전용 Role의 위험 Button 미노출과 직접 API 403
- ADM Cache Provider/지표/Evict/Reconcile
- File Job Dry-run/Apply/Retry/Cancel/Rollback
- Notification Retry/Cancel 독립 Action
- Runtime Control 대상별 결과와 Timeline

## 7. Runtime·DB

- Oracle/PostgreSQL/MariaDB Fresh Install → Upgrade → Rollback/Forward Recovery → Reapply
- Redis Standalone/Sentinel/Cluster와 TLS/Secret/장애/복구
- Cache Invalidation offline recovery와 다중 Instance checkpoint
- File Job 다중 Worker Lease/Fencing와 Process Kill 복구
- Runtime Control 대상 0건, 일부 실패, Unknown, Retry, Rollback, Drift Reconcile
- Generator 임의 Domain 2개 생성·Build·Runtime·삭제·재생성
- 생성 Domain CREATE/UPDATE/DELETE 동일 Key 재호출과 다른 Request Hash 충돌, 멱등 원장 Rollback/재적용

## 8. Evidence

Evidence마다 exact SHA, 실행 명령, Profile/환경, 시작·종료 시각, Requirement/Scenario ID, 실제 출력, 민감정보 제거 여부를 기록한다.


## 9. Local Runtime 물리 위치

- Gradle 논리 Project: `:cpf-local-runtime`, `:cpf-local-batch-runtime`
- 물리 Source: `cpf-tools/runtime/cpf-local-runtime`, `cpf-tools/runtime/cpf-local-batch-runtime`
- Root의 동일 이름 폴더는 이관 후 남아 있으면 안 된다.
- `deploy` 아래에는 Source Module을 두지 않는다.
