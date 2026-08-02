# CPF 플랫폼 운영 매뉴얼 — 설치·설정·배포·관측·정상화

> **주 독자**: 인프라 운영자, DBA, 배포 담당자, 보안 담당자, 관측 담당자, 재해복구 담당자
> **완료 결과**: CPF Runtime과 선택 제품을 설치하고, Artifact·Property·Secret·DB·메시지 브로커·배포·관측·백업·장애 대응·Rollback을 역할별 절차와 판정 기준에 따라 수행한다.

<!-- CPF-TOC:START -->
## 전체 목차

- [0. 문서 기준](#0-문서-기준)
- [1. 문서 사용 순서와 역할 경계](#1-문서-사용-순서와-역할-경계)
- [2. 지원 환경과 설치 기준](#2-지원-환경과-설치-기준)
  - [2.1 정본 Stack과 Artifact](#21-정본-stack과-artifact)
  - [2.2 Docker 개발·시험 Runtime](#22-docker-개발시험-runtime)
- [3. 설치 전 점검](#3-설치-전-점검)
- [4. Artifact·Checksum·Manifest](#4-artifactchecksummanifest)
  - [4.1 필수 인계 항목](#41-필수-인계-항목)
  - [4.2 확인 명령 예](#42-확인-명령-예)
- [5. 계정·Directory·파일 권한](#5-계정directory파일-권한)
- [6. Property·환경변수·Profile 관리](#6-property환경변수profile-관리)
  - [6.1 Property Catalog 필수 열](#61-property-catalog-필수-열)
  - [6.2 기준 Source에서 확인된 공통 설정](#62-기준-source에서-확인된-공통-설정)
  - [6.3 변경 절차](#63-변경-절차)
- [7. Secret·Certificate](#7-secretcertificate)
  - [7.1 Secret 원칙](#71-secret-원칙)
  - [7.2 Docker Secret Source 예](#72-docker-secret-source-예)
  - [7.3 Certificate 점검](#73-certificate-점검)
- [8. DB 설치·Migration·Drift](#8-db-설치migrationdrift)
  - [8.1 계정 분리](#81-계정-분리)
  - [8.2 Lifecycle](#82-lifecycle)
  - [8.3 설치 명령 시작점](#83-설치-명령-시작점)
  - [8.4 정상 결과](#84-정상-결과)
  - [8.5 실패·부분 실패](#85-실패부분-실패)
- [9. 메시지 브로커 운영](#9-메시지-브로커-운영)
  - [9.1 제공 범위 범위](#91-제공-범위-범위)
  - [9.2 운영 점검표](#92-운영-점검표)
  - [9.3 결과 불명과 대사](#93-결과-불명과-대사)
- [10. 기동·Readiness·종료](#10-기동readiness종료)
  - [10.1 기동 순서](#101-기동-순서)
  - [10.2 종료 순서](#102-종료-순서)
- [11. 배포 전략](#11-배포-전략)
  - [11.1 Rolling](#111-rolling)
  - [11.2 Blue-Green](#112-blue-green)
  - [11.3 Canary](#113-canary)
  - [11.4 Partial Apply](#114-partial-apply)
- [12. Log·Metric·Trace·Audit](#12-logmetrictraceaudit)
  - [12.1 Log](#121-log)
  - [12.2 Metric](#122-metric)
  - [12.3 Trace](#123-trace)
  - [12.4 Audit](#124-audit)
- [13. Capacity 관리](#13-capacity-관리)
- [14. Backup·Restore](#14-backuprestore)
  - [14.1 범위](#141-범위)
  - [14.2 Restore 판정](#142-restore-판정)
- [15. Upgrade·Rollback](#15-upgraderollback)
- [16. 장애 Runbook](#16-장애-runbook)
  - [16.1 DB 연결·Lock·Drift](#161-db-연결lockdrift)
  - [16.2 메시지 브로커](#162-메시지-브로커)
  - [16.3 Instance Crash·OOM·Stuck](#163-instance-crashoomstuck)
  - [16.4 Network·TLS](#164-networktls)
  - [16.5 Disk·Memory 임계](#165-diskmemory-임계)
  - [16.6 설정 부분 적용](#166-설정-부분-적용)
- [17. DR](#17-dr)
- [18. Docker 개발·시험 환경](#18-docker-개발시험-환경)
- [19. 플랫폼 운영 EDU](#19-플랫폼-운영-edu)
  - [EDU-OPS-01 — 신규 설치와 정상 종료](#edu-ops-01-신규-설치와-정상-종료)
  - [EDU-OPS-02 — 응답 유실과 결과 대사](#edu-ops-02-응답-유실과-결과-대사)
  - [EDU-OPS-03 — 부분 적용과 Rollback](#edu-ops-03-부분-적용과-rollback)
  - [19.1 플랫폼 운영 EDU 15개 전수표](#191-플랫폼-운영-edu-15개-전수표)
- [20. 운영 인계표](#20-운영-인계표)
- [21. 완료 점검표](#21-완료-점검표)
- [22. 종단간 예제: 신규 환경 설치와 첫 업무 확인](#22-종단간-예제-신규-환경-설치와-첫-업무-확인)
  - [22.1 업무 결과](#221-업무-결과)
  - [22.2 선택 기준](#222-선택-기준)
  - [22.3 역할과 권한](#223-역할과-권한)
  - [22.4 시작 전에 결정할 값](#224-시작-전에-결정할-값)
  - [22.5 결과물](#225-결과물)
  - [22.6 단계별 절차](#226-단계별-절차)
  - [22.7 입력·기본값·허용 범위](#227-입력기본값허용-범위)
  - [22.8 정상 결과와 완료 판정](#228-정상-결과와-완료-판정)
  - [22.9 오류·동시성·시간초과·응답 유실·부분 실패](#229-오류동시성시간초과응답-유실부분-실패)
  - [22.10 재시도·재처리·대사·보상·되돌리기](#2210-재시도재처리대사보상되돌리기)
  - [22.11 로그·지표·추적·감사](#2211-로그지표추적감사)
  - [22.12 교육 예제](#2212-교육-예제)
  - [22.13 조직 영역과 CPF 유지 영역](#2213-조직-영역과-cpf-유지-영역)
  - [22.14 운영 인계](#2214-운영-인계)
- [23. Artifact·Checksum·SBOM·공급망](#23-artifactchecksumsbom공급망)
  - [23.1 업무 결과](#231-업무-결과)
  - [23.2 선택 기준](#232-선택-기준)
  - [23.3 역할과 권한](#233-역할과-권한)
  - [23.4 시작 전에 결정할 값](#234-시작-전에-결정할-값)
  - [23.5 결과물](#235-결과물)
  - [23.6 단계별 절차](#236-단계별-절차)
  - [23.7 입력·기본값·허용 범위](#237-입력기본값허용-범위)
  - [23.8 정상 결과와 완료 판정](#238-정상-결과와-완료-판정)
  - [23.9 오류·동시성·시간초과·응답 유실·부분 실패](#239-오류동시성시간초과응답-유실부분-실패)
  - [23.10 재시도·재처리·대사·보상·되돌리기](#2310-재시도재처리대사보상되돌리기)
  - [23.11 로그·지표·추적·감사](#2311-로그지표추적감사)
  - [23.12 교육 예제](#2312-교육-예제)
  - [23.13 조직 영역과 CPF 유지 영역](#2313-조직-영역과-cpf-유지-영역)
  - [23.14 운영 인계](#2314-운영-인계)
- [24. 계정·Directory·파일 권한](#24-계정directory파일-권한)
  - [24.1 업무 결과](#241-업무-결과)
  - [24.2 선택 기준](#242-선택-기준)
  - [24.3 역할과 권한](#243-역할과-권한)
  - [24.4 시작 전에 결정할 값](#244-시작-전에-결정할-값)
  - [24.5 결과물](#245-결과물)
  - [24.6 단계별 절차](#246-단계별-절차)
  - [24.7 입력·기본값·허용 범위](#247-입력기본값허용-범위)
  - [24.8 정상 결과와 완료 판정](#248-정상-결과와-완료-판정)
  - [24.9 오류·동시성·시간초과·응답 유실·부분 실패](#249-오류동시성시간초과응답-유실부분-실패)
  - [24.10 재시도·재처리·대사·보상·되돌리기](#2410-재시도재처리대사보상되돌리기)
  - [24.11 로그·지표·추적·감사](#2411-로그지표추적감사)
  - [24.12 교육 예제](#2412-교육-예제)
  - [24.13 조직 영역과 CPF 유지 영역](#2413-조직-영역과-cpf-유지-영역)
  - [24.14 운영 인계](#2414-운영-인계)
- [25. Property·환경변수·Profile](#25-property환경변수profile)
  - [25.1 업무 결과](#251-업무-결과)
  - [25.2 선택 기준](#252-선택-기준)
  - [25.3 역할과 권한](#253-역할과-권한)
  - [25.4 시작 전에 결정할 값](#254-시작-전에-결정할-값)
  - [25.5 결과물](#255-결과물)
  - [25.6 단계별 절차](#256-단계별-절차)
  - [25.7 입력·기본값·허용 범위](#257-입력기본값허용-범위)
  - [25.8 정상 결과와 완료 판정](#258-정상-결과와-완료-판정)
  - [25.9 오류·동시성·시간초과·응답 유실·부분 실패](#259-오류동시성시간초과응답-유실부분-실패)
  - [25.10 재시도·재처리·대사·보상·되돌리기](#2510-재시도재처리대사보상되돌리기)
  - [25.11 로그·지표·추적·감사](#2511-로그지표추적감사)
  - [25.12 교육 예제](#2512-교육-예제)
  - [25.13 조직 영역과 CPF 유지 영역](#2513-조직-영역과-cpf-유지-영역)
  - [25.14 운영 인계](#2514-운영-인계)
- [26. Secret·Certificate·Key Rotation](#26-secretcertificatekey-rotation)
  - [26.1 업무 결과](#261-업무-결과)
  - [26.2 선택 기준](#262-선택-기준)
  - [26.3 역할과 권한](#263-역할과-권한)
  - [26.4 시작 전에 결정할 값](#264-시작-전에-결정할-값)
  - [26.5 결과물](#265-결과물)
  - [26.6 단계별 절차](#266-단계별-절차)
  - [26.7 입력·기본값·허용 범위](#267-입력기본값허용-범위)
  - [26.8 정상 결과와 완료 판정](#268-정상-결과와-완료-판정)
  - [26.9 오류·동시성·시간초과·응답 유실·부분 실패](#269-오류동시성시간초과응답-유실부분-실패)
  - [26.10 재시도·재처리·대사·보상·되돌리기](#2610-재시도재처리대사보상되돌리기)
  - [26.11 로그·지표·추적·감사](#2611-로그지표추적감사)
  - [26.12 교육 예제](#2612-교육-예제)
  - [26.13 조직 영역과 CPF 유지 영역](#2613-조직-영역과-cpf-유지-영역)
  - [26.14 운영 인계](#2614-운영-인계)
- [27. DB 3 Vendor 설치·Migration·Drift](#27-db-3-vendor-설치migrationdrift)
  - [27.1 업무 결과](#271-업무-결과)
  - [27.2 선택 기준](#272-선택-기준)
  - [27.3 역할과 권한](#273-역할과-권한)
  - [27.4 시작 전에 결정할 값](#274-시작-전에-결정할-값)
  - [27.5 결과물](#275-결과물)
  - [27.6 단계별 절차](#276-단계별-절차)
  - [27.7 입력·기본값·허용 범위](#277-입력기본값허용-범위)
  - [27.8 정상 결과와 완료 판정](#278-정상-결과와-완료-판정)
  - [27.9 오류·동시성·시간초과·응답 유실·부분 실패](#279-오류동시성시간초과응답-유실부분-실패)
  - [27.10 재시도·재처리·대사·보상·되돌리기](#2710-재시도재처리대사보상되돌리기)
  - [27.11 로그·지표·추적·감사](#2711-로그지표추적감사)
  - [27.12 교육 예제](#2712-교육-예제)
  - [27.13 조직 영역과 CPF 유지 영역](#2713-조직-영역과-cpf-유지-영역)
  - [27.14 운영 인계](#2714-운영-인계)
- [28. 메시지 Broker·Provider 운영](#28-메시지-brokerprovider-운영)
  - [28.1 업무 결과](#281-업무-결과)
  - [28.2 선택 기준](#282-선택-기준)
  - [28.3 역할과 권한](#283-역할과-권한)
  - [28.4 시작 전에 결정할 값](#284-시작-전에-결정할-값)
  - [28.5 결과물](#285-결과물)
  - [28.6 단계별 절차](#286-단계별-절차)
  - [28.7 입력·기본값·허용 범위](#287-입력기본값허용-범위)
  - [28.8 정상 결과와 완료 판정](#288-정상-결과와-완료-판정)
  - [28.9 오류·동시성·시간초과·응답 유실·부분 실패](#289-오류동시성시간초과응답-유실부분-실패)
  - [28.10 재시도·재처리·대사·보상·되돌리기](#2810-재시도재처리대사보상되돌리기)
  - [28.11 로그·지표·추적·감사](#2811-로그지표추적감사)
  - [28.12 교육 예제](#2812-교육-예제)
  - [28.13 조직 영역과 CPF 유지 영역](#2813-조직-영역과-cpf-유지-영역)
  - [28.14 운영 인계](#2814-운영-인계)
- [29. 기동·Readiness·종료](#29-기동readiness종료)
  - [29.1 업무 결과](#291-업무-결과)
  - [29.2 선택 기준](#292-선택-기준)
  - [29.3 역할과 권한](#293-역할과-권한)
  - [29.4 시작 전에 결정할 값](#294-시작-전에-결정할-값)
  - [29.5 결과물](#295-결과물)
  - [29.6 단계별 절차](#296-단계별-절차)
  - [29.7 입력·기본값·허용 범위](#297-입력기본값허용-범위)
  - [29.8 정상 결과와 완료 판정](#298-정상-결과와-완료-판정)
  - [29.9 오류·동시성·시간초과·응답 유실·부분 실패](#299-오류동시성시간초과응답-유실부분-실패)
  - [29.10 재시도·재처리·대사·보상·되돌리기](#2910-재시도재처리대사보상되돌리기)
  - [29.11 로그·지표·추적·감사](#2911-로그지표추적감사)
  - [29.12 교육 예제](#2912-교육-예제)
  - [29.13 조직 영역과 CPF 유지 영역](#2913-조직-영역과-cpf-유지-영역)
  - [29.14 운영 인계](#2914-운영-인계)
- [30. Rolling·Blue-Green·Canary·Partial Apply](#30-rollingblue-greencanarypartial-apply)
  - [30.1 업무 결과](#301-업무-결과)
  - [30.2 선택 기준](#302-선택-기준)
  - [30.3 역할과 권한](#303-역할과-권한)
  - [30.4 시작 전에 결정할 값](#304-시작-전에-결정할-값)
  - [30.5 결과물](#305-결과물)
  - [30.6 단계별 절차](#306-단계별-절차)
  - [30.7 입력·기본값·허용 범위](#307-입력기본값허용-범위)
  - [30.8 정상 결과와 완료 판정](#308-정상-결과와-완료-판정)
  - [30.9 오류·동시성·시간초과·응답 유실·부분 실패](#309-오류동시성시간초과응답-유실부분-실패)
  - [30.10 재시도·재처리·대사·보상·되돌리기](#3010-재시도재처리대사보상되돌리기)
  - [30.11 로그·지표·추적·감사](#3011-로그지표추적감사)
  - [30.12 교육 예제](#3012-교육-예제)
  - [30.13 조직 영역과 CPF 유지 영역](#3013-조직-영역과-cpf-유지-영역)
  - [30.14 운영 인계](#3014-운영-인계)
- [31. Log·Metric·Trace·Audit·Alert](#31-logmetrictraceauditalert)
  - [31.1 업무 결과](#311-업무-결과)
  - [31.2 선택 기준](#312-선택-기준)
  - [31.3 역할과 권한](#313-역할과-권한)
  - [31.4 시작 전에 결정할 값](#314-시작-전에-결정할-값)
  - [31.5 결과물](#315-결과물)
  - [31.6 단계별 절차](#316-단계별-절차)
  - [31.7 입력·기본값·허용 범위](#317-입력기본값허용-범위)
  - [31.8 정상 결과와 완료 판정](#318-정상-결과와-완료-판정)
  - [31.9 오류·동시성·시간초과·응답 유실·부분 실패](#319-오류동시성시간초과응답-유실부분-실패)
  - [31.10 재시도·재처리·대사·보상·되돌리기](#3110-재시도재처리대사보상되돌리기)
  - [31.11 로그·지표·추적·감사](#3111-로그지표추적감사)
  - [31.12 교육 예제](#3112-교육-예제)
  - [31.13 조직 영역과 CPF 유지 영역](#3113-조직-영역과-cpf-유지-영역)
  - [31.14 운영 인계](#3114-운영-인계)
- [32. Capacity·성능·자원 한도](#32-capacity성능자원-한도)
  - [32.1 업무 결과](#321-업무-결과)
  - [32.2 선택 기준](#322-선택-기준)
  - [32.3 역할과 권한](#323-역할과-권한)
  - [32.4 시작 전에 결정할 값](#324-시작-전에-결정할-값)
  - [32.5 결과물](#325-결과물)
  - [32.6 단계별 절차](#326-단계별-절차)
  - [32.7 입력·기본값·허용 범위](#327-입력기본값허용-범위)
  - [32.8 정상 결과와 완료 판정](#328-정상-결과와-완료-판정)
  - [32.9 오류·동시성·시간초과·응답 유실·부분 실패](#329-오류동시성시간초과응답-유실부분-실패)
  - [32.10 재시도·재처리·대사·보상·되돌리기](#3210-재시도재처리대사보상되돌리기)
  - [32.11 로그·지표·추적·감사](#3211-로그지표추적감사)
  - [32.12 교육 예제](#3212-교육-예제)
  - [32.13 조직 영역과 CPF 유지 영역](#3213-조직-영역과-cpf-유지-영역)
  - [32.14 운영 인계](#3214-운영-인계)
- [33. Backup·Restore·PITR](#33-backuprestorepitr)
  - [33.1 업무 결과](#331-업무-결과)
  - [33.2 선택 기준](#332-선택-기준)
  - [33.3 역할과 권한](#333-역할과-권한)
  - [33.4 시작 전에 결정할 값](#334-시작-전에-결정할-값)
  - [33.5 결과물](#335-결과물)
  - [33.6 단계별 절차](#336-단계별-절차)
  - [33.7 입력·기본값·허용 범위](#337-입력기본값허용-범위)
  - [33.8 정상 결과와 완료 판정](#338-정상-결과와-완료-판정)
  - [33.9 오류·동시성·시간초과·응답 유실·부분 실패](#339-오류동시성시간초과응답-유실부분-실패)
  - [33.10 재시도·재처리·대사·보상·되돌리기](#3310-재시도재처리대사보상되돌리기)
  - [33.11 로그·지표·추적·감사](#3311-로그지표추적감사)
  - [33.12 교육 예제](#3312-교육-예제)
  - [33.13 조직 영역과 CPF 유지 영역](#3313-조직-영역과-cpf-유지-영역)
  - [33.14 운영 인계](#3314-운영-인계)
- [34. DR Failover·Failback](#34-dr-failoverfailback)
  - [34.1 업무 결과](#341-업무-결과)
  - [34.2 선택 기준](#342-선택-기준)
  - [34.3 역할과 권한](#343-역할과-권한)
  - [34.4 시작 전에 결정할 값](#344-시작-전에-결정할-값)
  - [34.5 결과물](#345-결과물)
  - [34.6 단계별 절차](#346-단계별-절차)
  - [34.7 입력·기본값·허용 범위](#347-입력기본값허용-범위)
  - [34.8 정상 결과와 완료 판정](#348-정상-결과와-완료-판정)
  - [34.9 오류·동시성·시간초과·응답 유실·부분 실패](#349-오류동시성시간초과응답-유실부분-실패)
  - [34.10 재시도·재처리·대사·보상·되돌리기](#3410-재시도재처리대사보상되돌리기)
  - [34.11 로그·지표·추적·감사](#3411-로그지표추적감사)
  - [34.12 교육 예제](#3412-교육-예제)
  - [34.13 조직 영역과 CPF 유지 영역](#3413-조직-영역과-cpf-유지-영역)
  - [34.14 운영 인계](#3414-운영-인계)
- [35. Property Catalog 표준](#35-property-catalog-표준)
- [36. 장애 Runbook 공통 8단계](#36-장애-runbook-공통-8단계)
- [37. 환경 인수 체크리스트](#37-환경-인수-체크리스트)
- [38. Docker 개발·시험 환경 한 줄 실행](#38-docker-개발시험-환경-한-줄-실행)

<!-- CPF-TOC:END -->

## 0. 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Source: `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)
- 최상위 요구 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 실제 Source·SQL·Config·Script·Test가 문서보다 우선한다.

## 1. 문서 사용 순서와 역할 경계

| 역할 | 이 문서에서 수행하는 일 | 다른 매뉴얼로 이동하는 일 |
|---|---|---|
| 플랫폼 운영자 | 설치, 기동·종료, 배포, 관측, 용량, 정상화 | 업무 API 개발은 01 매뉴얼 |
| DBA | 계정, Schema, Migration, Drift, Backup·Restore | 업무 SQL 설계는 DB 표준서 |
| 보안 담당자 | Secret, Certificate, TLS, 권한 분리, 감사 | 업무 Permission 설계는 01·03·95 |
| 배포 담당자 | Artifact, Checksum, Manifest, Rollback | Starter 선택은 90 매뉴얼 |
| Docker 환경 운영자 | 통합시험 Runtime 설치·선택 기동 | 상세 명령은 `cpf-docs/environment/docker/` |

운영자는 CPF가 소유하지 않는 업무 원장을 직접 수정하지 않는다. 상태 불일치는 Owner API, Operation, Outbox·Inbox, Batch Metadata, Audit와 대사하여 정상화한다.

## 2. 지원 환경과 설치 기준

### 2.1 정본 Stack과 Artifact

| 구분 | 제품 계약 | 운영 확인 |
|---|---|---|
| Java | 25 | Runtime과 Build Toolchain 일치 |
| Gradle | 9.1.0 | Wrapper 사용 |
| Spring Boot | 4.1.0 | BOM·Manifest 일치 |
| Spring Cloud | 2025.1.2 | Gateway/Resilience 호환 |
| Spring Batch | 6.0.4 | Job Repository·Restart 의미 |
| DB Vendor | MariaDB·PostgreSQL·Oracle | Fresh·Upgrade·Rollback·Restore |
| Artifact | JAR·WAR·Static·Worker·Job Pack·Route Pack | Source SHA·Checksum·SBOM·Signature |
| Container | Docker Linux/amd64 개발·시험 환경 | Image ID/Digest·Compose·Secret |

### 2.2 Docker 개발·시험 Runtime

현재 설치 Script는 MariaDB 12.3.2, PostgreSQL 18.4, Oracle 26ai Free, Redis 8.8.1, Kafka 4.3.1, Toxiproxy 2.12, OTel Collector 0.157, Trivy 0.70, ORT 87.3, Java25/Node22/PowerShell7.6.4/Playwright1.62 Toolchain을 준비한다. 확장 설치는 WireMock·SFTP·Vault·Keycloak을 추가한다.

RabbitMQ·JMS·IBM MQ·TCP·Notification Starter는 실제 Consumer와 Reliability/Attempt/Receipt Ledger를 검증할 Compose·Fixture·Fault Scenario를 추가해 운영 인수한다. 상세 기준은 `cpf-docs/environment/docker/` 문서를 따른다.


## 3. 설치 전 점검

어느 폴더에서 실행해도 되도록 Repository 경로를 명시한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}; git -C $repo remote get-url origin; git -C $repo branch --show-current; git -C $repo rev-parse HEAD; git -C $repo rev-parse origin/master; git -C $repo status --short; git -C $repo diff --name-status; git -C $repo ls-files --others --exclude-standard
```

확인 기준:

1. Branch가 `master`인지 확인한다.
2. Local 변경과 미추적 파일을 Owner별로 분류한다.
3. Ahead·Behind·Diverged이면 임의 정리하지 않는다.
4. 배포 대상 Commit, Artifact Version, SHA-256을 기록한다.
5. 대상 Topology, Instance 수, DB Vendor, Message Provider, Secret Provider를 확정한다.
6. Port·DNS·TLS·Proxy·Firewall·시간 동기화를 확인한다.
7. Backup ID, Rollback Version, 작업 중단 기준과 승인자를 기록한다.

## 4. Artifact·Checksum·Manifest

### 4.1 필수 인계 항목

| 항목 | 판정 기준 |
|---|---|
| Artifact 이름·Version | 배포 요청과 일치 |
| Git Commit | Source 기준과 일치 |
| SHA-256 | 전송 전·후 동일 |
| Build Manifest | 모듈·의존성·Build 도구 Version 기록 |
| SBOM | 배포 Artifact와 같은 Build에서 생성 |
| DB Migration Pack | 대상 Vendor와 Version 일치 |
| Config Manifest | Profile·Property Version·Checksum 기록 |
| Rollback Artifact | 실제 저장 위치와 Hash 확인 |

### 4.2 확인 명령 예

```powershell
$artifact='C:\path\to\cpf-artifact.jar'; if(-not(Test-Path -LiteralPath $artifact -PathType Leaf)){throw "Artifact가 없습니다: $artifact"}; Get-Item -LiteralPath $artifact | Select-Object FullName,Length,LastWriteTime; Get-FileHash -LiteralPath $artifact -Algorithm SHA256
```

Artifact 이름만 같고 Hash가 다르면 배포하지 않는다. 재빌드 Artifact는 같은 Version을 덮어쓰지 않고 새 식별자 또는 Build Metadata로 구분한다.

## 5. 계정·Directory·파일 권한

| 대상 | 권장 분리 | 금지 또는 주의 |
|---|---|---|
| OS·Container User | Runtime 전용 계정 | 관리자 계정 상시 실행 |
| DB 계정 | Admin·Migration·Runtime·ReadOnly | Runtime DDL 권한 |
| Artifact Directory | 읽기 전용 배포본·별도 작업 Directory | 실행 중 Artifact 덮어쓰기 |
| Log Directory | Runtime 쓰기·운영 읽기 | Secret·개인정보 원문 기록 |
| Temp Directory | 용량·보존·정리 기준 | 광역 재귀 삭제 |
| Backup Directory | 운영계와 분리 | 복원 시험 없는 보관 |
| Secret Root | Repository 밖 접근 제한 | Git·ZIP·Evidence 포함 |

경로 예:

```text
Repository   : C:\dev\projects\jck\202412_01_CPF
Docker Root  : C:\dev\Docker\CPF
Secret Root  : C:\dev\Docker\Secrets
```

## 6. Property·환경변수·Profile 관리

### 6.1 Property Catalog 필수 열

```text
Key / Environment Variable / Type / Default / Required / Range
Consumer / Profile / Restart Required / Secret
Failure Symptom / Verify Command / Expected Result / Rollback
```

### 6.2 기준 Source에서 확인된 공통 설정

| 설정 | 환경변수·대체 입력 | Type·기본값 | Consumer | 변경 영향 |
|---|---|---|---|---|
| `cpfArtifactMode` | `CPF_ARTIFACT_MODE` | Enum, 기본 `LOCAL_DEV` | `settings.gradle` Plugin Resolution | Build 재실행 |
| `cpfArtifactRepositoryUrl` | `CPF_ARTIFACT_REPOSITORY_URL` | URL, REMOTE에서 필수 | Build Plugin·Dependency Resolution | Build 재실행 |
| Local Artifact Repository | `CPF_LOCAL_ARTIFACT_REPOSITORY` | 경로, 기본 사용자 홈 `.cpf/repository` | Local Build | Build 재실행 |
| Offline Artifact Repository | `CPF_OFFLINE_ARTIFACT_REPOSITORY` | 경로, OFFLINE에서 필수 | Offline Build | Build 재실행 |
| Repository User | `CPF_ARTIFACT_REPOSITORY_USER` | 문자열 | REMOTE Repository | Build 재실행 |
| Repository Password | `CPF_ARTIFACT_REPOSITORY_PASSWORD` | Secret | REMOTE Repository | Build 재실행 |
| Reference DB Vendor | `REF_DATABASE_VENDOR` 또는 Gradle `cpfDbVendor` | `mariadb`·`postgresql`·`oracle`, 기본 `mariadb` | `cpf-reference` Test·Migration 선택 | Test·Runtime 재기동 |
| Reference Feature Flags | `cpf.reference.features.<name>.enabled` | Boolean, 기준 Source 기본 `true` | `cpf-reference` Source Set | Build 재실행 |

Property가 문서에 없더라도 Source에 존재하면 Catalog 누락이다. 반대로 Source에 없는 Key를 문서에서 만들지 않는다.

### 6.3 변경 절차

1. 변경 목적·영향 Consumer·대상 Profile을 기록한다.
2. Secret 여부와 원문 노출 가능성을 확인한다.
3. Type·Range·Default·필수 여부를 검증한다.
4. 사전 Validation 또는 Dry Run을 실행한다.
5. Property Version·Checksum을 생성한다.
6. 제한 Target에 적용하고 ACK·NACK를 수집한다.
7. Health뿐 아니라 실제 업무 Probe를 확인한다.
8. 일부 Target만 적용되면 `PARTIAL_SUCCESS`로 기록하고 신규 변경을 중지한다.
9. Reconcile 또는 이전 Config Version으로 Rollback한다.

## 7. Secret·Certificate

### 7.1 Secret 원칙

- Secret 원문을 Git, Markdown, CSV, JSON Evidence, 명령 이력, 화면 캡처에 저장하지 않는다.
- Secret은 파일 경로·Reference ID·Version·만료일만 기록한다.
- 필수 Secret이 없거나 만료되면 운영 Profile Readiness를 열지 않는다.
- Rotation은 Old/New 중첩 시간, Target별 적용 결과, Rollback 조건을 포함한다.

### 7.2 Docker Secret Source 예

현재 Compose Source가 참조하는 파일 예:

```text
C:\dev\Docker\Secrets\redis-password.txt
C:\dev\Docker\Secrets\sftp-password.txt
C:\dev\Docker\Secrets\vault-token.txt
C:\dev\Docker\Secrets\keycloak-admin-password.txt
C:\dev\Docker\Secrets\keycloak-test-password.txt
C:\dev\Docker\Secrets\keycloak-service-client-secret.txt
```

파일 존재 여부만 확인하고 내용은 출력하지 않는다.

```powershell
$secretRoot='C:\dev\Docker\Secrets'; $names=@('redis-password.txt','sftp-password.txt','vault-token.txt','keycloak-admin-password.txt','keycloak-test-password.txt','keycloak-service-client-secret.txt'); foreach($n in $names){$p=Join-Path $secretRoot $n;[pscustomobject]@{Name=$n;Exists=Test-Path -LiteralPath $p -PathType Leaf}}
```

### 7.3 Certificate 점검

- Subject·SAN·Issuer·Chain
- NotBefore·NotAfter
- TLS Protocol·Cipher
- mTLS Client Certificate 매핑
- Revocation·Rotation 계획
- Clock Skew
- 이전 인증서 Rollback 가능 시간

## 8. DB 설치·Migration·Drift

### 8.1 계정 분리

| 계정 | 허용 | 금지 |
|---|---|---|
| Admin | 계정·DB·Schema 준비 | Application 상시 사용 |
| Migration | DDL·Migration 기록 | 업무 Runtime 사용 |
| Runtime | DML·필요 Sequence·Procedure 실행 | 임의 DDL |
| ReadOnly | 조회·검수 | 변경 |

### 8.2 Lifecycle

```text
Empty Environment
→ Fresh Install
→ Verify
→ Runtime Query
→ Upgrade
→ Verify
→ Rollback 또는 Forward Recovery
→ Reapply
→ Drift Detection
→ Backup
→ Isolated Restore
→ Application Query
```

### 8.3 설치 명령 시작점

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\initialize-cpf-database.ps1') -All -RequireRun
```

실제 Script Parameter는 `Get-Help -Full`로 확인한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; Get-Help (Join-Path $repo 'cpf-tools\scripts\initialize-cpf-database.ps1') -Full
```

### 8.4 정상 결과

- 설치 전 CPF Object Count 0 또는 승인된 Baseline
- 대상 Vendor용 Migration Pack 선택
- Migration History 중복·누락 없음
- Verify Script Exit Code 0
- Runtime 계정 Query 성공
- Upgrade 후 기존 데이터 의미 보존
- Rollback/Forward Recovery 후 Application Query 성공
- Drift 0 또는 승인된 예외 목록

### 8.5 실패·부분 실패

- DDL 일부 적용 후 실패하면 동일 Script 무조건 재실행 전에 Migration History와 실제 Object를 대조한다.
- Vendor별 SQL 의미가 다르면 한 Vendor 성공을 전체 성공으로 승계하지 않는다.
- Rollback SQL이 데이터 손실을 유발하면 Forward Recovery를 사용하고 승인·Backup ID를 기록한다.

## 9. 메시지 브로커 운영

### 9.1 제공 범위 범위

Kafka·RabbitMQ·Jakarta JMS·IBM MQ Provider는 공통 Reliability JDBC 계약으로 운영한다. 각 Provider는 Named Binding, 실제 Consumer, Destination·권한·Health, ACK/Confirm/Reason Code, DLQ/Replay와 결과 대사 절차를 갖는다.

### 9.2 운영 점검표

```text
Broker/Cluster Health
Topic·Queue·Subscription Definition
Partition·Replication·Durability
Producer Receipt·Confirm
Consumer Group·Instance·Assignment
Backlog·Lag·Oldest Age
Retry·DLQ/DLT·Poison Message
Schema·Header·Idempotency
Credential·TLS·Certificate
Replay Approval·Audit
```

### 9.3 결과 불명과 대사

Publisher가 전송 후 Receipt를 받지 못하면 신규 업무 요청을 만들지 않는다. Outbox·Attempt·Broker 상태·Consumer 업무 원장을 조회하여 `UNKNOWN_RESULT`를 해소한다.

## 10. 기동·Readiness·종료

### 10.1 기동 순서

1. DB·Secret Provider·메시지 브로커·Collector를 준비한다.
2. Migration Version·Drift·Backup ID를 확인한다.
3. Control Plane·Owner Service·Worker·Edge 순으로 기동한다.
4. Liveness가 아니라 Readiness와 실제 Synthetic Probe를 확인한다.
5. 제한된 Traffic을 열고 오류율·지연·Backlog·업무 대사를 관찰한다.

로컬 Runtime 시작점:

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\start-cpf-local.ps1'); pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\status-cpf-local.ps1')
```

### 10.2 종료 순서

1. 신규 Traffic과 Schedule을 중지한다.
2. Worker 신규 Claim을 차단한다.
3. 진행 Transaction·Outbox·Checkpoint·File Transfer를 Drain한다.
4. Runtime을 종료한다.
5. 필요 서비스만 중지하고 Volume은 보존한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\stop-cpf-local.ps1')
```

## 11. 배포 전략

### 11.1 Rolling

사용 조건:

- Mixed Version API 호환
- DB Expand/Contract 순서 검증
- Message Schema 하위 호환
- Worker·Scheduler 중복 실행 통제

### 11.2 Blue-Green

- DB·Broker·File Store 공유 시 이중 처리 방지
- 전환 전 Green Synthetic Probe
- 전환 후 Blue의 Worker·Scheduler 중지
- Rollback Traffic 경로와 Session 영향 확인

### 11.3 Canary

- 대상 Instance·Route·사용자 구간 정의
- 오류율·지연·Backlog·업무 건수·금액 대사 기준 설정
- 중단 임계치 도달 시 확대 중지와 Rollback

### 11.4 Partial Apply

Target별 Version·Checksum·ACK/NACK를 저장한다. 일부 Target만 성공하면 신규 배포를 겹치지 않고 성공 Target 유지 또는 이전 Version 복귀를 결정한다.

## 12. Log·Metric·Trace·Audit

### 12.1 Log

최소 상관 식별자:

```text
requestId / traceId / transactionId / operationId
businessKey / idempotencyKey / attempt
state / failureClass / owner / target
```

Payload·Password·Token·개인정보 원문을 기록하지 않는다.

### 12.2 Metric

- Request·Error·Latency
- Retry·Reconcile·UNKNOWN_RESULT
- Broker Backlog·DLQ/DLT
- DB Pool·Lock·Slow Query
- Lease Conflict·Fencing Rejection
- Batch Read·Write·Skip·Rollback
- File Size·Transfer·Checksum Failure
- OTel Export Backpressure

### 12.3 Trace

Gateway→Owner→DB·Broker·외부 Provider의 전체 Timeout Budget과 Attempt를 연결한다. Sampling으로 누락되는 위험 거래는 Audit·Operation으로 보완한다.

### 12.4 Audit

운영 변경은 Actor, Role, Permission, Data Scope, Reason, Approval, Expected Version, Before/After, Target Result를 남긴다.

## 13. Capacity 관리

| 자원 | 관측 | 임계 시 행동 |
|---|---|---|
| CPU·Memory·Heap·GC | 사용률·Pause·OOM | Traffic·Concurrency 제한 |
| Thread·Connection Pool | Active·Queue·Timeout | 원인 제거 전 무조건 증설 금지 |
| DB | Session·Lock·IO·Disk | 신규 작업 제한·Slow Query 분석 |
| Broker | Partition·Queue·Lag·Oldest Age | Consumer·Poison·Provider 상태 확인 |
| Batch | Chunk·Partition·Worker·Commit | 분할·Commit Interval 조정 |
| File | Size·Concurrency·Disk | 신규 수락 제한·보존 정책 실행 |
| Log·Trace | Queue·Drop·Exporter Latency | Sampling·Buffer·Collector 점검 |

부하 시험은 정상 처리량뿐 아니라 Provider 장애 중 Backlog 증가율과 정상화 시간을 측정한다.

## 14. Backup·Restore

### 14.1 범위

- DB Data·Migration History
- Broker Metadata·필요 Message Snapshot
- Config Version·Checksum
- Certificate·Secret Reference Metadata
- Gateway Route·Policy·LKG
- BZA 조직·권한·결재 정책 Version
- Artifact Manifest·SBOM

### 14.2 Restore 판정

격리 환경에서 Restore 후 Runtime을 기동하고 실제 조회·권한·결재·배치·메시지 Probe를 수행한다. Backup 파일 존재만으로 Restore 성공으로 기록하지 않는다.

## 15. Upgrade·Rollback

```text
Compatibility Matrix
→ Backup·Restore 확인
→ DB Pre-check
→ Artifact·Config Validation
→ 제한 Target 적용
→ ACK·Readiness·업무 대사
→ 확대 또는 중단
→ Rollback/LKG/Forward Recovery
→ Drift 0 확인
```

Rollback 기준:

- 오류율·지연 임계 초과
- Readiness 실패
- DB·Message Schema 비호환
- Target Partial Apply
- 권한·Masking·Audit 회귀
- 업무 건수·금액 대사 불일치

## 16. 장애 Runbook

### 16.1 DB 연결·Lock·Drift

1. 연결 오류와 인증·네트워크·Pool 고갈을 구분한다.
2. Lock Owner·대기 Transaction·업무 Operation을 확인한다.
3. 직접 Row 수정 전에 Owner Service와 대사한다.
4. Drift는 승인된 Migration Pack과 실제 Object를 비교한다.
5. 정상화 후 Runtime Query·업무 대사·Audit를 확인한다.

### 16.2 메시지 브로커

1. Broker Health, Producer Receipt, Consumer Assignment를 분리 확인한다.
2. Backlog·Oldest Age·Poison Message를 확인한다.
3. 중복 가능성이 있으면 Idempotency·Inbox를 확인한다.
4. Replay는 승인·범위·시작점·종료점·Audit를 기록한다.
5. 업무 원장과 Consumer Result를 대사한다.

### 16.3 Instance Crash·OOM·Stuck

- 마지막 Operation·Lease·Fencing Token
- Thread Dump·Heap·GC·Container Exit Code
- Readiness·Traffic 제외 여부
- 재기동 후 과거 Owner가 상태를 덮어쓰지 않는지 확인

### 16.4 Network·TLS

DNS, TLS Chain, Clock, Connection Reset, Latency, Half-open, Packet Loss를 분리한다. Toxiproxy Fault를 사용했다면 적용·제거 시간을 기록한다.

### 16.5 Disk·Memory 임계

신규 작업 수락을 제한하고 정확한 대상 파일·보존기간만 정리한다. `docker system prune`, `docker volume prune`, Repository 전체 미추적 파일 삭제를 사용하지 않는다.

### 16.6 설정 부분 적용

Target별 Version·Checksum을 수집하고 성공 Target을 다시 변경하지 않는다. NACK 원인을 제거한 뒤 실패 Target만 Reconcile하거나 전체를 LKG로 되돌린다.

## 17. DR

- 전환 조건·승인자·선언 시각
- DNS·Route·Certificate·Secret 전환
- DB·Broker·Object/File Restore 순서
- Scheduler·Worker 중복 기동 차단
- Operation·Outbox·Receipt·Batch Metadata 대사
- RPO·RTO Actual 기록
- 원복 조건과 Data Reconciliation

## 18. Docker 개발·시험 환경

상세 문서: [`cpf-docs/environment/docker/`](../environment/docker/README.md)

기본 원칙:

- Container 자동 시작 금지, `restart: "no"`
- 필요한 서비스만 시작
- 작업 종료 시 이번 작업에서 시작한 서비스만 중지
- 사용자 DB·Volume·Image·Secret 임의 삭제 금지
- Runtime Source와 실행본 Hash 비교
- Container Health와 Product Consumer 시험을 분리

## 19. 플랫폼 운영 EDU

### EDU-OPS-01 — 신규 설치와 정상 종료

1. 별도 QA DB와 Secret Root를 준비한다.
2. Artifact·Hash·Manifest를 확인한다.
3. DB Fresh Install·Verify를 실행한다.
4. 필요한 Docker Service만 기동한다.
5. Runtime을 기동하고 Readiness·Synthetic Probe를 확인한다.
6. Log·Metric·Trace·Audit 상관 식별자를 확인한다.
7. 신규 작업 수락을 중지하고 Drain 후 종료한다.
8. Running CPF Container 0과 Volume 보존을 확인한다.

### EDU-OPS-02 — 응답 유실과 결과 대사

1. 외부 Provider 지연 또는 연결 Reset을 주입한다.
2. 호출 Timeout과 Operation 상태를 확인한다.
3. 신규 요청을 만들지 않고 Attempt·Outbox·Target 결과를 대사한다.
4. `UNKNOWN_RESULT` 해소 후 업무 건수·Audit를 확인한다.

### EDU-OPS-03 — 부분 적용과 Rollback

1. 다중 Target 중 하나에 잘못된 Config를 적용해 NACK를 재현한다.
2. Traffic 확대를 중지한다.
3. Target별 Version·Checksum을 수집한다.
4. 실패 Target Reconcile 또는 LKG Rollback을 수행한다.
5. Drift 0과 업무 Probe를 확인한다.

직접 실행한 환경·명령·Exit Code·시작/종료 시각·Sanitized Evidence Hash를 남긴다.

### 19.1 플랫폼 운영 EDU 15개 전수표

`EDU-OPS-01~15`는 전체 135개 EDU 체계의 플랫폼 운영 영역이다. 일부 항목은 격리 Process Script로 실행할 수 있지만, 격리 실행 성공을 실제 DB·Broker·배포·DR·보안 사고 시험으로 대체하지 않는다.

| 교육 ID | 확인할 기능 | 역할 | 실행 안내 | 완료 판정 |
|---|---|---|---|---|
| `EDU-OPS-01` | 신규 환경 설치·Artifact·Checksum 검증 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-02` | Profile·환경변수·Property 전체 검증 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-03` | Secret·Certificate 배포·Rotation·만료 대응 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-04` | DB 3종 Fresh·Migration·Drift·Rollback | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-05` | 메시지 Broker Topic·ACL·Consumer Group Lifecycle | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-06` | 기동·종료·Health·의존 대상 순서 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-07` | Rolling 배포·Session·Connection Drain | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-08` | Blue-Green·Canary 전환·Rollback | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-09` | Config 변경 Partial Apply·Reconciliation | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-10` | Log·Metric·Trace 수집 장애·Retention·Capacity | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-11` | Backup·Restore·Point-in-time Recovery·대사 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-12` | DR 전환·복귀·Split-brain 방지 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-13` | Disk·Memory·Network·DB 장애 Runbook | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-14` | 보안 사고·계정·Key·Session 긴급 차단 | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-OPS-15` | Upgrade·DB 호환·Application Rollback | `CPF_PLATFORM_OPERATOR` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |

전수 판정 절차:

1. `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1`의 허용 ID와 기능 Catalog를 대조한다.
2. Process Consumer가 생성한 결과 경로와 SHA-256을 확인한다.
3. 실제 DB·Broker·Container·Application Instance를 사용한 시험은 별도 실행한다.
4. Backup·Restore·DR·Security 항목은 복원 후 업무 합계·Version·Permission·Audit까지 대사한다.

## 20. 운영 인계표

```text
Git Commit / Artifact Version / SHA-256 / SBOM
Topology / Instance / Port / DNS / TLS
Property Version / Environment Variable / Profile
Secret Reference / Certificate Expiry
DB Vendor / Migration Version / Drift / Backup ID
Broker / Destination / Consumer / Retry / DLQ
Health / Readiness / Synthetic Probe
Log / Metric / Trace / Audit Dashboard
Capacity / Threshold / Alert Owner
Deployment / Rollback / LKG
Fault / DR Runbook
제약·운영 전제·환경별 결정값
```

## 21. 완료 점검표

- [ ] 기준 Source과 Artifact Hash가 기록됐다.
- [ ] Property·환경변수·Profile·Secret Catalog가 실제 Consumer와 일치한다.
- [ ] DB Vendor별 설치·변경·대사·Restore가 확인됐다.
- [ ] 기동·Readiness·Synthetic Probe·종료 결과가 기록됐다.
- [ ] Target별 배포·Config ACK와 Partial Apply 처리가 확인됐다.
- [ ] Log·Metric·Trace·Audit가 같은 Operation을 가리킨다.
- [ ] Backup·Restore·Upgrade·Rollback·DR 절차가 실행 기록과 연결된다.
## 22. 종단간 예제: 신규 환경 설치와 첫 업무 확인

### 22.1 업무 결과

빈 서버·DB 환경에 CPF Artifact, DB, Broker, Secret, ADM/BZA/Gateway와 업무 Runtime을 설치하고 상태 점검·업무 Smoke Test·Backup 기준을 확정한다.

### 22.2 선택 기준

신규 개발·시험·검증·운영 환경을 만들 때 사용한다. 기존 환경 업그레이드는 Upgrade 장의 호환·Rollback 절차를 함께 적용한다.

### 22.3 역할과 권한

플랫폼 운영자, DBA, 보안, 배포, 관측 담당자 권한을 분리한다. 초기 관리자와 장기 운영 계정을 분리한다.

### 22.4 시작 전에 결정할 값

환경명, Network Zone, Host/Container, Java/Profile, DB Vendor, Broker Provider, Port, Directory, Service Account, Secret/Certificate, Artifact Repository, Backup/DR 목표를 정한다.

### 22.5 결과물

설치 계획, 계정·Directory, Artifact Manifest, Config Catalog, DB/Broker 설치 기록, Health/Smoke 결과, Backup 기준, 운영 인계.

### 22.6 단계별 절차

1. Repository·Branch·Artifact Version·SHA-256·SBOM·서명을 확인한다.
2. Service Account와 Directory를 만들고 소유권·권한을 적용한다.
3. Secret Provider와 Certificate Trust를 준비한다.
4. DB Vendor별 Admin/Migration/Runtime/ReadOnly 계정을 만든다.
5. Fresh Migration과 초기 데이터를 적용하고 Schema Version·Drift를 확인한다.
6. Broker와 Topic/Queue·ACL·Consumer Group을 준비한다.
7. Profile과 Property를 환경별 Config Source에 등록한다.
8. 의존 Runtime → 업무 Runtime → ADM/BZA/Gateway 순서로 기동한다.
9. Liveness, Readiness, Version, Config Checksum, DB/Broker 연결을 확인한다.
10. 조회·Command·Batch·Message·File·Gateway의 Smoke Test를 실행한다.
11. Log·Metric·Trace·Audit 수집과 Alert를 확인한다.
12. 첫 Backup을 만들고 Restore 절차·보존·Owner를 기록한다.

### 22.7 입력·기본값·허용 범위

| 입력 | 예 | 규칙 |
|---|---|---|
| Environment | `dev`, `test`, `prod` | 이름·Network·Data 분리 |
| Artifact Version | Release Manifest 값 | SHA-256·SBOM 일치 |
| Profile | 환경 Profile | 암묵 Default 금지 |
| DB Vendor | oracle/postgresql/mariadb | Vendor Pack과 일치 |
| Secret Reference | Provider Path/Key | 원문 파일 저장 금지 |
| Timezone | `Asia/Seoul` 등 | DB·JVM·Scheduler 일치 |

### 22.8 정상 결과와 완료 판정

모든 Process가 기대 Version·Config Checksum으로 Ready이고 Smoke Test의 업무·DB·Log·Trace·Audit가 같은 ID로 연결된다. Drift·미적용 Instance·미수집 신호가 0이다.

### 22.9 오류·동시성·시간초과·응답 유실·부분 실패

Artifact Hash 불일치, Migration 일부 실패, Secret 권한 오류, Certificate 오류, Broker ACL, Readiness 실패, Config 부분 적용, Port 충돌을 단계별로 구분한다.

### 22.10 재시도·재처리·대사·보상·되돌리기

실패 단계 이후를 진행하지 않는다. Migration은 원인 수정·상태 검토 후 재실행한다. Config는 이전 Version으로 복원한다. 일부 Instance만 적용됐으면 목표/LKG Version으로 Reconcile한다.

### 22.11 로그·지표·추적·감사

설치 Operation, Artifact/Config/Schema Version, Instance ID, Health, Migration, Broker, Smoke Transaction, Audit, Alert를 기록한다.

### 22.12 교육 예제

`EDU-OPS-01`, `EDU-OPS-02`, `EDU-OPS-04`, `EDU-OPS-06`을 실행한다.

### 22.13 조직 영역과 CPF 유지 영역

환경별 Host·Port·계정·보존·용량은 도입 조직이 정한다. Artifact Trust, Config/Secret, Health, Migration, Audit 계약은 CPF가 유지한다.

### 22.14 운영 인계

환경 구성도, 계정·Directory, Artifact·Checksum, Property·Secret, DB/Broker, 기동·종료, Backup·DR, 연락망을 인계한다.


## 23. Artifact·Checksum·SBOM·공급망

### 23.1 업무 결과

배포 파일의 출처·내용·의존성·Source SHA를 검증하고 승인된 Artifact만 설치한다.

### 23.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 23.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 23.4 시작 전에 결정할 값

Release Manifest, Artifact Mode, Repository, SHA-256, Signature, SBOM, Java/WAS 호환을 정한다.

### 23.5 결과물

Artifact·Checksum·SBOM·공급망 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 23.6 단계별 절차

Manifest를 읽고 파일별 Hash·크기·Version을 확인한다. SBOM과 취약점 정책을 검토하고 배포 전·후 Hash를 비교한다.

### 23.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 23.8 정상 결과와 완료 판정

설치 파일과 Manifest가 일치하고 Source SHA·Build Mode·Dependency가 추적된다.

### 23.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 23.10 재시도·재처리·대사·보상·되돌리기

불일치 Artifact는 격리하고 재다운로드한다. 일부 Node가 다른 Hash면 트래픽에서 제외하고 승인본을 재배포한다.

### 23.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 23.12 교육 예제

`EDU-OPS-01·15`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 23.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 23.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 24. 계정·Directory·파일 권한

### 24.1 업무 결과

Service Account와 Runtime·Config·Log·Data·Temp·Backup Directory를 최소 권한으로 분리한다.

### 24.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 24.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 24.4 시작 전에 결정할 값

계정, Group, UID/GID, 소유권, 읽기/쓰기/실행, Umask, Rotation, Temp Cleanup을 정한다.

### 24.5 결과물

계정·Directory·파일 권한 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 24.6 단계별 절차

Directory를 만들고 Service Account 소유권을 적용한다. Config/Secret은 읽기 전용, Data/Log/Temp는 필요한 쓰기만 허용한다.

### 24.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 24.8 정상 결과와 완료 판정

다른 서비스 계정이 Secret/Data를 읽지 못하고 Process가 필요한 경로만 사용한다.

### 24.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 24.10 재시도·재처리·대사·보상·되돌리기

권한 오류는 관리자 권한으로 Process를 실행해 우회하지 않고 소유권·ACL을 수정한다.

### 24.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 24.12 교육 예제

`EDU-OPS-01·14`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 24.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 24.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 25. Property·환경변수·Profile

### 25.1 업무 결과

모든 설정의 Key·Type·Default·범위·Consumer·재기동·Rollback을 Catalog로 관리한다.

### 25.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 25.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 25.4 시작 전에 결정할 값

환경별 값, Secret 여부, 변경 방식, Dynamic/Restart, Validation, Config Version/Checksum을 정한다.

### 25.5 결과물

Property·환경변수·Profile 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 25.6 단계별 절차

정본 Catalog에서 환경 Overlay를 만들고 Secret Reference를 연결한다. Apply 전 Diff/Preview, 적용 후 Instance별 Version/Checksum을 대사한다.

### 25.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 25.8 정상 결과와 완료 판정

모든 Instance가 같은 목표 Version을 사용하고 잘못된 값은 기동 또는 적용 전에 거부된다.

### 25.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 25.10 재시도·재처리·대사·보상·되돌리기

부분 적용은 성공 Instance를 유지하고 실패·미확정만 재적용하거나 LKG로 복원한다.

### 25.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 25.12 교육 예제

`EDU-OPS-02·09`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 25.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 25.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 26. Secret·Certificate·Key Rotation

### 26.1 업무 결과

비밀번호·Token·Private Key·Certificate를 원문 노출 없이 배포·교체·회수한다.

### 26.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 26.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 26.4 시작 전에 결정할 값

Provider, Path, ACL, Rotation 주기, 이전/신규 공존, 만료 경보, 폐기·사고 대응을 정한다.

### 26.5 결과물

Secret·Certificate·Key Rotation 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 26.6 단계별 절차

Secret Reference를 배포하고 접근을 Test한다. Rotation은 신규 발급→양쪽 신뢰→Consumer 전환→이전 폐기 순서로 수행한다.

### 26.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 26.8 정상 결과와 완료 판정

원문이 Repository·Config·Log·명령 이력에 없고 모든 Consumer가 신규 Version을 사용한다.

### 26.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 26.10 재시도·재처리·대사·보상·되돌리기

일부 Consumer 전환 실패는 이전/신규 공존 기간 안에 Reconcile한다. 유출 Version은 즉시 차단·교체하고 Session/Token을 회수한다.

### 26.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 26.12 교육 예제

`EDU-OPS-03·14`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 26.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 26.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 27. DB 3 Vendor 설치·Migration·Drift

### 27.1 업무 결과

Oracle·PostgreSQL·MariaDB를 Fresh 설치하고 Upgrade·Rollback·Restore·Drift를 같은 의미로 운영한다.

### 27.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 27.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 27.4 시작 전에 결정할 값

Schema·계정·Tablespace/Database, Charset, Timezone, Connection Pool, Backup, Migration Window를 정한다.

### 27.5 결과물

DB 3 Vendor 설치·Migration·Drift 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 27.6 단계별 절차

Admin 계정으로 저장소를 준비하고 Migration 계정으로 Vendor Pack을 적용한다. Runtime 계정 권한을 확인하고 Fresh/Upgrade Schema Hash·대사 SQL을 실행한다.

### 27.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 27.8 정상 결과와 완료 판정

Schema Version·Constraint·Index·초기 데이터·업무 Query 의미가 3 Vendor에서 일치한다.

### 27.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 27.10 재시도·재처리·대사·보상·되돌리기

Lock·Disk·권한·부분 Migration을 구분하고 Backup·Flyway 상태·대사 후 재실행 또는 Forward Fix한다.

### 27.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 27.12 교육 예제

`EDU-OPS-04·11·15`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 27.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 27.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 28. 메시지 Broker·Provider 운영

### 28.1 업무 결과

Kafka·RabbitMQ·JMS·IBM MQ 등 선택 Provider의 Topic/Queue·ACL·보존·소비·DLQ를 운영한다.

### 28.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 28.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 28.4 시작 전에 결정할 값

Provider, Cluster, Destination, Partition/Queue, HA, Retention, ACL, Retry, DLQ, Replay, Capacity를 정한다.

### 28.5 결과물

메시지 Broker·Provider 운영 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 28.6 단계별 절차

Destination과 Service Account를 생성하고 Producer/Consumer 권한을 적용한다. Publish/Consume/ACK, Outbox/Inbox, DLQ Replay를 Smoke Test한다.

### 28.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 28.8 정상 결과와 완료 판정

Broker·Outbox·Inbox·업무 상태가 Message ID로 대사되고 Lag·Oldest Age·DLQ가 기준 안에 있다.

### 28.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 28.10 재시도·재처리·대사·보상·되돌리기

ACK 유실·Rebalance·Poison·Queue Full·Split Brain을 구분한다. Provider 상태 조회 전 Command를 반복하지 않는다.

### 28.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 28.12 교육 예제

`EDU-OPS-05·13`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 28.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 28.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 29. 기동·Readiness·종료

### 29.1 업무 결과

의존성 순서와 업무 준비 상태를 기준으로 Process를 기동·Drain·종료한다.

### 29.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 29.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 29.4 시작 전에 결정할 값

Startup Order, Health Endpoint, Warm-up, Connection Drain, Grace Period, 작업 중단·Lease 이전을 정한다.

### 29.5 결과물

기동·Readiness·종료 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 29.6 단계별 절차

DB/Broker/Secret/Discovery 상태를 확인하고 Runtime을 기동한다. Readiness와 업무 Smoke를 통과한 뒤 트래픽에 편입한다. 종료 전 새 요청·Claim을 차단하고 진행 작업을 Drain한다.

### 29.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 29.8 정상 결과와 완료 판정

Ready Instance만 트래픽을 받고 종료 후 활성 Lease·미확정 작업이 남지 않는다.

### 29.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 29.10 재시도·재처리·대사·보상·되돌리기

강제 종료 후 Operation·Lease·Outbox·Batch를 Reconcile하고 Stale Owner를 Fencing한다.

### 29.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 29.12 교육 예제

`EDU-OPS-06·13`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 29.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 29.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 30. Rolling·Blue-Green·Canary·Partial Apply

### 30.1 업무 결과

혼합 Version 구간의 호환성을 유지하며 단계적으로 배포하고 오류 증가 시 중단·Rollback한다.

### 30.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 30.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 30.4 시작 전에 결정할 값

배치 단위, Max Unavailable, Canary 비율, 관찰 시간, 중단 임계, DB/Message 호환, LKG를 정한다.

### 30.5 결과물

Rolling·Blue-Green·Canary·Partial Apply 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 30.6 단계별 절차

Preflight→Canary→관찰→확대→완료 순서로 진행한다. Instance별 Version·Checksum·Health·업무 지표를 확인한다.

### 30.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 30.8 정상 결과와 완료 판정

활성 Instance가 승인 Version이고 오류·지연·업무 대사가 기준 안에 있다.

### 30.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 30.10 재시도·재처리·대사·보상·되돌리기

부분 적용은 성공/실패/미확정을 분리한다. DB 비호환이면 Application Rollback을 중단하고 Forward Fix한다.

### 30.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 30.12 교육 예제

`EDU-OPS-07·08·09·15`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 30.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 30.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 31. Log·Metric·Trace·Audit·Alert

### 31.1 업무 결과

업무와 플랫폼 신호를 공통 식별자로 연결하고 민감정보 없이 탐지·진단·감사한다.

### 31.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 31.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 31.4 시작 전에 결정할 값

수집 Endpoint, Retention, Sampling, Label Cardinality, Masking, Alert, Audit Delivery를 정한다.

### 31.5 결과물

Log·Metric·Trace·Audit·Alert 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 31.6 단계별 절차

구조화 Log·Metric·Trace·Audit를 수집하고 Smoke Transaction으로 상관 검색한다. 경보를 발생·확인·종료해 연락망을 검증한다.

### 31.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 31.8 정상 결과와 완료 판정

Transaction ID 하나로 요청·DB·Broker·외부·Batch·Audit를 연결하고 신호 유실·PII 노출이 없다.

### 31.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 31.10 재시도·재처리·대사·보상·되돌리기

Collector 장애는 Runtime 업무를 무제한 Block하지 않되 Buffer·Drop·Backpressure를 Metric으로 남긴다. Audit는 재전송한다.

### 31.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 31.12 교육 예제

`EDU-OPS-10·13`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 31.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 31.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 32. Capacity·성능·자원 한도

### 32.1 업무 결과

CPU·Memory·Disk·Thread·Connection·Queue·Batch 처리량의 제한과 확장 조건을 관리한다.

### 32.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 32.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 32.4 시작 전에 결정할 값

SLO, Peak TPS, Batch Window, Pool, Queue, File Size, Disk Headroom, Scale 조건을 정한다.

### 32.5 결과물

Capacity·성능·자원 한도 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 32.6 단계별 절차

부하·장시간·대용량 Test로 포화 지점을 찾고 제한값·Backpressure·Alert를 설정한다.

### 32.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 32.8 정상 결과와 완료 판정

목표 부하에서 오류·지연·GC·Queue·Pool·Disk가 기준 안에 있고 과부하가 다른 기능으로 확산되지 않는다.

### 32.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 32.10 재시도·재처리·대사·보상·되돌리기

포화 시 신규 수락 제한·Queue 상한·Circuit Breaker·Scale-out을 적용하고 무제한 Thread/Queue 증가를 금지한다.

### 32.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 32.12 교육 예제

`EDU-OPS-10·13`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 32.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 32.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 33. Backup·Restore·PITR

### 33.1 업무 결과

DB·Broker Metadata·File·Config·Secret Metadata·Audit를 일관된 시점으로 백업하고 복원 후 업무를 대사한다.

### 33.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 33.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 33.4 시작 전에 결정할 값

RPO/RTO, Backup 범위, 암호화, 보존, Offsite, Restore 순서, 검증 주기를 정한다.

### 33.5 결과물

Backup·Restore·PITR 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 33.6 단계별 절차

백업을 생성하고 Hash·Catalog를 확인한다. 격리 환경에 복원한 뒤 Schema Version, 업무 건수·금액·Hash, Outbox/Inbox, File Hash, 권한을 대사한다.

### 33.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 33.8 정상 결과와 완료 판정

복원 환경이 지정 시점과 일치하고 미확정 외부 부수 효과가 별도 목록으로 남는다.

### 33.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 33.10 재시도·재처리·대사·보상·되돌리기

Backup 성공 Log만으로 완료 처리하지 않는다. 불완전 백업은 폐기하고 다음 주기를 기다리지 않고 재생성한다.

### 33.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 33.12 교육 예제

`EDU-OPS-11`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 33.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 33.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 34. DR Failover·Failback

### 34.1 업무 결과

주 Site 장애 시 단일 Writer를 보장하며 DR Site로 전환하고 데이터·메시지·외부 결과를 대사한 뒤 복귀한다.

### 34.2 선택 기준

해당 운영 기능이 필요한 모든 환경에 적용하며 외부 관리 제품을 사용하더라도 CPF의 Version·상태·대사 계약을 유지한다.

### 34.3 역할과 권한

플랫폼 운영자·DBA·보안·배포·관측·승인자 권한을 기능별로 분리한다.

### 34.4 시작 전에 결정할 값

RPO/RTO, Writer Fencing, DNS/Traffic, DB/Broker 복제, Secret, 외부 Allowlist, Failback을 정한다.

### 34.5 결과물

DR Failover·Failback 계획·설정·실행 기록·검증 증적·Rollback Runbook.

### 34.6 단계별 절차

사고 선언→주 Site 쓰기 차단→복제 위치 확인→DR 기동→Smoke→트래픽 전환→대사 순서로 수행한다.

### 34.7 입력·기본값·허용 범위

입력값은 환경 Catalog에 실제 Key·Type·Default·필수·범위·Consumer·재기동·Secret·Rollback 열로 관리한다.

### 34.8 정상 결과와 완료 판정

한 Site만 쓰기 가능하고 업무·Message·Batch·Audit가 RPO 범위 안에서 일치한다.

### 34.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Timeout·Network·Disk·부분 적용·응답 유실·혼합 Version을 독립적으로 판정한다.

### 34.10 재시도·재처리·대사·보상·되돌리기

Split Brain 위험이면 전환을 중단한다. Failback은 신규 변경과 복제 방향을 확인한 뒤 별도 승인으로 수행한다.

### 34.11 로그·지표·추적·감사

Environment, Instance, Version, Checksum, Operation, Actor, Approval, Result, Trace, Audit를 기록한다.

### 34.12 교육 예제

`EDU-OPS-12`를 실행해 정상·부분 실패·Rollback을 확인한다.

### 34.13 조직 영역과 CPF 유지 영역

환경 값과 용량·보존은 도입 조직이 정한다. 제품 계약·검증·대사 방식은 CPF가 유지한다.

### 34.14 운영 인계

설정·명령·정상 결과·경보·Rollback·연락망을 인계한다.


## 35. Property Catalog 표준

| Key | 환경변수 | Type | Default | 필수 | 범위 | 사용 기능 | Profile | 재기동 | 비밀값 | 오류 | 확인 명령 | 정상 결과 | 되돌리기 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cpfArtifactMode` | `CPF_ARTIFACT_MODE` | Enum | `LOCAL_DEV` | 배포 방식별 | LOCAL_DEV/REMOTE/OFFLINE | Build·Artifact 해석 | build | Build 재실행 | 아니오 | Repository 선택 오류 | Gradle properties 출력 | 승인 Mode 사용 | 이전 Mode 복원 |
| `cpfArtifactRepositoryUrl` | `CPF_ARTIFACT_REPOSITORY_URL` | URL | 없음 | REMOTE | HTTPS 승인 Repository | Dependency·Plugin | build | Build 재실행 | Credential 별도 | Resolution 실패 | Gradle Repository Report | 승인 URL만 사용 | 이전 URL 복원 |
| `cpf.reference.features.batch.enabled` | 환경 규칙에 따른 대응값 | Boolean | 환경 정책 | EDU | true/false | Batch EDU | edu | 예 | 아니오 | 기능 비활성 | Capability 조회 | 목록에 활성 표시 | 이전 값 복원 |
| `cpf.reference.features.operations.enabled` | 환경 규칙에 따른 대응값 | Boolean | 환경 정책 | EDU | true/false | ADM EDU | edu | 예 | 아니오 | 기능 비활성 | Capability 조회 | 목록에 활성 표시 | 이전 값 복원 |
| `cpf.reference.features.gateway.enabled` | 환경 규칙에 따른 대응값 | Boolean | 환경 정책 | EDU | true/false | Gateway EDU | edu | 예 | 아니오 | 기능 비활성 | Capability 조회 | 목록에 활성 표시 | 이전 값 복원 |
| `cpf.reference.features.backoffice.enabled` | 환경 규칙에 따른 대응값 | Boolean | 환경 정책 | EDU | true/false | BZA EDU | edu | 예 | 아니오 | 기능 비활성 | Capability 조회 | 목록에 활성 표시 | 이전 값 복원 |
| `cpf.repository-root` | 환경 규칙에 따른 대응값 | Path | 없음 | Process EDU | 존재하는 Repository Root | Process EDU | edu | 예 | 아니오 | 경로 검증 실패 | Process EDU Preflight | Source 경로 확인 | 이전 경로 복원 |

전체 Catalog는 Configuration Properties Source, 환경 Overlay, 배포 Manifest와 자동 대조한다. 문서 표에 없는 실제 Key가 발견되면 같은 변경에서 Catalog와 매뉴얼을 갱신한다.

## 36. 장애 Runbook 공통 8단계

1. 영향 업무·환경·Version·시작시각을 고정한다.
2. 최근 변경과 승인·배포 Operation을 확인한다.
3. Liveness·Readiness·업무 Smoke를 분리한다.
4. DB·Broker·Network·Disk·Memory·Certificate·Config 중 실패 영역을 좁힌다.
5. 결과가 생겼을 수 있는 Command·Batch·Message는 재실행 전에 대사한다.
6. 영향 확산을 차단하고 신규 요청·Claim·배포를 제한한다.
7. 승인된 Retry·Reconcile·LKG·Rollback·Forward Fix 중 하나를 선택한다.
8. 업무 상태·Version·Checksum·Audit·경보가 정상 기준에 도달하면 종료하고 재발 방지를 기록한다.

## 37. 환경 인수 체크리스트

- [ ] Artifact·Checksum·SBOM·Source SHA
- [ ] Service Account·Directory·ACL
- [ ] Property Catalog·Config Version·Secret Reference
- [ ] DB 3 Vendor 적용 대상과 Schema Version
- [ ] Broker Destination·ACL·Retention·DLQ
- [ ] Instance·Port·Health·Readiness·Version
- [ ] Log·Metric·Trace·Audit·Alert
- [ ] Capacity·Pool·Queue·Disk Headroom
- [ ] Backup·Restore·RPO/RTO
- [ ] Rolling·Canary·LKG·Rollback
- [ ] 장애·보안·DR 연락망
- [ ] ADM/BZA/Gateway 운영 계정과 Permission

## 38. Docker 개발·시험 환경 한 줄 실행

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\environment\docker-development-test\CPF_도커_개발테스트환경_전체설치.ps1') -RepoRoot $repo
```

실행 후에는 `docker compose ps`, 서비스별 Health, DB/Broker 연결, EDU·Fault Test를 확인한다. Container가 `Up`인 것만으로 CPF 업무 준비 상태를 판정하지 않는다.
