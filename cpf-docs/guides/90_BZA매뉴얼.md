# CPF BZA 매뉴얼

## 문서 기준

| 항목 | 기준 |
|---|---|
| Repository | `https://github.com/freeangelsun/202412_01_CPF` |
| Branch | `master` |
| Source 기준 Commit | `61dcbbe7d81e44a4ba3534ecd0f91f7adfa4e9c5` (`04_09`) |
| 최상위 목표 정본 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 문서 표준 정본 | `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md` |
| 주 독자 | 조직·인사·권한·결재·업무관리 담당자와 연동 개발자 |
| 문서 사용 결과 | BZA를 설치하고 조직·사용자·Role·Permission·Data Scope·결재를 구성해 운영한다. |
| 구현 상태 | `완료` — 사용자가 요청한 산출물 작성 전제 |
| 이 작성 세션의 Runtime 재실행 | 수행하지 않음 |
| 문서 현행화 범위 | Source·Catalog·Route·공식 문서 구조와 절차 정합성 |

> 이 문서는 구현 기능을 사용할 수 있는 상태로 설명한다. 이 작성 세션에서 Runtime을 다시 실행하지 않았다는 사실은 기능 절차를 축소하는 근거가 아니며, 고객 환경 배포 승인 시에는 해당 환경의 실행 기록을 별도로 보존한다.
## 1. 도입 판단

BZA는 고객 업무가 조직·직원·사용자·권한·결재를 공통 모델로 사용하고 업무 Domain이 BZA Public Contract로 연동할 때 선택한다. 기존 IAM·HR·결재가 정본이면 Source System, 동기화 방향, Conflict Owner와 장애 시 운영 기준을 먼저 결정한다.

## 2. 설치

1. Backend와 Frontend Artifact Hash·SBOM을 검증한다.
2. `browser-bff`와 필요한 Data·Security·File·Notification Capability를 구성한다.
3. BZA DB Migration을 적용하고 Verify한다.
4. OpenAPI Generated Client와 Frontend Build를 확인한다.
5. Initial Admin Bootstrap Secret을 Repository 밖에서 공급한다.
6. 첫 로그인 후 Credential을 교체하고 Bootstrap 권한을 종료한다.

## 3. 메뉴 지도

| 메뉴 | Route | 업무 결과 |
|---|---|---|
| 대시보드 | / | 업무 운영 현황 |
| 조직 | /organizations | 조직 계층·유효기간 |
| 직원 | /employees | 직원 Profile·연락처 Masking |
| 직급 | /positions | 직급 기준정보 |
| 직책 | /jobTitles | 직책 기준정보 |
| 발령·겸직 | /assignments | 다중 소속·파견·대행 |
| 조직 책임 | /organizationResponsibilities | 조직장·승인 Owner |
| 사용자 | /users | BZA 인증 사용자 |
| 역할 | /roles | 업무 Role |
| 사용자 Role | /userRoles | 다중 Role·유효기간 |
| 메뉴 | /menus | 화면 Menu Registry |
| 권한 | /permissions | 화면·행위·API·Data Scope |
| 권한 분석 | /permissionTools | 실효 권한·Role 비교·Simulation |
| 결재 처리 | /approvalInbox | 참여자 Inbox와 승인/반려 |
| 결재 상신 | /approvalSubmissions | 정책 기반 멱등 상신 |
| 결재 정책 | /approvalPolicies | Versioned ALL·ANY·N_OF_M |
| 경로 Simulation | /approvalSimulation | 조직·Role·위임 해석 |
| 결재 위임 | /approvalDelegations | 유효기간 위임·대결 |
| 내 세션 | /sessions | Refresh Session·Revoke |
| 업무 감사 | /audits | Immutable Audit 검증 |
| 알림 | /notifications | 업무 알림 |
| 첨부파일 | /attachments | Upload·검사·Download |
| 저장 검색 | /savedSearches | 사용자 검색 조건 |
| 업무 설정 | /settings | BZA 설정 |
| 다운로드 정책 | /downloads | Download Policy |
| 다운로드 감사 | /downloadAudits | Download Audit |

## 4. 조직

입력값은 조직 Code, Name, 상위 조직, 유효 시작/종료, 상태, Source System이다.

1. 상위 조직과 유효 기간을 확인한다.
2. 순환 계층과 중복 Code를 차단한다.
3. Expected Version으로 저장한다.
4. 조직 책임자와 하위 조직 영향을 확인한다.
5. 변경 전후와 Source System을 Audit에서 확인한다.

종료 조직에는 신규 직원·발령·권한을 부여하지 않는다. 소급 변경은 진행 중 결재와 Data Scope 영향을 Preview한다.

## 5. 직원·직급·직책·발령

직원은 조직·직급·직책·재직 상태·유효 기간과 연결한다. 발령은 다중 소속, 겸직, 파견, 대행을 구분하고 Primary 여부와 기간을 기록한다.

중복 기간, 종료 조직, 퇴직 직원, 순환 대행을 차단한다. 연락처 원문은 권한이 있는 상세 기능에서만 조회하고 기본 목록은 Masking한다.

## 6. 사용자와 Session

사용자를 직원 또는 Service Account와 연결한다. 상태, 인증 Identifier, 연락처, 유효 기간을 입력한다.

