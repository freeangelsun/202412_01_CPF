# CPF README.md 작성·검수 상세 지침

## README 시각화·용어 보완 규칙 (2026-08-16)

- README에는 Architecture를 한 장으로 축약하지 않고 **두 수준**으로 제공한다. `CPF 한눈에 보기`는 처음 보는 사용자가 10초 안에 구조를 파악하는 단순 Overview로, `CPF 전체 Architecture`는 외부 진입·업무 Domain·Starter/Common Function·Core·Batch/ADM/Tools·DB/Messaging/Storage/External을 포함하는 대표 상세 그림으로 구성한다.
- `기본형 / 선택형 / 생성형`처럼 의미가 추상적인 분류보다 `기본 제공`, `필요한 기능 선택`, `필요 시 업무 Domain 생성`처럼 행동과 의미가 바로 드러나는 용어를 우선한다. 한국어 설명 속 영문 용어는 실제 코드/고유명칭에 필요한 경우만 병기한다.
- Gateway는 기능 목록만 보여주지 않고 `L4만`, `Gateway만`, `L4 + Gateway` 등 실제 사용 Case를 그림으로 비교하며, Gateway를 사용할 때 여러 CPF Runtime의 외부 진입을 하나의 Gateway Endpoint/Port와 Route 정책으로 단순화하는 장점을 보여준다. Domain 간 내부 호출은 Gateway 필수가 아님을 분리한다.
- Batch Runtime은 Component 관계만 그리지 않고 Worker/Agent 확장, 실행 제어와 실제 처리의 분리, 업무 증가에 따른 확장, Restart/Reprocess/Reconcile의 장점을 보인다.
- Starter 그림은 Starter 이름 나열로 끝내지 않는다. `필요 기능 선택 → Profile/Starter/Provider 조합 → 설정·의존성·AutoConfiguration 표준화 → 업무 Domain에서 Public API 사용` 흐름과 `추가/교체 용이`, `Provider 변경 영향 최소화`, `Canonical Catalog/Generator 연계`를 사용자 관점에서 보여준다.
- 본문 시각화는 외곽 비율·여백·폰트 크기·명도·선 두께를 일관되게 유지하고 README 실제 표시 크기에서 읽히지 않는 글자를 넣지 않는다.
- README는 Repository 주소만 전달받은 사람이나 검색·AI 도구가 README만 읽어도 CPF를 엉뚱한 방향으로 해석하지 않도록 **핵심 정체성과 사용 방향을 명시적으로 서술**한다. 그림이나 Module 이름만 보고 추론하게 두지 않는다.
- README 앞부분에는 장황한 FAQ를 만들지 않되 최소한 `CPF가 무엇인가`, `어떤 문제를 해결하는가`, `Spring Boot와 어떤 관계인가`, `개발 Golden Path`, `적합한 시스템 범위`, `빠른 실행 경로`를 짧고 독립적인 문장으로 확인할 수 있어야 한다.
- 위 핵심 설명은 마케팅 문구보다 Source로 확인 가능한 사실을 우선하며, `Spring Boot 기반`, `Public Starter/Public API`, `업무 Domain`, `선택 기능`, `Generator`, `Runtime` 등 CPF의 Canonical 용어를 일관되게 사용한다.

## 1. 문서 목적

본 지침은 Core Platform Framework(CPF)의 Repository 최상위 `README.md`를 신규 작성, 보완, 사실성 검증, 구조 검수할 때 적용하는 기준이다.

CPF의 README는 일반적인 오픈소스 프로젝트 README나 개발자 매뉴얼의 축약본으로 작성하지 않는다.

README의 핵심 역할은 다음 네 가지다.

1. **제품 브로셔**
   - CPF가 무엇인지 처음 보는 사람이 짧은 시간 안에 이해할 수 있게 한다.
   - CPF가 해결하려는 문제와 제품 범위를 설명한다.
   - 단순 기능 목록보다 제품 전체 구조와 역할 관계를 보여준다.
2. **Architecture의 시각적 진입점**
   - CPF의 주요 제품·Module·Runtime·외부 시스템 간 관계를 시각적으로 보여준다.
   - Online, Async, Batch, Administration, Gateway 등 주요 실행 경로를 이해할 수 있게 한다.
3. **공식 사용자 문서 진입점**
   - 사용자가 자신의 역할과 수행하려는 업무에 따라 어떤 공식 매뉴얼을 읽어야 하는지 바로 판단할 수 있게 한다.
4. **최소 Quick Start**
   - CPF를 처음 확인하는 사용자가 Build 또는 최소 실행까지 도달하기 위한 가장 짧은 경로만 제공한다.
   - 상세 개발·운영 절차는 공식 매뉴얼로 이동시킨다.

README를 다음 용도로 사용하지 않는다.

- 전체 기능 Reference
- API Reference
- 운영 Runbook
- 장애 대응서
- Property Reference
- 메뉴·화면 Reference
- 작업 보고서
- QA 결과 보고서
- 구현 Gap 보고서
- Handover 문서
- Requirement Matrix
- 개발 교육 교재 전체본

---

# 2. 기준 Repository와 정본

README 작성·수정·검수는 반드시 다음 Repository를 기준으로 수행한다.

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Remote: `origin`

작업 시작 시 반드시 최신 `origin/master`의 Commit SHA를 확인하고 기준 Commit으로 기록한다.

다음 정본을 우선한다.

## 2.1 최상위 제품 목표 정본

`CPF_FINAL_TARGET_REQUIREMENTS.md`

다음 판단의 기준으로 사용한다.

- CPF가 궁극적으로 무엇을 목표로 하는가
- 어떤 Business Platform 영역을 포함하려는가
- 제품의 범위와 비범위는 무엇인가
- 현재 구현과 제품 목표를 어떻게 구분할 것인가

## 2.2 문서 작성 표준 정본

`cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

README 작업을 시작하기 전에 반드시 먼저 읽는다.

문서의 구성, 용어, 근거 작성, 그림 사용, 링크, 사실성, 검증 범위 표현 등이 본 표준과 충돌하지 않아야 한다.

## 2.3 구현 사실의 우선순위

README 내용의 사실 여부를 판단할 때 다음 순서를 우선한다.

1. 실제 Source
2. SQL / Migration
3. API
4. Config
5. Frontend
6. Script
7. Test
8. 현재 공식 문서
9. 과거 작업 보고
10. 이전 대화나 작업자의 기억

과거 README에 기재되어 있다는 이유만으로 사실로 간주하지 않는다.

Interface가 존재한다고 실제 Consumer가 존재한다고 판단하지 않는다.

Sample이 존재한다고 실제 제품 기능으로 판단하지 않는다.

Property가 정의되어 있다고 모든 Profile과 배포 방식에서 사용된다고 판단하지 않는다.

Controller가 존재한다고 실제 Frontend에서 사용하는 API라고 판단하지 않는다.

---

# 3. 작업 시작 전 Git 안전 확인

README 작업 전에 Repository 상태를 반드시 확인한다.

최소 확인 항목은 다음과 같다.

| 확인 항목목적                       |                  |
| ----------------------------- | ---------------- |
| `git remote -v`               | 기준 Remote 확인     |
| `git branch --show-current`   | 현재 Branch 확인     |
| `git rev-parse HEAD`          | Local HEAD 확인    |
| `git rev-parse origin/master` | 기준 Commit 확인     |
| `git status --short`          | 기존 변경 보호         |
| `git diff --name-status`      | 수정된 추적 파일 확인     |
| `git diff --stat`             | 변경 규모 확인         |
| 미추적 파일 확인                     | 다른 작업자의 신규 파일 보호 |

Local Working Tree에 기존 변경이 존재하면 이를 보호 대상으로 본다.

사용자의 명시적 승인 없이 다음 작업을 수행하지 않는다.

- commit
- push
- branch 생성·삭제
- tag
- PR
- merge
- rebase
- cherry-pick
- revert
- stash
- checkout
- switch
- restore
- reset
- clean
- remote 변경
- force push

특히 다음 명령은 임의로 사용하지 않는다.

- `git reset --hard`
- `git clean -fd`
- `git restore .`
- `git checkout -- .`
- `git stash --include-untracked`

Local과 `origin/master`가 Ahead, Behind, Diverged인 경우 임의로 정리하지 않는다.

---

# 4. README의 독자 정의

README는 특정 한 역할만을 위한 문서가 아니다.

최소 다음 독자를 고려한다.

| 독자README에서 알고 싶은 것 |                                                   |
| ------------------ | ------------------------------------------------- |
| 처음 CPF를 접한 개발자     | CPF가 무엇이며 어디부터 봐야 하는가                             |
| 업무 개발자             | CPF를 이용해 어떤 방식으로 업무를 개발하는가                        |
| Batch 개발자          | Batch 영역이 어디에 있고 어떤 문서를 읽어야 하는가                   |
| ADM 개발자            | ADM Backend/Frontend가 전체 제품에서 어떤 위치인가             |
| 운영자                | 운영·관제·배포는 어떤 영역에서 수행하는가                           |
| Architecture 담당자   | CPF Module과 Business Application 간 책임 경계가 무엇인가    |
| 보안 담당자             | Authentication/Authorization/Audit 등이 어느 영역에 속하는가 |
| BZA 담당자            | BZA가 CPF 전체 구조에서 어떤 역할을 하는가                       |
| Gateway 담당자        | Gateway가 언제 필요하고 어디에 위치하는가                        |
| 도입 검토자             | CPF가 단순 공통 Library인지 Business Platform인지          |
| QA 담당자             | 상세 검증 자료를 어느 공식 문서에서 찾아야 하는가                      |

README를 읽은 뒤 각 독자가 자신에게 필요한 상세 Guide로 이동할 수 있어야 한다.

---

# 5. README 정보 설계 원칙

README는 다음 정보 흐름을 기본으로 한다.

**What → Why → What is included → How it is structured → How it flows → How to start → Where to learn more**

즉 사용자가 다음 질문 순서대로 자연스럽게 답을 얻어야 한다.

1. CPF가 무엇인가?
2. 왜 존재하는가?
3. 어떤 영역을 포함하는가?
4. 각 제품·Module은 어떤 역할인가?
5. 전체 Architecture는 어떻게 구성되는가?
6. 요청과 데이터는 어떻게 흐르는가?
7. 어떤 배치 형태를 지원하는가?
8. 개발·운영은 어떤 흐름으로 이루어지는가?
9. 내가 직접 사용하려면 어디부터 시작하는가?
10. 더 자세한 정보는 어느 문서를 봐야 하는가?

README를 기술 항목의 가나다순 또는 Module 이름순으로 단순 나열하지 않는다.

---

# 6. README 전체 구성 기준

현재 승인된 README의 브로셔형 구조와 시각적 흐름을 우선 보호한다.

사용자 승인 없이 다음 구성요소를 전면 교체하거나 전체 재작성하지 않는다.

- Hero
- Hero 이미지
- Section 구조
- Architecture
- Product Map
- Topology
- Execution Flow
- Operations Flow
- Domain Journey
- Guide Map
- Card
- CTA
- Desktop 구성
- Mobile 구성

신규 README를 설계해야 하는 상황이라면 다음 개념적 순서를 기준으로 삼는다.

1. Hero
2. Product Identity
3. CPF가 해결하는 문제
4. Product Map
5. Architecture
6. Supported Topology
7. Execution Flow
8. Operations Flow
9. Domain Journey
10. 핵심 Capability Map
11. Minimal Quick Start
12. Guide Map
13. 다음 행동을 안내하는 CTA

실제 Section 이름과 배치는 기존 승인 구조가 있으면 기존 구조를 따른다.

---

# 7. Hero 작성 기준

Hero는 사용자가 README를 열었을 때 가장 먼저 접하는 영역이다.

Hero만 보고 최소한 다음을 이해할 수 있어야 한다.

- 제품명
- CPF의 정식 명칭
- CPF가 어떤 종류의 제품인지
- 주요 사용 목적
- 상세 문서 또는 Quick Start로 이동할 수 있는 진입점

## 7.1 제품명

반드시 정식 명칭을 사용한다.

**Core Platform Framework**

필요하면 약칭 CPF를 함께 표시한다.

## 7.2 Hero 설명 문구

한두 문장 안에서 다음 중 핵심을 전달한다.

- Business Application을 구축하기 위한 Framework
- 개발뿐 아니라 운영·검증·확장·배포 등의 Platform 영역을 포함한다는 방향
- Online / Async / Batch / Administration / Gateway 등 주요 범위

단, 실제 구현하지 않은 기능까지 현재 지원한다고 표현해서는 안 된다.

## 7.3 Hero에서 금지되는 표현

근거 없이 다음과 같은 표현을 사용하지 않는다.

- 완벽한
- 완전한
- 최고
- 최상의
- 혁신적인
- 엔터프라이즈급
- 금융권 최고 수준
- 상용 수준
- Production Ready
- 운영 준비 완료
- 검증 완료
- 기능 완료
- 자동으로
- 실시간으로
- 무중단
- 무손실
- 완전 호환
- 항상
- 절대로
- 문제없이
- 즉시
- 원클릭
- 안정적으로
- 안전하게
- 통합 관리

제품 목표 표현과 현재 구현 표현을 반드시 구분한다.

---

# 8. Product Identity 작성 기준

Hero 이후 사용자가 가장 먼저 이해해야 할 내용은 CPF의 정체다.

다음 질문에 답해야 한다.

- CPF는 Library인가 Framework인가 Platform인가?
- 어떤 업무 시스템을 구축하기 위한 것인가?
- Business Application과 CPF의 역할은 어떻게 나뉘는가?
- CPF가 공통으로 담당하는 영역은 무엇인가?
- 업무 개발자가 직접 담당해야 하는 것은 무엇인가?

설명은 마케팅 문구가 아니라 역할 경계를 중심으로 작성한다.

예를 들어 다음 개념이 분명해야 한다.

**CPF가 관리하는 영역**

- 공통 실행 기반
- 공통 Architecture 규칙
- Transaction 관련 기반
- Persistence 기반
- Messaging 관련 기반
- Batch 기반
- Security
- Administration
- Gateway
- Observability
- DB Lifecycle
- Generator

단, 실제 구현 여부를 반드시 Source로 확인한다.

**업무 Application이 담당하는 영역**

- 업무 Use Case
- 업무 Domain Model
- 업무 Validation
- 업무 Policy
- 업무 데이터 구조
- 업무별 API
- 업무별 Event
- 업무별 Batch Logic

Framework와 Business 영역의 경계가 보이지 않는 README는 부적합하다.

---

# 9. Product Goal / Current Implementation 분리 기준

CPF는 장기 제품 목표와 현재 Repository 구현 수준을 혼합하여 설명하면 안 된다.

README에 범위를 설명할 때 최소 다음 네 가지 개념을 구분한다.

| 구분의미                    |                            |
| ----------------------- | -------------------------- |
| Product Goal            | CPF가 장기적으로 목표로 하는 범위       |
| Current Implementation  | 기준 Commit의 Source에 존재하는 구현 |
| Verified Scope          | 실제 실행 또는 Test로 확인한 범위      |
| Limitation / Unverified | 아직 검증하지 않았거나 제약이 있는 범위     |

특히 다음 오류를 금지한다.

- 목표 문서에 있다고 현재 구현된 기능처럼 표현
- Interface만 존재하는 기능을 지원 기능으로 표시
- 미사용 Module을 실제 Consumer가 있는 것처럼 표시
- Unit Test만 존재하는 기능을 Runtime 검증된 기능으로 표현
- Sample 동작을 제품 동작으로 일반화
- 특정 DB에서만 확인한 기능을 모든 DB 지원으로 일반화
- 단일 인스턴스 확인을 Scale-out 검증으로 표현

---

# 10. Product Map 작성 기준

Product Map은 사용자가 CPF 전체 구성요소를 한눈에 이해하기 위한 영역이다.

단순 Module 이름 목록이 되어서는 안 된다.

각 Product 또는 영역마다 최소 다음 정보를 식별할 수 있어야 한다.

| 필드설명           |                    |
| -------------- | ------------------ |
| Product/Area   | 제품 또는 기능 영역        |
| Responsibility | 무엇을 담당하는가          |
| Primary User   | 주요 사용자             |
| Main Runtime   | 어느 Runtime에서 동작하는가 |
| Related Guide  | 상세 설명 공식 매뉴얼       |

예시 개념은 다음과 같다.

- CPF Core
- Business Application
- ADM
- Batch
- BZA
- Gateway
- Database
- Messaging
- External Integration
- Operations

실제 Repository 기준으로 명칭과 관계를 확정한다.

## 10.1 Module 이름 사용 규칙

README에 실제 Java Module 이름을 표시한다면 다음을 확인한다.

- 실제 Directory 존재
- Build 설정 포함 여부
- Package 역할
- Dependency 방향
- 실제 Consumer
- 실행 Module인지 Library인지
- Public API 제공 여부

Repository의 Directory 이름만 보고 역할을 임의 추론하지 않는다.

---

# 11. Architecture 작성 기준

Architecture 영역은 README의 핵심이다. **README에서 삭제하거나 기능별 그림으로 대체하지 않는다.**

목표는 사용자가 Source를 열지 않고도 CPF의 **전체 기능과 구조, 주요 실행 경로, CPF 영역과 외부 영역의 경계**를 한눈에 이해하는 것이다. README의 다른 Gateway·Batch·Starter 그림은 이 대표 Architecture를 보완하는 상세 시각화이며 대표 Architecture를 대신하지 않는다.

대표 Architecture는 처음 CPF를 보는 사용자가 다음 질문에 답할 수 있게 해야 한다.

- 요청은 어디에서 들어오고 업무 Domain까지 어떻게 연결되는가?
- 업무 Domain과 CPF Framework의 책임 경계는 어디인가?
- Starter/Common Function, Batch, ADM/BZA, Gateway가 전체 구조에서 어디에 위치하는가?
- DB, Messaging, File/Storage, 외부 시스템은 CPF 밖의 어떤 자원으로 연결되는가?
- 기능을 선택하거나 업무 Domain을 추가해도 전체 구조가 어떻게 유지되는가?

## 11.1 Architecture에 표현할 대상

실제 구현과 제품 범위에 따라 다음 관계를 검토한다.

- Client
- External Consumer
- Gateway
- Business Application
- Application Layer
- Domain Layer
- Persistence
- CPF Core
- Messaging
- Batch
- ADM Backend
- ADM Frontend
- BZA
- Database
- Kafka 또는 Messaging Infrastructure
- External System
- Observability
- Deployment / Runtime Boundary

## 11.2 Architecture 그림이 반드시 알려줘야 하는 것

그림을 본 사용자가 다음을 판단할 수 있어야 한다.

- 요청은 어디에서 들어오는가
- 어떤 Component를 통과하는가
- Business Logic은 어디에 위치하는가
- CPF 공통 기능은 어디에서 개입하는가
- 데이터는 어디에 저장되는가
- 비동기 메시지는 어디로 전달되는가
- Batch 실행 영역은 어디인가
- ADM은 업무 처리 경로와 어떤 관계인가
- Gateway는 어느 경계에 있는가
- 외부 시스템은 어느 지점에서 연계되는가

## 11.3 Dependency 방향

Architecture에서 Dependency 방향을 보여줄 경우 반드시 Source와 대조한다.

특히 다음을 확인한다.

- Domain → Infrastructure 역참조 여부
- Application → Domain 관계
- Adapter → Port 관계
- Framework → Business 의존 여부
- Business → Framework Public API 의존 여부
- ADM → Owner Module 호출 방식
- Gateway → Target Service 관계

그림이 실제 Dependency와 반대라면 시각적으로 좋아도 사용할 수 없다.

---

# 12. Architecture 그림 작성 규칙

그림 하나만 넣고 설명을 생략하지 않는다.

각 주요 Architecture 그림 아래에는 최소 다음 설명이 필요하다.

1. 그림의 목적
2. 주요 Component
3. Component 간 관계
4. 주요 요청 방향
5. 그림에서 생략한 세부 범위
6. 자세한 내용이 있는 Guide 링크

색상만으로 의미를 구분하지 않는다.

텍스트 Label을 함께 사용한다.

Desktop과 Mobile에서 읽을 수 있는 크기인지 확인한다.

이미지 파일을 사용하는 경우 다음을 확인한다.

- 실제 Repository에 존재
- 상대경로 유효
- 파일명 의미 있음
- 불필요한 임시 이미지 아님
- 오래된 Architecture가 아님
- Source와 일치

---

# 13. Topology 작성 기준

Topology는 논리 Architecture와 구분한다.

Topology에서는 실제 배치 관점의 관계를 설명한다.

검토해야 할 예시는 다음과 같다.

- Single Instance
- Multi Instance
- Local Call
- Remote Call
- Modular Monolith
- MSA
- Gateway 포함 Topology
- ADM 분리 배치
- Batch Runner / Worker
- Database
- Kafka
- External System

단, 실제 지원 여부를 확인한다.

## 13.1 Topology별 최소 정보

각 Topology는 가능하면 다음을 알려준다.

- 어떤 상황에서 사용하는가
- 주요 Process / Instance
- Network Boundary
- 호출 방식
- Shared Resource
- Scale-out 지점
- 중요한 제약
- 상세 Guide

README에서는 배포 명령이나 전체 Property를 설명하지 않는다.

---

# 14. Online Execution Flow 작성 기준

Online 처리 흐름은 대표적인 사용자 요청이 Business Logic과 DB까지 도달하는 과정을 보여준다.

예시는 개념적으로 다음과 같다.

`Client → Gateway → API → Application → Domain → Persistence → Database`

실제 구조와 다르면 Source 기준으로 변경한다.

## 14.1 표시해야 할 핵심 개념

필요한 경우 다음을 시각적으로 보여준다.

- Authentication
- Authorization
- Request Validation
- Application Use Case
- Transaction Boundary
- Domain Processing
- Persistence
- Response

단, README에서는 구현 Method와 Class를 전수 설명하지 않는다.

상세 API 사용법은 `01_개발자매뉴얼.md` 등 해당 Guide로 연결한다.

---

# 15. Async Execution Flow 작성 기준

비동기 기능이 실제 구현되어 있다면 다음 흐름을 사용자가 이해할 수 있게 한다.

개념 예:

`Business Transaction → Outbox → Publisher → Kafka → Consumer → Inbox / Idempotency → Business Processing`

실제 Source에서 사용되는 구성요소에 맞게 수정한다.

README에서 최소한 다음 질문은 답해야 한다.

- 왜 비동기 처리를 사용하는가
- 메시지는 어디에서 생성되는가
- 어디에서 전달되는가
- Consumer는 어디에 있는가
- 중복 처리 방지는 어느 영역에서 담당하는가
- 실패와 복구의 상세 내용은 어느 Guide에 있는가

Retry, UNKNOWN\_RESULT, Reconciliation 등의 상세 상태 전이는 매뉴얼에서 설명한다.

---

# 16. Batch Execution Flow 작성 기준

Batch가 CPF의 주요 Product 영역이면 README에서 별도 흐름으로 보여준다.

검토 대상:

- Scheduler
- ADM
- Job Registry
- Batch Runner
- Spring Batch Job
- Step
- Reader
- Processor
- Writer
- Metadata DB
- Worker
- Partition
- Artifact

실제 구현에 없는 구성요소는 표시하지 않는다.

README 수준에서는 다음을 이해할 수 있으면 된다.

- Batch를 누가 요청하는가
- 어디에서 실행되는가
- 어떤 Engine을 사용하는가
- 실행 상태는 어디에서 관리되는가
- 운영자는 어디에서 확인하는가
- 개발자는 어느 Guide를 봐야 하는가

세부 JobParameter, Checkpoint, Restart, Abandon, Center-Cut, Lease, Fencing 등은 `02_배치개발매뉴얼.md`로 이동한다.

---

# 17. Operations Flow 작성 기준

README에는 개발부터 운영까지 CPF의 전체 Lifecycle을 한눈에 볼 수 있는 대표 흐름이 필요하다.

예시 개념:

`Develop → Build → Test → Package → Deploy → Start → Observe → Operate → Recover / Upgrade`

실제 구현과 Script를 확인하고 사용한다.

## 17.1 Operations Flow에서 보여줄 수 있는 개념

- Build
- Artifact
- DB Migration
- Deployment
- Configuration
- Runtime Start
- Health
- Logging
- Metrics
- Trace
- ADM
- Backup
- Restore
- Upgrade
- Rollback

README는 흐름과 진입점만 보여준다.

구체적인 명령, Property, 장애 Runbook은 `05_플랫폼운영매뉴얼.md`에서 설명한다.

---

# 18. Domain Journey 작성 기준

Domain Journey는 단순 Architecture Diagram과 다르다.

한 개의 대표 업무가 CPF를 어떻게 통과하는지 Story 형태로 보여준다.

예를 들어:

1. Client가 업무 요청
2. Gateway를 통과
3. Business API 진입
4. Application Use Case 실행
5. Domain Rule 수행
6. DB Transaction 처리
7. 필요 시 Event 기록
8. Async Consumer 실행
9. ADM 또는 Audit에서 결과 확인

실제 Repository에서 대표 가능한 업무가 있을 때만 구체적인 Domain 예제를 사용한다.

가상의 계좌이체, 주문, 결제 등의 사례를 Repository에 구현되어 있는 것처럼 표현하지 않는다.

가상 예시라면 반드시 예시임을 표시한다.

---

# 19. Capability Summary 작성 기준

README에 기능 Summary를 둘 경우 **전체 Reference 목록으로 만들지 않는다.**

기능은 사용자가 제품 범위를 인지할 수 있는 수준으로 그룹화한다.

예:

| Capability GroupREADME 수준에서 설명할 내용 |                         |
| ---------------------------------- | ----------------------- |
| Application Development            | 업무 Application 개발 기반    |
| Persistence                        | DB 접근과 Transaction 기반   |
| Messaging                          | Async 처리 기반             |
| Batch                              | Batch 개발·실행·운영 기반       |
| Security                           | 인증·인가 관련 기반             |
| Administration                     | 관리·운영 기능                |
| Gateway                            | 요청 진입 및 정책 처리           |
| Observability                      | Log·Metric·Trace 영역     |
| DB Lifecycle                       | Migration·Upgrade 관련 영역 |
| Generator                          | 개발 산출물 생성 지원            |

각 Capability는 실제 구현을 확인한다.

README에 Package/Class/Method를 수십 개 나열하지 않는다.

---

# 20. API·명령어 작성 기준

README에 API나 명령어를 전혀 넣지 않는 것이 원칙은 아니다.

다만 **제품 진입에 필요한 최소 API와 최소 명령어만** 포함한다.

## 20.1 README에 허용되는 명령

예:

- Repository Build
- 최소 Test
- Sample 또는 대표 Application 실행
- Health 확인

## 20.2 README에 넣지 않는 명령

- 전체 운영 명령 Reference
- DB 관리 명령 전수
- Batch 제어 명령 전체
- 장애 복구 명령 전체
- ADM 운영 명령 전체
- Kafka 관리 명령 전체

이 내용은 해당 매뉴얼로 이동한다.

## 20.3 명령어 작성 형식

명령을 제공한다면 반드시 다음을 확인한다.

- 실행 위치
- 선행 조건
- 실제 명령
- 정상 결과 판단 기준
- 실패 시 어디를 볼 것인지

단순히 명령어 한 줄만 던져놓지 않는다.

---

# 21. Minimal Quick Start 작성 기준

Quick Start의 목적은 **처음 제품을 받은 사용자가 가장 짧은 경로로 CPF를 확인하게 하는 것**이다.

전체 개발 Tutorial이 아니다.

최소 다음 순서로 구성한다.

## 21.1 Prerequisites

실제 Build 설정을 근거로 작성한다.

예:

- JDK Version
- Build Tool
- Docker 필요 여부
- DB 필요 여부
- Kafka 필요 여부

값을 추측하지 않는다.

## 21.2 Build

실제 동작하는 Build 명령을 작성한다.

명령 실행 위치를 표시한다.

## 21.3 Start

실제로 어떤 Module 또는 Application을 실행해야 하는지 명시한다.

Sample을 사용하는 경우 Sample임을 명확히 한다.

## 21.4 Verify

정상 기동을 어떻게 판단하는지 작성한다.

예:

- Process
- Port
- Health Endpoint
- 특정 Log

실제로 존재하는 방법만 작성한다.

## 21.5 Next Step

다음 작업에 따라 공식 Guide로 연결한다.

예:

- 업무 개발 → 01
- Batch → 02
- ADM 개발 → 03
- ADM 운영 → 04
- Platform 운영 → 05
- BZA → 90
- Gateway → 91

---

# 22. Guide Map 작성 기준

Guide Map은 README의 필수 핵심 영역으로 본다.

공식 사용자 문서는 다음 9개만 허용한다.

1. `README.md`
2. `cpf-docs/guides/00_프레임워크안내.md`
3. `cpf-docs/guides/01_개발자매뉴얼.md`
4. `cpf-docs/guides/02_배치개발매뉴얼.md`
5. `cpf-docs/guides/03_ADM개발자매뉴얼.md`
6. `cpf-docs/guides/04_ADM운영자매뉴얼.md`
7. `cpf-docs/guides/05_플랫폼운영매뉴얼.md`
8. `cpf-docs/guides/90_BZA매뉴얼.md`
9. `cpf-docs/guides/91_Gateway매뉴얼.md`

별도의 `cpf-docs/guides/README.md`를 만들지 않는다.

별도 공식 Quick Start, EDU, Reference, Case, Troubleshooting, Report, Runbook 문서를 만들지 않는다.

해당 내용은 기존 공식 매뉴얼 내부로 통합한다.

---

# 23. Guide Map은 역할 중심으로 작성한다

파일명만 나열하지 않는다.

사용자가 자신의 질문을 기준으로 문서를 선택할 수 있어야 한다.

예:

| 사용자가 하려는 일읽어야 할 문서            |                |
| ----------------------------- | -------------- |
| CPF 전체 구조를 이해하고 싶다            | 00 프레임워크 안내    |
| 신규 업무 API를 개발하고 싶다            | 01 개발자 매뉴얼     |
| Transaction 처리 방법을 찾고 싶다      | 01 개발자 매뉴얼     |
| Batch Job을 개발하고 싶다            | 02 배치개발 매뉴얼    |
| Batch 재시작과 운영을 알고 싶다          | 02 배치개발 매뉴얼    |
| ADM Backend/Frontend를 개발하고 싶다 | 03 ADM 개발자 매뉴얼 |
| ADM 화면을 이용해 운영하고 싶다           | 04 ADM 운영자 매뉴얼 |
| 설치·기동·배포·장애 대응을 하고 싶다         | 05 플랫폼 운영 매뉴얼  |
| BZA를 구축하거나 운영하고 싶다            | 90 BZA 매뉴얼     |
| Gateway를 구축·설정·운영하고 싶다        | 91 Gateway 매뉴얼 |

---

# 24. README와 각 매뉴얼의 책임 경계

## README

**무엇인지와 어디로 가야 하는지 알려준다.**

## 00 프레임워크 안내

**전체 구조와 개념을 이해시킨다.**

## 01 개발자 매뉴얼

**업무 개발자가 실제 Source를 만들 수 있게 한다.**

## 02 배치개발 매뉴얼

**Batch 개발·실행·복구를 수행할 수 있게 한다.**

## 03 ADM 개발자 매뉴얼

**ADM Backend/Frontend를 개발할 수 있게 한다.**

## 04 ADM 운영자 매뉴얼

**ADM 화면을 이용해 실제 운영 업무를 수행할 수 있게 한다.**

## 05 플랫폼 운영 매뉴얼

**설치·기동·배포·관제·장애·복구·Upgrade를 수행할 수 있게 한다.**

## 90 BZA 매뉴얼

**BZA 도입·개발·운영 업무를 수행할 수 있게 한다.**

## 91 Gateway 매뉴얼

**Gateway 구축·설정·운영·복구를 수행할 수 있게 한다.**

README가 이 상세 내용을 흡수하지 않는다.

---

# 25. 링크 작성 기준

README의 모든 링크는 최종 검증 대상이다.

검증 항목:

- 파일 존재
- 상대경로 정확성
- 대소문자
- 한글 파일명
- Anchor
- 이미지 경로
- 외부 URL
- 공식 Guide 링크

특히 GitHub에서는 Anchor 생성 규칙이 Markdown Renderer에 따라 영향을 받을 수 있으므로 실제 렌더링 기준으로 확인한다.

깨진 링크를 남기지 않는다.

---

# 26. 이미지 작성 기준

README에서 이미지는 장식이 아니라 정보 전달 수단이다.

이미지를 사용할 때 다음을 확인한다.

- 이미지가 설명하려는 내용을 명확히 전달하는가
- 텍스트를 대체하기 적합한가
- 작은 화면에서도 읽히는가
- Source와 일치하는가
- 현재 Architecture와 일치하는가
- 오래된 Module이 남아 있지 않은가
- 가상 Component가 포함되어 있지 않은가

README에서 이미지를 무조건 많이 사용하는 것을 품질로 간주하지 않는다.

그림 수보다 **정보 전달력과 구현 정합성**을 우선한다.

---

# 27. Desktop / Mobile 검수 기준

README는 GitHub Desktop 화면뿐 아니라 Mobile에서도 읽을 수 있어야 한다.

확인 대상:

- Hero 잘림
- 가로 스크롤
- Architecture 이미지 가독성
- Card 배열
- 긴 표의 가독성
- 긴 코드 블록
- Guide Map
- CTA
- 이미지 Caption

복잡한 HTML Layout을 사용할 경우 GitHub Markdown 환경에서 실제 렌더링을 확인한다.

---

# 28. 표 작성 기준

README의 표는 빠른 비교와 탐색을 위해 사용한다.

복잡한 운영 데이터 표를 README에 넣지 않는다.

좋은 표의 기준:

- Column 수가 과도하지 않음
- Cell이 여러 문단으로 길어지지 않음
- 한 행이 한 개념을 표현
- 상세 정보는 Guide 링크로 이동
- Desktop/Mobile 모두 고려

추천 용도:

- Product Map
- 역할별 Guide Map
- 지원 범위 Summary
- Quick Start Prerequisite

부적절한 용도:

- 전체 API 목록
- 전체 Property
- 전체 Permission
- 전체 Route
- 전체 DB Table
- 전체 Test Case

---

# 29. 코드 블록 작성 기준

README의 코드 블록은 최소한으로 사용한다.

코드 예제의 목적은 다음 중 하나여야 한다.

- Build
- Start
- Verify
- 아주 짧은 사용 개념 설명

수십 줄의 Java Sample을 README에 넣지 않는다.

업무 개발 전체 코드는 `01_개발자매뉴얼.md`로 이동한다.

코드 블록이 실제 Repository의 Code와 불일치하지 않는지 확인한다.

---

# 30. Source Evidence 조사 기준

README에서 기능을 소개하기 전에 작성자는 다음 근거를 찾아야 한다.

## Module

- `settings.gradle`
- `build.gradle`
- Maven POM
- 실제 Directory

## Public API

- Public Interface
- Annotation
- Service
- 실제 Consumer

## Runtime

- `Application` Entry Point
- Profile
- Port
- Config

## Database

- Migration
- Repository
- Schema
- Supported Vendor 설정

## Messaging

- Producer
- Consumer
- Outbox
- Inbox
- Topic Config

## Batch

- Job
- Step
- Registry
- Runner
- Scheduler
- Metadata

## ADM

- Backend Controller
- Owner Port
- Frontend Route
- Component
- API Client

## Gateway

- Route
- Predicate
- Filter
- Security
- Config

README 설명이 Source Evidence와 연결되지 않는다면 사용하지 않는다.

---

# 31. Consumer 확인 기준

기능이 존재하는 것과 실제로 사용되는 것은 다르다.

README에 주요 Capability로 소개하기 전에 가능하면 다음을 확인한다.

1. 정의 위치
2. 구현 위치
3. 호출 위치
4. 실제 Consumer
5. Runtime 연결
6. Test 존재 여부

다음 예시를 주의한다.

- SPI만 존재하고 구현체가 없음
- 구현체는 있지만 Bean 등록되지 않음
- Bean은 있지만 Consumer가 없음
- Frontend API Client는 있지만 Route에서 사용하지 않음
- Property는 있지만 읽는 코드가 없음
- Table은 있지만 Runtime에서 사용하지 않음

이런 경우 README에 현재 사용 기능으로 단정하지 않는다.

---

# 32. 지원 Topology 표현 기준

"MSA 지원", "Modular Monolith 지원", "다중 인스턴스 지원" 같은 표현은 매우 신중하게 사용한다.

최소 다음을 조사한다.

- Local Invocation
- Remote Invocation
- Service Discovery
- HTTP Client
- Serialization
- Authentication
- Timeout
- Retry
- Idempotency
- Distributed Lock 또는 Fencing
- Shared DB 여부
- Instance Identity
- Reconciliation

구현 일부만 존재한다면 **부분 구현** 또는 해당 범위만 설명한다.

---

# 33. DB Vendor 표현 기준

README에서 지원 DB를 소개하는 경우 다음을 확인한다.

- Build Dependency
- Driver
- Dialect
- Migration
- Vendor-specific SQL
- Test
- 실제 실행 검증

예를 들어 PostgreSQL Migration만 존재한다면 모든 RDBMS를 지원한다고 표현하지 않는다.

지원, 부분 구현, 미검증을 구분한다.

---

# 34. Security 표현 기준

Security 기능을 소개할 경우 실제 구현을 기준으로 한다.

검토 대상:

- Authentication
- Authorization
- Role
- Permission
- Data Scope
- Masking
- Reason
- Approval
- Audit
- Session
- Token
- HMAC
- TLS

README에서는 구조와 역할 정도만 설명한다.

Permission ID 전체 목록과 보안 운영 절차는 해당 Guide로 이동한다.

---

# 35. Observability 표현 기준

"관제 지원" 한 문장으로 끝내지 않는다.

README 수준에서는 다음 항목 중 실제 구현된 범위를 그룹으로 보여줄 수 있다.

- Log
- Metric
- Trace
- Health
- Audit

그러나 구체적인 Metric 이름, Log Pattern, Dashboard, Alert Threshold는 운영 매뉴얼로 이동한다.

---

# 36. Failure / Recovery 표현 기준

README에서 장애·복구를 과도하게 상세히 설명하지 않는다.

대신 CPF가 어떤 Failure Management 개념을 가지고 있는지 Architecture 수준에서 보여줄 수 있다.

예:

- Retry
- Restart
- Reprocess
- UNKNOWN\_RESULT
- Reconciliation
- Compensation
- Rollback

단 실제 구현을 확인한다.

사용자가 실제 복구 작업을 수행하기 위한 단계별 절차는 각 공식 매뉴얼에 작성한다.

---

# 37. README에서 상태 표현 기준

기능 상태를 표현해야 하는 경우 다음 용어만 사용한다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

다음과 같은 애매한 표현은 사용하지 않는다.

- 거의 완료
- 사실상 완료
- 대부분 지원
- 문제없어 보임
- 정상으로 판단
- 충분히 지원
- 거의 Production 수준

---

# 38. 금지되는 README 콘텐츠

다음 내용은 README에 넣지 않는다.

### 작업 관리 정보

- Current Request
- Current Status
- Progress
- Next Action
- Handover
- 담당자 메모

### QA 정보

- QA Issue 전체 목록
- Finding
- Severity Matrix
- Test Pass Rate
- Requirement Coverage

### 개발 내부 정보

- 전체 Class 목록
- 전체 Package 목록
- 전체 API 목록
- 전체 SQL 목록
- 전체 Config 목록

### 운영 Reference

- 전체 Property
- 전체 Environment Variable
- 전체 Runbook
- 장애별 명령 Reference
- Backup 명령 전체
- Kafka 운영 명령 전체

### ADM Reference

- Route 전체
- Menu 전체
- Permission 전체
- Field 전체
- Button 전체

이러한 상세 내용은 해당 공식 매뉴얼에 둔다.

---

# 39. 별도 문서 생성 금지

README 내용을 줄이기 위해 임의로 다음 공식 사용자 문서를 신규 생성하지 않는다.

- Quick Start
- FAQ
- Troubleshooting
- Runbook
- API Reference
- Property Reference
- EDU
- Tutorial
- Case Study
- Command Reference
- Architecture Guide

필요한 내용은 공식 9개 문서 중 적절한 문서에 통합한다.

신규 공식 문서 추가는 사용자 승인 없이 수행하지 않는다.

---

# 40. README 수정 방식

기존 승인 README가 존재하면 **전체 재작성보다 최소 수정**을 우선한다.

## 허용되는 최소 수정 예

- 오탈자 수정
- 깨진 링크 수정
- 잘못된 경로 수정
- 잘못된 Module명 수정
- 구현과 다른 설명 수정
- 오래된 Version 수정
- 사실과 다른 Architecture Label 수정
- 공식 Guide 링크 수정

## 사용자 승인 없이 하지 않는 것

- Hero 교체
- Hero 이미지 전면 교체
- Section 전체 재배치
- Architecture 전면 교체
- Product Map 전면 재설계
- Topology 삭제
- Execution Flow 삭제
- Operations Flow 삭제
- Domain Journey 삭제
- Guide Map 삭제
- Card UI 전면 재구성
- 전체 README Markdown 재작성

---

# 41. README 작성 시 질문 기반 검수

README 완료 전 실제 독자의 질문으로 검수한다.

## 제품 이해

- CPF의 정식 명칭은?
- CPF는 무엇인가?
- 왜 필요한가?
- 일반 공통 Library와 무엇이 다른가?
- Business Application과 CPF의 책임 경계는?

## Architecture

- 주요 제품 구성요소는?
- 어떤 Module이 어떤 역할을 하는가?
- 요청은 어떻게 흐르는가?
- DB는 어디에 있는가?
- Kafka는 어디에 있는가?
- ADM은 어디에 있는가?
- Gateway는 어디에 있는가?
- Batch는 어디에 있는가?

## 사용

- 처음 실행하려면 무엇이 필요한가?
- Build는 어떻게 하는가?
- 정상 실행 여부는 어떻게 판단하는가?

## 역할별 탐색

- 업무 개발자는 어디를 보는가?
- Batch 개발자는 어디를 보는가?
- ADM 개발자는 어디를 보는가?
- ADM 운영자는 어디를 보는가?
- Platform 운영자는 어디를 보는가?
- BZA 담당자는 어디를 보는가?
- Gateway 담당자는 어디를 보는가?

README 안에서 해당 답을 찾을 수 없거나 Guide로 이동할 수 없다면 보완 대상이다.

---

# 42. 정보 탐색성 검수

README 품질은 단순 분량으로 평가하지 않는다.

다음 시간을 목표로 검수한다.

### 30초 안에

사용자가 다음을 인지할 수 있어야 한다.

- CPF가 무엇인지
- 주요 제품 영역
- 상세 문서 위치

### 2\~3분 안에

다음을 파악할 수 있어야 한다.

- 전체 Architecture
- 대표 실행 흐름
- 자신이 읽어야 할 Guide

### 처음 사용하는 사용자

Source 검색 없이 최소 Quick Start를 수행할 수 있어야 한다.

---

# 43. 사실성 검수

README의 명사 하나까지 실제 Repository와 대조한다.

특히 다음은 오류 가능성이 높으므로 별도 점검한다.

- Module 명
- Package 명
- Application 명
- Port
- URL
- Profile
- Property
- DB Vendor
- Kafka
- Route
- API Endpoint
- Script
- Build 명령
- 실행 명령
- Guide 경로
- 이미지 경로

기억이나 과거 문서만으로 작성하지 않는다.

---

# 44. 문구 품질 기준

문장은 짧고 명확하게 작성한다.

좋지 않은 예:

> CPF는 다양한 기능을 통합 관리하며 안정적이고 편리하게 개발과 운영을 지원합니다.

왜 좋지 않은가:

- 어떤 기능인지 알 수 없음
- "통합 관리"의 범위 불명
- "안정적" 근거 없음
- 개발자 행동으로 연결되지 않음

더 나은 방향:

> CPF는 업무 Application이 공통으로 사용하는 실행 기반을 제공하며 Online, Async, Batch, ADM, Gateway 영역을 공식 Guide 체계로 분리해 설명한다.

실제 기능을 확인한 후 더 구체화한다.

---

# 45. 한 Section 한 목적 원칙

각 Section은 한 가지 사용자 질문에 답하도록 한다.

예:

- Product Map → "무엇으로 구성되어 있나?"
- Architecture → "서로 어떻게 연결되어 있나?"
- Execution Flow → "요청은 어떻게 처리되나?"
- Topology → "어떻게 배치할 수 있나?"
- Quick Start → "처음 어떻게 실행하나?"
- Guide Map → "자세한 내용은 어디에서 찾나?"

한 Section 안에 Architecture, 운영명령, API Reference를 모두 혼합하지 않는다.

---

# 46. 중복 방지 원칙

README에서 동일 설명을 여러 Section에 반복하지 않는다.

예:

Architecture에 이미 ADM 역할이 설명되어 있다면 Product Map에서는 요약만 한다.

상세 설명은 Guide Map 링크를 통해 해당 매뉴얼로 이동한다.

README가 길어지는 주요 원인은 기능 누락보다 **중복 설명**인 경우가 많으므로 반드시 중복을 제거한다.

---

# 47. 링크를 설명의 대체물로 사용하지 않는 원칙

"자세한 내용은 매뉴얼 참조"만 작성하고 README 자체 설명을 생략하면 안 된다.

README에서도 최소한 다음은 설명한다.

- 해당 영역이 무엇인지
- 왜 필요한지
- 전체 구조에서 어디에 있는지
- 누구를 위한 것인지

그 후 상세 Guide로 이동시킨다.

---

# 48. 그림을 설명의 대체물로 사용하지 않는 원칙

Architecture 이미지만 넣고 본문 설명을 생략하지 않는다.

그림만으로 이해하기 어려운 사용자를 고려한다.

그림 전후의 짧은 설명으로:

- 목적
- 주요 Component
- 핵심 흐름
- 상세 문서

를 제공한다.

---

# 49. README 분량 판단 기준

README 품질은 Line 수로 판단하지 않는다.

너무 짧아서도 안 되고 매뉴얼처럼 길어져서도 안 된다.

판단 기준은:

**제품 이해 → Architecture 이해 → 최소 실행 → 상세 Guide 이동**

이라는 목적을 달성하는 데 필요한 만큼 작성한다.

상세 정보가 늘어나는 경우 README를 계속 확장하는 것이 아니라 적절한 공식 Guide로 이동시킨다.

---

# 50. README 작업 완료 전 검증 절차

최종 수정 후 다음을 수행한다.

## 50.1 Git 상태

- `git status --short`
- `git diff --name-status`
- `git diff --stat`
- `git diff --check`

## 50.2 README 자체

- Markdown 렌더링
- 이미지
- 상대 링크
- Anchor
- Table
- Code Block
- Desktop
- Mobile

## 50.3 공식 문서

공식 사용자 문서가 정확히 허용된 체계를 유지하는지 확인한다.

특히 다음 파일을 신규 생성하지 않았는지 확인한다.

`cpf-docs/guides/README.md`

## 50.4 사실성

- Source
- SQL
- Config
- Frontend
- Script
- Test

와 README 설명을 재대조한다.

---

# 51. 완료 판정 기준

다음 조건을 모두 만족해야 README 작업을 완료로 판단할 수 있다.

### 제품 이해

- CPF가 무엇인지 설명 가능
- 제품 목표와 현재 구현이 혼동되지 않음
- 주요 Product 영역이 보임

### Architecture

- 주요 Component 관계가 보임
- Dependency 또는 호출 방향이 실제 Source와 일치
- Online / Async / Batch 등 주요 흐름이 설명됨

### 사용성

- 최소 Quick Start가 실제 수행 가능
- 사용자가 역할별 Guide를 바로 찾을 수 있음

### 사실성

- Source에 없는 API, Class, Property, Module, 화면, Permission을 만들지 않음
- 링크와 이미지가 유효함
- 명령이 실제 Repository와 일치함

### 문서 체계

- 공식 9개 문서 체계를 지킴
- 별도 사용자 문서를 추가하지 않음
- README에 세부 Reference를 과다하게 넣지 않음

### 기존 구조 보호

- 사용자 승인 없이 기존 브로셔형 구조를 훼손하지 않음

---

# 52. README 작업을 완료로 볼 수 없는 경우

다음 중 하나라도 해당하면 완료 처리하지 않는다.

- Architecture가 Source와 다름
- 존재하지 않는 Module을 표시
- 깨진 Guide 링크 존재
- Quick Start 명령을 직접 확인하지 못했는데 성공으로 기재
- 현재 구현과 Product Goal을 혼합
- 미검증 기능을 지원 완료로 표현
- README를 전체 Reference 문서로 변경
- 기존 승인 Hero나 Section을 임의로 전면 교체
- 공식 Guide 이외 별도 사용자 문서를 생성
- 모바일에서 주요 Architecture를 읽을 수 없음
- README에 QA/Handover/Gap 정보를 삽입
- 임시 이미지 또는 작업 파일이 Repository에 남음

---

# 53. 작업 종료 시 산출물 제공 기준

README를 실제 수정하는 작업을 수행했다면 결과는 하나의 ZIP으로 제공한다.

ZIP은 CPF Repository Root 기준 상대경로를 그대로 유지한다.

예:

```
README.md
assets/...

