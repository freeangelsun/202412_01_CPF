# CPF 공식 문서 최종 정비 작업 지침

## 사용자 중심 문서 공통 규칙 (2026-08-16 보완)

이 절은 README를 제외한 모든 공식 매뉴얼·가이드·Specification에 공통 적용한다.

1. **독자는 CPF를 잘 아는 Framework 개발자가 아니라 처음 또는 자주 참조하는 실제 사용자다.** 문서 작성자의 의도, 내부 구현 배경, QA 과정, Framework 내부 책임을 설명하기보다 사용자가 자신의 목적에 맞는 기능·API·명령·옵션을 판단하고 적용하는 데 필요한 정보를 우선한다.
2. README를 제외한 공식 문서는 표지 다음에 **전체 목차 전용 페이지**를 둔다. 목차 항목은 해당 장으로 이동하는 내부 링크/Bookmark를 제공한다. 각 주요 장에는 전체 목차로 돌아가는 링크를 제공한다.
3. 문서 초반에는 문서의 성격이나 읽는 방법을 장황하게 설명하지 않는다. `이 문서는 ~ 문서입니다`, `먼저 아래 표를 보십시오` 같은 자기설명 문구는 제거하고, 필요한 경우 실제 기능 맥락을 설명하는 1~3문장 뒤에 Summary를 둔다.
4. Summary는 **상세 문서를 읽기 전에 기능의 존재, 용도, 선택 기준, 주요 옵션을 판단**하게 하는 영역이다. API 이름만 나열하거나 내부 Package를 나열하지 않는다.
5. Summary/기능표의 Header는 목적에 맞게 `기능`, `용도`, `사용 API/메소드`, `Annotation`, `주요 옵션`, `선택 기준`, `참고사항`, `실행 위치`, `명령어` 등을 사용한다. `먼저 볼 것`, `빈도`, `결과`처럼 모호하거나 비표준적인 Header를 반복 사용하지 않는다.
6. 표는 비교·선택·Reference처럼 표가 가장 읽기 좋은 경우에만 사용한다. **표 안 장문 설명을 금지**하고, 긴 설명은 본문·절차·예제로 이동한다. 짧은 값/상태/구분 열은 좁게, 설명/API/선택 기준 열은 넓게 하는 **내용 기반 가변 열 폭**을 사용한다. 모든 열을 동일 폭으로 나누지 않는다.
7. 많은 항목을 탐색하는 용도는 긴 표 한 장으로 해결하지 않는다. 목차·Quick Finder·기능군별 Summary·내부 링크를 사용해 빠르게 찾아가게 한다.
8. 기능 설명은 가능한 한 `표준 흐름/그림 → Summary → 선택 기준/옵션 → 최소 실무 예제 → 주의사항/상세 Reference` 순서로 구성한다. Source 예제만 던져 독자가 스스로 사용 체계를 추론하게 하지 않는다.
9. 명령어는 실제 Repository에서 지원되는 것만 작성하고, `작업 목적`, `실행 위치/경로`, `명령어`, `대상·주요 옵션`, `참고사항`으로 그룹화한다. 내부적으로 명령을 한 곳에 모은 이유 같은 구현 배경은 설명하지 않는다.
10. 문서 생성 후 DOCX와 PDF를 모두 실제 렌더하여 모든 페이지를 시각검수한다. 표 잘림, 과도한 행 높이, 코드 블록 분리, 글자 겹침, 한글 깨짐, 이미지 비율 불일치, 빈 페이지, 고아 제목을 허용하지 않는다.

11. 기능 목록을 많이 제공해야 하는 문서도 **핵심 Summary와 전체 Reference를 분리**한다. 처음 몇 페이지에는 핵심 기능만 보여주고, 50~100개 수준의 전체 API/기능 목록은 기능군별 Quick Reference/부록으로 내려 상세 탐색에 사용한다.
12. `Controller`, `Service`, `Repository/DAO`, `Transaction`, 내부 Domain 호출, 외부 연계처럼 사용 패턴이 중요한 기능은 단순 API 목록보다 **표준 흐름 그림과 대표 코드 패턴을 먼저 제공**한다.
13. 외부 연계·Messaging·Cache 등 계층이 여러 개인 기능은 업무 개발 Golden Path와 고급/저수준 API를 구분하여, 처음 사용하는 사용자가 내부 API부터 선택하지 않게 한다.

## 1. 작업 목적

CPF의 사용자·개발자·운영자 대상 공식 문서 체계를 최종 정비한다.

이번 작업의 목표는 문서를 많이 만드는 것이 아니다.

최종적으로 사용자가 README에서 시작하여 자신의 목적에 맞는 공식 문서로 이동하고, **아래 7종 문서만으로 CPF의 사용·개발·운영·기술 규격을 모두 확인할 수 있도록 하는 것**이 목표다.

문서를 세분화하여 새로운 Guide를 계속 추가하지 않는다.

