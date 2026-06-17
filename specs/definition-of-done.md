# Definition of Done

신규 기능은 다음 기준을 만족해야 완료로 봅니다.

1. 테이블명, FK/UK/index 이름, 공통 감사 컬럼이 SQL 표준을 따른다.
2. Java, MyBatis, SQL, Swagger, 문서의 명칭이 서로 일치한다.
3. Controller/Service/Mapper/DTO/Test가 모듈 템플릿을 따른다.
4. 요청 DTO validation과 공통 오류 응답이 정의되어 있다.
5. 변경성 업무 API는 감사 사유와 권한 검사를 적용한다.
6. 민감정보, DB password, JWT secret 기본값을 prod profile에 두지 않는다.
7. 로그에 민감정보를 남기지 않고 trace id/correlation id를 유지한다.
8. 단위 또는 슬라이스 테스트를 추가하고, DB 변경은 smoke 또는 integration 검증을 수행한다.
9. README는 짧게 유지하고 상세 가이드를 함께 갱신한다.
10. `qualityGate`와 필요한 MariaDB 검증 결과를 남긴다.
