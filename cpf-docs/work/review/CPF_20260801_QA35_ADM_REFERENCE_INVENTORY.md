# CPF QA35 ADM Reference Inventory

## 사용자 제공 자료

- 파일: `메뉴캡쳐이미지.zip`
- Batch 화면: 15장
- Online/Common/System/Analysis 화면: 29장
- 총 44장

## 최소 Batch Capability로 추출한 항목

Job 등록·검색·상세, Job Group, 등록 요청 현황, Dependency Diagram, Instance 목록/상세, 강제실행·재실행·상태검증, 실행 이력, 로그 Tail/다운로드, Report 파일, Schedule Simulation, Calendar, Server/Agent 상태, 환경/알림/사용자 설정.

## 최소 Online/Common/System Capability로 추출한 항목

거래 정의, 전후처리, Pipeline·속성, Dependency Rule, DBIO, Deployment 상태, 외부 연계 상태, 처리량·Thread·Dependency·지연 비동기·처리 Flow, 거래/Error/Dependency/전문/통합 Log, 처리 통계, 사용자·권한·메뉴·공지·로그인/사용 이력·DB Schema, Parameter·Log Level·Message·Code·Label·Notification, Node·Datasource·Cache·File·지원 연결 Monitoring, 분석 도구.

## 적용 원칙

- CPF는 이 목록보다 기능이 많아야 한다.
- 오래된 제품명과 화면 구조는 복제하지 않는다.
- 지속형 TCP는 CPF 공식 범위가 아니므로 의무화하지 않는다. 공식 지원 Protocol/Connection Monitoring으로 해석한다.
- CPF의 Immutable Job Pack, Recovery, Approval, Break-glass, Gateway Security, Unified Transaction Trace 등 더 발전한 기능은 보호한다.