```

필요한 경우 수정된 이미지도 해당 실제 상대경로로 포함한다.

ZIP 내부에 다음과 같은 임의 상위 Directory를 만들지 않는다.

```
CPF_README_RESULT/
README_FINAL/
output/

```

Repository Root에서 그대로 압축을 풀어 덮어쓸 수 있어야 한다.

---

# 54. 최종 작업 보고 필수 정보

실제 README 수정 작업의 최종 보고에는 다음을 포함한다.

- ZIP 링크
- ZIP 파일명
- SHA-256
- 포함 파일 수
- 포함 파일 경로
- 기준 Repository
- 기준 Branch
- 기준 Commit SHA
- 신규 파일
- 수정 파일
- 삭제 파일
- 적용 방법
- 수행한 검증 명령
- 실제 검증 결과
- 미검증 항목
- Rollback 방법
- Commit 미수행 여부
- Push 미수행 여부

ZIP이 없으면 실제 수정 작업을 완료했다고 보고하지 않는다.

---

# 55. 가비지·빈 폴더 정리 기준

README 작업 과정에서 다음을 생성했다면 종료 전 확인한다.

- 임시 이미지
- Capture
- HTML Preview
- Patch
- Script
- Backup
- ZIP 중간본
- Log
- Temp Directory

최종 Repository와 ZIP에는 요청 범위에 필요한 파일만 남긴다.

작업 종료 보고에는 Repository Root에서 실행 가능한 안전한 한 줄 정리 명령을 제공한다.

정리 명령은 이번 작업으로 생성된 정확한 파일과 빈 Directory만 대상으로 한다.

다음은 사용하지 않는다.

- `git clean`
- 광범위한 Wildcard 삭제
- Repository 전체 재귀 삭제
- 다른 미추적 파일 삭제

---

# 56. README 작성자의 최종 자기검수 질문

최종 제출 전 아래 질문에 모두 답한다.

## A. 제품

- CPF가 무엇인지 처음 보는 사람이 알 수 있는가?
- 단순 Framework Library와 Business Platform의 차이를 알 수 있는가?
- 제품 목표와 현재 구현이 구분되는가?

## B. 구조

- 주요 제품과 Module이 한눈에 보이는가?
- 각 Component가 왜 존재하는지 알 수 있는가?
- Business와 Framework 경계가 보이는가?

## C. 흐름

- Online 요청 흐름을 이해할 수 있는가?
- Async가 있다면 메시지 흐름을 이해할 수 있는가?
- Batch 실행 흐름을 이해할 수 있는가?
- 운영 Lifecycle을 이해할 수 있는가?

## D. Topology

- Local/Remote 개념을 잘못 설명하지 않았는가?
- MSA/Modular Monolith 범위를 과장하지 않았는가?
- Multi-instance 지원을 근거 없이 표현하지 않았는가?

## E. 시작

- 필요한 환경을 알 수 있는가?
- Build 명령을 알 수 있는가?
- Start 방법을 알 수 있는가?
- 정상 여부를 판정할 수 있는가?

## F. 탐색

- 신규 업무 개발자는 어디로 가야 하는가?
- Batch 개발자는 어디로 가야 하는가?
- ADM 개발자는 어디로 가야 하는가?
- ADM 운영자는 어디로 가야 하는가?
- Platform 운영자는 어디로 가야 하는가?
- BZA 담당자는 어디로 가야 하는가?
- Gateway 담당자는 어디로 가야 하는가?

## G. 정합성

- Source와 다른 내용이 없는가?
- 존재하지 않는 API를 만들지 않았는가?
- 존재하지 않는 Property를 만들지 않았는가?
- 오래된 Diagram을 사용하지 않았는가?
- 모든 링크가 유효한가?

## H. 문서 체계

- README에 너무 상세한 Reference를 넣지 않았는가?
- 상세 내용이 적절한 Guide로 이동되어 있는가?
- 공식 7개 문서 외 사용자 문서를 생성하지 않았는가?

모든 질문을 충족하지 못하면 README 작업을 완료로 처리하지 않는다.

---

# 57. README의 최종 품질 정의

CPF README의 품질 목표는 README 자체에 모든 정보를 집어넣는 것이 아니다.

README의 품질은 다음 세 가지로 평가한다.

### 1. 이해

처음 CPF를 접한 사용자가 Source 역분석 없이 제품의 정체와 전체 구조를 이해한다.

### 2. 탐색

사용자가 자신의 역할과 질문에 맞는 공식 매뉴얼을 빠르게 찾는다.

### 3. 사실성

README의 모든 주요 제품 설명, Architecture, Flow, Module, 명령, 링크가 기준 Commit의 실제 구현과 일치한다.

따라서 CPF README는 다음 원칙으로 작성한다.

> **넓게 보여주고, 구조를 이해시키고, 최소한 실행하게 하고, 상세 업무는 정확한 공식 매뉴얼로 연결한다.**

README 자체를 개발자 매뉴얼이나 운영자 매뉴얼로 만들지 않는다.

---

# 58. 작업자용 최종 핵심 규칙

README 작업자는 최소 다음 15개 규칙을 항상 지킨다.

1. 작업 시작 전에 `origin/master` 기준 Commit을 확인한다.
2. `CPF_DOCUMENTATION_STANDARD.md`를 먼저 읽는다.
3. 실제 Source가 기존 문서보다 우선한다.
4. README는 제품 브로셔이자 Architecture 진입점이다.
5. README는 전체 Reference 문서가 아니다.
6. Product Goal과 Current Implementation을 구분한다.
7. 기능이 존재한다고 실제 사용 중이라고 단정하지 않는다.
8. Architecture는 실제 Dependency와 맞아야 한다.
9. Online·Async·Batch·운영 흐름을 필요한 범위에서 시각적으로 보여준다.
10. Quick Start는 최소 시작 경로만 다룬다.
11. 상세 개발·운영 절차는 공식 Guide로 연결한다.
12. 공식 사용자 문서는 지정된 7개만 유지한다.
13. 사용자 승인 없이 기존 README 브로셔 구조를 전면 변경하지 않는다.
14. 근거 없는 품질·지원·운영성 표현을 사용하지 않는다.
15. README를 읽은 사용자가 **“CPF가 무엇이고, 어떻게 구성되며, 나는 다음에 어느 문서를 읽어야 하는가”**에 답할 수 있어야 한다.