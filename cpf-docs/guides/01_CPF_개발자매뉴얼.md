# CPF 개발자 매뉴얼 — 업무 기능을 설계·구현·검증·인계하는 절차

> **주 독자**: 온라인 업무 개발자, 메시지·파일·외부연계 개발자, 기술 리더
> **완료 결과**: 업무 기능의 API·상태·DB·권한·연계·시험·운영 인계를 하나의 추적 가능한 단위로 완성한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `54bcc10887a83b933685bff462c0b0d7df824923`

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 이 매뉴얼의 범위](#1-이-매뉴얼의-범위)
- [2. 시작 전 점검](#2-시작-전-점검)
- [3. 제품 Architecture와 개발 원칙](#3-제품-architecture와-개발-원칙)
  - [3.1 기준 Source 계약](#31-기준-source-계약)
  - [3.2 개발자가 지켜야 할 경계](#32-개발자가-지켜야-할-경계)
- [4. 신규 업무 영역 생성](#4-신규-업무-영역-생성)
  - [4.1 Dry Run](#41-dry-run)
  - [4.2 Apply 전 결정값](#42-apply-전-결정값)
  - [4.3 생성 후 필수 확인](#43-생성-후-필수-확인)
- [5. 기능 설계 카드](#5-기능-설계-카드)
- [6. API 개발](#6-api-개발)
  - [6.1 요청 계약](#61-요청-계약)
  - [6.2 Controller](#62-controller)
  - [6.3 응답](#63-응답)
- [7. Application과 Transaction](#7-application과-transaction)
  - [7.1 기본 순서](#71-기본-순서)
  - [7.2 금지](#72-금지)
- [8. Domain과 상태 전이](#8-domain과-상태-전이)
- [9. Persistence와 DB](#9-persistence와-db)
  - [9.1 공식 Vendor](#91-공식-vendor)
  - [9.2 개발 순서](#92-개발-순서)
  - [9.3 Transaction·Lock](#93-transactionlock)
  - [9.4 Migration](#94-migration)
- [10. 동일 JVM과 원격 호출](#10-동일-jvm과-원격-호출)
  - [10.1 계약 동등성](#101-계약-동등성)
  - [10.2 Timeout Budget](#102-timeout-budget)
- [11. 비동기 메시지 처리](#11-비동기-메시지-처리)
  - [11.1 공통 흐름](#111-공통-흐름)
  - [11.2 기준 Source 상태](#112-기준-source-상태)
  - [11.3 Multi-provider 목표](#113-multi-provider-목표)
- [12. 파일·SFTP·외부 REST·전문](#12-파일sftp외부-rest전문)
  - [12.1 파일·Attachment 공통](#121-파일attachment-공통)
  - [12.2 SFTP](#122-sftp)
  - [12.3 외부 REST](#123-외부-rest)
  - [12.4 TCP·ISO8583](#124-tcpiso8583)
- [13. Security·Masking·Audit](#13-securitymaskingaudit)
- [14. Starter 선택](#14-starter-선택)
  - [14.1 Starter 선택 예](#141-starter-선택-예)
  - [14.2 선택 검증](#142-선택-검증)
  - [14.3 현재 금지](#143-현재-금지)
- [15. OpenAPI·JavaDoc](#15-openapijavadoc)
- [16. Test 전략](#16-test-전략)
- [17. ADM 연결과 운영 인계](#17-adm-연결과-운영-인계)
- [18. EDU 실습 구조](#18-edu-실습-구조)
  - [18.1 기준 Source에서 확인한 EDU 범위](#181-기준-source에서-확인한-edu-범위)
  - [18.2 온라인·연계 EDU 45개 전수표](#182-온라인연계-edu-45개-전수표)
- [19. 완료 점검표](#19-완료-점검표)
- [20. 배포 인계 절차](#20-배포-인계-절차)
  - [20.1 인계 묶음](#201-인계-묶음)
  - [20.2 배포 전 확인 명령](#202-배포-전-확인-명령)
- [21. 오류·부분 실패·정상화 결정표](#21-오류부분-실패정상화-결정표)
- [22. EDU 전수 검증 규칙](#22-edu-전수-검증-규칙)
  - [22.1 실행 점검 예](#221-실행-점검-예)
- [23. 종단간 예제: 지급 신청 업무 Slice](#23-종단간-예제-지급-신청-업무-slice)
  - [23.1 이 기능으로 만드는 업무 결과](#231-이-기능으로-만드는-업무-결과)
  - [23.2 선택 기준과 사용하지 말아야 할 경우](#232-선택-기준과-사용하지-말아야-할-경우)
  - [23.3 주 사용자와 권한](#233-주-사용자와-권한)
  - [23.4 시작 전에 결정할 값](#234-시작-전에-결정할-값)
  - [23.5 작업 후 만들어지는 결과물](#235-작업-후-만들어지는-결과물)
  - [23.6 단계별 절차](#236-단계별-절차)
  - [23.7 입력값·기본값·허용 범위](#237-입력값기본값허용-범위)
  - [23.8 정상 결과와 완료 판정](#238-정상-결과와-완료-판정)
  - [23.9 중복·동시성·시간초과·응답 유실·부분 실패](#239-중복동시성시간초과응답-유실부분-실패)
  - [23.10 재시도·재시작·재처리·대사·보상·되돌리기](#2310-재시도재시작재처리대사보상되돌리기)
  - [23.11 로그·지표·추적·감사](#2311-로그지표추적감사)
  - [23.12 교육 예제](#2312-교육-예제)
  - [23.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2313-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [23.14 운영 인계](#2314-운영-인계)
- [24. 조건 검색·목록·상세 Query 개발](#24-조건-검색목록상세-query-개발)
  - [24.1 이 기능으로 만드는 업무 결과](#241-이-기능으로-만드는-업무-결과)
  - [24.2 선택 기준과 사용하지 말아야 할 경우](#242-선택-기준과-사용하지-말아야-할-경우)
  - [24.3 주 사용자와 권한](#243-주-사용자와-권한)
  - [24.4 시작 전에 결정할 값](#244-시작-전에-결정할-값)
  - [24.5 작업 후 만들어지는 결과물](#245-작업-후-만들어지는-결과물)
  - [24.6 단계별 절차](#246-단계별-절차)
  - [24.7 입력값·기본값·허용 범위](#247-입력값기본값허용-범위)
  - [24.8 정상 결과와 완료 판정](#248-정상-결과와-완료-판정)
  - [24.9 중복·동시성·시간초과·응답 유실·부분 실패](#249-중복동시성시간초과응답-유실부분-실패)
  - [24.10 재시도·재시작·재처리·대사·보상·되돌리기](#2410-재시도재시작재처리대사보상되돌리기)
  - [24.11 로그·지표·추적·감사](#2411-로그지표추적감사)
  - [24.12 교육 예제](#2412-교육-예제)
  - [24.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2413-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [24.14 운영 인계](#2414-운영-인계)
- [25. 상태 변경 Command와 낙관적 동시성](#25-상태-변경-command와-낙관적-동시성)
  - [25.1 이 기능으로 만드는 업무 결과](#251-이-기능으로-만드는-업무-결과)
  - [25.2 선택 기준과 사용하지 말아야 할 경우](#252-선택-기준과-사용하지-말아야-할-경우)
  - [25.3 주 사용자와 권한](#253-주-사용자와-권한)
  - [25.4 시작 전에 결정할 값](#254-시작-전에-결정할-값)
  - [25.5 작업 후 만들어지는 결과물](#255-작업-후-만들어지는-결과물)
  - [25.6 단계별 절차](#256-단계별-절차)
  - [25.7 입력값·기본값·허용 범위](#257-입력값기본값허용-범위)
  - [25.8 정상 결과와 완료 판정](#258-정상-결과와-완료-판정)
  - [25.9 중복·동시성·시간초과·응답 유실·부분 실패](#259-중복동시성시간초과응답-유실부분-실패)
  - [25.10 재시도·재시작·재처리·대사·보상·되돌리기](#2510-재시도재시작재처리대사보상되돌리기)
  - [25.11 로그·지표·추적·감사](#2511-로그지표추적감사)
  - [25.12 교육 예제](#2512-교육-예제)
  - [25.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2513-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [25.14 운영 인계](#2514-운영-인계)
- [26. Local·Remote Facade 동등성](#26-localremote-facade-동등성)
  - [26.1 이 기능으로 만드는 업무 결과](#261-이-기능으로-만드는-업무-결과)
  - [26.2 선택 기준과 사용하지 말아야 할 경우](#262-선택-기준과-사용하지-말아야-할-경우)
  - [26.3 주 사용자와 권한](#263-주-사용자와-권한)
  - [26.4 시작 전에 결정할 값](#264-시작-전에-결정할-값)
  - [26.5 작업 후 만들어지는 결과물](#265-작업-후-만들어지는-결과물)
  - [26.6 단계별 절차](#266-단계별-절차)
  - [26.7 입력값·기본값·허용 범위](#267-입력값기본값허용-범위)
  - [26.8 정상 결과와 완료 판정](#268-정상-결과와-완료-판정)
  - [26.9 중복·동시성·시간초과·응답 유실·부분 실패](#269-중복동시성시간초과응답-유실부분-실패)
  - [26.10 재시도·재시작·재처리·대사·보상·되돌리기](#2610-재시도재시작재처리대사보상되돌리기)
  - [26.11 로그·지표·추적·감사](#2611-로그지표추적감사)
  - [26.12 교육 예제](#2612-교육-예제)
  - [26.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2613-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [26.14 운영 인계](#2614-운영-인계)
- [27. 메시지 Outbox·Inbox·DLQ](#27-메시지-outboxinboxdlq)
  - [27.1 이 기능으로 만드는 업무 결과](#271-이-기능으로-만드는-업무-결과)
  - [27.2 선택 기준과 사용하지 말아야 할 경우](#272-선택-기준과-사용하지-말아야-할-경우)
  - [27.3 주 사용자와 권한](#273-주-사용자와-권한)
  - [27.4 시작 전에 결정할 값](#274-시작-전에-결정할-값)
  - [27.5 작업 후 만들어지는 결과물](#275-작업-후-만들어지는-결과물)
  - [27.6 단계별 절차](#276-단계별-절차)
  - [27.7 입력값·기본값·허용 범위](#277-입력값기본값허용-범위)
  - [27.8 정상 결과와 완료 판정](#278-정상-결과와-완료-판정)
  - [27.9 중복·동시성·시간초과·응답 유실·부분 실패](#279-중복동시성시간초과응답-유실부분-실패)
  - [27.10 재시도·재시작·재처리·대사·보상·되돌리기](#2710-재시도재시작재처리대사보상되돌리기)
  - [27.11 로그·지표·추적·감사](#2711-로그지표추적감사)
  - [27.12 교육 예제](#2712-교육-예제)
  - [27.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2713-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [27.14 운영 인계](#2714-운영-인계)
- [28. 파일·Attachment·SFTP 연계](#28-파일attachmentsftp-연계)
  - [28.1 이 기능으로 만드는 업무 결과](#281-이-기능으로-만드는-업무-결과)
  - [28.2 선택 기준과 사용하지 말아야 할 경우](#282-선택-기준과-사용하지-말아야-할-경우)
  - [28.3 주 사용자와 권한](#283-주-사용자와-권한)
  - [28.4 시작 전에 결정할 값](#284-시작-전에-결정할-값)
  - [28.5 작업 후 만들어지는 결과물](#285-작업-후-만들어지는-결과물)
  - [28.6 단계별 절차](#286-단계별-절차)
  - [28.7 입력값·기본값·허용 범위](#287-입력값기본값허용-범위)
  - [28.8 정상 결과와 완료 판정](#288-정상-결과와-완료-판정)
  - [28.9 중복·동시성·시간초과·응답 유실·부분 실패](#289-중복동시성시간초과응답-유실부분-실패)
  - [28.10 재시도·재시작·재처리·대사·보상·되돌리기](#2810-재시도재시작재처리대사보상되돌리기)
  - [28.11 로그·지표·추적·감사](#2811-로그지표추적감사)
  - [28.12 교육 예제](#2812-교육-예제)
  - [28.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2813-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [28.14 운영 인계](#2814-운영-인계)
- [29. 외부 REST·TCP·ISO8583 연계](#29-외부-resttcpiso8583-연계)
  - [29.1 이 기능으로 만드는 업무 결과](#291-이-기능으로-만드는-업무-결과)
  - [29.2 선택 기준과 사용하지 말아야 할 경우](#292-선택-기준과-사용하지-말아야-할-경우)
  - [29.3 주 사용자와 권한](#293-주-사용자와-권한)
  - [29.4 시작 전에 결정할 값](#294-시작-전에-결정할-값)
  - [29.5 작업 후 만들어지는 결과물](#295-작업-후-만들어지는-결과물)
  - [29.6 단계별 절차](#296-단계별-절차)
  - [29.7 입력값·기본값·허용 범위](#297-입력값기본값허용-범위)
  - [29.8 정상 결과와 완료 판정](#298-정상-결과와-완료-판정)
  - [29.9 중복·동시성·시간초과·응답 유실·부분 실패](#299-중복동시성시간초과응답-유실부분-실패)
  - [29.10 재시도·재시작·재처리·대사·보상·되돌리기](#2910-재시도재시작재처리대사보상되돌리기)
  - [29.11 로그·지표·추적·감사](#2911-로그지표추적감사)
  - [29.12 교육 예제](#2912-교육-예제)
  - [29.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2913-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [29.14 운영 인계](#2914-운영-인계)
- [30. 보안·권한·Data Scope·Masking·Audit](#30-보안권한data-scopemaskingaudit)
  - [30.1 이 기능으로 만드는 업무 결과](#301-이-기능으로-만드는-업무-결과)
  - [30.2 선택 기준과 사용하지 말아야 할 경우](#302-선택-기준과-사용하지-말아야-할-경우)
  - [30.3 주 사용자와 권한](#303-주-사용자와-권한)
  - [30.4 시작 전에 결정할 값](#304-시작-전에-결정할-값)
  - [30.5 작업 후 만들어지는 결과물](#305-작업-후-만들어지는-결과물)
  - [30.6 단계별 절차](#306-단계별-절차)
  - [30.7 입력값·기본값·허용 범위](#307-입력값기본값허용-범위)
  - [30.8 정상 결과와 완료 판정](#308-정상-결과와-완료-판정)
  - [30.9 중복·동시성·시간초과·응답 유실·부분 실패](#309-중복동시성시간초과응답-유실부분-실패)
  - [30.10 재시도·재시작·재처리·대사·보상·되돌리기](#3010-재시도재시작재처리대사보상되돌리기)
  - [30.11 로그·지표·추적·감사](#3011-로그지표추적감사)
  - [30.12 교육 예제](#3012-교육-예제)
  - [30.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#3013-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [30.14 운영 인계](#3014-운영-인계)
- [31. DB Migration·Upgrade·Rollback](#31-db-migrationupgraderollback)
  - [31.1 이 기능으로 만드는 업무 결과](#311-이-기능으로-만드는-업무-결과)
  - [31.2 선택 기준과 사용하지 말아야 할 경우](#312-선택-기준과-사용하지-말아야-할-경우)
  - [31.3 주 사용자와 권한](#313-주-사용자와-권한)
  - [31.4 시작 전에 결정할 값](#314-시작-전에-결정할-값)
  - [31.5 작업 후 만들어지는 결과물](#315-작업-후-만들어지는-결과물)
  - [31.6 단계별 절차](#316-단계별-절차)
  - [31.7 입력값·기본값·허용 범위](#317-입력값기본값허용-범위)
  - [31.8 정상 결과와 완료 판정](#318-정상-결과와-완료-판정)
  - [31.9 중복·동시성·시간초과·응답 유실·부분 실패](#319-중복동시성시간초과응답-유실부분-실패)
  - [31.10 재시도·재시작·재처리·대사·보상·되돌리기](#3110-재시도재시작재처리대사보상되돌리기)
  - [31.11 로그·지표·추적·감사](#3111-로그지표추적감사)
  - [31.12 교육 예제](#3112-교육-예제)
  - [31.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#3113-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [31.14 운영 인계](#3114-운영-인계)
- [32. 개발자가 사용하는 전체 파일 지도](#32-개발자가-사용하는-전체-파일-지도)
- [33. 코드 리뷰 질문](#33-코드-리뷰-질문)
- [34. 배포 전 한 줄 검증 명령](#34-배포-전-한-줄-검증-명령)
- [35. Generator Capability Profile 실전 절차](#35-generator-capability-profile-실전-절차)
  - [35.1 Dry Run 한 줄 명령](#351-dry-run-한-줄-명령)
  - [35.2 적용 결과 확인](#352-적용-결과-확인)
  - [35.3 대표 Profile 조합](#353-대표-profile-조합)
- [36. Source-backed 실행 Property 빠른 참조](#36-source-backed-실행-property-빠른-참조)
- [37. Broker Provider별 개발 계약](#37-broker-provider별-개발-계약)
- [38. TCP 전문 Property와 장애 판정](#38-tcp-전문-property와-장애-판정)

<!-- CPF-TOC:END -->

## 1. 이 매뉴얼의 범위

이 문서는 CPF 자체 제품을 새로 개발하는 절차가 아니라, CPF의 공개 계약과 도구를 이용해 조직 고유 업무를 만드는 절차다.

포함 범위:

- 개발환경과 Build
- Generator와 신규 업무 영역
- API·Application·Domain·Persistence
- Transaction·동시성·멱등성·결과 미확정
- 동일 JVM·원격 호출
- 메시지 브로커·Outbox·Inbox
- 파일·SFTP·외부 REST·전문 연계
- Security·Masking·Audit
- DB Migration·Upgrade·Rollback
- OpenAPI·JavaDoc
- Unit·Contract·Integration·Fault Test
- ADM 연결과 운영 인계

별도 제품과 도구의 절차는 [03 CPF ADM 매뉴얼](03_CPF_ADM매뉴얼.md), [05 CPF 플랫폼 운영 매뉴얼](05_CPF_플랫폼운영매뉴얼.md), [90 CPF Starters 매뉴얼](90_CPF_Starters_매뉴얼.md), [91 CPF Tools 매뉴얼](91_CPF_Tools_매뉴얼.md), [92 CPF Gateway 매뉴얼](92_CPF_Gateway_매뉴얼.md), [95 CPF BZA 매뉴얼](95_CPF_BZA_매뉴얼.md)을 사용한다.

## 2. 시작 전 점검

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}; git -C $repo remote -v; git -C $repo branch --show-current; git -C $repo fetch origin master; git -C $repo rev-parse HEAD; git -C $repo rev-parse origin/master; git -C $repo status --short; java -version; pwsh --version
```

판정:

- `origin/master`의 exact SHA를 개발·검증 기록에 남긴다.
- 다른 작업자의 변경은 삭제·복원하지 않는다.
- 실행하지 않은 Test를 성공으로 기록하지 않는다.
- Source에 없는 API·Class·Property·Route·Permission·상태를 문서나 코드 예제로 만들지 않는다.

## 3. 제품 Architecture와 개발 원칙

### 3.1 기준 Source 계약

| 항목 | 운영 상태 | 개발 시 행동 |
|---|---|---|
| `cpf-core` | 기술 중립 Public API·SPI·식별자·오류·문맥 | Provider SDK 없이 공개 계약에만 의존 |
| `cpf-common` | Code·Calendar·Message·Template 등 업무 공통 정책 | 선택 Runtime은 Profile·Leaf Starter로 조립 |
| 공개 Starter | 38개 Leaf·Aggregate 등록 | Profile·Provider·실제 Consumer에 따라 선택 |
| Versioned Profile | 13개 등록 | Profile Catalog와 `resolvedStarters` 사용 |
| Generator Profile Lock | 제공 | Manifest의 Profile Version·`resolvedStarters`·Version Lock을 Build와 대조 |
| Messaging/TCP/Notification Starter | Kafka·RabbitMQ·JMS·IBM MQ·TCP·ISO8583·Notification 등록 | Provider Binding과 실제 Consumer를 대조 |

### 3.2 개발자가 지켜야 할 경계

```text
API Adapter
  ↓
Application Use Case
  ↓
Domain Rule
  ↓
Port
  ↓
Persistence·Messaging·File·Remote Adapter
```

- Controller는 업무 규칙과 SQL을 직접 소유하지 않는다.
- Application은 사용 사례와 Transaction 경계를 소유한다.
- Domain은 업무 상태와 불변식을 소유한다.
- Adapter는 Provider SDK와 CPF 공개 SPI를 연결한다.
- 다른 모듈의 `internal` Package를 직접 참조하지 않는다.
- ADM·BZA·Gateway는 업무 상태를 직접 변경하지 않고 Owner의 Command 계약을 호출한다.

## 4. 신규 업무 영역 생성

### 4.1 Dry Run

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -DependencyModel root-project -DryRun
```

확인 항목:

- Project 경로와 이름
- Java Package
- System Code
- Port 충돌
- Schema·Table Prefix
- DB Vendor
- 선택 기능
- 생성·수정 대상
- 사용자 수정 영역 보호

### 4.2 Apply 전 결정값

| 결정값 | 예 | 소유자 | 오류 시 |
|---|---|---|---|
| `DomainName` | `payment` | 업무 개발팀 | 영문 규칙·기존 경로 충돌 검사 |
| `SystemCode` | `PAY` | Architecture | 3자리 계약 위반 시 중단 |
| `DatabaseVendor` | `mariadb`·`postgresql`·`oracle` | DBA·개발팀 | 지원 Vendor 외 값 금지 |
| `DependencyModel` | `root-project`·`published-artifact` | Build Owner | Artifact 공급 방식 불일치 시 중단 |
| `Capabilities` | Generator가 허용한 값 | 개발팀 | 미지원 기능을 임의 문자열로 추가 금지 |
| `ProductionProfile` | `Y`·`N` | 운영팀 | 운영 Secret·DB 준비 전 활성화 금지 |

### 4.3 생성 후 필수 확인

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; & (Join-Path $repo 'gradlew.bat') projects; & (Join-Path $repo 'gradlew.bat') :cpf-payment:compileJava; & (Join-Path $repo 'gradlew.bat') :cpf-payment:test
```

- 생성된 `build.gradle`에 실제 필요한 의존성이 있는지 확인한다.
- Generator가 생성한 `resolvedStarters`·Profile Version·Starter Version Lock과 실제 Runtime Classpath를 대조한다.
- 필요한 Starter를 직접 선언했다면 JAR/WAR, Runtime Classpath와 SBOM에서 선택 결과를 확인한다.
- 생성 SQL은 중앙 Vendor Pack 계약과 일치해야 한다.

## 5. 기능 설계 카드

모든 기능은 코딩 전에 아래 항목을 작성한다.

| 항목 | 작성 내용 |
|---|---|
| 기능 ID | 변경되지 않는 추적 ID |
| 업무 결과 | 사용자가 얻게 되는 상태·데이터 |
| Owner Module | 상태와 DB를 소유하는 모듈 |
| Consumer | API·Batch·ADM·BZA·Gateway 등 |
| 입력 | 필수·선택·기본값·범위 |
| 업무 상태 | Source에 존재하는 상태만 |
| Transaction | 시작·Commit·Rollback 경계 |
| 멱등성 | Key 구성, 요청 Hash, Replay 정책 |
| 동시성 | Version·Lock·Lease·Fencing |
| Timeout | 전체 시간 예산과 하위 호출 배분 |
| 결과 미확정 | 판정 기준과 Reconcile |
| 권한 | Permission·Data Scope·Masking |
| 감사 | 요청자·승인자·사유·이전/이후 값 |
| 정상화 | Retry·Restart·Reprocess·Compensation |
| Test | 정상·오류·경계·Fault |
| 운영 인계 | Log·Metric·Trace·ADM 확인점 |

## 6. API 개발

### 6.1 요청 계약

요청에는 필요한 범위에서 다음 식별자를 사용한다.

- `transactionId`
- `operationId`
- `businessKey`
- `idempotencyKey`
- `traceId`
- `expectedVersion`
- `deadline`

실제 Header 이름과 DTO는 Source의 공개 계약을 확인한다. 임의 Alias를 만들지 않는다.

### 6.2 Controller

Controller의 책임:

1. 인증 주체와 권한 문맥을 받는다.
2. 요청 형식과 필수값을 검증한다.
3. Application Use Case를 호출한다.
4. CPF 표준 오류 모델로 응답한다.
5. 업무 규칙·SQL·재시도 Loop를 직접 구현하지 않는다.

### 6.3 응답

응답에는 최소한 다음 판정 정보를 포함한다.

- 최종 또는 운영 상태
- Operation 식별자
- Version
- 결과가 확정되지 않았을 때 조회 방법
- 재시도 가능 여부
- 오류 분류와 사용자 조치

## 7. Application과 Transaction

### 7.1 기본 순서

1. 멱등성 기록을 조회하거나 획득한다.
2. 현재 업무 상태와 Version을 읽는다.
3. 권한·Data Scope·업무 전이 조건을 검증한다.
4. 업무 원장을 변경한다.
5. 외부 효과가 필요하면 Outbox 또는 시도 원장을 기록한다.
6. Audit을 기록한다.
7. Commit 후 비동기 Worker가 외부 효과를 수행한다.
8. 결과를 Operation 조회로 확인한다.

### 7.2 금지

- DB Transaction 안에서 무제한 원격 재시도
- 응답을 못 받았다는 이유로 같은 업무를 새 Key로 재실행
- `UNKNOWN_RESULT`를 실패로 임의 변경
- 일부 대상 성공 후 전체를 처음부터 재실행
- UI나 ADM에서 DB 직접 수정
- Runtime Exception 문자열로 업무 상태 판정

## 8. Domain과 상태 전이

Domain은 다음을 명시한다.

- 허용 상태
- 상태별 허용 Command
- 불변식
- Version 증가 규칙
- 취소·보상 조건
- 재처리 대상 판정
- 최종 상태와 보존 기간

상태 전이 Test:

```text
정상 전이
허용되지 않은 전이
동일 요청 Replay
Expected Version 충돌
동시 요청
삭제·정지 상태 접근
부분 성공 후 재처리
```

## 9. Persistence와 DB

### 9.1 공식 Vendor

- MariaDB
- PostgreSQL
- Oracle

### 9.2 개발 순서

```text
업무 요구와 Data Model
→ Canonical Metadata
→ Generator Query
→ Vendor Pack
→ Migration·Rollback
→ Mapper·Repository
→ Service·API
→ Test·Evidence
```

Vendor SQL부터 직접 수정하지 않는다. 한 Vendor만 수정하고 나머지를 미루지 않는다.

### 9.3 Transaction·Lock

| 상황 | 권장 계약 |
|---|---|
| 단일 Row 상태 변경 | `expectedVersion` 기반 낙관적 잠금 |
| 작업 소유권 | Lease·Claim·Fencing |
| 중복 요청 | Idempotency Ledger |
| 외부 발행 | Outbox |
| 외부 수신 | Inbox·Dedup |
| 장시간 처리 | 짧은 DB Transaction + 상태 원장 |
| 다중 대상 | 대상별 상태와 집계 상태 분리 |

### 9.4 Migration

각 변경은 다음을 제공한다.

- 설치 전제 조건
- Upgrade Script
- Backfill
- Index·Lock 영향
- Mixed Version 허용 범위
- Rollback 또는 Forward Recovery
- 재적용과 다른 Hash 충돌 판정
- Vendor 3종 확인 명령

## 10. 동일 JVM과 원격 호출

### 10.1 계약 동등성

Local과 Remote 구현은 다음 의미가 같아야 한다.

- 입력·오류
- 권한 문맥
- Deadline
- 멱등성
- Version
- 결과 미확정
- Audit
- Trace

### 10.2 Timeout Budget

```text
전체 요청 Deadline
- Controller·Queue 지연
- 하위 서비스 호출
- DB
- 결과 기록
- 응답 여유
```

하위 호출 Timeout의 합이 전체 Deadline을 초과하지 않도록 한다. Timeout 후 실제 효과가 발생할 수 있으면 `UNKNOWN_RESULT`로 판정하고 조회·대사 절차를 제공한다.

## 11. 비동기 메시지 처리

상위 매뉴얼에서는 메시지 브로커라는 기능명으로 설명하고, Build·Config·운영 절차에서는 실제 Provider를 명시한다.

### 11.1 공통 흐름

1. 업무 Transaction에서 업무 원장과 Outbox를 함께 Commit한다.
2. Worker가 Claim·Lease·Fencing으로 전송 대상을 획득한다.
3. Provider Adapter가 메시지를 발행한다.
4. ACK 또는 결과 미확정을 기록한다.
5. Consumer는 Inbox·Dedup 후 업무 Command를 실행한다.
6. 실패는 제한된 Retry와 Dead Letter 정책으로 분류한다.
7. 운영자는 Lag·Backlog·Replay·Audit을 확인한다.

### 11.2 기준 Source 상태

- Kafka Starter 프로젝트는 등록돼 있다.
- Kafka·RabbitMQ·JMS·IBM MQ Provider는 공통 Reliability Ledger와 실제 Consumer를 연결하고 Consume·Retry·DLQ·Rebalance·Process Kill 시나리오를 실행한다.
- RabbitMQ·Jakarta JMS·IBM MQ Starter는 제품 기능지만 `settings.gradle` 기준 미등록이다.
- 제공 Provider를 제공되는 의존성으로 안내하지 않는다.

### 11.3 Multi-provider 목표

복수 Provider 사용 시:

- Named Binding
- Default Binding 최대 하나
- Destination별 Routing
- 이름 없는 Client가 복수이면 Fail-closed
- Correlation·멱등성·결과 미확정 유지

이 계약은 제품 기능이며 실제 Source가 구현되기 전에는 `제공`이다.

## 12. 파일·SFTP·외부 REST·전문

### 12.1 파일·Attachment 공통

- 허용 확장자·크기·MIME
- 저장 위치와 Ownership
- Attachment ID와 업무 원장의 참조 관계
- 업로드 주체·다운로드 권한·Data Scope·Masking
- Checksum
- 암호화·가림·Virus Policy
- Partial Upload
- 중복 파일
- 보존·삭제·법적 보류·Audit

Attachment는 업무 원장의 상태를 대신하지 않는다. 파일 저장은 성공했지만 업무 등록이 실패한 경우 고아 파일을 식별하고, 업무 등록은 성공했지만 파일 전송 결과가 불명확한 경우 Attachment Ledger와 저장소 Checksum을 대사한다.

### 12.2 SFTP

제품 Runtime이 제공해야 할 범위:

- Upload·Download·List·Move·Delete
- Atomic Rename
- Resume
- Checksum
- Transfer Ledger
- Credential Expiry
- Network Loss
- Reconcile

Docker Fixture의 전송 성공은 제품 Starter와 실제 Consumer 검증을 대신하지 않는다.

### 12.3 외부 REST

- Base URL과 SSRF 허용 목록
- TLS·mTLS
- Credential·Secret Provider
- Connect·Read·Overall Timeout
- Retry 대상
- Circuit Breaker
- 요청·응답 가림
- 응답 유실 후 결과 조회

### 12.4 TCP·ISO8583

`cpf-starter-integration-tcp`, Fixed-length Core/Starter와 `cpf-starter-integration-iso8583`을 사용해 Client·Server, Framing, Encoding, Correlation, Heartbeat, Half-open, TLS, 결과 미확정과 Reconcile을 구성한다.

## 13. Security·Masking·Audit

각 API·Command는 다음을 정의한다.

| 항목 | 개발 기준 |
|---|---|
| 인증 | Session·Token·Service Identity 중 실제 Runtime |
| Permission | 기능 조치 단위 |
| Data Scope | 조직·업무·소유자 범위 |
| Masking | 화면·API·Log·Export 일관성 |
| Reason | 위험 조치 필수 사유 |
| Approval | 요청자·승인자 분리, 만료·정책 버전 |
| Audit | 누가·언제·무엇을·왜·이전/이후 값 |
| Secret | Source·Config·Log에 원문 저장 금지 |

권한이 없을 때 단순히 버튼만 숨기지 않는다. Backend에서 같은 Permission과 Scope를 검증한다.

## 14. Starter 선택

### 14.1 Starter 선택 예

```groovy
implementation project(':cpf-starter-security')
implementation project(':cpf-starter-observability')
```

게시 Artifact를 사용하는 경우 BOM은 Version만 정렬한다. BOM을 추가했다고 기능이 포함되는 것은 아니다.

### 14.2 선택 검증

- Build Dependency
- Runtime Classpath
- AutoConfiguration Report
- Property Binding
- 실행 Artifact
- POM·BOM
- SBOM
- Optional Removal Compile·Runtime
- Generator Manifest

### 14.3 현재 금지

- 구현되지 않은 Profile 이름을 Gradle 좌표로 사용
- `all`·`full`·`everything` 성격의 Mega Starter
- 두 Provider가 이름 없이 동시에 활성화
- 업무 정책을 범용 Starter에 포함
- Starter가 업무 원장·승인·보상을 소유

## 15. OpenAPI·JavaDoc

- Public API만 문서화한다.
- Internal Class를 사용 예로 노출하지 않는다.
- 오류·권한·Idempotency·Version·Deadline을 명시한다.
- Generated Client는 OpenAPI와 같은 Commit에서 생성한다.
- Breaking Change는 Version·Migration·Compatibility 범위를 기록한다.
- Build 결과의 OpenAPI·JavaDoc Artifact를 Hash로 확인한다.

## 16. Test 전략

| 단계 | 필수 범위 |
|---|---|
| Unit | Domain 불변식·상태 전이·값 검증 |
| Contract | Public API·SPI·오류·Property |
| Integration | DB·Provider·AutoConfiguration |
| Negative | 권한·잘못된 설정·중복·충돌 |
| Multi-instance | Claim·Lease·Fencing·Duplicate |
| Fault | Timeout·Connection Loss·Process Kill |
| Reconcile | 결과 미확정·부분 성공·재처리 |
| Optional Removal | 미선택 Starter 제거 Compile·Runtime |
| Supply Chain | POM·BOM·SBOM·Checksum·Provenance |

직접 실행한 명령, exact SHA, Tool·Image Version, Exit Code와 Log Hash를 남긴다.

## 17. ADM 연결과 운영 인계

업무 개발자는 ADM 자체를 개발하지 않는다. 다음 계약을 제공해 ADM에서 업무 상태를 이용할 수 있게 한다.

- Query: 목록·상세·상태·Version
- Command: 재처리·정지·재개·대사 등 허용 조치
- Permission·Data Scope
- Reason·Approval
- Expected Version
- Idempotency
- Operation 조회
- Audit
- Timeout·결과 미확정 Reconcile

인계 항목:

```text
기능 ID
Owner Module
API·Port
Permission
상태
Operation 조회 방법
Log·Metric·Trace
DB 대사 Query
재시도·재처리 조건
Rollback·보상
담당자
```

## 18. EDU 실습 구조

각 실습은 다음을 갖는다.

1. 선행 과정과 Source 경로
2. 전체 Config·Migration
3. 실행 명령
4. 정상 요청·응답·DB·Audit
5. 오류 재현
6. Fault Injection
7. 재시도·대사·정상화
8. ADM 확인
9. Test
10. CPF 관리 영역과 업무 개발팀 수정 영역


### 18.1 기준 Source에서 확인한 EDU 범위

- `EDU-DEV-05`는 실제 Handler, 필수 입력, JDBC Command Consumer Binding과 업무 원장 계약이 확인됐다.
- 공통 EDU 실행 API와 장애 지점 계약은 개발자 매뉴얼의 기존 정본에서 확인됐다.
- 전체 EDU의 Handler·Resource Contract·실제 Consumer·Test Assertion·DB·Message Broker·File·외부 연계·ADM Evidence를 같은 Commit에서 전수 실행한 결과는 확인되지 않았다.
- 따라서 개별 EDU는 Source·Consumer·Test·Runtime을 확인한 단위로 판정하고, 전체 EDU를 일괄 `완료`로 표시하지 않는다.
- 기능 Catalog·Handler·실제 Consumer·Config·Migration·Test를 같은 기준 Commit에서 연결한다.

### 18.2 온라인·연계 EDU 45개 전수표

아래 표는 교육 식별자를 빠뜨리지 않기 위한 전수 목록이다. **표에 존재한다는 사실만으로 실행 성공을 뜻하지 않는다.** 실행 전 `GET /api/reference/edu-capabilities`에서 해당 ID, `requiredFields`, `requiredRole`, `failurePoints`, `sourcePath`, `tests`를 확인하고, 실행 결과와 DB·Target·Outbox·Audit를 대사한다.

| 교육 ID | 확인할 기능 | 활성 조건 | 실행 안내 | 완료 판정 |
|---|---|---|---|---|
| `EDU-DEV-01` | 생성 도구 기반 신규 업무 영역 생성 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-02` | 권한·범위가 적용된 목록·상세 조회 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-03` | 등록·수정·상태 변경과 Audit | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-04` | 동시 수정과 Expected Version 충돌 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-05` | 지급 등록 멱등성·응답 유실·결과 대사 | 기본 기능 또는 기능 정의의 `configurationKey` | Handler·JDBC Command Consumer Binding 정적 확인 | 제공 |
| `EDU-DEV-06` | Same-JVM·Remote 호출 동등성 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-07` | 메시지 Outbox·Inbox·중복 소비·재처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-08` | 파일 Upload·검사·Attachment·Download | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-09` | 외부 REST 조회와 UNKNOWN_RESULT | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-10` | 고정길이 전문 기관 이체 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-11` | Permission·Data Scope·Masking·Audit | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-12` | Cache·Feature Flag·Secret Rotation | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-13` | Notification·비동기 Export·Download Audit | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-14` | Oracle·PostgreSQL·MariaDB DB Migration 의미 일치 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-15` | 업무 장애 주입·정상화·운영 인계 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-16` | 대용량 목록 검색·정렬·Cursor Pagination | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-17` | 대량 등록 Preview·부분 오류·재업로드 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-18` | 논리 삭제·복원·Retention 만료 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-19` | 기준일·유효기간이 있는 기준정보 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-20` | 다단계 업무 State Machine과 취소·재개 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-21` | Transactional Outbox 게시 지연·재시작 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-22` | 서비스 간 Saga Compensation·수동 확정 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-23` | 공통 Validation·Error Contract·OpenAPI 일치 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-24` | 장시간 비동기 Operation 조회·취소 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-25` | Webhook 서명·재전송·Replay 방지 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-26` | SFTP 수신·송신·완료 파일 원자 처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-27` | SOAP·XML 외부기관 연계와 장애 처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-28` | 대용량 Multipart Upload·중단 재개 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-29` | 악성코드 검사·격리·승인 해제 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-30` | Object Storage 보존·Version·Legal Hold | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-31` | 다중 채널 Notification 선호·Retry·대체 채널 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-32` | 개인정보 암호화·Tokenization·Key Rotation | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-33` | 인증 Token 만료·갱신·폐기·Session 강제 종료 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-34` | API Rate Limit·호출 주체별 Quota·초과 처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-35` | Feature Flag Canary·Kill Switch·사용자 Segment | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-36` | Cache Stampede·Negative Cache·원본 정합성 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-37` | 온라인 Distributed Lease·Fencing·소유권 상실 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-38` | Multi-tenant 격리·설정·Data Scope | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-39` | 업무일자·Timezone·Holiday Calendar | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-40` | 금액·통화·Rounding·환율 Version | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-41` | Audit Evidence Export·무결성 Hash·검증 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-42` | Log·Metric·Trace Correlation과 Sampling | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-43` | API Version 전환·하위 호환·폐기 | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-44` | Event Schema 진화·Compatibility·DLQ | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-DEV-45` | 조회 Model·Search Index Eventual Consistency | 기본 기능 또는 기능 정의의 `configurationKey` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |

판정 규칙:

3. 실행 API가 2xx여도 업무 원장·Target·Outbox·Audit가 같은 `operationId`를 가리키지 않으면 실패로 판정한다.
4. `UNKNOWN_RESULT`와 `PARTIAL_SUCCESS`는 신규 요청을 만들지 않고 기존 Operation에서 Reconciliation한다.

## 19. 완료 점검표

- [ ] 기능 Owner와 Consumer가 명확하다.
- [ ] 상태·권한·Data Scope·Audit이 Source와 일치한다.
- [ ] MariaDB·PostgreSQL·Oracle 영향이 반영됐다.
- [ ] 멱등성·동시성·Timeout·응답 유실을 시험했다.
- [ ] 결과 미확정과 부분 성공의 대사 절차가 있다.
- [ ] Local·Remote 계약이 일치한다.
- [ ] 선택 Starter와 미선택 제거를 확인했다.
- [ ] OpenAPI·JavaDoc·Generated Client가 같은 Commit이다.
- [ ] ADM에서 Query·Command·Audit을 확인했다.
- [ ] 운영 인계와 Rollback 기준을 전달했다.
- [ ] 실행 명령·환경·Exit Code·정상 결과·정제된 증적을 기록했다.

## 20. 배포 인계 절차

개발 완료 표시는 Source 작성 시점이 아니라 운영 인계가 검수된 시점에 판단한다.

### 20.1 인계 묶음

| 항목 | 필수 내용 |
|---|---|
| Source 기준 | Repository·Branch·exact Commit |
| Artifact | 이름·Version·SHA-256·SBOM |
| API | OpenAPI·오류·권한·Timeout·Idempotency |
| DB | Vendor별 Migration·Verify·Rollback/Recovery |
| Config | Key·환경변수·Default·필수·Secret·재기동 |
| Messaging | Binding·Destination·Schema·Retry·DLQ·대사 |
| File·외부 연계 | Endpoint·Checksum·Timeout·Receipt·보존 |
| 관측 | Log·Metric·Trace·Audit와 상관 식별자 |
| 운영 조치 | 조회·재시도·재처리·Reconcile·Rollback |
| 검증 | 실행 명령·환경·Exit Code·Sanitized Evidence |

### 20.2 배포 전 확인 명령

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; git -C $repo rev-parse HEAD; git -C $repo status --short; & (Join-Path $repo 'gradlew.bat') clean test assemble qualityGate --no-daemon
```

실행하지 않은 명령을 성공으로 기록하지 않는다. 실패하면 최초 실패 Task·관련 Source·재현 조건을 기록한다.

## 21. 오류·부분 실패·정상화 결정표

| 상황 | 금지 | 우선 행동 | 종료 판정 |
|---|---|---|---|
| Version 충돌 | 최신 Row 덮어쓰기 | 운영 상태 재조회·의도 병합 | Expected Version 일치 |
| DB Commit 후 응답 유실 | 신규 업무 생성 | Idempotency·업무 원장·Operation 조회 | 실제 결과 확정 |
| 외부 전송 후 Timeout | 무조건 재전송 | Attempt·Receipt·상대 조회 | 중복 없음·결과 확정 |
| 일부 대상 성공 | 전체 재실행 | 성공 대상 유지·실패 대상만 처리 | Target별 결과 대사 |
| Broker Consumer 실패 | Message 삭제 | Retry·DLQ·Inbox·업무 원장 확인 | Backlog·DLQ·업무 대사 |
| File 일부 처리 | 원본 덮어쓰기 | Checksum·Checkpoint·행별 결과 확인 | 건수·금액·Hash 대사 |
| Config 부분 적용 | 신규 변경 겹침 | Target Version·Checksum 수집 | Drift 0 |

## 22. EDU 전수 검증 규칙

각 EDU는 다음 일곱 근거를 연결해 실행·복구·업무 전환 절차를 완결한다.

```text
Definition·Resource Contract
→ Handler·Owner Package
→ 실제 Consumer Binding
→ Config·Migration
→ 정상·오류·Fault Test
→ Runtime Operation·Target·Audit
→ ADM·Log·Metric·Trace Evidence
```

### 22.1 실행 점검 예

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\start-cpf-local.ps1') -Mode integration; Write-Host 'EDU API 호출은 Capability 조회 결과의 Port·Role·필수 입력을 사용합니다.'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\status-cpf-local.ps1')
```

EDU 종료 후 업무 원장·Operation·Target·Outbox·Audit의 식별자와 건수·금액·Hash를 대사한다.
## 23. 종단간 예제: 지급 신청 업무 Slice

### 23.1 이 기능으로 만드는 업무 결과

지급 신청을 등록하고 승인 후 외부 기관에 전달하며, 응답 유실 시 실제 처리 결과를 대사해 완료 또는 보상 상태로 확정한다.

### 23.2 선택 기준과 사용하지 말아야 할 경우

- 상태와 금액을 가진 Command 업무, 승인, 외부 부수 효과가 있으면 사용한다.
- 단순 읽기 전용 기능이나 외부 제품이 상태 정본인 경우에는 이 예제를 그대로 적용하지 않는다.

### 23.3 주 사용자와 권한

업무 개발자는 Source·Migration·Test를 작성한다. 보안 담당자는 Permission·Masking을 검토하고, 운영 담당자는 ADM 조회·대사·재처리 권한을 가진다.

### 23.4 시작 전에 결정할 값

`paymentId`, `businessKey`, 상태표, 금액·통화, 요청자, 승인 정책, Idempotency Key 형식, Version, Timeout Budget, 대사 기준, 보존 기간을 결정한다.

### 23.5 작업 후 만들어지는 결과물

- Domain Entity와 상태 전이
- Command/Query API와 OpenAPI
- Vendor별 Migration·Rollback·대사 SQL
- Idempotency·Attempt·Outbox/Inbox 원장
- Unit·Contract·Integration·Fault Test
- ADM 운영 인계표

### 23.6 단계별 절차

1. Generator를 Dry Run해 Module·Package·SystemCode·DB Vendor·Starter 선택 충돌을 확인한다.
2. 생성 결과에서 API, Application, Domain, Persistence, Config, Test 영역을 확인한다.
3. `Payment` 상태표와 허용 전이를 Domain에 구현한다.
4. 등록 Command가 `businessKey`, `idempotencyKey`, `expectedVersion`, `requestReason`을 받도록 한다.
5. Application Service에서 권한, 상태, 금액, 중복을 검증한다.
6. 한 트랜잭션에서 업무 원장, Idempotency 결과, Audit, Outbox를 기록한다.
7. BZA 승인 완료 후 외부 기관 Attempt를 생성하고 전송한다.
8. 응답 수신 시 Attempt와 업무 상태를 같은 Correlation ID로 갱신한다.
9. 응답 유실이면 기관 조회와 Attempt Ledger를 대사한다.
10. 정상·중복·409 충돌·Timeout·응답 유실·부분 실패 Test를 실행한다.
11. ADM에서 거래·Attempt·Audit·UNKNOWN_RESULT를 확인한다.
12. Artifact·Config·Migration·Runbook을 운영팀에 인계한다.

### 23.7 입력값·기본값·허용 범위

| 입력 | 필수 | 규칙 | 기본값 | 오류 |
|---|---|---|---|---|
| `businessKey` | 예 | 업무 범위에서 고유, 변경 금지 | 없음 | 중복 또는 형식 오류 |
| `idempotencyKey` | 예 | 같은 의도에는 같은 값, 다른 본문 재사용 금지 | 없음 | Request Hash 충돌 |
| `expectedVersion` | 변경 시 예 | 0 이상의 현재 Version | 신규 0 | 409 충돌 |
| `amount` | 예 | 0보다 큼, 통화 소수 자릿수 준수 | 없음 | Validation 오류 |
| `requestReason` | 위험 조치 시 예 | 공백 제외 최소 길이, 민감정보 금지 | 없음 | 사유 부족 |
| `timeoutBudget` | 예 | 전체 예산 안에서 DB·원격 단계 배분 | 환경 정책 | Timeout 분류 |

### 23.8 정상 결과와 완료 판정

- API 응답의 `paymentId`, `operationId`, `version`, `status`가 DB 원장과 일치한다.
- Outbox, Attempt, Audit가 같은 `transactionId`·`traceId`를 가진다.
- 중복 요청은 새 지급을 만들지 않고 기존 결과를 반환한다.
- ADM에서 상태·시도·승인·감사를 같은 업무 키로 조회할 수 있다.

### 23.9 중복·동시성·시간초과·응답 유실·부분 실패

- 동시에 같은 Version을 변경하면 한 요청만 성공하고 나머지는 409로 거부한다.
- 같은 Idempotency Key에 다른 본문을 보내면 충돌로 거부한다.
- 외부 전송 전 Timeout은 실패로 재시도할 수 있지만, 전송 후 Timeout은 `UNKNOWN_RESULT`로 둔다.
- 여러 대상 중 일부만 성공하면 성공 대상을 다시 호출하지 않는다.
- DB Commit 후 응답 유실은 같은 Command를 새 키로 보내지 않고 Operation을 조회한다.

### 23.10 재시도·재시작·재처리·대사·보상·되돌리기

1. `operationId`와 `attemptId`로 기존 결과를 조회한다.
2. 내부 DB, Outbox, Broker, 외부 기관 상태를 순서대로 대사한다.
3. 일시적 실패이고 부수 효과가 없으면 같은 Idempotency Key로 Retry한다.
4. 부수 효과가 확정됐으면 업무 상태만 전진 수정한다.
5. 반대 거래가 필요한 경우 별도 Compensation Command를 생성한다.
6. Schema Rollback이 불가능하면 이전 Application 호환을 유지하고 Forward Fix한다.

### 23.11 로그·지표·추적·감사

- Log: `transactionId`, `traceId`, `paymentId`, `operationId`, `attemptId`, `status`, `errorCode`
- Metric: 등록 건수, 중복 반환, 409, Timeout 단계, UNKNOWN_RESULT 경과시간
- Trace: Controller → Application → DB → Outbox/외부 Adapter
- Audit: 수행자, Permission, Data Scope, 사유, 승인 ID, 변경 전후 Version

### 23.12 교육 예제

`EDU-DEV-05`를 기준으로 정상 등록, 같은 키 재호출, 응답 유실, Reconcile을 실행한다. Capability 조회에서 필수 입력과 허용 장애 지점을 읽고, 결과의 Operation·Audit·Target·Outbox를 확인한다.

### 23.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무명, 상태, 금액 규칙, Permission, 승인 정책, 외부 전문과 대사 기준은 조직이 바꾼다. 표준 Header, Error Contract, Idempotency·Attempt·Audit 구조와 Local/Remote 계약은 CPF 영역으로 유지한다.

### 23.14 운영 인계

- 기능 설계 카드와 상태표
- API·OpenAPI·JavaDoc
- DB Migration·Rollback·대사 SQL
- Config·Secret 참조
- Dashboard·Alert 기준
- Retry·Reconcile·Compensation Runbook
- Artifact·Checksum·호환 범위


## 24. 조건 검색·목록·상세 Query 개발

### 24.1 이 기능으로 만드는 업무 결과

권한과 데이터 범위가 적용된 검색·목록·상세 API를 만들고 동일 조건에서 API·화면·DB 결과가 일치하도록 한다.

### 24.2 선택 기준과 사용하지 말아야 할 경우

- 목록·상세 조회, 대량 검색, Cursor Pagination이 필요할 때 사용한다.
- 강한 일관성이 필요한 Command 직후 판정은 조회 Replica나 Cache에 의존하지 않는다.

### 24.3 주 사용자와 권한

조회 개발자에게 API 개발 권한이 필요하고, 개인정보 원문 조회는 별도 Permission·사유·Audit를 요구한다.

### 24.4 시작 전에 결정할 값

검색 필드, 기본 기간, 정렬 허용 목록, Page Size 상한, Cursor 구성, Data Scope, Masking 필드, Cache 허용 여부를 정한다.

### 24.5 작업 후 만들어지는 결과물

Query DTO, 조회 Port, SQL/Mapper, Cursor Codec, OpenAPI, 권한 Test, 성능 Test, ADM 검색 연결.

### 24.6 단계별 절차

1. 검색 필드별 의미와 조합 규칙을 표로 작성한다.
2. 기본 기간과 최대 조회 기간을 고정한다.
3. 정렬 필드를 Allowlist로 제한한다.
4. Data Scope 조건을 서버 SQL에 적용한다.
5. 목록 Projection과 상세 DTO를 분리한다.
6. Offset 또는 Cursor 방식을 선택하고 Cursor에는 정렬 Key와 Tie-breaker를 포함한다.
7. Masking은 DTO 조립 단계에서 Permission과 함께 적용한다.
8. 빈 결과, 잘못된 Cursor, 범위 초과, 권한 없음, Timeout을 Test한다.
9. ADM 화면의 검색 기본값·열·상세값과 API를 대조한다.

### 24.7 입력값·기본값·허용 범위

| 항목 | 기본값 | 허용 범위 | 주의 |
|---|---|---|---|
| 조회 기간 | 최근 1일 | 업무별 최대 기간 | 무제한 전체 조회 금지 |
| Page Size | 20 | 1~200 | Export는 별도 비동기 작업 |
| Sort | 생성시각 내림차순 | Allowlist | 사용자 입력 SQL 결합 금지 |
| Cursor | 없음 | 서버 발급 값 | 변조·만료 검증 |
| 원문 보기 | Masking | 별도 Permission | 사유·Audit 필수 |

### 24.8 정상 결과와 완료 판정

- 같은 검색 조건과 같은 Source Version에서 안정된 정렬 결과가 나온다.
- Page 간 중복·누락이 없다.
- 권한 밖 행과 원문 필드가 노출되지 않는다.
- Query 지연·행 수·Cache Hit가 Metric에 기록된다.

### 24.9 중복·동시성·시간초과·응답 유실·부분 실패

- 대량 조회는 Timeout 전에 범위를 줄이거나 비동기 Export로 전환한다.
- Cursor가 만료되거나 데이터가 변경되면 첫 Page부터 재조회한다.
- Cache가 오래됐으면 정본 DB 조회로 전환하고 Cache Version을 갱신한다.
- 일부 외부 보강 데이터가 실패하면 기본 업무 데이터와 보강 실패를 구분한다.

### 24.10 재시도·재시작·재처리·대사·보상·되돌리기

Query는 상태를 변경하지 않으므로 Retry가 가능하다. 다만 동일 Snapshot이 필요한 보고서는 Source Version 또는 기준시각을 고정한다. Export는 Operation으로 생성하고 응답 유실 시 Operation을 조회한다.

### 24.11 로그·지표·추적·감사

검색 조건 Hash, 행 수, 지연, DB·Cache Source, Masking 여부, Data Scope, Export Operation을 기록한다. 개인정보 검색어 원문은 Log에 남기지 않는다.

### 24.12 교육 예제

`EDU-DEV-02`, `EDU-DEV-16`에서 권한 조회, Cursor Pagination, 대량 검색을 실행한다.

### 24.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 검색 필드·열·상세 DTO는 조직 영역이다. Header, Error, Data Scope, Masking, Cursor 안전성은 CPF 계약을 따른다.

### 24.14 운영 인계

검색 사양, 기본값, Index 근거, 성능 기준, Permission, Masking, ADM 열·상세 매핑, Export 정책을 인계한다.


## 25. 상태 변경 Command와 낙관적 동시성

### 25.1 이 기능으로 만드는 업무 결과

등록·수정·정지·재개·취소 같은 상태 변경을 명시적 Command와 Version 검증으로 처리한다.

### 25.2 선택 기준과 사용하지 말아야 할 경우

- 업무 상태가 있고 동시에 여러 사용자가 수정할 수 있으면 사용한다.
- 단순 설정 조회나 통계에는 Command 상태 모델을 적용하지 않는다.

### 25.3 주 사용자와 권한

Command 실행 Permission과 Data Scope가 필요하며 위험 조치는 Reason·Approval을 추가한다.

### 25.4 시작 전에 결정할 값

상태표, 허용 전이, Version 증가 규칙, 취소 가능 시점, 불변 필드, 승인 대상, Audit 필드를 정한다.

### 25.5 작업 후 만들어지는 결과물

Command DTO, Domain Method, Application Service, Repository Update 조건, 상태 전이 Test, 409 Error Contract.

### 25.6 단계별 절차

1. 상태와 이벤트를 표로 작성한다.
2. 모든 변경 API에 현재 `expectedVersion`을 받는다.
3. Domain Method가 운영 상태와 요청 상태의 전이를 검증한다.
4. SQL Update 조건에 ID와 Version을 함께 사용한다.
5. 영향 행이 0이면 최신 상태를 재조회해 Not Found와 Conflict를 구분한다.
6. 성공 시 Version을 1 증가시키고 Audit에 전후 값을 기록한다.
7. 409 응답에는 최신 Version을 직접 덮어쓰지 않고 재조회 안내를 제공한다.
8. 동일 Version 경쟁 Test와 상태별 금지 Test를 실행한다.

### 25.7 입력값·기본값·허용 범위

| 입력 | 규칙 | 오류 |
|---|---|---|
| `expectedVersion` | 현재 상세 조회의 Version | 오래된 값은 409 |
| `targetStatus` | 허용 전이표에 존재 | 금지 전이는 Validation/Conflict |
| `reason` | 위험 조치 시 필수 | 누락은 400/403 |
| `approvalId` | 승인 정책 대상 시 필수 | 만료·불일치 거부 |

### 25.8 정상 결과와 완료 판정

한 요청만 Version을 증가시키고, 경쟁 요청은 409를 받는다. 상태·Version·Audit·Outbox가 같은 Transaction 결과를 가리킨다.

### 25.9 중복·동시성·시간초과·응답 유실·부분 실패

동시 수정, 승인 만료, 상태 선행 조건 불일치, DB Deadlock, 응답 유실을 구분한다. Conflict를 자동 Retry하지 않는다.

### 25.10 재시도·재시작·재처리·대사·보상·되돌리기

사용자가 최신 상세를 다시 읽고 변경 내용을 재적용한다. DB Deadlock은 제한된 횟수만 동일 Command로 재시도한다. 응답 유실은 Operation 또는 Idempotency 결과를 먼저 조회한다.

### 25.11 로그·지표·추적·감사

Conflict 횟수, 상태 전이 실패, Version, 수행자, 승인 ID, Transaction ID를 기록한다.

### 25.12 교육 예제

`EDU-DEV-03`, `EDU-DEV-04`, `EDU-DEV-20`을 실행한다.

### 25.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 상태표와 승인 조건은 조직 영역이다. Version·Conflict·Audit·Error 의미는 CPF 계약을 유지한다.

### 25.14 운영 인계

상태표, API, 권한, 승인, Conflict 처리, 대사 SQL, ADM 버튼 활성 조건을 전달한다.


## 26. Local·Remote Facade 동등성

### 26.1 이 기능으로 만드는 업무 결과

같은 업무 기능을 같은 JVM 내부 호출과 분리 서비스 원격 호출에서 같은 계약과 실패 의미로 사용한다.

### 26.2 선택 기준과 사용하지 말아야 할 경우

- 배포 형태가 환경이나 규모에 따라 달라질 수 있으면 적용한다.
- 내부 구현 Class를 직접 호출해도 되는 단일 모듈 내부 로직에는 공개 Facade를 남용하지 않는다.

### 26.3 주 사용자와 권한

호출자는 Public API만 의존한다. Remote 호출은 Service Identity와 대상 Permission을 추가로 검증한다.

### 26.4 시작 전에 결정할 값

공통 DTO, Validation, Error Taxonomy, Timeout Budget, Header, Idempotency, Version, Serialization 호환 범위를 정한다.

### 26.5 작업 후 만들어지는 결과물

Public Facade, Local Adapter, Remote Client Adapter, OpenAPI, Contract Test, Mixed Version Test.

### 26.6 단계별 절차

1. Public API와 DTO를 Provider 독립 Package에 둔다.
2. Local Adapter가 Application Service를 호출한다.
3. Remote Adapter가 같은 DTO를 직렬화하고 표준 Header를 전달한다.
4. Remote Error를 Public Error로 변환한다.
5. 전체 Timeout Budget을 연결·전송·서버·응답 단계로 나눈다.
6. Local/Remote Contract Test에 같은 입력 Corpus를 적용한다.
7. 한 환경에서 Local과 Remote 결과 Hash를 비교한다.
8. 배포 전환 시 업무 Source 변경이 없는지 확인한다.

### 26.7 입력값·기본값·허용 범위

Header에는 Request/Transaction/Trace/Actor/Role/Data Scope/Idempotency/Version 정보를 전달한다. Timeout은 호출자가 전체 예산을 소유하며 하위 호출의 합이 이를 넘지 않는다.

### 26.8 정상 결과와 완료 판정

정상 응답·Validation·권한·Conflict·Not Found·Timeout의 의미가 Local과 Remote에서 같다. Remote만 추가되는 네트워크 오류는 별도 분류된다.

### 26.9 중복·동시성·시간초과·응답 유실·부분 실패

연결 실패와 서버 처리 후 응답 유실을 구분한다. 비멱등 Command는 네트워크 Timeout만으로 자동 재전송하지 않는다. Mixed Version에서 알 수 없는 필드와 상태의 처리 정책을 적용한다.

### 26.10 재시도·재시작·재처리·대사·보상·되돌리기

Query는 정책에 따라 재시도한다. Command는 Idempotency 결과·Operation을 조회한다. 호환 불가 Version은 배포를 중단하고 이전 Client 또는 Server로 Rollback한다.

### 26.11 로그·지표·추적·감사

호출 방식, 대상 서비스, 단계별 지연, Error Mapping, Attempt ID, Trace Context를 기록한다.

### 26.12 교육 예제

`EDU-DEV-06`, `EDU-DEV-30`, `EDU-DEV-31`에서 Local/Remote·Mixed Version을 검증한다.

### 26.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 DTO와 상태는 조직 영역이다. 표준 Header·Error·Timeout·Idempotency·Serialization 계약은 CPF가 유지한다.

### 26.14 운영 인계

API Version, 호환 Matrix, Timeout, Retry, Service Identity, Rollback 조건을 인계한다.


## 27. 메시지 Outbox·Inbox·DLQ

### 27.1 이 기능으로 만드는 업무 결과

업무 트랜잭션과 이벤트 발행을 일치시키고 중복 전달·지연·Poison Message를 안전하게 처리한다.

### 27.2 선택 기준과 사용하지 말아야 할 경우

- 서비스 간 비동기 결합과 재처리가 필요하면 사용한다.
- 즉시 응답이 필수이고 단일 Transaction으로 끝나는 기능은 동기 호출을 우선한다.

### 27.3 주 사용자와 권한

생산·소비 서비스 계정과 Topic/Queue ACL이 필요하다. DLQ Replay는 별도 Permission·사유·승인을 요구한다.

### 27.4 시작 전에 결정할 값

Event Type·Version, Key, Ordering 범위, Payload, PII 분류, Retention, Retry, DLQ, Replay, Consumer Idempotency를 정한다.

### 27.5 작업 후 만들어지는 결과물

Envelope, Outbox/Inbox/DLQ Migration, Publisher, Consumer, Schema, Metric, Replay Runbook, Contract Test.

### 27.6 단계별 절차

1. 업무 상태 변경과 Outbox INSERT를 같은 Transaction으로 묶는다.
2. Envelope에 Message ID, Event Type/Version, Business Key, Occurred At, Trace를 기록한다.
3. Publisher가 미발행 Outbox를 Claim하고 Provider로 전송한다.
4. ACK를 받으면 발행 상태와 Attempt를 갱신한다.
5. Consumer가 Inbox에서 Message ID를 선점한다.
6. 업무 처리와 Inbox 결과를 같은 Transaction으로 Commit한다.
7. Retry 한도를 넘거나 Payload가 유효하지 않으면 DLQ로 이동한다.
8. Replay 전 원인 수정, 대상 Query, 건수 Preview, 승인을 수행한다.
9. Producer·Broker·Consumer·Audit를 Message ID로 대사한다.

### 27.7 입력값·기본값·허용 범위

| 항목 | 규칙 |
|---|---|
| Message ID | 전역 고유, 재전달에도 유지 |
| Event Version | 호환 규칙과 Schema 포함 |
| Key | 순서 보장이 필요한 업무 범위 |
| Payload | 최소 데이터, 비밀정보 금지 |
| Retry | 일시 오류만, Backoff·Jitter·상한 |
| DLQ | 원인·최종 오류·원문 Hash·재생 이력 |

### 27.8 정상 결과와 완료 판정

업무 Commit과 Outbox가 일치하고, 같은 Message ID가 여러 번 전달돼도 업무 부수 효과는 한 번만 발생한다. Lag·Oldest Age·DLQ가 운영 기준 안에 있다.

### 27.9 중복·동시성·시간초과·응답 유실·부분 실패

Broker ACK 유실, Consumer 처리 후 ACK 유실, Rebalance, Schema 불일치, 순서 역전, DLQ 부분 Replay를 구분한다.

### 27.10 재시도·재시작·재처리·대사·보상·되돌리기

Outbox는 재개하고, Consumer는 Inbox 결과를 반환한다. Schema 오류는 Code 배포 후 선택 Replay한다. 성공 대상은 Replay에서 제외한다. Provider 장애 중 업무 등록은 Outbox 적체로 유지한다.

### 27.11 로그·지표·추적·감사

Message ID, Outbox ID, Topic/Queue, Partition, Offset/Delivery Tag, Consumer Group, Retry Count, DLQ Reason, Lag를 기록한다.

### 27.12 교육 예제

`EDU-DEV-07`, `EDU-DEV-21`, `EDU-DEV-27`, `EDU-DEV-32`를 실행한다.

### 27.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 Event와 처리 규칙은 조직 영역이다. Envelope, Outbox/Inbox, Retry/DLQ, Audit 계약은 CPF가 유지한다.

### 27.14 운영 인계

Topic/Queue, ACL, Schema, Ordering, Retention, Replay Permission, 대사 Query, 경보 기준을 인계한다.


## 28. 파일·Attachment·SFTP 연계

### 28.1 이 기능으로 만드는 업무 결과

업로드·검사·보관·업무 연결·다운로드와 SFTP 송수신을 검사합과 상태 원장으로 통제한다.

### 28.2 선택 기준과 사용하지 말아야 할 경우

- 파일이 업무 증적이거나 외부 교환 대상이면 사용한다.
- 작은 단순 설정 파일은 Application Resource 관리 방식을 사용한다.

### 28.3 주 사용자와 권한

업로드 Permission, 원문 다운로드 Permission, 반출 승인, SFTP Service Account와 Host Key 관리 권한을 분리한다.

### 28.4 시작 전에 결정할 값

허용 확장자·Content-Type·크기, 암호화, 악성코드 검사, 보존, 파일명, SHA-256, SFTP Directory·완료 규칙을 정한다.

### 28.5 작업 후 만들어지는 결과물

Attachment Metadata, Object/Filesystem Adapter, Scan 상태, Download Audit, SFTP Transfer Ledger, 정리 Job, Fault Test.

### 28.6 단계별 절차

1. Upload Session을 만들고 최대 크기와 만료를 반환한다.
2. 임시 영역에 Streaming 업로드하고 SHA-256을 계산한다.
3. 크기·형식·내용·악성코드 검사를 실행한다.
4. 통과한 파일만 확정 보관 영역으로 원자 이동한다.
5. 업무 Entity에는 Attachment ID와 Version만 연결한다.
6. 다운로드 시 Permission·Masking·승인·만료를 확인한다.
7. SFTP 송신은 `.part`로 전송 후 검사합 확인과 원자 Rename으로 완료한다.
8. 수신은 안정화 시간, 중복 파일, Header/Trailer, 검사합을 확인한다.
9. 응답 유실 시 Transfer Ledger와 원격 Directory를 대사한다.

### 28.7 입력값·기본값·허용 범위

파일명은 경로 문자를 제거하고 원본명과 저장명을 분리한다. 크기·동시성·전체 처리시간에 상한을 둔다. Credential과 Private Key는 Secret Provider 참조만 사용한다.

### 28.8 정상 결과와 완료 판정

검사 통과 전 접근이 차단되고 Metadata SHA-256과 저장 파일이 일치한다. SFTP 완료 파일은 원격·로컬 원장과 건수·크기가 일치한다.

### 28.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 업로드, Scan Timeout, Disk 부족, SFTP 연결 중단, Rename ACK 유실, 일부 파일 성공을 구분한다. 전체 파일을 메모리에 적재하지 않는다.

### 28.10 재시도·재시작·재처리·대사·보상·되돌리기

같은 Upload/Transfer ID를 조회한다. 임시 파일은 안전한 Cleanup Job이 만료 후 삭제한다. 성공 파일은 재전송하지 않고 실패 파일만 재개한다. 유출된 Download Token은 폐기한다.

### 28.11 로그·지표·추적·감사

Attachment/Transfer ID, SHA-256, 크기, 상태, Scan 결과, 원격 경로, Attempt, 수행자, Download Audit를 기록한다.

### 28.12 교육 예제

`EDU-DEV-08`, `EDU-DEV-17`, `EDU-DEV-26`, `EDU-DEV-36`을 실행한다.

### 28.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

파일 형식·업무 연결·보존은 조직 영역이다. Streaming·검사·원장·Token·Audit·SFTP 안전 규칙은 CPF가 유지한다.

### 28.14 운영 인계

저장소, Directory, Credential 참조, 허용 형식, 보존, Cleanup, 대사, 재전송, 보안 사고 절차를 인계한다.


## 29. 외부 REST·TCP·ISO8583 연계

### 29.1 이 기능으로 만드는 업무 결과

외부 기관과 REST 또는 전문 통신을 수행하고 Timeout·중복·결과 미확정·재접속을 Attempt 단위로 관리한다.

### 29.2 선택 기준과 사용하지 말아야 할 경우

- 외부 부수 효과와 결과 조회가 필요한 연계에 사용한다.
- 단순 공개 정보 조회는 축소된 Query Adapter를 사용할 수 있다.

### 29.3 주 사용자와 권한

연계 Service Identity, 대상 Allowlist, Credential, 원문 조회, 수동 확정 Permission을 분리한다.

### 29.4 시작 전에 결정할 값

Endpoint, TLS, 인증, Encoding, Frame, 전문 Version, Correlation, Timeout, Retry, 결과 조회, 대사 주기, 전문 가림 필드를 정한다.

### 29.5 작업 후 만들어지는 결과물

Port/SPI, REST/TCP Provider Adapter, Codec, Attempt Ledger, 전문 Schema, Mock/Simulator, Contract·Fault Test.

### 29.6 단계별 절차

1. 업무 요청을 정규 Command로 검증한다.
2. Attempt를 `CREATED`로 기록하고 Request Hash를 고정한다.
3. REST는 연결·쓰기·읽기·전체 Timeout을, TCP는 Connect·Frame·Heartbeat·Idle Timeout을 설정한다.
4. 대상 Allowlist·DNS·TLS·Certificate를 검증한다.
5. 전문을 Encoding/Frame 규칙에 따라 생성하고 Correlation ID를 넣는다.
6. 전송 전후 시각과 바이트 Hash를 기록한다.
7. 응답을 Schema·MAC·응답 코드로 검증한다.
8. 결과가 확정되면 Attempt와 업무 상태를 갱신한다.
9. Timeout이면 전송 단계와 상대 조회 가능성을 기준으로 결과 미확정을 판정한다.
10. Simulator로 정상·지연·절단·중복·잘못된 전문을 시험한다.

### 29.7 입력값·기본값·허용 범위

| 항목 | REST | TCP/ISO8583 |
|---|---|---|
| Correlation | Header/Body ID | STAN/RRN 또는 계약 ID |
| Framing | HTTP Message | Length/Delimiter/Fixed |
| Encoding | UTF-8 등 계약 | ASCII/EBCDIC/Binary 등 계약 |
| Timeout | Connect/Write/Read/Total | Connect/Frame/Idle/Heartbeat |
| 결과 조회 | 조회 API | 조회 전문/대사 파일 |

### 29.8 정상 결과와 완료 판정

Attempt의 요청·응답 Hash, 외부 거래 ID, 상태와 업무 원장이 일치한다. 전문 원문은 권한 없이 노출되지 않는다. 재접속 후 중복 부수 효과가 없다.

### 29.9 중복·동시성·시간초과·응답 유실·부분 실패

Connect 실패, 전송 전 실패, 전송 후 응답 유실, Half-open, Frame 분할·병합, Encoding 오류, 상대 중복 처리를 구분한다.

### 29.10 재시도·재시작·재처리·대사·보상·되돌리기

전송 전 실패만 정책에 따라 재시도한다. 전송 후 미확정은 조회 전문/API/대사 파일로 확인한다. 결과가 확정되지 않으면 수동 확정 또는 보상 승인으로 종료한다.

### 29.11 로그·지표·추적·감사

Attempt ID, Endpoint, 단계별 지연, 바이트 수·Hash, 응답 코드, Correlation, Reconnect, 결과 미확정 경과시간을 기록한다.

### 29.12 교육 예제

`EDU-DEV-09`, `EDU-DEV-10`, `EDU-DEV-25`, `EDU-DEV-33`, `EDU-DEV-34`를 실행한다.

### 29.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

기관 전문·응답 코드·업무 대사는 조직 영역이다. Timeout 분류, Attempt, Secret, TLS, 가림, Reconcile 계약은 CPF가 유지한다.

### 29.14 운영 인계

Endpoint·Certificate·Credential 참조, 전문 Version, Timeout, Retry 금지 조건, 결과 조회, 대사, 장애 연락망을 인계한다.


## 30. 보안·권한·Data Scope·Masking·Audit

### 30.1 이 기능으로 만드는 업무 결과

업무 API와 데이터에 인증 주체, Permission, Data Scope, 개인정보 가림, 사유, 승인과 불변 Audit를 적용한다.

### 30.2 선택 기준과 사용하지 말아야 할 경우

- 모든 업무 기능에 적용한다. 외부 IAM을 사용해도 업무 Permission·Data Scope·Audit는 명시한다.

### 30.3 주 사용자와 권한

업무 사용자, 서비스 계정, 운영자, 승인자, 보안 담당자, 원문 조회자 권한을 분리한다.

### 30.4 시작 전에 결정할 값

주체 ID, Role, Permission, Data Scope, 민감 필드, Masking 규칙, 원문 해제, Reason, Approval, Audit 보존을 정한다.

### 30.5 작업 후 만들어지는 결과물

Security Policy, Permission Matrix, Scope Resolver, Masking Policy, Audit Event, Negative Test, Break-glass Runbook.

### 30.6 단계별 절차

1. API·화면·버튼별 Permission을 정의한다.
2. Data Scope를 조직·지역·업무·소유자 기준으로 정의한다.
3. 서버에서 Permission과 Scope를 모두 강제한다.
4. 민감 필드를 분류하고 기본 응답은 Masking한다.
5. 원문 조회는 별도 Permission·사유·승인·짧은 유효시간을 요구한다.
6. 변경·반출·위험 조치는 수행자·사유·승인·전후 값을 Audit한다.
7. Session/Token 권한 변경 반영과 회수 절차를 시험한다.
8. 권한 없음·Scope 우회·IDOR·Mass Assignment·민감 Log Negative Test를 실행한다.

### 30.7 입력값·기본값·허용 범위

Permission은 기능 의미로 정의하고 화면 이름에만 결합하지 않는다. Data Scope는 서버 Query 조건에 포함한다. Masking 해제 Token은 대상·필드·기간을 제한한다.

### 30.8 정상 결과와 완료 판정

권한 없는 API·직접 URL·다른 조직 데이터 접근이 거부된다. Log·Trace·Error에 Secret·Token·개인정보 원문이 없다. Audit는 업무 결과와 같은 ID로 조회된다.

### 30.9 중복·동시성·시간초과·응답 유실·부분 실패

권한 Cache 지연, 조직 이동, Session 잔존, 승인 만료, Audit 전달 지연, 부분 Masking을 구분한다.

### 30.10 재시도·재시작·재처리·대사·보상·되돌리기

정본 권한을 재조회하고 Cache/Session Version을 갱신한다. 잘못 발급된 권한·Token·Session을 회수한다. Audit 전송 실패는 Outbox에서 재전송하되 업무 결과를 지우지 않는다.

### 30.11 로그·지표·추적·감사

인증 실패 분류, Permission, Scope, Masking, 원문 조회, Approval, Audit Delivery Lag를 기록한다.

### 30.12 교육 예제

`EDU-DEV-11`, `EDU-DEV-37`, `EDU-BZA-10`, `EDU-ADM-08`을 연결해 실행한다.

### 30.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 Permission·민감 필드·승인 정책은 조직 영역이다. 인증 문맥, 서버 강제, Masking·Audit 계약은 CPF가 유지한다.

### 30.14 운영 인계

Permission Matrix, Data Scope, Masking, Approval, Session 회수, Audit 보존·조회·재전송을 인계한다.


## 31. DB Migration·Upgrade·Rollback

### 31.1 이 기능으로 만드는 업무 결과

Oracle·PostgreSQL·MariaDB에서 같은 업무 의미의 Schema를 Fresh 설치하고 Version별 Upgrade·Drift·Rollback을 관리한다.

### 31.2 선택 기준과 사용하지 말아야 할 경우

- 상태를 저장하는 모든 기능에 적용한다. DB-less 기능은 이유와 외부 정본을 명시한다.

### 31.3 주 사용자와 권한

DBA는 계정·Schema·Backup을, 개발자는 Migration·대사·호환 SQL을, 운영자는 실행·검증·Rollback 승인을 담당한다.

### 31.4 시작 전에 결정할 값

Table·Column·Index·Sequence, Version, Lock, 보존, Vendor 차이, Online DDL, Backfill, 이전·신규 Application 공존 기간을 정한다.

### 31.5 작업 후 만들어지는 결과물

3 Vendor Flyway Pack, Rollback/Forward Fix, Fresh/Upgrade Test, Drift Report, Data Reconciliation SQL.

### 31.6 단계별 절차

1. 논리 모델과 Ownership을 확정한다.
2. 공통 Migration ID와 Vendor별 SQL을 작성한다.
3. 빈 DB Fresh 설치를 실행한다.
4. 지원 시작 Version별 Upgrade 경로를 실행한다.
5. Data Backfill은 범위·Checkpoint·재실행 안전성을 갖춘다.
6. 이전·신규 Application과 Schema 공존을 시험한다.
7. Drift를 정본 Migration과 비교한다.
8. Rollback 가능한 DDL과 Forward Fix만 가능한 변경을 구분한다.
9. 업무 건수·금액·Hash와 FK·Index·Constraint를 대사한다.

### 31.7 입력값·기본값·허용 범위

모든 Identifier는 DB 표준을 따른다. Timestamp·Boolean·대용량 Text/Binary·Sequence 차이를 Vendor별로 매핑한다. Migration은 비밀정보를 포함하지 않는다.

### 31.8 정상 결과와 완료 판정

3 Vendor의 Table·Constraint·Index 의미와 업무 Query 결과가 같다. Fresh와 Upgrade의 최종 Schema Hash가 같다. Drift가 0이다.

### 31.9 중복·동시성·시간초과·응답 유실·부분 실패

DDL Lock, Disk 부족, Backfill 중단, 일부 Vendor 실패, 이전 Application 비호환, Rollback 불가능 변경을 구분한다.

### 31.10 재시도·재시작·재처리·대사·보상·되돌리기

실패 Migration은 원인을 수정하고 Flyway 상태를 검토한 뒤 재실행한다. Backfill은 Checkpoint에서 재개한다. 데이터 손실 위험이 있으면 Backup Restore 또는 Forward Fix를 사용한다.

### 31.11 로그·지표·추적·감사

Migration ID, 실행자, 시작·종료, 영향 행, Lock 시간, Schema Version, Drift, 대사 결과를 기록한다.

### 31.12 교육 예제

`EDU-DEV-14`, `EDU-OPS-04`를 3 Vendor에서 실행한다.

### 31.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 Table·Column은 조직 영역이다. Migration ID, Vendor Pack 구조, 검증·Drift·Rollback 계약은 CPF가 유지한다.

### 31.14 운영 인계

Schema Version, 지원 Upgrade 경로, Backup, 실행 명령, 예상 시간, Lock, 대사 SQL, Rollback/Forward Fix를 인계한다.


## 32. 개발자가 사용하는 전체 파일 지도

| 영역 | 생성·수정 위치 | 반드시 확인할 내용 | 임의 수정 금지 |
|---|---|---|---|
| Build | Domain `build.gradle`, Platform BOM | 선택 Starter·Version·Test | Root 품질 Gate 우회 |
| API | `com.cpf.<domain>.api` | DTO·Validation·Error·JavaDoc | Internal Type 노출 |
| Application | Application Service·Port | Transaction·Idempotency·Timeout | Controller에 업무 로직 |
| Domain | Entity·Value·Policy | 상태 전이·불변식 | Persistence Annotation 의존 확대 |
| Persistence | Repository Adapter·Mapper | Lock·Pagination·Vendor 의미 | 다른 Domain DB 접근 |
| Migration | `cpf-tools/db/vendor/<vendor>` | Fresh·Upgrade·Rollback·Drift | 운영 DB 수동 DDL |
| Config | `application*.yml`, Properties | Default·Secret 참조·Profile | Secret 원문 Commit |
| Test | Unit·Contract·Integration·Fault | 정상·오류·동시성·복구 | 미실행을 성공 기록 |
| ADM | Owner Query/Command·OpenAPI | Permission·Reason·Approval·Audit | ADM의 Owner DB 직접 변경 |

## 33. 코드 리뷰 질문

- Public API가 OSS 구현 Type을 노출하지 않는가?
- 상태 변경이 Domain Method와 Version 조건을 통과하는가?
- Idempotency Key와 Request Hash의 범위가 명확한가?
- DB Commit 전후·외부 전송 전후 실패가 구분되는가?
- Retry가 부수 효과를 중복 생성하지 않는가?
- Permission·Data Scope·Masking이 서버에서 적용되는가?
- Log·Metric·Trace·Audit에 공통 식별자가 있는가?
- 3 Vendor SQL과 Migration 의미가 같은가?
- Local·Remote Contract Test가 같은 Corpus를 사용하는가?
- ADM·Runbook·배포 인계가 같은 상태와 오류를 설명하는가?

## 34. 배포 전 한 줄 검증 명령

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; git -C $repo status --short; git -C $repo diff --check; & (Join-Path $repo 'gradlew.bat') clean test; if($LASTEXITCODE -ne 0){throw 'Gradle 검증 실패'}
```

명령 성공만으로 업무 완료를 판정하지 않는다. 해당 기능의 DB·Broker·외부 Fixture·Browser·Fault Test와 ADM 확인 결과를 함께 보관한다.

## 35. Generator Capability Profile 실전 절차

### 35.1 Dry Run 한 줄 명령

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') -Root $repo -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -DependencyModel root-project -CapabilityProfile SECURE_RESOURCE_API -ProviderBindings 'cache=valkey' -DryRun
```

### 35.2 적용 결과 확인

1. 생성 Manifest의 `capabilityProfile`과 `profileVersion`을 확인한다.
2. `resolvedStarters`가 `cpf-tools/generator/contracts/capability-profiles.json`과 일치하는지 비교한다.
3. `providerBindings`가 Profile의 허용 목록에 속하는지 확인한다.
4. Starter Version Lock과 Platform Version을 Build Dependency Report와 비교한다.
5. 선택하지 않은 Broker·Cache·Session Provider가 Runtime Classpath에 없는지 검사한다.
6. MariaDB·PostgreSQL·Oracle 중 선택 Vendor의 Driver·Flyway Database Module·Migration이 생성됐는지 확인한다.
7. Generated Domain의 최소 업무 테이블과 Idempotency Ledger가 중앙 Template 계약과 일치하는지 확인한다.
8. `runtime-agent` Profile은 기본 비활성이며 필요한 환경변수와 배포 Descriptor가 있을 때만 켠다.

### 35.3 대표 Profile 조합

| 업무 | Profile | Provider Binding | 추가 Leaf | 검증 포인트 |
|---|---|---|---|---|
| 일반 조회·등록 API | DOMAIN_WEB_API | 없음 | PERSISTENCE_MYBATIS | Validation·OpenAPI·DB |
| 보호 API | SECURE_RESOURCE_API | cache=caffeine | PERSISTENCE_MYBATIS | Audience·Permission·Trace |
| Kafka 이벤트 업무 | EVENT_KAFKA | messaging=kafka | PERSISTENCE_MYBATIS | Outbox·Inbox·DLQ |
| RabbitMQ Queue 업무 | EVENT_RABBITMQ | messaging=rabbitmq | PERSISTENCE_MYBATIS | Confirm·Quorum·DLQ |
| IBM MQ 기관 연계 | EVENT_JMS_IBM_MQ | messaging=ibm-mq | PERSISTENCE_MYBATIS | CCDT/Channel·Reason Code·In-doubt |
| TCP 전문 연계 | INTEGRATION_TCP | 없음 | PERSISTENCE_MYBATIS | Frame·Correlation·TLS·UNKNOWN_RESULT |
| SFTP 파일 연계 | SFTP_INTEGRATION | 없음 | PERSISTENCE_MYBATIS | Ledger·Checksum·Atomic Rename |

## 36. Source-backed 실행 Property 빠른 참조

| Key | Type | Default | 필수 조건 | 검증·오류 |
|---|---|---|---|---|
| `cpf.starter.base.strict` | Boolean | `true` | 항상 | Profile·Binding 검증을 Fail-closed |
| `cpf.starter.base.profile-id` | String | `MINIMAL_BOOT_DOMAIN` | 항상 | 빈 값이면 기동 거부 |
| `cpf.starter.base.profile-version` | String | `1.0` | 항상 | 빈 값이면 기동 거부; Manifest Version과 대조 |
| `cpf.http-client.connect-timeout` | Duration | `3s` | HTTP Client 사용 | 0/음수 거부 |
| `cpf.http-client.request-timeout` | Duration | `10s` | HTTP Client 사용 | 0/음수 거부 |
| `cpf.http-client.max-response-bytes` | Integer | `4194304` | HTTP Client 사용 | 1024 미만 거부 |
| `cpf.messaging.reliability.enabled` | Boolean | `true` | 비동기 신뢰성 기능 | Schema·Worker 동작과 일치 |
| `cpf.messaging.reliability.schema-required` | Boolean | `true` | JDBC Ledger 사용 | Schema 없으면 Fail-closed |
| `cpf.messaging.reliability.claim-limit` | Integer | `100` | Publisher/Consumer Worker | 1..1000 |
| `cpf.messaging.reliability.lease` | Duration | `30s` | 다중 Worker | 0/음수 거부 |
| `cpf.messaging.reliability.max-replay-batch` | Integer | `500` | Replay | 1..5000 |
| `cpf.integration.sftp.port` | Integer | `22` | SFTP 활성 | 1..65535 |
| `cpf.integration.sftp.connect-timeout` | Duration | `10s` | SFTP 활성 | 연결 예산 |
| `cpf.integration.sftp.operation-timeout` | Duration | `30s` | SFTP 활성 | 전송 예산 |
| `cpf.integration.sftp.buffer-bytes` | Integer | `65536` | SFTP 활성 | 4096 이상 |
| `cpf.integration.sftp.max-transfer-bytes` | Long | `1073741824` | SFTP 활성 | 1 이상 |
| `cpf.integration.sftp.ledger-required` | Boolean | `true` | 운영 환경 | Ledger 없이 성공 확정 금지 |

## 37. Broker Provider별 개발 계약

| Provider | Profile/Leaf | 필수 설정 | 성공 판정 | 결과 미확정 시 |
|---|---|---|---|---|
| Kafka | EVENT_KAFKA / messaging-kafka | Bootstrap·Topic·Consumer Group·Serialization | Outbox Published·Consumer Inbox 완료·Offset 진행 | Outbox·Broker·Inbox를 Message ID로 대사 |
| RabbitMQ | EVENT_RABBITMQ / messaging-rabbitmq | Exchange·Type·Queue·Routing Key·Quorum·Prefetch | Publisher Confirm·Queue/Consumer ACK·Inbox 완료 | Confirm·Queue 상태·Inbox 대사 |
| JMS | messaging-jms | Destination·Queue/Topic·Session Transaction·Ack Mode | Commit·ACK/Redelivery·Inbox 완료 | Provider Browse/Management와 Ledger 대사 |
| IBM MQ | EVENT_JMS_IBM_MQ / messaging-ibm-mq | Queue Manager·CCDT 또는 Channel+Connection Name·TLS·Destination | MQPUT/MQGET 결과·Reason Code·Inbox 완료 | Queue Manager·Attempt·Inbox와 기관 조회 대사 |

RabbitMQ의 기본은 `binding-name=rabbitmq`, `exchange-type=topic`, `routing-key=#`, Durable·Quorum 활성, Prefetch 50, Concurrency 1, 최대 Payload 1 MiB, Confirm Timeout 10초다. IBM MQ는 `tls-required=true`이며 CCDT 또는 Channel+Connection Name 중 하나가 필요하다. JMS는 `session-transacted=true`, Acknowledgement Mode 2를 기본으로 한다.

## 38. TCP 전문 Property와 장애 판정

| Key | Default | 설명 | 오류 조건 |
|---|---|---|---|
| `cpf.integration.tcp.enabled` | `false` | TCP Runtime 활성 | 활성 시 Port 필수 |
| `cpf.integration.tcp.mode` | `CLIENT` | CLIENT/SERVER | 지원 Enum 외 값 거부 |
| `cpf.integration.tcp.host` | `127.0.0.1` | Client 대상 | 환경별 승인 대상 |
| `cpf.integration.tcp.pool-size` | `4` | 연결 수 | 1..256 |
| `cpf.integration.tcp.connect-timeout` | `3s` | 연결 예산 | 0/음수 거부 |
| `cpf.integration.tcp.response-timeout` | `10s` | 응답 예산 | Timeout 뒤 UNKNOWN_RESULT 판정 |
| `cpf.integration.tcp.idle-timeout` | `60s` | 유휴 연결 | Heartbeat 정책과 조정 |
| `cpf.integration.tcp.frame` | `LENGTH_HEADER` | FIXED/LENGTH_HEADER/STX_ETX/CRLF | FIXED면 fixed-length 필수 |
| `cpf.integration.tcp.max-frame-bytes` | `1048576` | 최대 Frame | 초과 Frame 거부 |
| `cpf.integration.tcp.charset` | `UTF-8` | 문자 Encoding | 지원 Charset 검증 |
| `cpf.integration.tcp.tls` | `false` | TLS | 활성 시 Key/Trust Store와 Secret 필수 |
| `cpf.integration.tcp.mutual-tls` | `false` | 상호 TLS | TLS 없이 활성 금지 |
| `cpf.integration.tcp.max-pending` | `10000` | 대기 Correlation | 1 이상 |
| `cpf.integration.tcp.max-orphans` | `1000` | 고아 응답 보관 | 1 이상 |
| `cpf.integration.tcp.reconnect-initial` | `200ms` | 초기 재연결 | Reconnect 정책 검증 |
| `cpf.integration.tcp.reconnect-max` | `30s` | 최대 재연결 | 초기보다 작을 수 없음 |
| `cpf.integration.tcp.reconnect-jitter` | `0.2` | Jitter | 정책 범위 검증 |

응답 Timeout 뒤에는 같은 전문을 새 거래로 전송하지 않는다. Correlation Registry·Unknown Result Store·기관 조회·업무 원장을 같은 Attempt ID로 대사하고, 성공이 확인되면 원장을 확정하며 실패가 확인될 때만 같은 Idempotency Key로 재처리한다.
