# CPF 보안·재해복구·데이터 보존 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 보안 관리자, 운영자, 재해복구 담당자, 개인정보 담당자
> **목적**: 인증·권한·비밀값·마스킹·백업·복구·보존과 법적 보류를 통합 관리한다.
> **관련 문서**: [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md) · [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 공통 보안 계약은 `cpf-core`; 권한·감사 Owner는 ADM/BZA 및 각 업무영역 |
| 이 문서로 완료하는 일 | 인증·권한·Secret·Masking·승인·감사·Backup·DR·Retention·Legal Hold를 안전한 기본값과 복구 절차로 운영한다. |
| 적용 범위 | Online/Batch/Gateway/File/Log/DB/Artifact의 보안·보존·복구 |
| 주요 독자 | 보안 관리자, Architect, 운영자, DBA, 감사 담당자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

이 문서는 인증, 권한, 비밀값, 인증서, 민감정보, 감사, 백업, 복원, 재해복구와 보존의 공통 제품 기준을 정의한다.

## 2. 보안 기본값

- 인증되지 않은 요청 거부
- 최소 권한
- 외부 공개 기본 거부
- 비밀값 참조
- 민감정보 마스킹
- 입력/출력 검증
- 위험 조치 승인
- 감사
- 안전 차단

## 3. 인증

### 제품 간 운영 제어 요청

ADM이 게이트웨이·배치 등 소유 제품의 내부 제어 API를 호출할 때 일반 사용자 세션만으로 신뢰하지 않는다. Method, 정확한 Request Target, 정규화 Content-Type, Body SHA-256, Caller, 검증된 Operator, 요청 시각, Nonce, Audience와 Key ID를 Canonical 문자열로 서명한다. 수신 측은 시간 오차·허용 Caller·Audience·Key·서명을 검증하고 다중 인스턴스 공용 저장소에서 Nonce를 단 한 번 Claim한다. Key는 비밀값 공급자에서 가져오며 오류·로그·검증 증적에 노출하지 않는다.


지원 구성:

- 세션
- OAuth2/OIDC
- JWT
- mTLS
- API Key 참조
- 에이전트 인증서
- 서비스 계정

인증 실패와 권한 실패를 구분한다.

## 4. 세션

- Access Token 짧은 수명
- Refresh Rotation
- Reuse Detection
- Device 메타데이터
- 세션 목록과 폐기
- Logout
- Password 변경 후 폐기
- 권한 변경 재평가

## 5. 권한

평가 축:

- User
- 역할
- 권한
- Action
- Resource
- Environment
- 업무영역
- 조직
- Tenant
- Data 범위
- Time

Deny 우선순위를 명시한다.

## 6. 비밀값

계약:

```text
Secret Reference
→ Provider
→ 짧은 Scope Value
→ 사용
→ 메모리 정리
```

공급자:

- ENV
- Vault
- KMS
- HSM
- Cloud 비밀값 Manager

## 7. 비밀값 교체

1. 새 버전 생성
2. 대상 호환
3. 배포
4. 인스턴스 ACK
5. 연결시험
6. 구 버전 폐기
7. 감사

## 8. 인증서

관리 정보:

- Subject
- Issuer
- Serial
- Fingerprint
- Validity
- Key Usage
- SAN
- Algorithm
- Chain
- Revocation

개인 키는 API/검증 증적에 노출하지 않는다.

## 9. 애플리케이션 보안

- SQL Injection
- XSS
- CSRF
- SSRF
- Path Traversal
- 업로드
- Deserialization
- Open Redirect
- 헤더 Injection
- 명령 Injection
- 의존 대상/CVE

입력 검증과 Output Encoding을 적용한다.

## 10. 파일 보안

- 크기
- 확장자
- MIME
- Magic Number
- 경로
- 악성 검사
- Quarantine
- 내려받기 권한
- 체크섬
- 보존

Scanner 미구성 또는 장애 시 안전 차단한다.

## 11. 민감정보

분류:

- PUBLIC
- INTERNAL
- CONFIDENTIAL
- RESTRICTED

분류에 따라 저장, 로그, 내려받기, 마스킹, 보존과 암호화를 적용한다.

## 12. 마스킹

- API
- 화면
- 로그
- 감사
- 추적
- 검증 증적
- 반출
- Exception

원문 보기는 별도 권한·사유·감사와 제한 시간을 요구한다.

## 13. 감사

감사 필드:

- actor
- role
- target
- action
- reason
- before/after
- operationId
- approval
- result
- transactionId
- timestamp

감사 자체 실패 정책을 위험도에 따라 결정한다.

## 14. 해시 연결

- canonical payload
- previousHash
- recordHash
- chain head
- row lock
- tamper verify
- tail deletion detection

해시 Chain은 기밀성을 대신하지 않는다.

## 15. 데이터 보존

보존 정책:

- target
- duration
- action
- cutoff
- legal hold
- archive
- purge
- dry run
- kill switch
- approval
- audit

## 16. 법적 보류

Legal Hold는 Purge보다 우선한다.

- Case ID
- 대상
- 기간
- 사유
- 승인
- 해제
- 감사

## 17. 보관

```text
대상 Preview
→ Archive 저장
→ Checksum/Count
→ 원본 삭제
→ 결과
```

중간 실패 시 정합성을 보장한다.

## 18. 폐기

- 기본 비활성
- cutoff 필수
- 최대 건수
- Chunk
- 잠금/부하
- Kill Switch
- 중단/재개
- 검증 증적

## 19. 백업

- 암호화
- 접근 통제
- SHA-256
- 명세서
- Offsite
- Immutable
- 보존
- 복구 테스트

Password를 명령 Line에 넣지 않는다.

## 20. 복원

1. 격리 대상
2. 명세서
3. 체크섬
4. 공급자/DB
5. 복구
6. 스키마
7. 데이터
8. 애플리케이션 기본 동작
9. 거래 대사
10. 검증 증적

## 21. 재해복구

DR 범위:

- DB
- 산출물
- 설정
- 비밀값
- 인증서
- Message Offset
- 파일
- 등록부
- 게이트웨이
- 배치 체크포인트
- DNS/LB

## 22. RPO/RTO

고객 운영 정책으로 값을 정한다.

- RPO
- RTO
- 백업 주기
- 복구 순서
- 우선 서비스
- 데이터 대사
- 업무 재처리
- 훈련 주기

## 23. 재해복구 훈련

- 시나리오
- 격리 환경
- 실제 백업
- 복구
- 측정
- Failover
- 거래 확인
- 원복
- 개선

## 24. 비상 권한

- 재인증
- 제한 범위
- 자동 만료
- 경보
- 사후 승인
- 모든 활동 감사

## 25. 내려받기

로그·감사·설정 자료의 반출은 조회 화면의 Raw 페이로드를 브라우저에서 직접 저장하지 않는다. 서버가 권한과 사유를 확인하고 마스킹·크기 제한을 다시 적용한 별도 산출물을 생성한다. 산출물은 식별자와 만료 시각을 가지며, 클립보드와 내려받기 각각의 결과를 감사한다.


- 별도 권한
- 사유
- Masked 기본값
- 최대 크기
- 워터마크
- 체크섬
- 만료
- 감사

## 26. 보안 Gate

- 비밀값 형식
- 취약 URL
- TLS
- 의존 대상/CVE
- License
- External CDN
- 개인 키
- 권한 Seed
- 업로드
- Raw 로그

## 27. 테스트

- 인증
- 권한
- Deny
- 세션 Reuse
- 비밀값 마스킹
- Rotation
- 인증서 만료
- Scanner Down
- 감사 Tamper
- Legal Hold
- Purge Kill Switch
- 백업/복원
- DR

## 28. 체크리스트

- [ ] 비밀값 원문이 노출되지 않는다.
- [ ] 권한은 서버가 평가한다.
- [ ] 위험 조치에 승인과 감사가 있다.
- [ ] Scanner/Verifier 장애 시 안전 차단한다.
- [ ] 보존에 Legal Hold와 Kill Switch가 있다.
- [ ] 백업을 실제 복원 검증한다.
- [ ] DR 훈련으로 RPO/RTO를 측정한다.

## 부록 A. 위협 모델 점검

- 외부·내부 호출의 신뢰 경계와 인증 주체
- 권한 상승, 수평 권한 우회와 대리자 오용
- 헤더 위조, 요청 재생과 멱등 키 충돌
- 비밀값·토큰·인증서 유출
- 로그·오류·내보내기·검증 증적의 민감정보 노출
- 파일 경로 이동, 악성 파일과 저장형 스크립트
- 공급망 산출물 변조와 의존성 취약점
- 백업·복구 자료와 법적 보류 자료의 무단 접근
- 운영 명령 남용과 작성자·승인자 공모

## 부록 B. 비밀값 생명주기

`요청 → 승인 → 생성·등록 → 참조 배포 → 사용 → 교체 → 폐기 → 접근 이력 검토`

원문은 필요한 순간에만 해석하고 메모리·로그·오류·명령행에 오래 남기지 않는다.

## 부록 C. 복구 훈련

- 주기와 담당자
- 목표 복구 시점·시간
- 백업 선택과 무결성 확인
- 별도 환경 복구
- DB·메시지·파일·설정·비밀값·인증서·배치 체크포인트 정합성
- 대표 업무 읽기·쓰기와 결과 불명 대사
- 전환·복귀 절차와 승인
- 실제 소요시간, 실패 단계와 개선 과제

## 부록 D. 보존과 법적 보류

보존 정책은 자료 종류, 기준 시각, 기간, 삭제 방식, 보류 우선순위와 감사 이력을 정의한다. 법적 보류 대상은 일반 삭제 작업에서 제외하고, 해제 승인 뒤 다음 보존 주기에 처리한다.

## 33. 위협과 통제 연결표

| 위협 | 예방 | 탐지 | 복구 |
|---|---|---|---|
| 권한 우회 | 서버 권한·Principal 검증 | 401·403·감사 이상 | Session·Token 폐기, 영향 조회 |
| 비밀값 노출 | Secret Reference, 원문 직렬화 금지 | Secret Scanner·마스킹 Gate | 회전·폐기·재배포 |
| 로그 과다 수집 | 영역별 Capture Mode·크기 상한 | 정책 Drift·반출 감사 | Override 중지·Artifact 폐기 |
| 요청 재생 | Canonical HMAC·Audience·분산 Nonce Claim | Nonce 중복·시각·서명·감사 실패 | 키 회전·제어 채널 차단 |
| 공급망 변조 | Hash·서명·SBOM | Manifest 불일치 | 승격 중단·검증 Artifact 복구 |
| 위험 조치 오남용 | 사유·승인·작성자 분리 | 감사·비정상 빈도 | 권한 회수·상태 되돌리기 |
| 백업 유출 | 암호화·별도 계정·접근 제한 | Download·Restore 감사 | 키 회전·영향 분석 |

## 34. Secret 사용 절차

1. 설정에는 `vault://...` 같은 Reference만 저장한다.
2. 실행 직전에 권한 있는 Provider가 짧은 수명으로 원문을 해석한다.
3. 원문은 DTO, `toString()`, Exception, Command Line, 환경 덤프와 Evidence에 넣지 않는다.
4. 사용 범위를 `try-with-resources` 또는 동등한 수명 관리로 제한한다.
5. Provider 장애 시 평문 대체값으로 진행하지 않고 위험도에 따라 안전 차단한다.
6. 회전 시 구·신 Version의 호환 기간과 Instance별 적용 상태를 확인한다.
7. 노출 의심 시 Reference만 바꾸지 말고 원 Secret 폐기·감사·영향 거래 분석을 수행한다.

## 35. 재해복구 훈련 Runbook

1. RPO·RTO와 복구 대상 시스템·DB·메시지·파일·Batch Checkpoint를 확정한다.
2. Backup Manifest, Artifact·Config Version과 Secret Reference를 검증한다.
3. 격리된 복구 환경에 DB를 복원하고 정본 Schema·Checksum을 확인한다.
4. Application을 시작하되 외부 발신·Scheduler·Consumer를 안전 차단한다.
5. Registry·Gateway·Batch Projection을 복구 Version과 대조한다.
6. 거래·메시지 Offset·파일·결과 불명·Batch 실행을 대사한다.
7. 제한된 Smoke 뒤 외부 연계와 Traffic을 단계적으로 연다.
8. 실제 복구 시간, 데이터 손실 범위, 수동 조치와 미충족 RPO·RTO를 기록한다.
9. 훈련용 Secret과 민감정보를 폐기하고 Evidence를 정리한다.

## 36. 보존 종료와 삭제 안전성

- 법적 보류, 사고 조사, 대사 미완료와 결과 불명 상태를 먼저 확인한다.
- 원본·복제본·검색 Index·Cache·백업·내보내기 Artifact를 같은 보존 정책으로 추적한다.
- 삭제 대상 수, 기간, Owner와 Approval을 Dry Run으로 확인한다.
- 다중 인스턴스 삭제 Worker는 Claim·Lease·Fencing으로 중복·늦은 반영을 차단한다.
- 삭제 결과와 실패 항목을 분리하고 재처리 가능 상태로 남긴다.
- 삭제 Evidence에는 민감 원문을 넣지 않고 ID·범위·Hash·건수·결과만 기록한다.

## 37. 운영 제어 서명 검증 Runbook

1. `X-CPF-Gateway-Control-*` Header가 모두 있는지 확인한다.
2. Body를 수신한 그대로 SHA-256 계산해 Header와 상수 시간 비교한다.
3. Method·Target·Content-Type·Caller·Operator·Timestamp·Nonce·Audience·Key ID를 정본 순서로 조합한다.
4. Audience와 Key ID에 대응하는 비밀값으로 HMAC-SHA256을 검증한다.
5. 허용 시간 오차를 넘긴 요청을 거부한다.
6. `(audience, keyId, callerId, nonce)`를 공용 저장소에서 Claim한다.
7. Claim 중복·저장소 장애·보안 감사 저장 실패를 성공으로 처리하지 않는다.
8. 거부 단계와 정제된 원인을 보안 감사에 남긴다.

서명에 URI Path만 넣고 Query·Target 정규화 규칙이 Caller와 수신 측에서 다르면 정상 요청도 실패하거나 우회 여지가 생긴다. 정본 함수와 Header 상수는 `cpf-core` 공개 계약을 함께 사용한다.

## 38. 실행 Artifact 서명 정책

Batch 승인 Shell과 원격 실행 Artifact는 다음을 모두 통과해야 한다.

- Catalog에 고정된 Artifact ID·Version·SHA-256
- 허용 Algorithm의 Detached Signature
- Trust Store에 등록된 공개키 또는 검증된 X.509 Chain
- 인증서 유효기간과 약한 Signature Algorithm 차단
- 실행 계정·Interpreter·고정 인자·허용 매개변수·출력 상한

서명 검증 기능이 미구성되거나 Key가 없으면 실행을 중단한다. 개발 편의를 위한 Hash-only 모드는 운영 Profile의 자동 대체값으로 사용하지 않는다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Secret Contract | `git ls-files cpf-core | Select-String "SecretReference|SecretValue|Mask"` | Secret 원문 분리·마스킹 |
| Auth/Audit | `cpf-admin`, `cpf-biz-admin`의 Security·Audit Package | 권한·승인·감사 |
| Log Protection | `CpfLogCaptureGuard.java`, `CpfPayloadProtectionPort.java` | 수집 단계 보호 |
| Control Security | `CpfGatewayControlSigner.java`, `CpfGatewayControlHeaders.java`, `CpfGatewayControlNoncePort.java` | Canonical HMAC·Audience·Replay 차단 |
| Artifact Security | `JcaScriptArtifactVerifier.java`, `cpf-tools`의 `signature`, `hash`, `sbom` | 실행·공급망 무결성 |
| DR/Retention | DB Backup/Restore Script와 Retention SQL 검색 | 복구·보존 구현 확인 |

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
