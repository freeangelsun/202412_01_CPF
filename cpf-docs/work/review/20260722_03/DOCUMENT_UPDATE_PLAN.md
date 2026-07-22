# CPF 문서 갱신 계획

## 즉시

- Root README 단일화와 UTF-8
- 최상위 목표·현재 요청·Gap·검수 기준 복구
- 역할별 Guide 내용 분리
- 깨진 Link와 삭제 산출물 참조 제거

## 구현 중

- Source/API/SQL/UI 변경과 같은 Commit에서 Guide 갱신
- Generated Matrix는 필요 시 Script로 재생성
- Evidence는 최신 Commit과 환경에서 생성
- Root 작업 문서의 최종 위치 이동은 Script/CI/Gradle Link와 함께 수행

## 안정화 후

- Developer/Operator/API/OpenAPI/JavaDoc/EDU 최종 대조
- 설치, Migration, Deployment와 Recovery 실행 검증
- 최종 DOCX/PDF 생성
- 실제 Release 시 CHANGELOG/Release Notes 생성

## 금지

- 날짜별 복제 문서
- 동일 내용의 여러 Guide
- 미구현 기능의 사용 가능 표기
- 과거 Evidence 재사용
- Package 전달용 Manifest를 제품 Repository에 잔존