기존 Repository에 존재하는 과거 문서, 중복 문서, 임시 문서, 더 이상 사용하지 않는 문서는 필요한 내용을 공식 문서로 병합한 후 정리한다.

---

# 2. 최종 공식 문서

CPF의 사용자·개발자·운영자 대상 공식 문서는 다음 **7종으로 고정한다.**

| 번호공식 문서주요 대상주요 역할 |                       |             |                                       |
| ----------------- | --------------------- | ----------- | ------------------------------------- |
| 01                | README                | 전체 사용자      | 프로젝트 소개, Quick Start, 공식 문서 진입점       |
| 02                | 프레임워크 개발자 가이드         | 일반 개발자      | CPF를 이용한 일반 애플리케이션 개발                 |
| 03                | 배치 개발자 가이드            | Batch 개발자   | Batch·Job·Step·Worker·Scheduler 개발    |
| 04                | 운영자 매뉴얼               | 일반 운영자      | CPF Runtime 및 관리 기능 운영                |
| 05                | 배치 운영 가이드             | Batch 운영자   | Batch Runtime 및 Job 운영                |
| 06                | Gateway 개발/사용 가이드     | Gateway 개발자 | Gateway 개발·구성·설정·사용                   |
| 07                | Specification / 기술 명세 | 개발자·설계자·검증자 | API·SPI·Config·Data·처리 규칙 등 정확한 기술 계약 |

이 순서를 공식 문서 번호와 Navigation의 기준으로 사용한다.

---

# 3. 최종 문서 Navigation

최종 사용자 관점의 문서 구조는 다음과 같이 유지한다.

```
01. README
│
├─ 02. 프레임워크 개발자 가이드
├─ 03. 배치 개발자 가이드
├─ 04. 운영자 매뉴얼
├─ 05. 배치 운영 가이드
├─ 06. Gateway 개발/사용 가이드
└─ 07. Specification / 기술 명세

```

README는 모든 공식 문서의 최상위 진입점이다.

README의 공식 문서 목록에는 위 6개 하위 문서만 연결한다.

각 공식 문서에서 다른 문서를 연결할 때도 원칙적으로 위 공식 7종 범위 안에서 연결한다.

과거 문서, QA 문서, 작업 문서, 임시 문서, 중간 검토 문서를 일반 사용자용 Navigation에 포함하지 않는다.

---

# 4. 문서 작성 공통 원칙

모든 공식 문서는 다음 원칙을 적용한다.

### 4.1 처음 보는 사람 기준

기존 개발자나 작성자가 이미 알고 있다는 전제로 작성하지 않는다.

처음 CPF를 접한 사람이 다음을 이해할 수 있어야 한다.

- 무엇을 하는 기능인지
- 언제 사용하는지
- 어떻게 시작하는지
- 어떤 API나 명령어를 사용하는지
- 어떤 Config가 필요한지
- 정상적으로 되었는지 어떻게 확인하는지
- 문제가 발생하면 어디부터 확인하는지
- 더 정확한 기술 규격은 어디에서 확인하는지

---

### 4.2 실제 구현 기준

문서는 추측이나 계획이 아니라 현재 실제 구현을 기준으로 작성한다.

확인 대상에는 필요에 따라 다음을 포함한다.

- Source
- Public API
- SPI
- Internal 구현
- Config
- SQL
- DB Schema
- Script
- CLI
- REST API
- OpenAPI
- Frontend
- Batch Runtime
- Gateway Runtime
- Test
- Sample
- Generator
- 실제 Consumer

문서에 존재하지만 구현되지 않은 기능을 실제 기능처럼 설명하지 않는다.

구현에는 존재하지만 문서에 누락된 주요 기능은 추가한다.

---

### 4.3 설명과 Reference의 균형

단순 서술형 문서로 만들지 않는다.

개발자와 운영자가 필요한 내용을 빠르게 찾을 수 있도록 다음을 적극적으로 사용한다.

- Summary
- Quick Reference
- 기능 목록
- API 목록
- 명령어 목록
- Config 목록
- 상태값 목록
- Error Code 목록
- 주요 클래스 목록
- 역할별 표
- 상황별 선택표
- 단계별 절차
- 예제
- 관련 문서 링크

단, 동일한 정보를 여러 공식 문서에 완전히 복제하지 않는다.

---

# 5. README 작성 범위

README는 CPF의 첫 진입점이다.

README 자체를 거대한 사용자 매뉴얼로 만들지 않는다.

최소 다음 내용을 포함한다.

- CPF 소개
- 주요 기능
- Module 개요
- 요구 환경
- 가장 짧은 Quick Start
- 최소 실행 예제
- Repository 주요 구조
- 공식 문서 안내
- 어떤 경우 어떤 문서를 봐야 하는지
- Sample 또는 시작 지점
- License 등 Repository 기본 정보가 필요한 경우 링크

상세 개발 방법은 프레임워크 개발자 가이드로 연결한다.

상세 Batch 개발은 배치 개발자 가이드로 연결한다.

상세 운영은 운영자 매뉴얼로 연결한다.