- 잠금 해제·Credential Reset은 HIGH 조치로 처리
- Session 목록에서 Device·Created·Last Used·Expiry 확인
- 의심 Session은 Revoke
- 퇴직·권한 회수 시 활성 Session을 함께 종료
- Cookie Secure/SameSite·CSRF·Idle/Absolute Timeout 적용

## 7. Role·Permission·Data Scope

Role은 Permission 묶음이고 Data Scope는 접근 데이터 범위다. 사용자 직접 Permission은 예외로 제한한다.

1. Menu·Button·API·Action Permission을 정의한다.
2. 조직·Tenant·업무·소유자 Data Scope를 정의한다.
3. Masking 수준과 Export 권한을 연결한다.
4. Role에 Permission을 부여한다.
5. 사용자 Role과 유효 기간을 저장한다.
6. 권한 분석 메뉴에서 실효 권한과 충돌을 Simulation한다.
7. 변경 전후를 Audit한다.

## 8. 결재 정책

정책에는 업무 유형, 조건, Version, 참여자 해석, ALL·ANY·N_OF_M, 순차/병렬, 만료, 회수·취소 가능 상태를 정의한다.

1. 조직·Role·직책 기준 참여자를 설정한다.
2. 경로 Simulation으로 실제 참여자 Snapshot을 확인한다.
3. 자기 승인·순환·참여자 없음을 차단한다.
4. 정책 Version을 승인하고 Effective Time을 지정한다.
5. 진행 중 결재는 상신 시점의 Policy/Participant Snapshot을 유지한다.

## 9. 상신·처리·회수·재상신

상신 요청에는 Business Key, Policy ID/Version, Idempotency Key, 제목·내용·첨부, 요청자, Expected Business Version을 포함한다.

- 승인/반려: 참여자 권한과 현재 Step 검증
- 회수: 첫 처리 전 등 정책이 허용한 상태
- 취소: 업무가 취소 가능한 상태
- 재상신: 반려/취소 후 새 Submission ID
- 응답 유실: 동일 Key 또는 Submission ID로 조회
- 중복 클릭: 한 건의 결재와 Audit만 생성

## 10. 위임·대결

위임자, 대리자, 업무 범위, 시작/종료, 사유를 입력한다. 본인 위임, 순환 위임, 과도한 범위와 기간 중복을 차단한다. 원 승인자의 전체 권한을 복제하지 않고 대상 결재에만 적용한다. 기간 종료 후 실효 권한이 제거됐는지 확인한다.

## 11. 첨부

1. Size·Count·Extension·MIME·Checksum을 검증한다.
2. 악성 검사 상태를 확인한다.
3. Download Permission과 Masking/Watermark를 적용한다.
4. 결재 완료 후 변경은 새 Attachment Version과 Audit를 요구한다.
5. 재검사와 보안 상태 변경은 Reason과 권한을 요구한다.

## 12. 알림

결재 요청·승인·반려·만료·위임 이벤트를 Notification API로 전달한다. 전송 결과 불명은 Receipt를 확인한 뒤 재발송을 결정한다. 수신자와 본문은 Log에서 Masking한다.

## 13. 감사·다운로드

조직·직원·사용자·Role·Permission·Data Scope·결재·위임 변경은 Actor, Approver, Before/After, Reason, Source, Result를 기록한다.

Download는 정책과 Audit를 적용하고 파일 만료, Masking, Download 횟수와 Hash를 기록한다.

## 14. 업무 Domain 연동

업무 Domain은 BZA DB를 직접 조회하지 않고 Public Query/Command Contract를 사용한다. 사용자·조직·권한 Snapshot의 Version과 유효 시간을 전달한다. Remote Timeout 후 Operation ID로 조회하고 결과를 추정하지 않는다.

## 15. Backup·Restore

BZA DB, Attachment Reference, Role/Permission Catalog, 진행 중 결재, Audit, Session Store, Config와 Artifact Version을 Manifest로 묶는다.

Restore 후 조직 계층, 직원 유효기간, 사용자 상태, 실효 권한, 진행 중 결재 참여자, Attachment 접근, Audit 연속성을 확인한다.

## 16. Upgrade·Rollback

Backend·Frontend·OpenAPI·DB Schema 호환을 확인한다. 진행 중 결재의 상태 전이와 Participant Snapshot을 Test한다. 비가역 데이터 변경은 Forward Recovery와 보정 Script를 준비한다.

## 17. 운영 주기

- 일일: 동기화 실패, 잠긴 사용자, 만료 위임, 지연 결재, 알림 실패
- 주간: Role/Permission 변경, Data Scope 과다, Download, Audit Delivery
- 월간: 휴면·퇴직 사용자, 조직 Drift, 권한 재인증, Restore Test

## 18. EDU — 신규 직원 Onboarding

조직 확인 → 직원 생성 → 사용자 생성 → Role·Data Scope 부여 → Login → 메뉴·API 권한과 Masking 확인 → Audit 확인 순서로 수행한다.

## 19. EDU — 결재와 대결

2단계 정책 생성 → 경로 Simulation → 상신 → 2차 승인자 부재 기간 대결 설정 → 승인 → 기간 종료 → 대결 실효 권한 제거 확인 순서로 수행한다.
