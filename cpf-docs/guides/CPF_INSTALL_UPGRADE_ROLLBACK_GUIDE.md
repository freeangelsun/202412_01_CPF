# CPF 설치·업그레이드·되돌리기 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 설치 담당자, 배포 담당자, 변경 승인자, 복구 담당자
> **목적**: 신규 설치와 단계적 업그레이드, 되돌리기와 재해복구를 검증 가능한 절차로 수행한다.
> **관련 문서**: [산출물 공급과 CI/CD](CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md) · [데이터베이스 도구](CPF_DATABASE_TOOL_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | Release·Runtime·DB Owner 공동 |
| 이 문서로 완료하는 일 | 신규 설치, Rolling·Canary·Blue-Green Upgrade, DB Expand/Contract와 Rollback/Forward Fix를 Manifest와 Evidence로 재현한다. |
| 적용 범위 | Artifact, Config, Secret, DB, Registry, Application, Gateway, Batch, Frontend |
| 주요 독자 | 설치 담당자, Release Manager, DBA, 운영 승인자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

이 문서는 CPF를 신규 환경에 설치하고, 기존 환경을 안전하게 업그레이드하며, 장애 시 이전 상태로 되돌리는 전체 절차를 정의한다.

## 2. 설치 대상

- CPF 라이브러리 산출물
- 플랫폼 애플리케이션
- 게이트웨이
- 배치 실행 환경
- ADM/BZA 프런트엔드
- 데이터베이스
- 설정과 비밀값 참조
- 등록부
- 모니터링
- 인증서
- 생성 업무영역

## 3. 사전 준비

- 지원 Java/Gradle/Node
- 운영체제와 파일 시스템
- DB 공급자/버전
- Network
- DNS
- TLS
- 산출물 저장소
- 비밀값 공급자
- 서비스 계정
- Port
- 백업 저장소
- 운영 승인

## 4. 설치 명세서

명세서:

- releaseId
- productVersion
- sourceCommit
- artifact 해시
- module list
- DB version
- config version
- environment
- owner
- createdAt
- signature
- SBOM 참조

## 5. 산출물 검증

```text
Download
→ Manifest
→ SHA-256
→ Signature
→ SBOM/License
→ Version
→ Compatibility
→ Install
```

## 6. 신규 설치 순서

1. 파일 시스템과 계정
2. 비밀값 공급자
3. 데이터베이스 Provision
4. 데이터베이스 Install/Seed/Verify
5. 산출물 배치
6. 설정
7. 등록부
8. 애플리케이션 시작
9. 준비 상태
10. 게이트웨이 바인딩
11. 배치 실행 환경
12. 프런트엔드
13. 기본 동작
14. 검증 증적

## 7. 데이터베이스 설치

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

생성 업무영역도 설치한다.

## 8. 실행 환경 시작

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
```

운영 환경에서는 배포 명세서와 프로세스 Manager를 사용한다.

## 9. 설치 검증

- 프로세스
- 생존 상태
- 준비 상태
- 등록부
- DB
- Login
- 대표 API
- 게이트웨이
- 배치 기본 동작
- 로그/추적
- 감사
- 프런트엔드 경로
- 비밀값 노출
- 버전

## 10. 업그레이드 계획

변경 분류:

- API
- DB
- 설정
- Message
- 파일
- 산출물
- 프런트엔드
- 실행 정책
- 인증서
- 작업 정의

호환성 Matrix를 만든다.

## 11. 업그레이드 전략

- 순차 교체
- 소규모 선행 배포
- 이중 환경 전환
- 전체 중지
- DB 확장 후 축소

DB 파괴 변경은 애플리케이션 호환 기간을 둔다.

## 12. 확장 후 축소

```text
Expand
→ 새 Column/Table 추가
→ 구/신 Version 동시 지원
→ 데이터 이관
→ 신 Version 전환
→ 검증
→ Contract
```

한 번에 Rename/Drop하지 않는다.

## 13. 사전 검사

- Clean 소스
- 산출물 서명
- 백업
- DB 정본 불일치
- Disk
- Capacity
- 인증서
- 비밀값
- 현재 사고
- Maintenance Window
- 되돌리기 산출물

## 14. 업그레이드 실행

1. 변경 승인
2. 트래픽 배수
3. 백업
4. DB Upgrade
5. 소규모 선행 배포 인스턴스
6. 상태 점검/기본 동작
7. 단계적 확대
8. 게이트웨이/정책 적용
9. 배치 Resume
10. 최종 검증
11. 감사/검증 증적

## 15. 순차 교체

- maxUnavailable
- minHealthy
- 준비 상태 Gate
- In-flight Drain
- 버전 혼재 호환
- 실패 시 중단
- 자동 되돌리기

## 16. 소규모 선행 배포

- 대상 비율
- 사용자/채널
- 관측 지표
- 오류 허용량
- 최소 관찰 시간
- 확대 조건
- 중단 조건

## 17. 이중 환경 전환

- DB 호환
- 세션
- 큐
- 파일
- DNS/LB
- Warm-up
- Cutover
- Backout

## 18. 되돌리기 판단

- 오류율
- Latency
- 준비 상태
- 데이터 정합성
- 이관 실패
- 게이트웨이 정본 불일치
- 배치 실패
- 보안
- 운영 승인

## 19. 애플리케이션 되돌리기

- 이전 산출물
- 설정 버전
- 등록부
- 게이트웨이 바인딩
- 실행 정책
- 상태 점검
- 캐시
- 세션

## 20. DB 되돌리기

DB 되돌리기는 데이터 손실 가능성을 검사한다.

- 신규 데이터
- 신규 Column 사용
- Identity/Sequence
- 외래 키
- Archive
- Message 스키마
- 애플리케이션 버전

되돌리기 불가 시 전진 수정 또는 연결 이관을 사용한다.

## 21. 설정 되돌리기

버전이 부여된 정책을 과거 버전으로 게시한다. 인스턴스 ACK와 정본 불일치를 확인한다.

## 22. 게이트웨이 되돌리기

검증된 바인딩 버전으로 되돌리고 인스턴스별 적용 상태를 확인한다.

## 23. 배치 되돌리기

- Definition 버전
- 일정
- 실행 중 실행
- 에이전트 산출물
- 체크포인트
- Restart 호환
- 결과 불명

실행 중인 작업의 의미를 임의로 변경하지 않는다.

## 24. 프런트엔드 되돌리기

백엔드 API 호환성을 확인하고 정적 산출물을 교체한다. 브라우저 캐시와 서비스 작업자 정책을 처리한다.

## 25. 실패 복구

설치/업그레이드 중 실패하면:

1. 단계 확인
2. 변경 중단
3. Traffic 차단
4. DB 상태
5. 산출물 버전
6. 설정
7. 결과 불명 거래
8. 되돌리기 또는 전진 수정
9. Verify
10. 사고

## 26. 재해복구

- 백업 복구
- 산출물 복구
- 설정/비밀값
- 등록부
- DNS/LB
- Message Offset
- 파일
- 배치 체크포인트
- 거래 대사
- RPO/RTO

## 27. 검증 증적

- Change ID
- 승인
- 릴리스 명세서
- 소스 Commit
- 산출물 해시
- DB Plan
- 백업 명세서
- 명령
- 시각
- 인스턴스별 결과
- 기본 동작
- 되돌리기 여부
- 사고
- Sanitizing

## 28. 체크리스트

- [ ] 릴리스 명세서가 있다.
- [ ] 산출물 해시와 서명을 검증했다.
- [ ] DB 백업과 되돌리기 계획이 있다.
- [ ] 버전 혼재 호환성을 확인했다.
- [ ] Drain과 상태 점검 Gate가 있다.
- [ ] 결과 불명 거래를 대사한다.
- [ ] 게이트웨이/배치/설정 버전을 함께 관리한다.
- [ ] 설치·업그레이드·되돌리기 검증 증적이 있다.

## 부록 A. 설치 디렉터리 예

```text
/opt/cpf/
├─ releases/<releaseId>/
├─ current -> releases/<releaseId>
├─ config/
├─ logs/
├─ work/
├─ certificates/
└─ manifests/
```

실행 계정은 산출물과 설정을 읽고 작업·로그 디렉터리에만 쓸 수 있도록 최소 권한을 부여한다.

## 부록 B. 사전 점검 명세

- 운영체제·JDK·파일 시스템·시간 동기화
- CPU·메모리·디스크·파일 설명자
- DNS·방화벽·프록시·부하분산기
- TLS 체인·호스트명·만료
- DB 버전·문자집합·시간대·권한·공간
- 저장소 접근·산출물 해시·서명·자재 명세서
- 비밀값 공급자와 서비스 계정
- 현재 사고·변경 동결·점검창
- 백업 복구 지점과 되돌리기 산출물

## 부록 C. 되돌리기 결정

| 상황 | 우선 선택 |
|---|---|
| 애플리케이션만 실패, DB 호환 | 이전 산출물·설정으로 되돌리기 |
| 새 DB 구조를 구 버전도 읽을 수 있음 | 애플리케이션 되돌리기 후 원인 수정 |
| 새 자료가 구 구조에 맞지 않음 | 전진 수정 또는 연결 이관 |
| 일부 인스턴스만 실패 | 실패 인스턴스 배수·복구, 확대 중단 |
| 보안 위험 | 즉시 경로 차단·비밀값 폐기·안전 버전 복귀 |
| 결과 불명 거래 존재 | 거래 대사 뒤 재처리·보상 |

## 부록 D. 설치 완료 증적

릴리스 명세서, 산출물 해시·서명, 설정 버전, DB 버전, 인스턴스 목록, 준비 상태, 대표 API, 로그인, 게이트웨이 연결시험, 배치 시험, 로그·추적·감사와 민감정보 점검 결과를 기준 Commit과 함께 보존한다.

## 29. 설치 실행표

| 단계 | 실행 또는 확인 | 성공 기준 | 중단 기준 |
|---|---|---|---|
| Source | `git rev-parse HEAD`, Release Manifest | 승인된 Source Commit 일치 | Commit·Manifest 불일치 |
| Artifact | SHA-256·서명·SBOM 확인 | 모든 산출물 Hash 일치 | 누락·변조·서명 실패 |
| DB 사전 검사 | 정본·현재 Schema·Migration Plan 비교 | Drift가 계획에 포함됨 | 설명되지 않은 Drift |
| DB 설치 | `initialize-cpf-database.ps1 -All -RequireRun` | 공급자별 설치·Seed·Verify 성공 | 일부 DB 실패·검증 실패 |
| 실행 환경 | Process 시작 후 Liveness·Readiness | 모든 필수 Instance 준비 | 준비 상태 미달·Secret 오류 |
| Registry | Service·Endpoint·Instance 등록 확인 | 기대 Version과 상태 일치 | 중복·잘못된 Owner·환경 불일치 |
| Gateway·Batch | Binding Apply와 Projection Sync | 인스턴스별 ACK·실행 가능 | 일부 적용·Stale Version |
| Smoke | 로그인·대표 API·거래·Batch | 거래 ID와 최종 결과 확인 | 결과 불명·감사 누락 |
| Evidence | 명령·환경·시각·결과 저장 | Source Commit과 연결 | 원문 Secret·미기록 결과 |

## 30. 단계 배포 절차

1. 변경 목록을 API, DB, 설정, 메시지, 파일, Batch Definition과 Frontend로 나눈다.
2. 구·신 Version 혼재 기간의 호환 Matrix를 작성한다.
3. Rollback Artifact, Config Version, DB Backup과 Forward Fix 절차를 준비한다.
4. 대상 Instance를 배수하고 실행 중 거래·Batch를 확인한다.
5. DB는 확장 변경을 먼저 적용하고 파괴 변경은 호환 기간 뒤로 미룬다.
6. Canary Instance를 시작하고 Readiness, 대표 API, 로그·추적·감사를 확인한다.
7. 오류율·지연·결과 불명·데이터 정합성·자원 사용량을 최소 관찰 시간 동안 측정한다.
8. 기준을 충족하면 단계적으로 확대하고, 위반하면 즉시 확대를 중단한다.
9. Gateway Binding·설정 정책·Batch Projection의 기대·적용 Version을 확인한다.
10. 최종 Smoke와 대사를 수행하고 변경 승인·실행 결과를 감사한다.

## 31. 되돌리기와 Forward Fix 판단표

| 상황 | Application 되돌리기 | DB 되돌리기 | 권장 판단 |
|---|---|---|---|
| 코드 오류, DB 호환 유지 | 가능 | 불필요 | 이전 Artifact·Config로 되돌리기 |
| 확장 Column 추가만 수행 | 가능 | 보통 불필요 | 구 Version 호환 확인 후 Application 되돌리기 |
| 신 Version이 새 Column에 데이터 기록 | 조건부 | 데이터 손실 위험 | Bridge 또는 Forward Fix 우선 |
| 파괴 DDL 수행 | 매우 위험 | 복구 가능성 검토 | Backup Restore 또는 Forward Fix 계획 사용 |
| 메시지 Schema 변경 | Consumer 호환성 필요 | 해당 없음 | 구·신 Consumer Matrix로 판단 |
| 일부 Gateway/Config 적용 | Version별 가능 | 해당 없음 | 확대 중단, 인스턴스별 Reconcile |
| Batch 실행 중 Definition 변경 | 직접 변경 금지 | 업무 데이터 영향 | 현재 실행 의미 보존 후 새 Version 배포 |

되돌리기 명령을 실행할 수 있다는 사실만으로 Rollback 가능으로 판정하지 않는다. 변경 이후 생성된 데이터, 메시지, 파일, 외부 처리와 실행 중 거래를 함께 확인한다.

## 32. 운영 Evidence 예시

```text
changeId: CHG-20260730-0184
sourceCommit: b7c6146e952c10b885952fa2bc6b6786f4611d86
releaseId: CPF-2026.07.30.09
environment: PROD-A
startedAt: 2026-07-30T23:00:00+09:00
finishedAt: 2026-07-30T23:42:18+09:00
strategy: CANARY_THEN_ROLLING
dbPlanHash: <검증된 계획 Hash>
artifactManifestHash: <검증된 Manifest Hash>
result: SUCCESS | PARTIAL | FAILED | RECONFIRM_REQUIRED
rollbackUsed: false
sanitized: true
```

각 명령의 원본 출력과 Instance별 결과를 별도 파일로 보관하고, 현재 Commit에서 다시 유효한지 확인한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| DB Install | `cpf-tools/scripts/initialize-cpf-database.ps1` | 공식 DB 설치·검증 |
| Local Runtime | `start-cpf-local.ps1`, `status-cpf-local.ps1`, `stop-cpf-local.ps1` | 통합 실행 환경 |
| Batch Runtime | `start-bat-local-distributed.ps1`, `stop-bat-local-distributed.ps1` | 분산 Batch 실행 |
| Full Verify | `verify-cpf-final-completion.ps1`와 QA 검증 Script | 통합 검증 Entry |
| Release Source | Artifact Manifest·DB Pack·Config Version | Upgrade/Rollback 입력 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