정확한 계약은 Specification으로 연결한다.

---

# 6. 프레임워크 개발자 가이드 작성 범위

일반 개발자가 CPF를 사용하여 애플리케이션을 개발할 수 있도록 작성한다.

필요한 주요 내용은 다음과 같다.

- CPF 개발 모델
- 전체 개발 흐름
- Module 구조
- Module 선택 기준
- Dependency 구성
- Package 구조
- 기본 프로젝트 구성
- Config
- Environment
- Profile
- API
- SPI
- Extension Point
- Annotation
- Command
- Transaction
- Validation
- Exception/Error 처리
- Logging
- Context
- Security 연계
- Data Access
- 외부 연계
- Messaging
- Serialization
- Cache 등 실제 존재하는 공통 기능
- 테스트
- Local 실행
- Debugging
- Sample 활용
- Generator가 있는 경우 사용 방법
- 기존 애플리케이션에 CPF 적용 방법
- 버전 변경 시 개발자가 확인할 내용
- 일반적인 문제 확인 방법

각 기능 설명에는 가능한 경우 다음 구조를 사용한다.

1. 기능 목적
2. 사용 시점
3. 핵심 개념
4. 사용 가능한 API/명령어/설정 Summary
5. 가장 단순한 예제
6. 실제 사용 예제
7. 처리 흐름
8. Transaction과의 관계
9. 오류 처리
10. 주의사항
11. 테스트 방법
12. 관련 Specification 링크

---

# 7. 배치 개발자 가이드 작성 범위

배치 개발자는 일반 Framework 개발과 다른 개념과 개발 흐름을 사용하므로 별도 문서로 관리한다.

최소 다음을 검토하여 실제 구현에 존재하는 항목을 설명한다.

- Batch Architecture
- Batch Runtime
- Job
- Step
- Task
- Worker
- Scheduler
- Center-Cut
- Job Parameter
- Execution Context
- Job Instance
- Execution
- 상태 전이
- Transaction
- Commit 단위
- Chunk 처리
- Retry
- Skip
- Restart
- 재실행
- Idempotency
- 중복 실행 처리
- 동시 실행
- Parallel
- Partition
- 분산 실행
- 다중 Instance
- Worker 할당
- Scheduler 등록
- 실패 처리
- 개발 API
- SPI
- Annotation
- Config
- CLI/Command
- Local 실행
- Test
- 실제 Sample
- 개발자가 확인해야 할 Runtime 정보

반드시 주요 API·명령어·설정값을 빠르게 찾을 수 있는 Summary 표를 제공한다.

Transaction과 Job/Step/Worker 실행 관계도 명확히 설명한다.

운영자가 수행하는 Job 실행·중단·재실행 절차의 상세 설명은 배치 운영 가이드로 연결한다.

---

# 8. 운영자 매뉴얼 작성 범위

CPF를 처음 운영하는 사람도 문서를 찾아가며 기본적인 운영을 수행할 수 있도록 작성한다.

최소 다음 범위를 검토한다.

- 운영 구성 이해
- 설치 후 확인
- Runtime 구성
- Config 적용
- 환경별 Config
- 기동
- 종료
- 재기동
- 상태 확인
- Health
- Log 확인
- 관리 화면 접근
- 사용자 관리
- 권한 관리
- 주요 관리 메뉴
- 상태 조회
- 주요 운영 명령
- Gateway 운영
- 외부 연계 상태 확인
- Scheduler 등 공통 운영 기능
- 오류 확인
- 문제 발생 시 확인 순서
- 로그와 상태정보를 이용한 원인 확인
- 운영 Checklist

단순히 메뉴 이름만 나열하지 않는다.

각 운영 절차에는 가능한 경우 다음을 포함한다.

- 언제 수행하는 작업인지
- 필요한 권한
- 화면 또는 명령 위치
- 입력값
- 실행 방법
- 정상 결과
- 비정상 결과
- 확인할 Log/상태
- 주의사항
- 관련 개발자 가이드 또는 Specification

---

# 9. 배치 운영 가이드 작성 범위

Batch 운영은 일반 Runtime 운영과 성격이 다르므로 별도 문서로 유지한다.

최소 다음 내용을 검토한다.

- Batch 운영 구조
- Job 조회
- Job 상세
- Job 상태
- Job 실행
- 즉시 실행
- 예약 실행
- 실행 Parameter
- Job 중지
- Job 재실행
- 실패 Job 처리
- 실행 이력
- Step 실행 상태
- Scheduler 조회
- Scheduler 등록/변경/중지 기능이 존재하는 경우 사용법
- Worker 상태
- Worker 관리
- 다중 Instance
- 장시간 실행 Job 확인
- 중복 실행 확인
- Center-Cut
- Batch 운영 화면
- Batch REST API
- Batch CLI/Command
- 상태값
- 오류 코드
- Job별 문제 확인 순서
- 필요한 로그
- 운영 Checklist

개발 구현 방식은 배치 개발자 가이드에 두고, 배치 운영 가이드에서는 실제 운영 행위를 중심으로 작성한다.

