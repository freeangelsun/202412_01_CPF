# CPF 설치·업그레이드·되돌리기 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 설치 담당자, 배포 담당자, 변경 승인자, 복구 담당자
> **목적**: 신규 설치와 단계적 업그레이드, 되돌리기와 재해복구를 검증 가능한 절차로 수행한다.
> **관련 문서**: [산출물 공급과 CI/CD](CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md) · [데이터베이스 도구](CPF_DATABASE_TOOL_GUIDE.md)

---

## 1. 목적

이 문서는 CPF를 신규 환경에 설치하고, 기존 환경을 안전하게 업그레이드하며, 장애 시 이전 상태로 되돌리는 전체 절차를 정의한다.

## 2. 설치 대상

- CPF 라이브러리 산출물
- Platform 애플리케이션
- 게이트웨이
- 배치 실행 환경
- ADM/BZA 프런트엔드
- 데이터베이스
- 설정과 비밀값 참조
- 등록부
- Monitoring
- 인증서
- 생성 업무영역

## 3. 사전 준비

- 지원 Java/Gradle/Node
- OS와 Filesystem
- DB 공급자/버전
- Network
- DNS
- TLS
- 산출물 저장소
- 비밀값 공급자
- 서비스 Account
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

1. Filesystem과 계정
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
- 작업 Definition

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
- Current 사고
- Maintenance Window
- 되돌리기 산출물

## 14. 업그레이드 실행

1. Change 승인
2. Traffic Drain
3. 백업
4. DB Upgrade
5. 소규모 선행 배포 인스턴스
6. 상태 점검/기본 동작
7. 단계적 확대
8. 게이트웨이/정책 적용
9. 배치 Resume
10. 최종 Verify
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
- Unknown

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
