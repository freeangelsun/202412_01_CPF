# CPF BZA 매뉴얼 — 조직·사용자·권한·결재 운영

> **주 독자**: 조직·인사·사용자·권한·결재 담당자와 BZA 연동 개발자
> **완료 결과**: 조직·직원·사용자·Role·Permission·Data Scope·결재·위임을 업무 시스템에 적용하고 운영한다.

## 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `dafe5c0e5260ea8149234e8ab2e75347e75338c1` (`20260802_07`)
- 활성 요구: `CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`와 Final Matrix
- Source·SQL·API·Config·Frontend·Script·Test가 설명보다 우선한다.
- 목표 기능과 현재 사용 가능한 기능을 구분한다.
- 실행하지 않은 Runtime·DB·Browser·다중 인스턴스·장애 시험은 `미검증`이다.


## 1. 도입 판단

BZA를 선택할 조건:

- 여러 업무가 조직·사용자·권한을 공유한다.
- Data Scope와 결재 정책을 공통으로 운영한다.
- 위임·대결·유효기간·감사가 필요하다.

단일 업무가 자체 권한만 사용하면 BZA 강제 도입보다 업무 소유 모델을 검토한다.

## 2. 초기 설정

1. BZA Artifact와 DB Migration을 적용한다.
2. 초기 관리자 계정을 Secret 절차로 준비한다.
3. 조직 Root와 기준 시점을 등록한다.
4. Role·Permission Catalog를 Import/검증한다.
5. 운영·승인·보안 역할을 분리한다.
6. 로그인·세션·감사·Masking을 확인한다.

## 3. 조직·직원·사용자

- 조직은 유효 시작·종료와 상하 관계를 가진다.
- 직원과 로그인 사용자를 구분한다.
- 퇴직·휴직·이동 시 권한과 위임 종료를 함께 처리한다.
- 조직 변경은 과거 결재·감사 해석에 필요한 Snapshot을 유지한다.

## 4. Role·Permission·Data Scope

| 개념 | 예 |
|---|---|
| Role | 지급 운영자, 승인자, 보안 관리자 |
| Permission | 지급 조회, 재처리 요청, 승인 |
| Data Scope | 본인 조직, 하위 조직, 지정 조직 |
| Masking | 개인정보 일부 표시 |

화면 접근 Permission과 Command Permission을 분리한다. Export와 원문 조회는 별도 Permission·Reason·Approval을 사용한다.

## 5. 결재

결재 정책은 다음을 포함한다.

```text
업무 유형
금액·건수·위험 조건
결재선
요청자/승인자 분리
유효시간
정책 Version
반려·취소
위임·대결
```

정책 Version 또는 대상 Hash가 달라지면 기존 승인을 재사용하지 않는다.

## 6. 위임·대결

- 시작·종료 시각과 대상 업무를 제한한다.
- 위임자가 가진 범위를 초과할 수 없다.
- 순환 위임을 차단한다.
- 위임 실행은 원 권한자와 대행자를 모두 감사한다.

## 7. Attachment·Notification

- 첨부는 File Type·Size·Checksum·보존·접근 권한을 적용한다.
- Notification은 Preference·Quiet Hours·Retry·Receipt를 따른다.
- Email/SMS Provider Timeout 후 중복 발송을 막기 위해 Receipt를 대사한다.

QA38 Notification Starter 전체는 기준 Commit에서 미구현 상태다.

## 8. Session·Masking·Audit

- Session JDBC/Resource Server 구성을 실제 Starter로 확인한다.
- 개인정보는 목록·상세·Export 각각 Masking 정책을 적용한다.
- 감사에는 사용자·대행자·Role·Data Scope·Permission·Reason·Approval·Before/After를 남긴다.

## 9. 업무 Domain 연계

BZA는 업무 상태를 소유하지 않는다. 업무 Domain은 BZA에서 인증·권한·승인 결과를 받고 자신의 Transaction과 Audit에 참조를 저장한다.

Timeout 후 승인 결과가 불명하면 Approval ID와 Policy Version으로 조회하고 새 승인 요청을 만들지 않는다.

## 10. 운영

- 조직 유효기간 오류
- 휴면·잠금·퇴직 사용자
- Role·Permission Drift
- 위임 만료·순환
- 승인 대기·만료
- Session·Credential 문제
- Notification 실패
- Audit 누락

각 문제는 대상·영향·원인·정상화 조치·재검증을 기록한다.

## 11. Backup·Restore·Upgrade

조직·권한·결재는 업무 해석에 직접 영향을 준다. DB Backup뿐 아니라 Policy Version·Permission Catalog·Artifact·Config를 함께 복원한다. Upgrade 전후 대표 권한·결재 시나리오와 과거 감사 조회를 검증한다.

## 12. EDU

1. 조직 3단계 등록
2. 사용자·직원 연결
3. Role·Permission·Data Scope 적용
4. Masked 목록·상세 확인
5. 2단계 결재와 반려
6. 위임·만료 확인
7. Timeout 후 Approval 결과 조회
8. Audit와 Export 권한 확인


## 13. 처음 사용하는 담당자의 실행 순서

1. 조직·직원·사용자 정본 Owner와 기준 시점을 정한다.
2. 초기 관리자와 운영·승인·보안 역할을 분리한다.
3. 조직 Root와 하위 조직을 등록하고 유효기간을 확인한다.
4. 직원과 로그인 사용자를 연결한다.
5. Role·Permission·Data Scope·Masking 정책을 등록한다.
6. 대표 업무의 결재 정책과 요청자·승인자 분리를 구성한다.
7. 위임·대결·만료·순환 차단을 시험한다.
8. 업무 Domain에 인증·권한·승인 결과 계약을 연결한다.
9. Session·Export·Audit·Notification을 확인한다.
10. Backup·Restore·Upgrade 후 대표 권한·결재 시나리오를 재검증한다.

## 14. 정상 결과와 상태 변화

| 영역 | 정상 결과 |
|---|---|
| 조직 | 유효기간·상하 관계·고아·순환 오류 없음 |
| 사용자 | 직원 연결·상태·Credential 정책 일치 |
| 권한 | 화면·API·Command Permission 일치 |
| Data Scope | 조직·업무 범위 밖 데이터 제외 |
| Masking | 목록·상세·Export·Log 정책 일치 |
| 결재 | 정책 Version·대상 Hash·승인 이력 일치 |
| 위임 | 기간·대상·원 권한 범위 안에서만 적용 |
| Audit | Actor·대행자·Reason·Before/After 기록 |

## 15. 현재 상태 판정

- 기준 Commit에 `cpf-biz-admin` Product가 등록돼 있다.
- 조직·권한·결재 기능은 Source·Frontend·DB와 실제 Route·Permission을 다시 전수 대조해야 한다.
- Notification Provider 전체, Runtime·Browser·다중 인스턴스·장애 시험은 `미검증`이다.