---

# 10. Gateway 개발/사용 가이드 작성 범위

Gateway는 전문 기능이지만 전체 공식 문서 체계에서는 **06번 전문 가이드**로 유지한다.

Gateway를 개발하거나 구성하는 사용자가 필요한 내용을 제공한다.

실제 구현에 따라 다음을 검토한다.

- Gateway 개요
- Gateway Architecture
- 요청 처리 흐름
- Route
- Route Matching
- Filter
- Interceptor
- Header
- Request Context
- Response 처리
- 인증 연계
- 인가 연계
- Token 연계
- Session 연계
- Timeout
- Retry
- Circuit 관련 기능
- Load Balancing 관련 기능
- 외부 시스템 연계
- 오류 처리
- Error Mapping
- 주요 Config
- 환경별 설정
- API
- SPI
- 명령어
- 상태 확인
- Local 실행
- Test
- 실제 Route 예제
- 문제 확인 방법

Gateway 운영에만 필요한 일반 상태 확인 및 관리 절차는 운영자 매뉴얼에 둔다.

별도의 Gateway 운영 가이드는 만들지 않는다.

---

# 11. Specification / 기술 명세 작성 범위

Specification은 튜토리얼이 아니라 **정확한 기술 계약을 찾는 Reference**로 작성한다.

실제 구현을 기준으로 다음을 정리한다.

- Module Specification
- Public API
- SPI
- 주요 Interface
- 주요 DTO
- Annotation
- Command
- Config
- Config Type
- Default Value
- Required 여부
- Environment Variable
- 상태값
- Error Code
- HTTP API
- OpenAPI 계약
- Request
- Response
- 데이터 구조
- DB 관련 계약
- Transaction 규칙
- Security 기술 계약
- Batch 기술 계약
- Gateway 기술 계약
- 제약사항
- 호환성
- Deprecated 항목

개발 방법을 장황하게 설명하지 않는다.

사용법은 각 개발자 가이드에서 설명하고, 정확한 값과 계약을 확인할 때 Specification을 사용하도록 역할을 분리한다.

---

# 12. 별도 공식 문서 생성 금지

다음 종류의 내용을 발견하더라도 **새로운 독립 공식 Guide를 생성하지 않는다.**

| 별도 생성하지 않을 문서통합할 공식 문서        |                                              |
| ----------------------------- | -------------------------------------------- |
| Getting Started Guide         | README                                       |
| Quick Start Guide             | README                                       |
| Installation Guide            | README / 개발자 가이드 / 운영자 매뉴얼                   |
| Architecture Guide            | 개발자 가이드 + Specification                      |
| Module Guide                  | 프레임워크 개발자 가이드                                |
| API Guide                     | 해당 개발자 가이드 + Specification                   |
| API Reference                 | Specification                                |
| SPI Guide                     | 해당 개발자 가이드 + Specification                   |
| Configuration Guide           | 해당 가이드 + Specification                       |
| Config Reference              | Specification                                |
| Command Guide                 | 해당 개발자/운영자 가이드                               |
| Command Reference             | 해당 가이드의 Summary/Reference                    |
| Transaction Guide             | 프레임워크 개발자 가이드                                |
| Database Guide                | 개발자 가이드 + Specification                      |
| Logging Guide                 | 개발자 가이드 + 운영자 매뉴얼                            |
| Monitoring Guide              | 운영자 매뉴얼                                      |
| Troubleshooting Guide         | 해당 개발자/운영자 가이드                               |
| Error Code Guide              | Specification                                |
| Testing Guide                 | 해당 개발자 가이드                                   |
| Upgrade Guide                 | 기존 공식 가이드에 통합                                |
| Migration Guide               | 개발자 가이드 + Specification                      |
| Security Guide                | 관련 공식 가이드 + Specification                    |
| Admin Guide                   | 운영자 매뉴얼                                      |
| Gateway API Guide             | Gateway 개발/사용 가이드 + Specification            |
| Gateway Config Guide          | Gateway 개발/사용 가이드 + Specification            |
| Gateway 운영 가이드                | 운영자 매뉴얼                                      |
| Gateway Troubleshooting Guide | Gateway 가이드 + 운영자 매뉴얼                        |
| Batch API Guide               | 배치 개발자 가이드 + Specification                   |
| Batch Config Guide            | 배치 개발자 가이드 + Specification                   |
| Batch Command Guide           | 배치 개발자 가이드 또는 배치 운영 가이드                      |
| Batch Troubleshooting Guide   | 배치 개발자 가이드 + 배치 운영 가이드                       |
| FAQ 독립 문서                     | 해당 공식 문서의 FAQ/문제 확인 절                        |
| Tutorial 독립 문서                | README 또는 해당 개발자 가이드                         |
| Example Guide                 | 해당 개발자 가이드 또는 Sample 자체 README가 꼭 필요한 경우 최소화 |

단순히 내용이 많다는 이유로 새로운 Guide로 분리하지 않는다.

