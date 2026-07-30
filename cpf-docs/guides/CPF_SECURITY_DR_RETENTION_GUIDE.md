# CPF 보안·재해복구·데이터 보존 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 보안 관리자, 운영자, 재해복구 담당자, 개인정보 담당자
> **목적**: 인증·권한·비밀값·마스킹·백업·복구·보존과 법적 보류를 통합 관리한다.
> **관련 문서**: [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md) · [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

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

ADM이 게이트웨이·배치 등 소유 제품의 내부 제어 API를 호출할 때 일반 사용자 세션만으로 신뢰하지 않는다. 요청 시각, Nonce, Method, 경로와 Body Hash를 서명하고, 수신 측은 시간 오차·Nonce 재사용·서명과 허용 호출자를 검증한다. Key는 비밀값 공급자에서 가져오며 오류·로그·검증 증적에 노출하지 않는다.


지원 구성:

- 세션
- OAuth2/OIDC
- JWT
- mTLS
- API Key 참조
- 에이전트 인증서
- 서비스 Account

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
