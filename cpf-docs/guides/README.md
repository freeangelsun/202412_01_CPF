<div align="center">

<picture>
  <source media="(max-width: 720px)" srcset="../assets/readme/cpf-guide-map-mobile.png">
  <img src="../assets/readme/cpf-guide-map-desktop.png" alt="CPF 문서 체계" width="100%">
</picture>

# CPF 문서 홈

**설계자, 개발자, 운영자, 데이터베이스 담당자와 배포 담당자가 같은 제품 기준을 찾는 시작점**

[← 제품 소개](../../README.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md)

</div>

---

## 문서의 역할

CPF 문서는 목적에 따라 분리합니다.

| 문서 종류 | 역할 | 포함하지 않는 내용 |
|---|---|---|
| 제품 소개 | CPF의 가치, 구조, 핵심 실행 모델과 시작 경로 | 상세 옵션, 장애별 명령, 작업 진행률 |
| 구조·정책 가이드 | 소유권, 의존성, 배포 구성과 제품 계약 | 특정 세션 작업 내역 |
| 사용·운영 가이드 | 실제 작업을 처음부터 끝까지 수행하는 절차 | 검증하지 않은 완료 보고 |
| API·도구 참조 | 입력, 출력, 기본값, 오류와 예제 | 개념 설명의 불필요한 반복 |
| 교육 문서 | 실제 제품 API를 사용하는 실습과 복구 훈련 | 별도 장난감 규격 |
| 작업·검토·인수인계 | 현재 개발 요청, 검수 상태와 다음 작업 | 완성 제품의 사용 설명 |
| 검증 증적 | 기준 Commit, 실행 명령, 환경과 실제 결과 | 민감정보 원문 |

---

## 역할별 추천 순서

### 구조를 설계하는 사람

1. [구조와 배포 구성](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md)
2. [공개 API와 생성 업무영역](CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md)
3. [개발자 가이드](CPF_DEVELOPER_GUIDE.md)
4. [보안·재해복구·데이터 보존](CPF_SECURITY_DR_RETENTION_GUIDE.md)
5. [테스트와 검증 증적](CPF_TEST_AND_EVIDENCE_GUIDE.md)

### 업무 기능을 개발하는 사람

1. [개발자 가이드](CPF_DEVELOPER_GUIDE.md)
2. [기반 API](CPF_FOUNDATION_API_GUIDE.md)
3. [공개 API와 생성 업무영역](CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md)
4. [업무영역 생성기](CPF_GENERATOR_TOOL_GUIDE.md)
5. [교육·예제 범위](CPF_EDU_COVERAGE_GUIDE.md)

### 플랫폼과 업무를 운영하는 사람

1. [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md)
2. [업무 관리자](CPF_BIZ_ADMIN_GUIDE.md)
3. [화면 표준](CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md)
4. [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)
5. [보안·재해복구·데이터 보존](CPF_SECURITY_DR_RETENTION_GUIDE.md)

### 배치와 연계를 운영하는 사람

1. [비동기·메시징·보상](CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md)
2. [배치 실행 환경과 원격 에이전트](CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md)
3. [배치 스케줄러와 실행 생명주기](CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md)
4. [게이트웨이 운영](CPF_GATEWAY_OPERATIONS_GUIDE.md)
5. [상태 점검과 서비스 등록부](CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md)

### 설치·데이터베이스·배포를 담당하는 사람

1. [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)
2. [데이터베이스 도구](CPF_DATABASE_TOOL_GUIDE.md)
3. [데이터베이스 프로필과 업무영역 DB](DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md)
4. [설정과 실행 정책 배포](CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md)
5. [산출물 공급과 CI/CD](CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md)
6. [도구 운영](CPF_TOOLS_GUIDE.md)과 [도구 상세 참조](CPF_TOOL_REFERENCE.md)

---

## 전체 문서 지도

### 구조와 개발

- [CPF 구조와 배포 구성 가이드](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) — 모듈 소유권, 의존성, 동일 JVM·분리 WAS, 제어 영역과 실행 영역
- [CPF 개발자 가이드](CPF_DEVELOPER_GUIDE.md) — 기능 설계부터 구현·검증까지의 개발 표준
- [CPF 기반 API 가이드](CPF_FOUNDATION_API_GUIDE.md) — 문자열, 숫자, 날짜, 식별자, 오류, 페이징과 공통 자료구조
- [CPF 공개 API와 생성 업무영역 가이드](CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md) — 공개 경계, 로컬·원격 호출, 생성 업무영역 확장
- [CPF 업무영역 생성기 가이드](CPF_GENERATOR_TOOL_GUIDE.md) — 계획, 충돌 검사, 생성, 재실행, 업그레이드와 제거
- [CPF 교육·예제 범위 가이드](CPF_EDU_COVERAGE_GUIDE.md) — 학습 경로, 실습, 오류·복구·운영 시나리오