Chapter와 Section을 활용한다.

---

# 13. 신규 문서 생성 판단 절차

새 문서를 만들기 전에 반드시 다음 순서로 판단한다.

1. 기존 7종 중 어느 문서가 Owner인지 확인한다.
2. 기존 문서의 Chapter로 추가할 수 있는지 확인한다.
3. Subsection으로 추가할 수 있는지 확인한다.
4. Summary 또는 Reference 표로 처리할 수 있는지 확인한다.
5. Specification 항목으로 처리할 수 있는지 확인한다.

위 방법으로 수용할 수 있으면 새로운 공식 문서를 만들지 않는다.

**기본 판단은 신규 문서 생성 금지다.**

---

# 14. 문서 Owner 원칙

하나의 정보는 가능한 한 하나의 Owner 문서에서 상세하게 관리한다.

| 정보Owner             |                   |
| ------------------- | ----------------- |
| 프로젝트 소개             | README            |
| Quick Start         | README            |
| 일반 개발               | 프레임워크 개발자 가이드     |
| 일반 API 사용법          | 프레임워크 개발자 가이드     |
| Transaction 사용법     | 프레임워크 개발자 가이드     |
| Batch 개발            | 배치 개발자 가이드        |
| Job/Step/Worker 개발  | 배치 개발자 가이드        |
| 일반 Runtime 운영       | 운영자 매뉴얼           |
| Gateway 운영          | 운영자 매뉴얼           |
| Batch Runtime 운영    | 배치 운영 가이드         |
| Scheduler/Worker 운영 | 배치 운영 가이드         |
| Gateway 개발          | Gateway 개발/사용 가이드 |
| Route/Filter 개발     | Gateway 개발/사용 가이드 |
| 정확한 API/SPI 계약      | Specification     |
| Config 전체 규격        | Specification     |
| 상태값 및 Error Code    | Specification     |
| 기술 제약 및 상세 계약       | Specification     |

다른 문서에서 동일 정보가 필요하면 짧게 설명하고 Owner 문서로 연결한다.

---

# 15. 과거 문서 전수 조사

작업 시작 시 Repository 전체의 문서 파일을 확인한다.

특정 docs 폴더만 확인하지 않는다.

다음과 같은 형식과 위치를 포함하여 문서성 파일을 확인한다.

- Repository Root
- docs
- module별 docs
- guide
- manual
- reference
- sample
- examples
- scripts 주변 문서
- 과거 workspace
- deliverables
- 이전 session 산출물
- 기타 Markdown/Text/HTML 등의 문서

과거 문서가 현재 공식 문서보다 더 정확한 내용을 가지고 있다면 필요한 내용을 공식 문서로 병합한다.

과거 문서를 그대로 공식 문서로 연결하여 문제를 피하지 않는다.

---

# 16. 정리 대상

다음에 해당하는 문서는 정리 대상으로 분류한다.

- 과거 버전 문서
- 현재 구현과 맞지 않는 문서
- 공식 문서와 내용이 중복되는 문서
- 같은 기능을 여러 번 설명한 문서
- 과거 작업용 Guide
- 임시 Guide
- 중간 결과 문서
- 중간 Review 문서
- 임시 분석 문서
- 복사본
- Backup 파일
- 오래된 문서 버전
- Placeholder
- 빈 문서
- 사용하지 않는 Tutorial
- 사용하지 않는 Example 설명
- 더 이상 연결되지 않는 과거 사용자 문서
- 공식 문서로 내용이 완전히 이전된 문서
- 이름만 다르고 역할이 동일한 중복 문서

---

# 17. 과거 문서 병합 우선

과거 문서를 발견했다고 바로 삭제하지 않는다.

반드시 다음 순서로 처리한다.

1. 내용을 검토한다.
2. 현재 구현과 유효한 내용인지 판정한다.
3. 공식 7종 중 Owner 문서를 결정한다.
4. 필요한 내용을 공식 문서에 병합한다.
5. 중복을 제거한다.
6. 기존 링크를 공식 문서 링크로 변경한다.
7. 더 이상 필요한 내용이 없는지 재확인한다.
8. 삭제 대상으로 등록한다.

유효한 정보를 잃은 상태로 삭제하면 안 된다.

---

# 18. 공식 문서 링크 정리

최종적으로 공식 사용자용 링크는 공식 7종 중심으로 정리한다.

다음을 전수 확인한다.

- README
- Markdown 링크
- 상대경로 링크
- 문서 Index
- Navigation
- See Also
- Related Documents
- 각 Chapter의 참조 링크
- Sample에서 공식 문서로 연결하는 링크

과거 사용자 문서로 가는 링크를 그대로 남겨두지 않는다.

과거 링크는 가능한 경우 해당 내용을 흡수한 공식 문서의 정확한 Section으로 교체한다.

---

# 19. 내부 관리 문서와 공식 문서 구분

Repository 운영과 개발·QA를 위해 필요한 내부 자료는 공식 사용자 문서와 구분한다.

