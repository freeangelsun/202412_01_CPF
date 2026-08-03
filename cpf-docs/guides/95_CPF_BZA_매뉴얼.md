# CPF BZA 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. BZA 역할

BZA는 고객 업무 관리 제품으로 조직, 직원, 사용자, Role, Permission, Data Scope, 결재, 위임·대결, Attachment, Notification, Session, Masking, Audit와 Export를 제공한다.

Backend: `cpf-biz-admin/**`
Frontend: `cpf-biz-admin/frontend/**`

## 2. 도입 판단

고객 업무가 조직·사용자·권한·결재를 공통 모델로 사용하고, 업무 Domain이 BZA Public Contract로 연동할 때 적용한다. 기존 IAM/HR/결재 시스템이 정본이면 Adapter와 동기화 Ownership을 먼저 결정한다.

## 3. 설치

`browser-bff` Profile과 Security/Data Capability를 구성하고 BZA DB Migration을 적용한다. Backend/Frontend Artifact Hash와 OpenAPI Generated Client를 확인한다.

## 4. 초기 관리자

초기 관리자는 일회성 Bootstrap 절차로 생성하고 첫 로그인에서 Credential을 교체한다. Bootstrap Secret은 Repository·Log에 남기지 않는다. 이후 관리자는 승인된 사용자·Role 절차로만 추가한다.

## 5. 조직·직원·사용자

1. 조직 Code, 상위 조직, 유효 기간을 등록한다.
2. 직원과 조직·직책·상태를 연결한다.
3. 사용자를 직원 또는 Service Account와 연결한다.
4. 중복 ID, 종료 조직, 재직 상태, 유효 기간을 검증한다.
5. 변경 전후와 Source System을 Audit에 기록한다.

## 6. Role·Permission·Data Scope

Role은 Permission 묶음이며 Data Scope와 분리한다. 사용자에게 직접 Permission을 남용하지 않는다. 조직/업무/지역/소유자 범위와 Masking 수준을 함께 결정한다.

## 7. 결재

결재선, 단계, 병렬/순차, 금액·위험 조건, 대결/위임, 유효 기간을 정의한다. 요청은 Expected Version과 Reason을 포함하고 승인/반려/회수/취소 상태를 구분한다.

## 8. 위임·대결

위임자는 기간·업무 범위·대상자를 지정한다. 대결은 원 승인자의 권한을 무제한 복제하지 않고 대상 결재와 기간에 제한한다. 본인 승인과 순환 위임을 차단한다.

## 9. Attachment

File Capability로 업로드·검사·저장한다. Size/MIME/Extension/Checksum/악성 검사와 Download Permission을 적용한다. 결재 완료 후 첨부 변경은 새 Version과 Audit를 요구한다.

## 10. Notification

결재 요청·처리·만료·위임 이벤트를 Notification API로 전달한다. Email/SMS 결과 불명은 Receipt/Reconcile 후 재발송을 결정한다.

## 11. Session·Masking

Browser Session, CSRF, Cookie Secure/SameSite, Idle/Absolute Timeout을 적용한다. 목록·상세·Export의 Masking 정책을 동일하게 유지한다.

## 12. Audit·Export

조직·사용자·Role·Permission·Data Scope·결재 변경은 Actor, Approver, Before/After, Reason, Source, Result를 기록한다. Export는 비동기 Job과 만료 Download를 사용한다.

## 13. 업무 Domain 연계

업무 Domain은 BZA DB를 직접 조회하지 않고 Public Query/Command Contract를 사용한다. 사용자/조직 Snapshot의 Version과 유효 기간을 전달하고 Remote Timeout 후 Operation ID로 결과를 조회한다.

## 14. 확장

고객 IAM/HR/결재 Provider는 SPI와 Adapter로 연결한다. Internal Package를 import하지 않고 Conformance Test를 통과해야 한다. Source System 정본과 Conflict 해결 규칙을 문서화한다.

## 15. Backup·Restore

BZA DB, Attachment Reference, Config, Role/Permission Catalog, Audit, Session Store의 Backup 관계를 Manifest에 기록한다. Restore 후 조직 계층, 사용자 상태, 결재 진행 건, Permission와 Audit 연속성을 확인한다.

## 16. Upgrade·Rollback

Schema와 Frontend/Backend API 호환성을 확인한다. 진행 중 결재의 상태 전이 호환을 Test한다. Rollback이 어려운 데이터 변경은 Forward Recovery와 Migration 보정 Script를 준비한다.

## 17. 운영 절차

- 일일: 동기화 실패, 잠긴 사용자, 만료 위임, 결재 지연, Notification 실패
- 주간: Role/Permission 변경, Data Scope 과다, Export, Audit Delivery
- 월간: 휴면/퇴직 사용자, 조직 Drift, 권한 재인증, Backup Restore Test

## 18. EDU

### 신규 직원 Onboarding

조직/직원/사용자 생성 → Role/Data Scope 부여 → Login/Masking 확인 → Audit 확인.

### 결재와 대결

2단계 결재 생성 → 2차 승인자 부재 기간 대결 설정 → 요청 승인 → 기간 종료 후 권한 제거 확인.

### 응답 유실

승인 Command 응답을 차단하고 동일 버튼을 반복 누르지 않은 채 Operation ID로 상태를 조회한다. 기존 승인 결과와 Audit가 한 건인지 확인한다.