### 운영과 화면

- [CPF 플랫폼 운영자 가이드](CPF_ADMIN_OPERATOR_GUIDE.md) — 서비스·거래·로그·배치·설정·보안 운영
- [CPF 업무 관리자 가이드](CPF_BIZ_ADMIN_GUIDE.md) — 사용자·조직·권한·결재·알림·첨부와 감사
- [CPF ADM·BZA 화면 표준 가이드](CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md) — 탐색, 검색, 표, 양식, 접근성, 위험 조치와 화면 구조
- [CPF 관측·장애대응·복구 가이드](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md) — 로그·추적·지표·경보·사고 대응과 복구 절차
- [CPF 상태 점검과 서비스 등록부 가이드](CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md) — 생존·준비 상태, 등록·심박·만료·배수·점검 상태

### 실행과 연계

- [CPF 게이트웨이 운영 가이드](CPF_GATEWAY_OPERATIONS_GUIDE.md) — 경로·대상군·정책·연결시험·적용·대사·되돌리기
- [CPF 비동기·메시징·보상 처리 가이드](CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md) — 송신함·수신함·재시도·격리·재생·대사·보상
- [CPF 배치 실행 환경과 원격 에이전트 가이드](CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md) — 작업정의, 제어 서버, 작업자, 에이전트와 대량 실행
- [CPF 배치 스케줄러와 실행 생명주기 가이드](CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md) — 일정, 달력, 시간대, 오실행, 재시작·재실행·재처리

### 보안·설정·데이터

- [CPF 보안·재해복구·데이터 보존 가이드](CPF_SECURITY_DR_RETENTION_GUIDE.md) — 인증·권한·비밀값·인증서·마스킹·백업·보존과 법적 보류
- [CPF 설정과 실행 정책 배포 가이드](CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md) — 설정 우선순위, 버전, 승인, 적용 확인, 정본 불일치와 되돌리기
- [CPF 데이터베이스 도구 가이드](CPF_DATABASE_TOOL_GUIDE.md) — 정본 SQL, 설치, 이관, 업그레이드, 되돌리기, 백업과 복구
- [CPF 데이터베이스 프로필과 업무영역 DB 가이드](DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md) — 공급자별 연결, 계정, 논리 DB, 다중 자료원과 읽기 복제본

### 공급·도구·검증

- [CPF 설치·업그레이드·되돌리기 가이드](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md) — 신규 설치, 단계적 배포, 이중 환경 전환, 데이터베이스 호환과 복구
- [CPF 산출물 공급과 CI/CD 가이드](CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md) — 저장소, 승격, 서명, 자재 명세서, 폐쇄망과 공급망 검증
- [CPF 도구 운영 가이드](CPF_TOOLS_GUIDE.md) — 도구 분류, 공통 안전 규칙과 대표 작업 흐름
- [CPF 도구 상세 참조](CPF_TOOL_REFERENCE.md) — 명령별 매개변수, 입력·출력, 종료 코드와 복구
- [CPF 테스트와 검증 증적 가이드](CPF_TEST_AND_EVIDENCE_GUIDE.md) — 단위·계약·통합·실행·장애·브라우저·다중 인스턴스 검증
- [CPF 용어와 계약 참조](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md) — 한글 우선 용어, 상태 코드와 공통 계약

---

## 문서를 사용하는 원칙

1. 제품 소개에서 전체 지도를 파악합니다.
2. 구조 가이드에서 소유권과 배포 경계를 확인합니다.
3. 수행하려는 작업의 상세 가이드로 이동합니다.
4. 정확한 매개변수는 도구 참조, 정확한 프로그램 계약은 OpenAPI·JavaDoc에서 확인합니다.
5. 현재 개발 상태는 작업 요청·검토·인수인계 문서에서 확인하고 제품 가이드에 섞지 않습니다.
6. 실행하지 않은 검증을 성공으로 기록하지 않습니다.

---

## 공통 문서 구성

각 상세 가이드는 가능한 한 다음 순서를 따릅니다.

`대상과 목적 → 선행 조건 → 책임 경계 → 전체 흐름 → 설정과 계약 → 정상 절차 → 오류·부분 실패 → 복구·되돌리기 → 보안·감사 → 테스트 → 문제 해결 → 완료 점검`

용어가 혼동될 때는 [CPF 용어와 계약 참조](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)를 먼저 확인합니다.