다음은 필요할 경우 유지할 수 있으나 **공식 사용자 문서 Navigation에는 포함하지 않는다.**

예:

- Governance
- Requirement 원장
- QA Requirement
- QA Review
- QA Rework Request
- Evidence
- Test 결과
- Change Manifest
- Package Manifest
- Handover
- Continuity
- 개발 작업 기록
- 내부 검토 자료

이 자료를 사용자용 Guide처럼 노출하지 않는다.

단, 현재 유효한 QA·Requirement·Evidence 자료는 단순히 과거 문서라는 이유로 삭제하면 안 된다.

---

# 20. 저장소 관리 파일

다음과 같은 Repository 관리 파일은 공식 7종 문서 수에 포함하지 않는다.

예:

- LICENSE
- CHANGELOG
- 기타 빌드 또는 Repository 운영상 반드시 필요한 파일

이 파일은 사용자용 공식 Guide와 별도 범주다.

필요한 Repository 관리 파일까지 억지로 7종에 포함하거나 제거하지 않는다.

---

# 21. 삭제 원칙

공식 7종으로 내용이 완전히 통합되고 실제로 더 이상 사용하지 않는 과거·중복·임시 문서는 최종적으로 정리한다.

단, 삭제는 매우 보수적으로 수행한다.

다음 파일은 문서 정리라는 이유로 삭제해서는 안 된다.

- Product Source
- SQL
- API 구현
- Test
- Config
- Frontend
- Script
- Build 관련 실제 파일
- Runtime에서 사용하는 파일
- 현재 공식 문서
- 현재 유효한 Requirement
- 최종 QA 자료
- 최종 Evidence
- Migration
- 현재 Working Tree에서 작업 중인 변경
- 실제 Consumer가 참조하는 파일

파일 이름이나 디렉터리 이름만 보고 삭제하지 않는다.

디렉터리 안에 제품 파일과 과거 문서가 섞여 있다면 디렉터리 전체를 삭제하지 않는다.

삭제 대상 파일을 개별 분류한다.

---

# 22. Delete Manifest

실제 삭제 전에 삭제 대상을 exact path 기준으로 정리한다.

Delete Manifest에는 최소 다음을 기록한다.

| 항목내용              |                    |
| ----------------- | ------------------ |
| exact\_path       | 실제 삭제 대상 경로        |
| type              | 파일/디렉터리 유형         |
| reason            | 삭제 사유              |
| replacement       | 내용을 흡수한 공식 문서      |
| content\_migrated | 필요한 내용 병합 여부       |
| link\_checked     | 참조 링크 확인 여부        |
| runtime\_usage    | Runtime 사용 여부      |
| source\_usage     | Source/Build 참조 여부 |
| deletion\_risk    | 삭제 영향              |
| decision          | 삭제 가능 여부           |

실제 삭제는 승인된 exact allowlist에 대해서만 수행한다.

광범위 명령을 사용하지 않는다.

다음과 같은 명령은 문서 정리 용도로 사용하지 않는다.

```
git clean
git reset --hard
git restore .

```

상위 디렉터리 전체 삭제 역시 원칙적으로 금지한다.

---

# 23. 문서 품질 점검

각 공식 문서 작성 후 다음을 점검한다.

### 내용

- 실제 기능과 일치하는가
- 존재하지 않는 기능을 설명하지 않았는가
- 중요한 실제 기능이 빠지지 않았는가
- 처음 보는 사람이 이해할 수 있는가
- 실제로 따라 할 수 있는가
- API와 명령어를 찾기 쉬운가
- 설정을 찾기 쉬운가
- 예제가 충분한가
- 오류 상황도 설명되는가

### 구조

- 목차가 지나치게 복잡하지 않은가
- 관련 정보가 Owner 문서에 모여 있는가
- 중복 설명이 과도하지 않은가
- Summary 표가 필요한 곳에 있는가
- 상세 기술 계약은 Specification으로 적절히 연결되는가

### 링크

- 공식 문서 링크가 정상인가
- Anchor 링크가 정상인가
- 상대경로가 정상인가
- 삭제된 과거 문서 링크가 남아 있지 않은가
- 동일 내용을 여러 구버전 문서로 연결하고 있지 않은가

---

# 24. 문서 파일 증가 억제

작업 중 편의를 위해 새로운 `.md` 파일을 계속 생성하지 않는다.

예를 들어 다음과 같은 파일을 임의로 추가하지 않는다.

```
API_GUIDE.md
CONFIG_GUIDE.md
TRANSACTION_GUIDE.md
ERROR_GUIDE.md
ARCHITECTURE_GUIDE.md
TEST_GUIDE.md
TROUBLESHOOTING.md
INSTALLATION_GUIDE.md
GATEWAY_API_GUIDE.md
BATCH_COMMAND_GUIDE.md

```

이러한 내용은 공식 Owner 문서의 Chapter로 통합한다.

작업 도중 임시 분석이 필요하면 최종 Repository에 불필요한 임시 문서를 남기지 않는다.

---

# 25. 중복 내용 처리 방식

동일 정보를 여러 문서에 길게 반복하지 않는다.

예:

README:

> Transaction 기능을 제공한다.
> 자세한 사용 방법은 프레임워크 개발자 가이드의 Transaction 장을 참고한다.

프레임워크 개발자 가이드:

> Transaction 사용법, API, 적용 범위, 예제, 주의사항을 설명한다.

Specification:

> Transaction 관련 정확한 계약, 상태, 설정값 등을 정의한다.

이와 같이 문서별 역할을 구분한다.

---

# 26. Summary / Reference 강화

문서를 추가로 분리하지 않는 대신 각 전문 가이드 안의 탐색성을 높인다.

필요한 곳에는 다음 Summary를 둔다.

### 개발자 가이드 예

- 사용 가능한 주요 기능
- 주요 API
- 주요 Annotation
- 주요 SPI
- 주요 Command
- 주요 Config
- 주요 Exception
- Transaction 관련 기능
- Module별 기능
- 기능별 시작 위치

### 배치 개발자 가이드 예

- Job/Step API
- Batch Annotation
- 실행 Command
- Scheduler 기능
- Worker 기능
- 상태값
- Retry/Restart 기능
- Config
- 주요 개발 시나리오

### 운영자 매뉴얼 예

- 주요 운영 메뉴
- 주요 명령어
- 상태 확인 위치
- 주요 로그
- 권한별 기능
- 상황별 점검 순서

### 배치 운영 가이드 예

- Job 상태
- 운영 명령
- 재실행 조건
- Scheduler 관리
- Worker 상태
- 오류별 확인 순서

### Gateway 가이드 예

- Route 설정
- Filter
- 주요 Config
- 주요 API
- Header/Context
- Error 처리
- 테스트 방법

---

# 27. 금지 사항

다음 방식으로 작업하지 않는다.

- 문서 수를 늘려 문제를 해결하지 않는다.
- 과거 문서를 그대로 두고 새 문서만 또 만들지 않는다.
- 같은 내용을 여러 공식 문서에 복제하지 않는다.
- 실제 구현을 확인하지 않고 문서를 작성하지 않는다.
- 클래스명이나 설정명을 추측하지 않는다.
- 존재하지 않는 CLI나 API를 만들어서 설명하지 않는다.
- Placeholder를 남기고 완료 처리하지 않는다.
- TODO만 남기고 완료 처리하지 않는다.
- 링크되지 않는 새 문서를 대량 생성하지 않는다.
- 구버전 문서를 공식 Navigation에 함께 노출하지 않는다.
- 문서 정리 과정에서 Product Source를 삭제하지 않는다.
- QA/Evidence를 과거 파일로 오인해 무단 삭제하지 않는다.
- 광범위 Git 정리 명령을 사용하지 않는다.

---

# 28. 작업 완료 조건

다음 조건을 모두 만족해야 문서 정비 작업을 완료한 것으로 본다.

### 공식 문서

-  공식 사용자 문서가 7종 체계로 정리되어 있다.
-  문서 번호와 순서가 통일되어 있다.
-  Gateway 개발/사용 가이드가 06번이다.
-  README가 전체 문서 진입점 역할을 한다.
-  README에서 공식 문서를 명확하게 연결한다.

### 내용

-  실제 Source/API/Config/Runtime과 문서가 일치한다.
-  주요 API가 빠지지 않았다.
-  주요 명령어가 빠지지 않았다.
-  주요 설정이 빠지지 않았다.
-  개발자가 사용할 Summary가 충분하다.
-  운영자가 필요한 절차를 찾을 수 있다.
-  Batch 개발과 Batch 운영이 명확히 분리되어 있다.
-  Gateway 전문 개발 내용이 06번 가이드에 정리되어 있다.
-  정확한 기술 계약은 Specification에 정리되어 있다.

### 중복 제거

-  API Guide 등의 별도 중복 문서를 신규 생성하지 않았다.
-  기존 중복 문서의 필요한 내용이 공식 문서로 병합되었다.
-  과거 문서의 유효한 내용이 누락되지 않았다.
-  공식 문서끼리 불필요한 내용 복제가 없다.

### 링크

-  공식 문서 간 링크가 정상이다.
-  깨진 링크가 없다.
-  과거 사용자 문서 링크가 공식 Navigation에 남아 있지 않다.
-  필요한 링크는 공식 Owner 문서로 변경되었다.

### 정리

-  과거·중복·미사용 문서를 전수 확인했다.
-  실제 삭제 대상이 Delete Manifest에 exact path로 정리되어 있다.
-  유효한 내용을 병합하지 않고 삭제한 문서가 없다.
-  Product Source·Test·Config·Script 등이 삭제 대상에 포함되지 않았다.
-  최종 QA/Requirement/Evidence가 보호되어 있다.
-  승인된 범위 외 파일을 삭제하지 않았다.

---

# 29. 최종 결과 보고

최종 보고에는 최소 다음을 구분하여 기록한다.

## 공식 문서

- 최종 7종 문서 경로
- 각 문서 상태
- 신규 작성 여부
- 기존 문서 개편 여부

## 내용 검증

- 실제 Source 확인 범위
- API 확인 범위
- Config 확인 범위
- CLI 확인 범위
- Batch 확인 범위
- Gateway 확인 범위
- 운영 UI/API 확인 범위

## 정리

- 기존 문서 전체 조사 결과
- 공식 문서로 병합한 문서
- 유지한 내부 문서
- 삭제 대상 문서
- Delete Manifest
- 삭제하지 못한 항목과 사유

## 검증

- Markdown Link 검사
- 문서 Navigation 검사
- 중복 문서 검사
- 누락 기능 검사
- 실제 구현과 문서 정합성 검사

---

# 30. 최종 강제 원칙

이번 문서 정비 이후 **CPF의 공식 사용자·개발자·운영자 문서는 아래 7종만 유지한다.**

```
01. README
02. 프레임워크 개발자 가이드
03. 배치 개발자 가이드
04. 운영자 매뉴얼
05. 배치 운영 가이드
06. Gateway 개발/사용 가이드
07. Specification / 기술 명세

```

API Guide, Architecture Guide, Configuration Guide, Command Guide, Troubleshooting Guide, Installation Guide, Testing Guide 등의 별도 공식 문서를 추가하지 않는다.

필요한 내용은 위 7종의 적절한 Owner 문서에 Chapter·Section·Summary·Reference 형태로 통합한다.

과거 문서에 유효한 정보가 있으면 먼저 공식 문서에 병합한다.

내용이 공식 문서에 완전히 반영되었고 더 이상 사용하지 않는 과거·중복·임시 문서는 정리한다.

공식 사용자 문서의 Navigation은 위 7종만을 기준으로 한다.

내부 QA·Requirement·Evidence·Governance 자료는 필요한 경우 별도 유지하되 사용자용 공식 문서로 연결하지 않는다.

문서를 추가하여 복잡도를 높이는 대신 **이 7종의 완성도와 탐색성, 실제 구현과의 정합성을 높이는 것을 최우선으로 한다.**

**특별한 구조적 필요가 새롭게 확인되지 않는 한 추가 공식 문서는 만들지 않는다.**

# 사용 목적 중심 문서 공통 품질 기준

README를 제외한 공식 문서는 표지 다음에 전체 목차 전용 페이지를 두고, 주요 장/절로 이동 가능한 내부 링크 또는 Bookmark를 제공한다.

모든 매뉴얼/가이드는 Framework를 이미 잘 아는 사람을 전제로 하지 않는다. 사용자가 자신의 이용 목적에 맞는 기능·명령·옵션을 빠르게 판단하고 필요할 때 상세로 내려갈 수 있어야 한다.

공통 규칙:

- 문서의 성격이나 읽는 방법을 설명하는 메타 문구를 서두에 두지 않는다.
- Summary는 상세를 반복하지 않고 기능 존재 여부, 용도, 선택 기준, 주요 옵션을 압축한다.
- 표 Header는 `기능`, `용도`, `사용 API/명령`, `선택 기준`, `주요 옵션`, `참고사항` 등 실제 의미가 드러나는 용어를 사용한다.
- 표 안의 장문 설명과 과도한 줄바꿈을 피하고, 긴 설명은 본문/예제로 이동한다.
- 짧은 컬럼은 좁게, 긴 설명 컬럼은 넓게 배치하며 모든 컬럼을 균등 분할하지 않는다.
- 많은 항목을 찾는 용도라면 긴 표보다 목차/Quick Finder/내부 링크를 우선한다.
- Source/API/명령을 실제 확인해 사용자에게 직접 필요한 정보만 남기고 내부 구현 세부로 분량을 채우지 않는다.
- 그림은 구조 요소 이름만 나열하지 말고 사용 목적, 선택 차이, 확장성 또는 운영 장점이 이해되게 작성한다.

---

# 사용자 목적 중심 Summary 추가 기준

공식 02~07 문서의 첫 Summary/Quick Finder는 문서 자체의 읽는 방법을 설명하지 않는다. 해당 문서에서 사용자가 실제로 찾는 **기능/상태/작업 목적**을 바로 보여준다.

- 개발자: 기능/API/Starter/선택 기준
- 배치 개발자: 처리 모델/Annotation/재실행·확장 판단
- 운영자: 증상/식별자/상태/허용 조치/확인 위치
- 배치 운영: Stop/Restart/Reprocess/Reconcile 선택
- Gateway: 구성 유형/Route/Target/정책/오류
- Specification: 기능/API/Annotation/Config/State의 정확한 계약

표가 단순 Navigation 역할만 한다면 내부 링크가 있는 목차나 Quick Finder로 대체한다. Summary 표는 상세 설명을 압축해 **그 표만 읽어도 기능의 목적과 선택 차이를 대략 판단**할 수 있을 때만 사용한다.

