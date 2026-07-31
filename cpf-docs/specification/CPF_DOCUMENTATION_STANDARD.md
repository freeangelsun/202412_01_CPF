# CPF 문서·가이드 작성 및 관리 표준

## 1. 정본 정보

- 정본 파일명: `CPF_DOCUMENTATION_STANDARD.md`
- 정본 배치 경로: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 적용 대상:
  - Repository Root `README.md`
  - `cpf-docs/guides/` 아래 역할별 Guide
  - 공식 매뉴얼 내부의 EDU, 기능 참조, 업무 사례, Runbook, Diagram, Screenshot
- 정본 우선순위:
  1. `CPF_FINAL_TARGET_REQUIREMENTS.md`
  2. Architecture·Specification 정본
  3. `CPF_DOCUMENTATION_STANDARD.md`
  4. 역할별 Guide
  5. 공식 매뉴얼 내부의 EDU·기능 참조·업무 사례·Runbook
- 이 지침은 문서 작업을 시작할 때마다 먼저 확인한다.
- 문서 구조, 이름, 역할, 완료 기준을 바꾸려면 이 지침을 먼저 갱신한다.
- README나 Guide 개별 파일에서 별도의 문서 정책을 중복 정의하지 않는다.
- 이 지침의 대목차·중목차·세부 항목은 선택 예시가 아니라 최소 품질선이다.
- 실제 Source에서 추가 기능이 확인되면 이 지침보다 더 상세하게 작성한다.
- 지침에 없다는 이유로 기능·등록·변경·운영·오류·부분 실패·복구·Upgrade·Rollback을 생략하지 않는다.
- 지침보다 적은 범위로 작성하고 완료로 판정하는 것을 금지한다.


## 1.1 공식 Git Repository

- 공식 Repository: `https://github.com/freeangelsun/202412_01_CPF`
- 공식 기준 Branch: `master`
- 기준 Remote 이름: `origin`
- 최상위 제품 목표 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 작성 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

다른 Repository, Fork, Branch, 과거 ZIP, 이전 대화에서 전달된 파일, 작업자의 기억을 기준 정본으로 사용하지 않는다.

Commit SHA는 이 지침에 고정하지 않는다. 모든 작업을 시작할 때마다 최신 `origin/master`의 SHA를 확인하고, 구현·검수·문서·Evidence에 실제 기준 Commit을 기록한다.

최신 `origin/master` 확인은 Local Working Tree의 기존 변경을 삭제·덮어쓰기·초기화한다는 뜻이 아니다. Local 변경은 우선 보호 대상으로 식별한다.

## 1.2 작업 시작 전 Git 확인

모든 작업은 파일 수정 전에 다음 상태를 먼저 확인한다.

```bash
git remote -v
git branch --show-current
git fetch origin master
git rev-parse HEAD
git rev-parse origin/master
git log -1 --oneline origin/master
git status --short
git diff --name-status
git diff --stat
git ls-files --others --exclude-standard
```

작업 시작 시 최소한 다음을 판단한다.

1. 현재 Repository가 공식 Repository와 일치하는가
2. 현재 Branch가 `master`인가
3. 현재 `HEAD`와 `origin/master`가 같은가
4. Local이 Ahead·Behind·Diverged 상태인가
5. 수정·삭제·신규·미추적 파일이 있는가
6. 다른 작업자가 진행 중인 변경이 있는가
7. README와 공식 매뉴얼에 보호해야 할 변경이 있는가
8. 이번 작업의 수정 대상과 보호 대상을 분리했는가
9. 최신 `master`의 Source와 Local 작업 결과를 함께 검토해야 하는가
10. 충돌·회귀·가비지 위험이 있는가

Working Tree에 변경이 있으면 변경 출처와 역할을 판단하기 전에는 Checkout·Restore·Reset·Clean·Stash·Rebase·Merge를 수행하지 않는다.

Local이 Ahead·Behind·Diverged 상태이면 임의로 정리하지 않는다. 현재 상태, 충돌 가능성, 보호 대상과 필요한 선택지를 사용자에게 보고한다.

## 1.3 Git 기준 상태 판단 순서

작업 기준은 다음 순서로 판단한다.

1. 최신 `origin/master`
2. 사용자가 현재 작업 중이라고 명시한 Local 변경
3. 현재 작업 요청서
4. 실제 Source·SQL·API·Config·Frontend·Script·Test
5. Requirement·Gap·Review·Handover·Evidence
6. 이전 대화와 작업자 보고

문서의 완료 표시와 과거 성공 보고보다 실제 Git 구현 상태를 우선한다.

GitHub의 최신 `master`와 Local Working Tree는 별도로 확인한다. GitHub 조회 결과만으로 Local 변경 상태를 추정하지 않는다.

## 1.4 사용자 승인 없는 Git 쓰기 작업 금지

사용자의 명시적 승인 없이 다음 작업을 수행하지 않는다.

- `git commit`
- `git push`
- Branch 생성·변경·삭제
- Tag 생성·변경·삭제
- Pull Request 생성·수정·Merge
- `git merge`
- `git rebase`
- `git cherry-pick`
- `git revert`
- `git stash`
- `git checkout`을 이용한 파일 또는 Branch 변경
- `git switch`를 이용한 Branch 변경
- `git restore`
- `git reset`
- `git clean`
- Remote 변경
- Force Push
- GitHub Issue·Release·Repository 설정·권한 변경
- GitHub 파일 생성·수정·삭제

특히 다음 명령은 사용자 승인 없이 절대 실행하지 않는다.

```bash
git reset --hard
git clean -fd
git clean -fdx
git checkout -- .
git restore .
git restore --staged .
git stash --include-untracked
git push --force
git push --force-with-lease
```

“정리”, “복원”, “최신화”, “가비지 제거”라는 이유로 기존 작업자 변경을 삭제하지 않는다.

## 1.5 수정 전 보호 Baseline

파일 수정 전 최소한 다음 정보를 기록한다.

```text
Repository URL
현재 Branch
현재 HEAD
origin/master SHA
Ahead·Behind·Diverged 상태
수정 파일
삭제 파일
신규 파일
미추적 파일
README Hash
공식 매뉴얼 8개 Hash
문서 작성 표준 Hash
이번 작업 수정 대상
이번 작업 보호 대상
```

문서 작업에서는 특히 다음 파일을 보호한다.

```text
README.md

cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md

cpf-docs/guides/00_프레임워크안내.md
cpf-docs/guides/01_개발자매뉴얼.md
cpf-docs/guides/02_배치개발매뉴얼.md
cpf-docs/guides/03_ADM개발자매뉴얼.md
cpf-docs/guides/04_ADM운영자매뉴얼.md
cpf-docs/guides/05_플랫폼운영매뉴얼.md
cpf-docs/guides/90_BZA매뉴얼.md
cpf-docs/guides/91_Gateway매뉴얼.md
```

README는 최신 승인된 브로셔형 구조를 보호 Baseline으로 삼는다.

사용자의 명시적 승인 없이 Hero, 이미지, Section 구조, Architecture, Product Map, Topology, Execution·Operations 흐름, Domain Journey, Guide Map, 카드·CTA, Desktop·Mobile 브로셔 구조를 전면 변경하지 않는다.

## 1.6 GitHub 접근 원칙

GitHub에서 Repository 내용을 읽고 확인할 수 있다.

사용자의 명시적 승인 없이 GitHub에 다음 변경을 수행하지 않는다.

- Commit
- Push
- Branch
- Tag
- Pull Request
- Issue
- Release
- Repository 설정
- 권한 설정
- 파일 생성·수정·삭제

GitHub의 최신 상태를 확인했더라도 Local Working Tree의 기존 변경을 임의로 정리하지 않는다.

## 1.7 작업 완료 전 Git 검증

작업 완료를 보고하기 전에 다음을 확인한다.

```bash
git status --short
git diff --name-status
git diff --stat
git diff --check
git ls-files --others --exclude-standard
```

문서 작업에서는 필요한 경우 다음도 확인한다.

```bash
git diff -- README.md
git diff -- cpf-docs/guides
git diff -- cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md
```

최종 보고에는 최소한 다음을 포함한다.

```text
기준 Repository
기준 Branch
기준 Commit
origin/master SHA
Working Tree 상태
변경 파일
신규 파일
삭제 파일
보호한 기존 변경
실행한 검증 명령
실제 검증 결과
미실행·미검증 항목
Rollback 방법
Commit·Push 미수행 여부
```

직접 실행하지 않은 검증은 성공으로 기록하지 않는다.

## 1.8 Git 산출물 적용 안전성

산출물 Package를 전달하거나 적용할 때 다음을 지킨다.

- Repository Root 상대경로 유지
- 신규·수정·삭제 파일 구분
- SHA-256 제공
- 정확한 파일 수 제공
- Dry Run 제공
- 적용 명령 제공
- Rollback 방법 제공
- 적용 후 검증 명령 제공
- README와 다른 작업자 변경 보호
- Wildcard 삭제 금지
- 전체 미추적 파일 일괄 삭제 금지
- Repository Root 가비지 생성 금지

ZIP을 덮어쓰는 것만으로 기존 추가 파일이 삭제된다고 가정하지 않는다. 적용 후 중복 파일·폐기 파일·미추적 파일을 별도로 확인한다.

---

# 2. 문서 체계

## 2.1 정본 파일 목록

CPF의 공식 사용자 문서는 다음 9개만 허용한다.

```text
README.md

cpf-docs/
├─ specification/
│  └─ CPF_DOCUMENTATION_STANDARD.md
└─ guides/
   ├─ 00_프레임워크안내.md
   ├─ 01_개발자매뉴얼.md
   ├─ 02_배치개발매뉴얼.md
   ├─ 03_ADM개발자매뉴얼.md
   ├─ 04_ADM운영자매뉴얼.md
   ├─ 05_플랫폼운영매뉴얼.md
   ├─ 90_BZA매뉴얼.md
   └─ 91_Gateway매뉴얼.md
```

- 별도 공식 EDU·Reference·Case·Report 문서를 만들지 않는다.
- EDU, 기능 참조, 업무 사례별 따라하기, Troubleshooting, Runbook은 담당 매뉴얼 내부 장으로 통합한다.
- Current Request, Handover, Review, Matrix, Evidence, Manifest는 공식 사용자 문서가 아니며 기존 내부 정본 경로에서만 관리한다.

## 2.2 제거 대상

- `cpf-docs/guides/README.md`는 만들지 않는다.
- 문서 진입점은 Repository Root의 `README.md` 하나로 유지한다.
- 같은 역할의 Guide를 다른 이름으로 중복 생성하지 않는다.
- 작업 일지, Handover, QA 결과, Evidence는 Guide 폴더에 두지 않는다.

## 2.3 파일명 규칙

- 공식 사용자 문서 파일명은 2.1의 목록으로 고정한다.
- `91_Gateway매뉴얼.md`의 `Gateway` 대소문자를 정확히 유지한다.
- 공식 문서에 날짜·작업차수·버전·최종·검토 같은 접미사를 붙이지 않는다.
- 같은 역할의 영문·한글 중복 Guide를 만들지 않는다.
- 공식 파일명·경로·역할 변경은 사용자의 명시적 승인 없이는 금지한다.
- 본문 제목과 설명은 한글을 기본으로 하며 Class·Method·API·Property·공식 기술명은 원문을 유지한다.

---

# 3. 공통 작성 원칙

## 3.1 역할 완결성

각 Guide는 제목에 해당하는 사용자가 다른 사람의 추가 설명 없이 자신의 업무를 완료할 수 있어야 한다.

- `01_개발자매뉴얼.md`: 개발자가 따라 실제 기능을 만들고 시험·복구·배포 준비까지 수행할 수 있어야 한다.
- `02_배치개발매뉴얼.md`: Batch Job·Scheduler·Center-Cut을 등록·실행·중단·재시작·대사·복구할 수 있어야 한다.
- `03_ADM개발자매뉴얼.md`: ADM Backend·Frontend·Menu·Route·API·Permission·Approval·Audit 기능을 실제로 개발하고 시험할 수 있어야 한다.
- `04_ADM운영자매뉴얼.md`: 운영자가 ADM의 모든 기능을 조회·판단·통제·승인·복구할 수 있어야 한다.
- `05_플랫폼운영매뉴얼.md`: 설치·설정·기동·배포·Upgrade·Rollback·장애 복구를 수행할 수 있어야 한다.
- `90_BZA매뉴얼.md`: BZA를 도입·설정·사용·운영·확장할 수 있어야 한다.
- `91_Gateway매뉴얼.md`: Route를 개발·게시·운영·복구할 수 있어야 한다.

## 3.2 기능 완결성

모든 기능 설명은 다음 항목을 포함한다.

1. 기능 목적
2. 해결하는 문제
3. 대상 사용자
4. 사용 시점
5. 사용하지 말아야 할 경우
6. 선행 조건
7. 소유 Module
8. 실제 접근 위치
9. 실제 API·SPI·Annotation·Property·화면·명령
10. 입력값과 기본값
11. 전체 동작 흐름
12. 정상 실행 절차
13. 정상 결과
14. 상태 변화
15. 오류 코드
16. 경계 조건
17. 중복·동시성
18. Timeout·부분 실패
19. Retry·Restart·Reprocess·Reconcile·Rollback
20. 권한·Data Scope·Masking
21. Reason·Approval·Audit
22. Test 방법
23. 운영 화면 또는 ADM 확인 방법
24. Evidence
25. 연관 기능
26. 주의사항과 금지 사용법

## 3.3 사실성

- Source에 없는 기능을 작성하지 않는다.
- 가상의 API, Class, Property, 화면, Permission을 사용하지 않는다.
- Interface 존재만으로 제품 기능으로 설명하지 않는다.
- Sample을 제품 구현으로 설명하지 않는다.
- 실제 Consumer가 없는 기능은 완료로 설명하지 않는다.
- 직접 실행하지 않은 Test를 성공으로 기록하지 않는다.
- Source·SQL·API·Config·Frontend·Script·Test와 문서가 일치해야 한다.

## 3.4 언어

- 본문은 한글 중심으로 작성한다.
- 다음은 영문을 유지한다.
  - CPF, ADM, BZA, Gateway
  - Module명
  - Class, Method, API, Property
  - Spring Batch, Kafka, OpenAPI, JavaDoc
  - 상태 코드와 공식 기술 용어
- 일반 설명을 불필요하게 영문으로 쓰지 않는다.
- `Quick Start`는 `빠른 시작`, `Reference`는 `기능 참조`, `Case Playbook`은 `업무 사례별 따라하기`로 쓴다.

---

# 4. README.md 작성 표준

## 4.1 역할

README는 다음 세 가지 역할만 수행한다.

1. CPF 제품 브로셔
2. CPF Architecture의 시각적 소개
3. 역할별 공식 Guide 진입점

README는 전체 기능 사전, 운영 절차서, 개발 Reference 또는 QA 보고서가 아니다.

## 4.2 작성 기준

- 기존 브로셔형 구성과 이미지 중심 흐름을 기준으로 유지·개선한다.
- Hero, Architecture, Product Map, Topology, Execution, Operations, Domain Journey, Guide Map 같은 시각 자료를 중심으로 구성한다.
- 문장은 짧고 명확하게 작성한다.
- 각 단락은 하나의 핵심 메시지만 전달한다.
- 상세 기술 설명은 Guide로 연결한다.
- README 전체를 긴 기능 목록으로 만들지 않는다.
- 명령은 최소 진입 명령만 제공한다.
- 제품 상태, Gap, 완료 판정, QA 현황을 넣지 않는다.
- 작업 시작 시 최신 `master`의 README를 보호 Baseline으로 기록한다.
- 사용자 승인 없이 Hero, 이미지, Section 순서, Card·CTA, Architecture·Product Map·Guide Map을 전면 변경하지 않는다.
- README 수정은 기본적으로 오탈자·사실 오류·깨진 링크·공식 Guide 링크·접근성 보완의 최소 범위로 제한한다.
- 구조 변경이 필요하면 변경 전후 Section·이미지·문구·링크 Mapping과 Rollback 방안을 먼저 제출해 사용자 승인을 받는다.
- 브로셔형 README를 긴 기능 목록, 기술 Reference, 작업 보고서 또는 Guide Portal로 바꾸지 않는다.

## 4.3 대메뉴와 단락

### 1. Hero

#### 포함 내용

- `Core Platform Framework`
- 한 문장 제품 메시지
- 핵심 적용 형태
- 대표 Guide 링크
- Desktop·Mobile Hero 이미지

#### 작성 기준

- 제품 전체를 표현한다.
- 특정 Module 하나를 중심으로 만들지 않는다.
- 기술 키워드를 과도하게 나열하지 않는다.
- 첫 화면만 보고 제품 성격을 이해할 수 있어야 한다.

### 2. CPF가 해결하는 문제

#### 포함 내용

- 프로젝트마다 반복되는 공통 개발
- 서로 다른 API·Header·오류·Paging 규격
- 배포 구조에 종속되는 호출
- 장애 이후 결과 불명과 복구 문제
- 개발·운영·감사·배포의 단절

#### 작성 기준

- 3~5개 핵심 문제만 제시한다.
- 긴 Requirement 목록을 넣지 않는다.
- 해결 방식은 다음 단락의 Architecture와 연결한다.

### 3. CPF 전체 구조

#### 포함 내용

- 기본 플랫폼
- 생성 업무영역
- ADM
- Batch
- DB
- Kafka
- 외부 시스템
- 선택 제품인 BZA와 Gateway

#### 시각 자료

- 전체 Architecture 이미지
- 기본 플랫폼과 선택 제품 구분 이미지

#### 작성 기준

- Module 책임을 한 문장씩 설명한다.
- 세부 API와 Package는 넣지 않는다.
- 선택 제품을 기본 필수 제품처럼 표현하지 않는다.

### 4. 배포 형태와 동일한 계약

#### 포함 내용

- Modular Monolith
- MSA
- 동일 JVM
- 분리 WAS
- Local Adapter
- Remote Adapter
- 다중 인스턴스

#### 시각 자료

- Topology 이미지

#### 작성 기준

- 배포 위치가 달라도 공개 계약이 유지되는 핵심만 설명한다.
- 상세 구현은 개발 Guide로 연결한다.

### 5. 중단돼도 이어지는 실행

#### 포함 내용

- 거래 식별
- 멱등성
- 상태 관리
- Timeout
- 결과 불명
- 대사·복구

#### 시각 자료

- 실행 상태와 복구 흐름 이미지

#### 작성 기준

- 정상 처리보다 실패 후 상태를 잃지 않는 제품 특성을 전달한다.
- 상세 상태값과 오류 코드는 넣지 않는다.

### 6. Batch 실행

#### 포함 내용

- Spring Batch
- Job·Step
- Scheduler
- Worker
- Agent
- Center-Cut
- Restart

#### 작성 기준

- Spring Batch를 Primary Engine으로 사용하는 책임 경계를 설명한다.
- 상세 Job 개발법은 Batch 개발 Guide로 연결한다.

### 7. 개발과 연결된 운영

#### 포함 내용

- ADM
- 거래·Log·Trace
- Batch·Gateway 상태
- 설정과 Runtime 통제
- 승인·감사·복구

#### 시각 자료

- 운영 흐름 이미지

#### 작성 기준

- ADM 개발 지원과 ADM 운영 역할을 구분한다.
- 화면 메뉴 목록은 넣지 않는다.

### 8. 신규 업무영역 확장

#### 포함 내용

- Generator
- DomainName
- SystemCode
- Module·Package·DB 충돌 검사
- 생성 후 개발 Guide 연결

#### 시각 자료

- Domain Journey 이미지

#### 작성 기준

- 명령은 한 개의 대표 Dry Run만 둔다.
- Generator 전체 옵션은 개발 Guide로 이동한다.

### 9. 역할별 Guide

#### 포함 문서

- `00_프레임워크안내.md`
- `01_개발자매뉴얼.md`
- `02_배치개발매뉴얼.md`
- `03_ADM개발자매뉴얼.md`
- `04_ADM운영자매뉴얼.md`
- `05_플랫폼운영매뉴얼.md`
- `90_BZA매뉴얼.md`
- `91_Gateway매뉴얼.md`

#### 작성 기준

- 각 Guide가 어떤 사용자를 위한 것인지 설명한다.
- 각 Guide에서 완료할 수 있는 업무를 한 문장으로 적는다.
- 별도 Guide Portal을 만들지 않는다.

### 10. 빠른 시작

#### 포함 내용

- 필수 Toolchain
- Build 시작 명령
- 최소 실행 명령
- 신규 Domain 생성 시작 명령
- 상세 Guide 링크

#### 제외 내용

- 전체 DB 초기화 옵션
- 전체 Profile
- 전체 환경변수
- 장애 처리
- Upgrade·Rollback 상세 절차

## 4.4 README 금지 내용

- Current Request
- Handover
- QA 결과
- Gap 목록
- Requirement Matrix
- 완료 판정 기준
- 전체 Property 목록
- 전체 메뉴 목록
- 전체 API 목록
- 장문의 Source Path
- 세부 운영 Runbook
- 전체 설치 절차
- 전체 Test 결과
- Evidence 목록
- 근거 없는 홍보 문구

---

# 5. 00_프레임워크안내.md

## 5.1 역할

CPF가 제공하는 주요 기능과 각 기능의 역할·소유 Module·사용자·연관 Guide를 설명하는 제품 기능 지도다.

모든 세부 사용법을 설명하는 문서가 아니다.

## 5.2 대메뉴

### 1. CPF 제품 개요

- 제품 목적
- 적용 대상
- 기본 제품
- 선택 제품
- 책임 범위
- 비책임 범위

### 2. 제품 구성

각 Module별로 다음을 작성한다.

- 역할
- 대표 기능
- 주요 사용자
- 의존 방향
- 연결 Guide

### 3. 지원 Architecture

- Modular Monolith
- MSA
- 동일 JVM
- 분리 WAS
- 다중 인스턴스
- 비동기
- Batch Worker
- Gateway 경유

### 4. 공통 개발 표준

- Header
- Context
- ID
- Error
- Validation
- Paging
- Date·Time
- 공통 응답

### 5. 온라인 거래와 신뢰성

- Transaction
- 멱등성
- 상태 관리
- Timeout
- Retry
- Circuit Breaker
- 결과 불명
- 대사·보상

### 6. 메시징과 외부 연계

- Kafka
- Outbox·Inbox
- Retry·DLT
- 외부 API
- File
- Attachment
- 전문

### 7. Batch와 자동화

- Spring Batch
- Scheduler
- Center-Cut
- Agent
- Runner
- Worker
- Restart
- Lease·Fencing

### 8. 보안과 감사

- 인증
- 권한
- Data Scope
- 마스킹
- 사유
- 승인
- 감사
- Break-glass

### 9. 개발·운영 Control Plane

- ADM 개발 지원
- ADM 운영 통제
- BZA
- Gateway Control Plane
- 위험 조치 통제

### 10. DB와 제품 Lifecycle

- 지원 DB
- Migration
- 설치
- 배포
- Upgrade
- Rollback
- Backup·Restore
- DR

### 11. 확장과 품질

- Generator
- OpenAPI
- JavaDoc
- EDU
- Test
- Evidence

### 12. 역할별 다음 Guide

각 기능을 상세 Guide의 정확한 단락으로 연결한다.

## 5.3 기능별 작성 깊이

- 기능 목적
- 해결 문제
- 핵심 동작
- 소유 Module
- 사용자
- 관련 Guide

코드·Property·상태값·화면 Field·오류별 복구는 역할별 Guide로 보낸다.

---

# 6. 01_개발자매뉴얼.md

## 6.1 역할

개발자가 CPF의 모든 개발 기능을 사용해 실제 업무 기능을 설계·구현·시험·운영 확인·복구 준비까지 완료하게 한다.

## 6.2 전체 구조

### 제1부. 개발 준비

1. 개발자 역할
2. Toolchain
3. Repository Clone
4. Build
5. Local 실행
6. IDE 설정
7. Profile과 환경변수
8. 개발 확인 Checklist

### 제2부. Repository와 Architecture

1. 공식 Module
2. 업무영역 Module
3. Package 규칙
4. Public API
5. SPI
6. Internal
7. 의존성 방향
8. 금지 Dependency
9. 개발 Workflow

### 제3부. Generator와 신규 업무영역

1. DomainName
2. SystemCode
3. Module
4. Package
5. Port
6. Route
7. DB
8. Dry Run
9. 충돌 검사
10. 생성
11. 생성 산출물
12. 사용자 수정 영역
13. 재생성
14. Rollback

### 제4부. 표준 API 개발

1. Header
2. Request
3. Response
4. Validation
5. Error
6. Paging
7. Sort
8. Versioning
9. OpenAPI
10. Generated Client

### 제5부. Application과 Domain

1. Command
2. Query
3. Application Service
4. Domain Service
5. Aggregate
6. 상태 전이
7. Event
8. Transaction Owner
9. Consumer 연결

### 제6부. Persistence와 DB

1. Repository
2. Mapper
3. Query Resource
4. Entity
5. Vendor별 SQL
6. Index
7. FK
8. Migration
9. Seed
10. Upgrade
11. Rollback
12. Drift

### 제7부. Transaction과 동시성

1. Transaction 경계
2. readOnly
3. Propagation
4. Isolation
5. Rollback 조건
6. Self Invocation
7. Optimistic Lock
8. Deadlock
9. Retry
10. Remote Call 분리
11. 장기 Transaction 금지

### 제8부. Local·Remote Service Call

1. 공통 계약
2. Local Adapter
3. Remote Adapter
4. Service Discovery
5. Header 전달
6. 인증 문맥
7. Timeout Budget
8. Error Mapping
9. Retry
10. Circuit Breaker
11. Bulkhead
12. Contract Test

### 제9부. 멱등성과 결과 불명

1. Idempotency Key
2. Request Hash
3. 중복 요청
4. 중복 결과
5. 상태기계
6. 응답 유실
7. UNKNOWN_RESULT
8. 결과 조회
9. Reconciliation
10. Compensation

### 제10부. Messaging과 비동기

1. Message Envelope
2. Producer
3. Consumer
4. Outbox
5. Inbox
6. Retry
7. DLT
8. 순서 보장
9. 중복 처리
10. 재처리
11. Callback

### 제11부. 외부 연계

1. 외부 API
2. 전문
3. 고정길이 전문
4. File
5. Attachment
6. Archive
7. Download
8. Shell·Process
9. Timeout
10. Retry
11. 결과 대사

### 제12부. Security·Permission·Audit

1. Authentication
2. Authorization
3. Permission
4. Data Scope
5. Masking
6. Reason
7. Approval
8. Audit
9. Secret
10. Security Test

### 제13부. 공통 기능

1. Config
2. Code
3. Message
4. Response Code
5. Business Calendar
6. Cache
7. Feature 설정
8. 변경 반영
9. 재기동 영향

### 제14부. 관측과 ADM 확인

1. Log
2. Metric
3. Trace
4. Transaction 조회
5. ADM에서 기능 확인
6. 오류 분석
7. 운영 영향 확인

### 제15부. Test

1. Unit
2. Slice
3. Integration
4. Contract
5. DB Vendor
6. Kafka
7. Fault
8. Concurrency
9. Security
10. Browser 연계
11. Evidence

### 제16부. 전체 Reference 구현

실제 Reference Domain 하나를 기준으로 다음을 연결한다.

```text
Generator
→ API
→ Application
→ Domain
→ DB
→ Local·Remote
→ Transaction
→ Idempotency
→ Kafka
→ File·외부 연계
→ Security
→ Audit
→ OpenAPI
→ Test
→ ADM 확인
→ 장애·복구
```

Reference는 코드 조각이 아니라 실행 가능한 전체 구현이어야 한다.

### 제17부. EDU

#### 기초

- 개발환경
- Build
- Generator
- 표준 API
- DB·Migration

#### 중급

- Local·Remote
- Transaction
- 멱등성
- Kafka
- File
- 외부 연계

#### 고급

- 부분 실패
- 결과 불명
- 대사·보상
- 동시성
- 보안·감사
- Fault Test

각 EDU 필수 항목:

1. 교육 목표
2. 선행 EDU
3. 준비 환경
4. 시작 상태
5. 단계별 작업
6. 전체 코드
7. 설정
8. DB Migration
9. 실행 명령
10. 정상 출력
11. 상태 변화
12. ADM 확인
13. 오류 재현
14. Fault Injection
15. 복구
16. Test
17. Evidence
18. 완료 Checklist
19. 수행 결과 확인·Evidence

### 제18부. 업무 사례별 따라하기

- 신규 업무영역 생성
- 표준 온라인 거래
- Local·Remote 전환
- 멱등 거래
- Kafka 비동기 처리
- 파일·첨부
- 외부 시스템
- 권한·마스킹·감사
- 결과 불명과 대사
- DB Upgrade·Rollback

### 제19부. 기능 참조표

- Public API
- SPI
- Annotation
- Property
- Header
- 상태값
- 오류 코드
- Permission
- DB·Migration
- Script
- Test
- EDU
- 업무 사례

---

# 7. 02_배치개발매뉴얼.md

## 대메뉴

1. Batch 개발 시작
2. CPF Batch Architecture
3. Job·Step 설계
4. Tasklet·Chunk·Flow
5. Reader·Processor·Writer
6. JobParameter·Identity
7. JobRepository·Metadata
8. ExecutionContext·Checkpoint
9. Transaction·Commit·Skip·Retry
10. Stop·Restart·Abandon
11. Parallel·Partition
12. Remote Partition·Remote Chunk
13. Scheduler·Misfire
14. Center-Cut
15. Agent·Runner·Worker
16. Worker Pool
17. Lease·Claim·Fencing
18. Job Pack·Artifact·배포
19. File·DB·API·Shell Job
20. 멱등성
21. UNKNOWN_RESULT·Reconciliation
22. Security·Approval·Audit
23. 성능·용량·다중 인스턴스
24. ADM 조회·제어·복구
25. Test·Fault Injection·Evidence
26. Batch Reference
27. Batch EDU
28. Batch 업무 사례별 따라하기
29. Batch 수행 결과 확인·Evidence
30. Troubleshooting

## 필수 EDU

- Tasklet
- Chunk
- File Import
- DB 처리
- API Job
- Restart
- Partition
- Remote Worker
- Scheduler
- Center-Cut
- Lease·Fencing
- UNKNOWN_RESULT·Reconciliation

## 필수 사례

- Step 중간 실패 후 Restart
- Commit 직후 Process Kill
- 동일 Job 중복 실행
- Worker 응답 유실
- Lease 만료
- Scheduler Misfire
- File 중복 수신
- 부분 처리 대사

---

# 8. 03_ADM개발자매뉴얼.md

## 역할

ADM 제품 개발자가 Backend·Frontend·Menu·Route·API·Permission·Approval·Audit·Masking·Owner Runtime 연결을 실제 기능 단위로 개발하고 시험하게 한다.

일반 CPF 개발자가 ADM을 조회·시험에 활용하는 방법은 `01_개발자매뉴얼.md`의 ADM 활용 단원에서 다룬다.

## 필수 대목차·중목차

### 제1부. ADM 개발 시작

1. ADM 제품 역할
2. ADM 개발자 역할
3. Backend·Frontend Repository 구조
4. 지원 Toolchain
5. Local 실행
6. 개발 Profile
7. Test 계정과 Permission
8. 개발 전 Checklist

### 제2부. ADM Architecture와 Ownership

1. Control Plane 책임
2. Owner Runtime 책임
3. Query와 Command 분리
4. Same-JVM Adapter
5. Remote Adapter
6. BFF
7. Spring Security·Session
8. Frontend Architecture
9. Transaction 경계
10. 역방향 의존 금지
11. Owner 없는 공통 기능 금지

### 제3부. Backend 조회 기능 개발

1. Controller
2. Query Request
3. Validation
4. Application Service
5. Owner Query Port
6. Same-JVM Query Adapter
7. Remote Query Adapter
8. Paging·Sort·Filter
9. Timeout
10. Error Mapping
11. Permission·Data Scope
12. Masking
13. Contract Test

### 제4부. Backend 상태 변경 기능 개발

1. Command Request
2. Validation
3. Idempotency
4. Expected Version
5. Reason
6. Approval
7. Owner Command Port
8. Same-JVM·Remote Command Adapter
9. Timeout·응답 유실
10. UNKNOWN_RESULT
11. Partial Apply
12. Reconciliation
13. Rollback
14. Audit Transaction 분리
15. Fault Test

### 제5부. Permission·Data Scope·Masking·Audit

1. Permission 정의
2. Role Mapping
3. Data Scope
4. 조회 Masking
5. Export Masking
6. Reason
7. Approval
8. 다단계 승인
9. Break-glass
10. Audit Event
11. Security Test

### 제6부. OpenAPI·Generated Client

1. OpenAPI 설명
2. Request·Response Contract
3. Error Contract
4. Example
5. Permission 설명
6. Generated Client Tool
7. Orval 또는 실제 채택 Tool
8. Contract Drift
9. CI Gate

### 제7부. Frontend Architecture

1. Vue Router
2. Pinia
3. TanStack Vue Query
4. Zod
5. Element Plus
6. TanStack Table
7. Component 책임
8. API Client
9. Server State·Client State 구분
10. Error Handling
11. Accessibility
12. Responsive
13. 외부 Runtime CDN·Font·Script 금지
14. 실제 채택·구현 상태 구분

### 제8부. Menu·Route 개발

1. Menu Model
2. Route 등록
3. Permission 연결
4. Breadcrumb
5. Navigation
6. Route Guard
7. Lazy Loading
8. Feature Toggle
9. Deep Link
10. Route Test

### 제9부. 조회·검색 화면 개발

1. 검색 Field
2. Default
3. Validation
4. Paging
5. Sort
6. Table Column
7. Status 표현
8. Loading
9. Empty State
10. Error State
11. Stale State
12. Export
13. Masking

### 제10부. 상세·Timeline 화면 개발

1. 상세 Field
2. Related Resource
3. 상태 Timeline
4. Transaction·Log·Trace 연결
5. Version
6. Permission
7. Masking
8. Audit
9. 오류 표시
10. 화면 이동

### 제11부. 위험 조치·승인 화면 개발

1. Button
2. Button 활성 조건
3. 입력 Form
4. Reason
5. Approval
6. Expected Version
7. Confirm
8. Timeout
9. 응답 유실
10. Partial Apply
11. Retry 금지·허용
12. Reconciliation
13. Rollback
14. Audit

### 제12부. 실시간·진행 상태 화면 개발

1. Polling
2. Streaming
3. Refresh
4. Stale Data
5. Progress
6. Partial Status
7. Instance별 상태
8. ACK·NACK
9. Timeout
10. 오류 표시

### 제13부. 기능군별 ADM 개발

1. System·Service·Instance·Topology
2. Transaction·Trace·Log
3. Runtime Control
4. Config·Code·Response Code·Message
5. Business Calendar·Cache
6. Kafka·Retry·DLT
7. File·Attachment·Download
8. Batch Job·Execution·Scheduler
9. Center-Cut·Runner·Worker·Agent·Lease·Fencing
10. Gateway Route·Target·Apply Status
11. Permission·Approval·Audit
12. Incident·Recovery

### 제14부. Test

1. Unit Test
2. Controller Test
3. Owner Port Test
4. Same-JVM·Remote Contract Test
5. Permission Test
6. Approval Test
7. Partial Apply Test
8. Playwright
9. Accessibility
10. Responsive
11. Fault Injection
12. Evidence

### 제15부. 전체 Reference 기능 구현

실제 ADM 기능 하나를 다음 전체 흐름으로 연결한다.

```text
Permission
→ Backend API
→ Owner Port
→ Same-JVM·Remote Adapter
→ OpenAPI
→ Generated Client
→ Route·Menu
→ Search·Table·Detail
→ 위험 조치
→ Approval
→ Partial Apply
→ Reconciliation
→ Audit
→ Playwright
```

### 제16부. ADM 개발 EDU

1. 조회 화면 EDU
2. 상세 화면 EDU
3. 상태 변경 화면 EDU
4. 위험 조치 EDU
5. 승인 EDU
6. 실시간 상태 EDU
7. 부분 적용·대사 EDU
8. Batch·Center-Cut EDU
9. Gateway EDU
10. EDU에서 실제 ADM 기능으로 전환

### 제17부. 업무 사례·기능 참조·Troubleshooting

1. 조회 기능 사례
2. 위험 조치 사례
3. 승인 사례
4. 부분 적용·대사 사례
5. API 색인
6. Route 색인
7. Permission 색인
8. Component 색인
9. Error 색인
10. Test 색인
11. 증상별 Troubleshooting
12. 금지 Backend·Frontend 패턴

## 기능별 작성 형식

```text
운영·개발 문제
→ Requirement와 Owner Runtime
→ Permission·Data Scope
→ Backend API·Owner Port
→ Same-JVM·Remote 동작
→ OpenAPI·Generated Client
→ Menu·Route·화면 Field
→ 상태 변경·Approval·Audit
→ Timeout·응답 유실·부분 적용
→ Reconciliation·Rollback
→ Test·Evidence
```

---

# 9. 04_ADM운영자매뉴얼.md

## 역할

운영자가 ADM의 모든 화면과 기능을 이용해 조회·판단·통제·승인·복구·감사를 수행하게 한다.

## 목차 원칙

- 실제 ADM 최상위 메뉴 순서를 따른다.
- 메뉴 구조가 변경되면 Guide를 같은 작업에서 갱신한다.
- 모든 화면을 빠짐없이 다룬다.

## 모든 화면의 필수 항목

1. 대메뉴
2. 중메뉴
3. 화면명
4. Route
5. 운영 목적
6. 대상 역할
7. Permission
8. 사전 조건
9. 검색 Field
10. 기본값
11. 목록 Column
12. 정렬·Paging
13. 상세 Field
14. 상태값
15. Button
16. Button 활성 조건
17. 입력값
18. Reason
19. Approval
20. Expected Version
21. 정상 상태 변화
22. Audit
23. 오류 처리
24. 응답 유실
25. 부분 적용
26. Retry
27. Reprocess
28. Reconcile
29. Rollback
30. 관련 화면
31. Evidence

## 필수 대단원

1. 접속·권한·교대 시작
2. Dashboard
3. System·Service·Instance·Topology
4. Capacity·Health
5. 온라인 거래
6. Transaction Group
7. 표준 실행
8. Channel·Service Registry
9. Runtime Control
10. Gateway 운영
11. Batch Overview
12. Job·Execution·Scheduler
13. Worker·Agent·Center-Cut
14. Batch Deployment·Recovery·Lease
15. Message·File·Download
16. Log·Remote Log·Audit Log
17. Log Level·Log Policy
18. Config·Code·Response Code·Calendar·Cache
19. 사용자·운영자·Permission
20. Security·Password·Session·Secret
21. Approval·Break-glass
22. Incident·Alert·Reliability
23. Recovery Center
24. 감사·Evidence·교대 종료
25. 전체 Menu Catalog
26. 운영 EDU
27. 업무 사례별 따라하기
28. Incident 보고서
29. Troubleshooting

---

# 10. 05_플랫폼운영매뉴얼.md

## 대메뉴

1. 운영 Architecture
2. 지원 환경
3. Toolchain
4. Artifact·무결성
5. 계정·Directory·권한
6. 전체 Property
7. 환경변수
8. Profile·Override
9. Secret·Certificate
10. DB Vendor별 설치
11. Schema·Migration·Seed
12. Kafka·외부 의존 자원
13. 신규 설치
14. 기동·종료
15. Health·Readiness·Liveness
16. 배포 Topology
17. 다중 인스턴스·Rolling
18. Blue-Green·Canary
19. Config 변경
20. Log·Metric·Trace
21. Capacity·성능
22. Backup
23. Restore
24. DR
25. Upgrade
26. Rollback
27. Certificate Rotation
28. DB 장애
29. Kafka 장애
30. Instance 장애
31. Disk·Memory·Network 장애
32. 보안 Incident
33. Housekeeping
34. 운영 Reference
35. 운영 EDU
36. 업무 사례별 따라하기
37. 운영·장애 수행 결과 확인·Evidence
38. Runbook 색인

## Property 필수 항목

- Key
- 환경변수명
- Type
- Default
- 필수 여부
- 허용 범위
- 소비 Module·Class
- 적용 Profile
- 적용 시점
- 재기동 여부
- Secret 여부
- 오류 증상
- 확인 명령
- 정상 결과
- Rollback

## Runbook 필수 항목

1. 증상
2. Alert
3. 영향 범위
4. 즉시 통제
5. Log
6. Metric
7. Trace
8. DB·Kafka 확인
9. 원인 분류
10. 복구 명령
11. 정합성 확인
12. 정상화 판정
13. Rollback 조건
14. 재발 방지
15. Evidence
16. Incident 보고서

---

# 11. 90_BZA매뉴얼.md

## 대메뉴

1. BZA 선택 기준
2. 제품 Architecture
3. 활성화·설치
4. DB·Migration
5. Bootstrap
6. 초기 관리자
7. 조직
8. 직원·배치
9. 사용자·Account
10. Role
11. Permission
12. Data Scope
13. 결재 정책
14. 상신·승인·반려
15. 위임·대결
16. 결재 Simulation
17. Attachment·Download
18. Notification
19. Session
20. Masking
21. Audit
22. Export
23. 업무 Domain API 연계
24. Backend 확장
25. Frontend 확장
26. 화면 사용
27. 보안·권한 Test
28. Backup·Restore
29. Upgrade·Rollback
30. BZA Reference
31. BZA EDU
32. 업무 사례별 따라하기
33. 수행 결과 확인·Evidence
34. Troubleshooting

역할을 도입 담당자, 관리자, 업무 관리자, 개발자, 플랫폼 운영자로 구분한다.

---

# 12. 91_Gateway매뉴얼.md

## 대메뉴

1. Gateway 선택 기준
2. Architecture
3. Data Plane·Control Plane
4. 설치·실행
5. Route Definition
6. Predicate
7. Filter
8. Path Rewrite
9. Header Policy
10. Body·Streaming
11. Target Group
12. Service Discovery
13. Load Balancing
14. Authentication
15. Authorization
16. Trusted Header
17. HMAC·Audience·Body Hash·Nonce
18. SSRF·TLS
19. Timeout Budget
20. Retry
21. Circuit Breaker
22. Bulkhead
23. Idempotency
24. Attempt Ledger
25. UNKNOWN_RESULT
26. Route Validation
27. Version·Checksum
28. Approval
29. Publish
30. Instance ACK·NACK
31. Partial Apply
32. Last Known Good
33. Rollback
34. Scale-out·Drift
35. Reconciliation
36. Probe·Health
37. Transaction·Log·Trace
38. ADM 연계
39. 장애 Runbook
40. Gateway Reference
41. Gateway EDU
42. 업무 사례별 따라하기
43. 수행 결과 확인·Evidence
44. Troubleshooting

역할을 Gateway 개발자, 관리자, 승인자, 플랫폼 운영자, 장애 대응자로 구분한다.

---

# 13. EDU·기능 참조·업무 사례·수행 결과 관리

## 13.1 배치 원칙

- EDU·기능 참조·업무 사례별 따라하기·Troubleshooting·수행 결과 확인 양식은 별도 공식 사용자 문서로 만들지 않는다.
- 각 항목은 담당 역할별 매뉴얼 내부 장으로 포함한다.
- 동일 설명을 여러 매뉴얼에 복사하지 않고 Owner 매뉴얼의 정확한 Anchor로 연결한다.
- 실제 수행 Evidence와 내부 검수 자료는 기존 Evidence·Work 정본 경로에서 관리하며 Guide 폴더에 두지 않는다.

## 13.2 EDU

EDU는 실제 CPF API와 실제 제품 구조를 사용한다.

필수 항목:

- 교육 목표
- 대상 수준
- 선행 EDU
- 준비 환경
- 시작 상태
- 단계별 작업
- 전체 코드
- 설정
- Migration
- 실행 명령
- 정상 결과
- 상태 변화
- ADM 확인
- 오류 재현
- Fault Injection
- 복구
- Test
- Evidence
- 완료 Checklist
- 수행 결과 확인·Evidence 기록

## 13.3 Reference

필수 항목:

- 식별자
- 분류
- 소유 Module
- Source Path
- Public API·SPI·Internal
- 목적
- 입력
- 출력
- 기본값
- 상태 변화
- Consumer
- 코드 예제
- Property
- DB
- 오류
- Permission
- Test
- EDU
- 사례
- 변경 영향
- Deprecated
- 대체 기능

## 13.4 업무 사례별 따라하기

필수 항목:

- Case ID
- 업무 배경
- Requirement
- Architecture
- 사용 Module
- 선행 환경
- 시작 상태
- 단계별 Source·설정·화면·명령
- 예상 결과
- 정상 시나리오
- 오류 시나리오
- Fault Injection
- 복구
- 정합성 확인
- 보안·감사
- Test
- Evidence
- 수행 결과 확인·Evidence 기록
- 완료 조건

## 13.5 수행 결과 확인·Evidence 기록

이 항목은 공식 Guide 내부의 수행 결과 확인 양식과 내부 Evidence 정본에 적용한다. 별도 사용자용 Report 문서를 만들지 않는다.

필수 항목:

- Report ID
- Case ID
- Requirement
- 기준 Commit
- 수행자
- 시작·종료 시각
- 환경·Profile
- DB·Kafka·외부 시스템
- 변경 Source·Config·SQL
- 실행 명령
- 정상 결과
- 오류 결과
- 복구 결과
- Test 결과
- Evidence
- 민감정보 제거
- 미검증
- 제한사항
- 최종 판정

---

# 14. 금지어와 모호한 표현

## 근거 없이 사용하지 않는 표현

- 완벽한
- 완전한
- 최고
- 최상의
- 혁신적인
- 압도적인
- 무결점
- 모든 문제를 해결하는
- 엔터프라이즈급
- 금융권 최고 수준
- 상용 수준
- 운영 준비 완료
- Production Ready
- 검증 완료
- 기능 완료
- 최종본
- 더 이상 작업 불필요
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
- 투명하게
- 안정적으로
- 안전하게
- 통합 관리
- 향후 지원
- 추후 보완

## 구체화 없이 사용하지 않는 표현

- 지원한다
- 처리한다
- 관리한다
- 연동된다
- 안전하게 처리한다
- 쉽게 사용할 수 있다
- 유연하게 확장한다
- 필요시 재시도한다
- 상황에 따라 처리한다
- 적절히 설정한다

대신 누가, 언제, 어떤 조건에서, 무엇을 입력하고, 어떤 API·화면·명령을 사용하며, 어떤 상태로 바뀌고, 실패 시 어떻게 복구하는지 작성한다.

금지어는 단어 자체를 전면 금지하는 것이 아니다. 기준 Commit, 적용 범위, 전제 조건, 지원 Topology, 실패 조건, 검증 결과와 미검증 범위가 명확한 기술 계약에 한해 사용할 수 있다.

---

# 15. 변경 관리

다음 변경은 Guide 갱신 대상이다.

- Public API
- SPI
- Annotation
- Property
- 환경변수
- Header
- Error Code
- 상태값
- Permission
- Menu·Route
- 화면 Field·Button
- DB Schema
- Migration·Rollback
- Generator
- OpenAPI
- Script
- Batch Job
- Gateway Route
- 설치·배포 방식
- 보안·감사 정책
- Test·Evidence 규칙

변경 절차:

```text
Source 변경
→ 영향 기능 식별
→ 영향 Guide 식별
→ 본문 갱신
→ Reference 갱신
→ EDU·사례 영향 확인
→ 링크·명령·경로 검증
→ 역할별 사용성 검수
→ Evidence
→ 기준 Commit 갱신
```

---

# 16. 검수와 완료 판정

허용 상태:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

문서 완료 조건:

- 역할과 대상 사용자가 명확하다.
- 해당 역할의 전체 기능이 포함돼 있다.
- 실제 절차가 있다.
- 정상·오류·경계·부분 실패·복구가 있다.
- 권한·보안·감사·승인이 있다.
- 실제 API·설정·화면·명령이 있다.
- Reference가 있다.
- EDU가 있다.
- 업무 사례별 따라하기가 있다.
- 수행 결과 확인·Evidence 기록를 작성할 수 있다.
- Source·SQL·API·Config·Frontend·Test와 일치한다.
- 실행한 검증과 미검증이 구분돼 있다.
- 역할별 독립 사용성 검수를 통과한다.

완료로 인정하지 않는 것:

- 파일 존재
- 긴 분량
- 많은 표·그림
- Route 수
- Property 이름 일부
- Source Path만 나열
- Sample 하나
- 정상 예제만 존재
- 링크 검사
- 문자열 Gate
- 자체 생성 문서끼리 일치
- 실행하지 않은 Test
- Consumer 없는 Interface
- 메뉴 존재

---

# 17. 절대 금지사항

- 문서 역할 임의 변경
- 같은 역할 Guide 중복 생성
- 공통 문장을 기능명만 바꿔 반복
- 가상 API·Class·Property·화면 사용
- Source에 없는 기능 설명
- 개발 Guide를 개념·Sample 모음으로 작성
- ADM 개발자 매뉴얼을 ADM 조회 활용법이나 Route 목록만으로 작성
- ADM 운영 Guide를 Route 목록으로 작성
- 플랫폼 운영 Guide를 일부 Property와 일반 Runbook으로 작성
- 정상 흐름만 작성
- 오류·부분 실패·복구 누락
- 권한·보안·감사·승인 누락
- EDU를 제품 API와 다른 규격으로 작성
- 사례와 Report 없이 완료 선언
- 줄 수·그림 수·키워드 수를 품질로 사용
- 실행하지 않은 내용을 성공으로 기록
- 사용자 승인 없이 Commit·Push·Branch·Tag·PR 생성

---

# 18. 지침 적용 강도와 문서 상세도

## 18.1 이 지침은 Sample이 아니다

이 지침에 적힌 목차와 검수 항목은 문서 작성 방향을 보여주는 Sample이 아니다.

- 각 대목차는 반드시 존재 여부를 검토한다.
- 각 중목차는 실제 기능·절차·운영 책임이 있으면 반드시 작성한다.
- 세부 항목은 Source에 맞춰 더 세분화할 수 있으나, 이유 없이 합치거나 삭제하지 않는다.
- 실제 구현이 더 복잡하면 이 지침보다 더 상세하게 작성한다.
- 구현이 없으면 목차를 삭제하지 않고 `미구현` 또는 `미검증`으로 표시한다.
- “현재 Source에는 없다”는 판단에도 검색 범위, 기준 Commit, 확인 경로를 남긴다.
- 한 개의 정상 Sample로 기능군 전체를 설명하지 않는다.
- 기능군마다 등록·조회·변경·활성·비활성·삭제·Version·권한·감사·오류·복구 Lifecycle을 검토한다.

## 18.2 역할별 독립 수행 원칙

각 매뉴얼의 대상 사용자는 다음을 수행할 수 있어야 한다.

```text
업무 목적 파악
→ 선행 조건 준비
→ 실제 기능 위치 탐색
→ 등록·설정·개발
→ 실행
→ 상태·결과 확인
→ 오류 판단
→ 부분 실패 범위 확인
→ 재처리·대사·복구
→ Rollback
→ Audit·Evidence 확인
→ 완료 판정
```

다음 상황이면 매뉴얼은 완료가 아니다.

- 사용자가 Source를 열어 이름과 사용법을 다시 추론해야 한다.
- Class·Property·화면 이름만 있고 사용 절차가 없다.
- 정상 결과는 있으나 실패 시 실제 상태가 없다.
- 복구 명령은 있으나 실행 조건과 부작용이 없다.
- 운영 조치의 Permission·Reason·Approval·Audit이 없다.
- 설정값은 있으나 Default·Consumer·재기동 영향이 없다.
- 메뉴는 있으나 검색 Field·Column·Button·상태 변화가 없다.
- EDU는 있으나 실제 업무 코드로 전환하는 방법이 없다.

## 18.3 기능 Lifecycle 기본 구분

각 기능은 해당되는 Lifecycle을 빠짐없이 검토한다.

### 개발 기능

```text
설계 → 생성 → 구현 → 연결 → Test → 배포 준비 → 운영 확인 → 변경 → Deprecated → 제거
```

### 설정·Metadata 기능

```text
등록 → 조회 → 수정 → Version → 승인 → 적용 → ACK·NACK → 부분 적용 → Rollback → 폐기
```

### 실행 기능

```text
요청 → 검증 → 승인 → 실행 → 진행 → 완료·실패·결과 불명 → 재처리·대사·복구 → 종료
```

### 운영 Resource

```text
등록 → 인증 → Health → Capacity → Drain → 격리 → 재등록 → Upgrade → Rollback → 폐기
```

### DB Artifact

```text
Canonical Source → 생성 → 설치 → Migration → 검증 → Upgrade → Rollback → Drift 확인
```

## 18.4 기능 설명의 필수 근거

각 기능 설명에는 가능한 범위에서 다음 근거를 연결한다.

- Requirement ID
- Owner Module
- Public API·SPI·Internal Package
- 실제 Consumer
- Source Path와 주요 Class
- Controller·Endpoint
- Property와 소비 Class
- SQL·Migration
- Frontend Route·Component
- Permission
- Test
- ADM 확인 위치
- 실행 검증 결과
- 미검증 범위

경로만 나열하지 않고 그 파일이 어떤 책임을 가지는지 설명한다.

## 18.5 현재 상태 표시 Block

주요 기능 단락 시작부에는 다음 형식의 상태 Block을 둔다.

```text
제품 목표:
현재 구현:
Owner Module:
실제 Consumer:
지원 Topology:
지원 DB Vendor:
검증 범위:
미검증·제한사항:
상태:
```

## 18.6 사용자 작업 절차 Block

실제 조작·명령·개발 절차에는 다음을 포함한다.

1. 수행 목적
2. 수행 역할
3. 필요 Permission
4. 사전 승인
5. 선행 환경
6. 시작 상태
7. 접근 화면·API·명령
8. 입력값과 Default
9. 단계별 수행
10. 정상 응답
11. 상태 변화
12. DB 변화
13. Log·Metric·Trace
14. ADM 확인
15. 오류별 판단
16. 재요청 가능 여부
17. 자동 Retry 여부
18. 수동 조치 여부
19. Reprocess·Reconcile·Compensation
20. Rollback
21. Audit
22. Evidence
23. 완료 조건

## 18.7 탐색 구조

각 공식 매뉴얼은 문서 상단 또는 후반 색인에서 다음 탐색을 제공한다.

- 독자·역할별 읽기 순서
- 처음 사용하는 사람의 시작 위치
- 업무 목적별 바로가기
- 기능별 바로가기
- API·SPI·Annotation 색인
- Property·환경변수 색인
- DB·Migration 색인
- Menu·Route·Permission 색인
- 상태값·오류 코드 색인
- Script·명령 색인
- EDU 경로 색인
- 업무 사례 색인
- 증상·Runbook 색인

별도 `cpf-docs/guides/README.md`를 만들지 않고 각 매뉴얼 내부에서 탐색성을 확보한다.

---

# 19. README 브로셔 보호 강화 기준

## 19.1 보호 Baseline

README 작업을 시작할 때 최신 `master`의 `README.md`를 보호 Baseline으로 고정한다.

다음을 기록한다.

- 기준 Commit
- 파일 Hash
- 전체 Heading 순서
- Hero 문구
- Hero Desktop·Mobile 이미지
- Architecture 이미지
- Product Map 이미지
- Topology 이미지
- Execution·Recovery 이미지
- Operations 이미지
- Domain Journey 이미지
- Guide Map 이미지
- Card·Badge·CTA 구조
- 공식 Guide 링크
- 빠른 시작 명령

## 19.2 기본 수정 원칙

README는 “새로 잘 쓰는 것”보다 “검증된 브로셔 틀을 보호하면서 사실과 링크를 맞추는 것”을 우선한다.

기본적으로 허용되는 수정은 다음이다.

- 오탈자
- 깨진 링크
- 공식 Guide 파일명 변경 반영
- 실제 구현과 다른 설명의 최소 교정
- 접근성 대체 텍스트
- 이미지 경로 오류
- 최소 빠른 시작 명령의 사실 교정

## 19.3 구조 변경 승인 기준

다음은 사용자 승인 전에는 수정하지 않는다.

- Hero 문구의 전면 교체
- Hero 이미지 교체
- Section 추가·삭제·순서 변경
- Architecture·Product Map·Guide Map 재설계
- Card·Badge·CTA 디자인 변경
- 이미지 중심 구성을 긴 설명으로 변경
- 브로셔를 기술 Reference나 작업 보고서로 변경
- Desktop·Mobile 레이아웃 변경
- 기존 핵심 제품 문구 삭제
- README 전체 재작성

구조 변경이 필요하면 다음을 먼저 제출한다.

1. 변경 필요성
2. 현재 구조
3. 변경 구조
4. 문구 Diff
5. 이미지 Diff
6. 링크 영향
7. Desktop·Mobile 영향
8. Rollback 방법
9. 보호할 기존 요소

## 19.4 README 완료 Gate

- 기존 브로셔형 흐름이 유지됐다.
- 첫 화면에서 CPF 제품 성격을 이해할 수 있다.
- 기본 제품과 선택 제품이 구분된다.
- MSA·Modular Monolith·동일 JVM·분리 WAS가 과장 없이 설명된다.
- 중단 이후 재시작·대사·복구 특성이 전달된다.
- Spring Batch와 CPF Control Plane 책임이 혼동되지 않는다.
- 공식 Guide 8개 링크가 정확하다.
- QA·Gap·진행률·작업 일지가 없다.
- 장문의 Property·API·메뉴·Runbook이 없다.
- 사용자 승인 없는 구조 변경이 없다.
- 기존 이미지가 누락되지 않았다.
- Desktop·Mobile 자산이 함께 검수됐다.

README가 기술적으로 맞아도 기존 브로셔 틀을 훼손했으면 `실패`다.

---

# 20. 매뉴얼별 강화 통과 기준

## 20.1 `00_프레임워크안내.md`

다음 질문에 문서만으로 답할 수 있어야 한다.

1. CPF는 어떤 제품이며 무엇을 해결하는가?
2. CPF가 책임지는 것과 책임지지 않는 것은 무엇인가?
3. `cpf-core`, `cpf-common`, `cpf-admin`, `cpf-biz-admin`, `cpf-batch`, Gateway의 Owner 책임은 무엇인가?
4. Public API·SPI·Internal은 어떻게 구분하는가?
5. Modular Monolith와 MSA는 어떤 계약을 공유하는가?
6. Local Adapter와 Remote Adapter는 무엇이 같은가?
7. 다중 인스턴스·부분 실패에서 어떤 상태를 남기는가?
8. Transaction·멱등성·UNKNOWN_RESULT·Reconciliation의 관계는 무엇인가?
9. Kafka·File·외부 연계의 실패와 재처리는 누가 소유하는가?
10. Spring Batch와 CPF Control Plane은 각각 무엇을 소유하는가?
11. Center-Cut·Worker·Agent·Lease·Fencing은 어디에 속하는가?
12. Authentication·Permission·Data Scope·Masking·Approval·Audit은 어디서 적용하는가?
13. DB Vendor·Migration·Upgrade·Rollback·Drift는 어떻게 관리하는가?
14. Generator가 생성하는 것과 사용자가 수정하는 것은 무엇인가?
15. ADM·BZA·Gateway는 언제 사용하는가?
16. 현재 구현과 최종 제품 목표는 어떻게 구분되는가?
17. 각 역할은 어떤 매뉴얼로 이동해야 하는가?

단순 Architecture 소개가 아니라 기능→Owner→사용자→상세 매뉴얼 Mapping이 있어야 한다.

## 20.2 `01_개발자매뉴얼.md` 개발 여정 Gate

신규 개발자가 다음 전체 여정을 수행할 수 있어야 한다.

```text
Toolchain 확인
→ Repository Clone·Build
→ Local Runtime 기동
→ Generator Dry Run
→ DomainName·SystemCode 결정
→ Module·Package·Port·Route·DB 충돌 검사
→ 신규 업무영역 생성
→ 생성 산출물과 사용자 수정 영역 확인
→ 표준 API 설계
→ Application·Domain 구현
→ Persistence·Migration 연결
→ Transaction·동시성 설계
→ Local Service Call 실행
→ Remote Service Call 전환
→ 멱등성·UNKNOWN_RESULT 적용
→ Kafka·Outbox·Inbox 적용
→ File·Attachment·외부 연계 적용
→ Permission·Data Scope·Masking·Audit 적용
→ OpenAPI·JavaDoc 작성
→ Unit·Integration·Contract·Fault Test
→ ADM 거래·Log·Trace 확인
→ 오류·부분 실패 재현
→ Reconciliation·Compensation 수행
→ Build·배포 인계
```

## 20.3 `01_개발자매뉴얼.md` EDU 활용 Gate

EDU는 목록만 제공해서는 안 된다.

### 역할별 EDU 경로

- CPF 신규 입문자
- Spring 경험 개발자
- 신규 업무영역 개발자
- 공통 Framework 개발자
- 외부 연계 개발자
- Kafka 개발자
- 보안·권한 개발자
- Test·QA 개발자
- 기술 책임자

각 역할마다 다음을 제공한다.

- 필수 EDU
- 선택 EDU
- 선행 EDU
- 권장 순서
- 예상 수행 업무
- 완료 후 가능한 실무
- 관련 매뉴얼 단락
- 관련 Test
- 다음 학습 경로

### 업무별 EDU 경로

최소한 다음 경로를 제공한다.

1. 신규 REST 조회 API
2. 상태 변경 Command
3. Paging·Sort
4. Local·Remote 호출
5. Transaction·동시성
6. 멱등 거래
7. Kafka Producer·Consumer
8. Outbox·Inbox
9. Retry·DLT·재처리
10. File·Attachment
11. 외부 API
12. 고정길이 전문
13. Permission·Data Scope
14. Masking·Audit
15. UNKNOWN_RESULT·Reconciliation
16. DB Upgrade·Rollback

### 각 EDU의 필수 상세

- 교육 목표
- 적용 업무
- 선행 지식
- 기준 Commit
- 준비 환경
- 시작 Repository 상태
- 사용 Module
- 전체 Source
- 전체 설정
- Migration
- 입력 데이터
- 실행 명령
- 정상 출력
- 상태 변화
- ADM 확인
- 오류 재현
- Fault Injection
- 복구
- 자동 Test
- 완료 Checklist
- 실제 업무로 전환할 때 변경할 부분
- Framework 관리 영역과 변경 금지 영역
- 미검증·제한사항

### EDU에서 실무로 전환

```text
EDU 코드의 목적 파악
→ Framework 공통 영역 식별
→ 사용자 변경 가능 영역 식별
→ DomainName·SystemCode·Package·DB 변경
→ 업무 Validation·Permission·Masking 추가
→ 실제 Consumer 연결
→ Test 확장
→ Generator 관리 영역 보호
→ 배포 전 Gate 실행
```

EDU가 실제 CPF 공개 API와 다른 별도 규격을 사용하면 `실패`다.

## 20.4 `01_개발자매뉴얼.md` 기능별 최소 실습

다음 유형은 각자 실행 가능한 절차·코드·Test가 있어야 한다.

- 조회 API
- 상태 변경 Command
- Paging·Sort
- Local Service Call
- Remote Service Call
- Optimistic Lock
- 멱등 거래
- UNKNOWN_RESULT·Reconciliation
- Kafka Producer·Consumer
- Outbox·Inbox
- Retry·DLT·재처리
- File Upload·Download
- Attachment
- 외부 API
- 전문·고정길이 전문
- Permission·Data Scope
- Masking·Audit
- Config·Code·Message·Calendar·Cache
- DB Migration·Upgrade·Rollback

Sample 코드 조각이 아니라 실제 CPF Layer와 Consumer까지 연결한다.

## 20.5 `02_배치개발매뉴얼.md` 전체 Lifecycle Gate

Batch 매뉴얼은 Job 코드 작성만 설명해서는 안 된다. 다음 Lifecycle 전체를 다룬다.

```text
Job 설계
→ Job Definition 등록
→ Job Version 관리
→ JobParameter Schema 등록
→ Schedule 등록
→ Center-Cut 정의 등록
→ Job Pack·Artifact 등록
→ Runner·Worker·Agent 등록
→ Dry Run·대상 건수 Preview
→ 승인
→ 실행
→ 진행·Partition·Checkpoint 확인
→ Stop·Restart·Abandon·Drain
→ 오류·응답 유실·부분 실패 판단
→ 재처리·대사·복구
→ 결과·Audit·Evidence
→ Version 변경·Rollback·폐기
```

### Job 등록·관리 필수 항목

- Job ID
- Owner System·Module
- Job Type
- Job Version
- JobParameter Schema
- Identifying Parameter
- 중복 실행 정책
- 재시작 가능 여부
- 동시 실행 제한
- Timeout
- Skip·Retry
- Worker Capability
- Artifact·Job Pack
- 활성·비활성
- Permission
- Reason
- Approval
- Audit
- Rollback Version
- 폐기 조건

### Scheduler 등록·관리 필수 항목

- Schedule ID
- 대상 Job·Version
- Trigger 규격
- Cron 표현식 또는 실제 Trigger 계약
- Timezone
- Business Calendar
- 활성 기간
- JobParameter 생성 규칙
- 중복 Trigger 처리
- Misfire 정책
- 일시 중지·재개
- 변경·비활성·폐기
- Scheduler Leader 장애
- Trigger 성공과 Job 성공의 구분
- Permission·Approval·Audit
- ADM 확인

### Center-Cut 정의 등록 필수 항목

- Center-Cut ID
- 업무 목적
- Owner System·Module
- 대상 데이터 선정 조건
- 기준 시점
- 예상·전체 대상 건수 산정 방법
- Partition Key
- Partition 수
- Chunk 크기
- Commit 단위
- 동시 Runner 수
- 재실행 정책
- 중복 처리 정책
- 멱등성 Key
- Timeout
- 실패 허용 기준
- 대사 기준
- Compensation·원복 정책
- Permission
- Reason
- Approval
- Audit
- Version
- 활성·비활성
- 변경·폐기

### Center-Cut 실행 자원 등록 필수 항목

- Job Pack
- Artifact
- Artifact Version
- Checksum·Signature
- Runner
- Runner Group
- Worker Pool
- Agent
- Host
- Capability
- 최소·최대 Instance
- Concurrency
- Resource Limit
- Lease 기간
- Heartbeat
- Claim
- Fencing Token
- 배포 상태
- Health
- Drain
- 격리
- Rollback

### Center-Cut 실행 전 검증

- Dry Run
- 대상 건수 Preview
- Parameter Validation
- Permission
- 승인 상태
- Artifact 무결성
- Runner 가용성
- Worker Capacity
- DB Index·Lock
- 중복 실행 여부
- 기존 미완료 Execution
- Lease 상태
- 예상 소요 시간과 Resource
- 실행 승인

### Center-Cut 실행·진행 관리

- 실행 요청
- Execution ID
- JobInstance·JobExecution 연결
- Partition 생성
- Runner Claim
- Worker 할당
- Chunk 처리
- Checkpoint 저장
- 진행률 계산
- 성공·실패·보류 건수
- Retry
- 실패 Ledger
- Log·Metric·Trace
- ADM 진행 조회
- Audit

### Center-Cut 제어·복구

- Start
- Stop 요청
- Stop 완료 확인
- Restart 조건과 실행
- Abandon 조건과 영향
- Drain
- Scale-out·Scale-in
- Runner 격리
- Worker 격리
- Artifact Rollback
- Definition Version 변경
- Schedule 변경
- Partial Apply
- Expected Version
- Approval
- Audit

구현되지 않은 Pause·Resume 등의 상태를 추정해 작성하지 않는다.

### Batch 장애·복구 필수 시나리오

- Step 중간 실패
- Chunk 중간 실패
- Commit 직전 Process Kill
- Commit 직후 Process Kill
- 동일 Job 중복 실행
- Scheduler 중복 Trigger
- Misfire
- Runner Process Kill
- Worker Process Kill
- Worker 응답 유실
- Network 단절
- DB 장애
- Kafka 장애
- Lease 만료
- Stale Owner
- Fencing 충돌
- Partition 중복 Claim
- 일부 Partition만 완료
- File 중복 수신
- API 결과 불명
- UNKNOWN_RESULT
- Restart
- Abandon
- Reconciliation
- 처리 건수·업무 원장 대사

각 시나리오는 다음을 기록한다.

```text
운영 증상
→ 실제 Spring Batch 상태
→ CPF Ledger 상태
→ 자동 Retry 여부
→ 수동 조치 필요 여부
→ 재실행 가능 조건
→ 사용할 화면·API·명령
→ 대사 방법
→ 정상화 판정
```

### Batch EDU 필수 경로

- Tasklet
- Chunk
- Reader·Processor·Writer
- File Import
- DB 대량 처리
- API Job
- Shell Job
- Checkpoint·Restart
- Local Partition
- Remote Partition
- Remote Chunk
- Scheduler 등록
- Misfire
- Center-Cut 정의 등록
- Center-Cut 자원 등록
- Center-Cut Dry Run
- Center-Cut 실행·제어
- Runner·Worker·Agent
- Lease·Claim·Fencing
- UNKNOWN_RESULT·Reconciliation

Batch 개발자가 이 매뉴얼만 보고 등록·변경·실행·중단·재시작·대사를 수행하지 못하면 `부분 구현`이다.

## 20.6 `03_ADM개발자매뉴얼.md` 기능 유형 Gate

ADM 개발 매뉴얼은 최소한 다음 화면 유형을 각각 실제 End-to-End로 연결한다.

1. 조회·검색 화면
2. 상세·Timeline 화면
3. 상태 변경 화면
4. 위험 조치 화면
5. 승인·반려 화면
6. 실시간 진행 상태 화면
7. Instance별 부분 적용 화면
8. Reconciliation·Recovery 화면
9. Batch·Center-Cut 화면
10. Gateway Route 화면
11. Config·Code·Message·Calendar·Cache 화면
12. Log·Transaction·Trace 화면

각 유형은 다음 연결을 포함한다.

```text
Requirement
→ Permission
→ Backend Controller
→ Request·Response·Validation
→ Application Service
→ Owner Port
→ Same-JVM·Remote Adapter
→ Timeout·Expected Version·Idempotency
→ UNKNOWN_RESULT·Reconciliation
→ OpenAPI
→ Generated Client
→ Route·Menu
→ Query State·Validation
→ Search·Table·Detail·Form
→ Reason·Approval
→ Partial Apply·Rollback
→ Audit
→ Unit·Contract·Playwright Test
```

### ADM Frontend 필수 품질

- 기능 단위 Directory·Package
- Route 책임
- API Client 책임
- Server State와 Client State 구분
- Input Validation
- Loading·Empty·Error·Stale State
- Search·Paging·Sort
- 상태 표현
- Permission별 노출·비활성
- 위험 조치 Confirm
- Reason·Approval
- Expected Version
- 응답 유실·부분 적용 표시
- 접근성
- 반응형
- 외부 Runtime CDN·Font·Script 금지
- Browser Test

실제 채택되지 않은 OSS Stack을 현재 구현처럼 설명하지 않는다.

## 20.7 `04_ADM운영자매뉴얼.md` 화면 전수 Gate

다음 양방향 대조를 모두 수행한다.

```text
실제 Route·Component·Permission → 운영 매뉴얼 화면
운영 매뉴얼 화면 → 실제 Route·Component·Permission
```

모든 화면은 다음을 포함한다.

- 대메뉴·중메뉴·화면명
- Route
- 화면 목적
- 대상 운영 역할
- Permission
- 사전 조건
- 검색 Field와 Default
- 목록 Column
- 정렬·Paging
- 상세 Field
- 상태값과 의미
- Button
- Button 활성 조건
- 입력값
- Reason
- Approval
- Expected Version
- 정상 상태 변화
- Timeout·응답 유실
- 부분 적용
- Retry 가능 여부
- Reprocess·Reconcile·Rollback
- Audit
- 관련 화면
- Evidence

### ADM 운영 실무 Gate

운영자가 Source를 보지 않고 다음을 수행해야 한다.

1. 교대 시작과 Dashboard 확인
2. System·Service·Instance·Topology 조회
3. Capacity·Health 이상 판단
4. 거래·Transaction Group·Trace 추적
5. Runtime 위험 조치 요청
6. Gateway Route Publish·Partial Apply·Rollback
7. Batch Job·Schedule·Execution 조회
8. Center-Cut 정의·진행·Partition 조회
9. Runner·Worker·Agent·Lease·Fencing 확인
10. Stop·Restart·Abandon·Drain·격리
11. Kafka Retry·DLT·재처리
12. File·Attachment·Download 확인
13. Log·Remote Log·Audit Log 확인
14. Config·Code·Message·Calendar·Cache 변경
15. Permission·Session·Secret·Certificate 운영
16. Approval·Break-glass
17. Incident 생성·통제·정상화
18. UNKNOWN_RESULT·Reconciliation·Recovery
19. 교대 종료·Audit·Evidence 확인

실제 화면이 없는 기능은 가상 화면으로 작성하지 않고 현재 상태를 명시한다.

## 20.8 `05_플랫폼운영매뉴얼.md` 운영 Lifecycle Gate

플랫폼 운영자는 매뉴얼만 보고 다음 Lifecycle을 수행해야 한다.

```text
지원 환경 확인
→ Artifact·Checksum·SBOM 확인
→ Account·Directory·Permission 구성
→ Property·환경변수·Profile·Secret 설정
→ DB Vendor별 설치
→ Schema·Migration·Seed
→ Kafka·외부 자원 구성
→ 신규 설치
→ 기동·종료·Health 확인
→ Topology별 배포
→ Rolling·Blue-Green·Canary
→ Config 변경·Partial Apply·Rollback
→ Log·Metric·Trace 확인
→ Capacity 관리
→ Backup·Restore
→ Upgrade·Rollback
→ Certificate Rotation
→ 장애 대응
→ DR Failover·Failback
→ 정합성 대사·Evidence
```

### Property 전수 Gate

다음 양방향 대조를 수행한다.

```text
@ConfigurationProperties·application 설정·환경변수 → 매뉴얼
매뉴얼 Property → 실제 소비 Module·Class
```

각 Property는 다음을 포함한다.

- Key
- 환경변수명
- Type
- Default
- 필수 여부
- 허용 범위
- 소비 Module·Class
- Profile
- 적용 시점
- 재기동 여부
- Secret 여부
- 오류 증상
- 확인 명령
- 정상 결과
- Rollback

### 설치·배포 Gate

- 지원 OS·JDK·DB·Kafka Version
- Account·Directory·Permission
- Artifact와 Checksum
- DB와 Migration
- Config·Secret
- 기동 순서
- Health·Readiness·Liveness
- Smoke Test
- ADM 확인
- 설치 취소·Rollback
- Evidence

### Runbook Gate

최소한 다음 장애를 다룬다.

- DB 연결 실패·성능 저하·Failover
- Kafka Broker·Topic·ACL·Consumer Lag·DLT
- Instance 기동 실패·Crash Loop·부분 장애
- Gateway 장애·Route 부분 적용
- Batch·Worker·Agent 장애
- Disk 부족·I/O 오류
- Memory·Heap·GC
- CPU·Thread 고갈
- Connection Pool 고갈
- Network·DNS·Firewall
- Certificate 만료
- Secret 오류
- Config Drift·Partial Apply
- Log·Metric·Trace 수집 장애
- UNKNOWN_RESULT 대량 발생
- 보안 Incident
- DR 전환

각 Runbook은 증상→영향→즉시 통제→진단→복구→정합성→정상화→Rollback→재발 방지→Evidence를 포함한다.

## 20.9 `90_BZA매뉴얼.md` 역할 Gate

다음 역할을 구분해 작성한다.

- 도입 책임자
- 초기 관리자
- 조직 관리자
- 사용자·계정 관리자
- 권한 관리자
- 결재 운영자
- 업무 관리자
- BZA Backend·Frontend 개발자
- 플랫폼 운영자
- 감사자

각 역할은 다음 기능을 수행할 수 있어야 한다.

- 도입 판단과 활성화
- 설치·DB Migration·Bootstrap
- 초기 관리자 생성
- 조직·직원·배치
- 사용자·Account·잠금·비활성
- Role·Permission·Data Scope
- 결재 정책·결재선
- 상신·승인·반려·취소
- 위임·대결·만료
- 결재 Simulation
- Attachment·Download
- Notification·Retry
- Session
- Masking·Audit·Export
- 업무 Domain API 연계
- Backend·Frontend 확장
- Backup·Restore
- Upgrade·Rollback
- Security·권한 Test

BZA를 Core 필수 기능처럼 표현하지 않는다.

## 20.10 `91_Gateway매뉴얼.md` Control Plane Lifecycle Gate

Gateway 매뉴얼은 Route YAML 예제 하나로 완료 처리하지 않는다.

다음 전체 Lifecycle을 다룬다.

```text
Gateway 설치·기동
→ Route Definition 작성
→ Predicate·Filter·Rewrite
→ Target Group·Service Discovery
→ Authentication·Authorization·Trusted Header
→ HMAC·Audience·Body Hash·Nonce
→ SSRF·TLS
→ Timeout Budget·Retry·Circuit Breaker·Bulkhead
→ Idempotency·Attempt Ledger·UNKNOWN_RESULT
→ Route Validation
→ Version·Checksum·Diff
→ Reason·Approval
→ Publish
→ Instance ACK·NACK
→ Partial Apply·Drift
→ Last Known Good
→ Rollback
→ Scale-out·Drain·격리
→ Reconciliation
→ Probe·Health·Log·Metric·Trace
→ ADM 운영
→ 장애 복구
```

### Gateway 필수 시나리오

- 신규 Route 등록·게시
- Route 충돌 Validation 실패
- Target 일부 장애
- Timeout·Retry 소진
- Circuit Open
- Body Streaming
- Trusted Header 위조
- HMAC 실패
- Nonce Replay
- SSRF 차단
- Certificate 만료
- Instance ACK 지연
- Instance NACK
- 일부 Instance만 적용
- 신규 Instance Version Drift
- Last Known Good Rollback
- Publish 응답 유실
- UNKNOWN_RESULT
- Reconciliation

목표 Architecture만 있고 현재 구현·검증이 없으면 `부분 구현` 또는 `미검증`으로 표시한다.

---

# 21. 문서 작업 검증 절차

## 21.1 작성 전 검증

- 최신 `master` 기준 Commit 확인
- Working Tree 상태 확인
- README·공식 매뉴얼 8개 Hash 확인
- 다른 작업자의 변경 보호 대상 확인
- 관련 Requirement·Architecture·Specification 확인
- Owner Module·Consumer 확인
- Source·SQL·API·Config·Frontend·Script·Test Inventory 확인
- 공식 문서 외 신규 사용자 문서를 만들지 않음을 확인

## 21.2 작성 중 검증

- 실제 Path 존재 여부
- Class·Method·API 존재 여부
- Property Key와 Consumer 존재 여부
- DB Vendor별 Artifact 존재 여부
- Frontend Route·Component·Permission 존재 여부
- Script·명령 존재 여부
- Test 존재 여부
- 실제 Consumer 연결 여부
- 목표·구현·검증 상태 구분
- 민감정보 제거
- 동일 문장 반복과 분량 부풀리기 확인

## 21.3 작성 후 정적 Gate

- 공식 문서 수 확인
- `cpf-docs/guides/README.md` 부재 확인
- 중복 Guide·날짜 Guide·임시 Guide 확인
- 깨진 내부 링크 확인
- 깨진 이미지 확인
- Anchor 확인
- UTF-8 확인
- 줄 끝 정책 확인
- trailing whitespace 확인
- `git diff --check`
- Repository Root 가비지 확인
- ZIP·BAK·TMP·LOG 확인
- 민감정보·Token·Secret 확인

## 21.4 양방향 추적 Gate

### Requirement에서 문서로

```text
Requirement
→ Owner Module
→ Source·API·SQL·Config·Frontend
→ Test
→ 공식 매뉴얼 단락
→ EDU·업무 사례·Runbook
→ Evidence
```

### 구현에서 문서로

```text
Source·API·SQL·Config·Frontend·Script
→ Requirement
→ Owner Module
→ Consumer
→ 공식 매뉴얼 단락
→ 운영 확인·복구 절차
```

## 21.5 역할별 사용성 검수

다음 역할의 신규 사용자가 문서만 보고 지정 시나리오를 수행한다.

- 신규 CPF 개발자
- Batch·Center-Cut 개발자
- ADM 개발자
- ADM 운영자
- 플랫폼 운영자
- BZA 담당자
- Gateway 담당자

검수 중 질문·Source 역분석·작성자 설명이 필요했던 지점을 기록하고 문서를 보완한다.

## 21.6 직접 실행하지 않은 검증

직접 실행하지 않은 항목은 성공으로 기록하지 않는다.

예:

```text
문서·Source 대조: 완료
정적 링크 검사: 완료
PostgreSQL Runtime: 미검증
Oracle Runtime: 미검증
다중 인스턴스: 미검증
Browser 실화면: 미검증
Process Kill 복구: 미검증
Backup·Restore: 미검증
Upgrade·Rollback: 미검증
```

---

# 22. 산출물 적용 안전성

## 22.1 작업 전 보호

다음을 기록한다.

- 기준 Commit
- `git status --short`
- 보호할 변경 파일
- 수정 대상 파일
- 삭제 대상 파일
- README Hash
- 공식 Guide 목록과 Hash
- 적용 전 Backup 위치

## 22.2 Package 구성

문서 산출물을 ZIP 또는 Overlay로 전달할 경우 다음을 지킨다.

- Repository Root 상대경로 유지
- 신규·수정·삭제 목록 분리
- 정확한 파일명 사용
- Wildcard 삭제 금지
- 사용자 승인 없는 추적 파일 삭제 금지
- README 구조 변경 여부 표시
- SHA-256 제공
- 파일 수 제공
- 적용 명령 제공
- Dry Run 제공
- Rollback 제공
- 검증 명령 제공
- 미검증 항목 표시

## 22.3 적용 후 확인

- 예상한 파일만 변경됐는가?
- ZIP에 없던 과거 가비지가 남았는가?
- 중복 Guide가 생겼는가?
- 공식 문서 수가 9개인가?
- `cpf-docs/guides/README.md`가 다시 생기지 않았는가?
- README 브로셔 구조가 유지됐는가?
- 다른 작업자의 Source·SQL·Frontend가 보호됐는가?
- 링크·이미지·Anchor가 유효한가?
- trailing whitespace가 없는가?
- `git diff --check`가 통과하는가?

## 22.4 삭제 원칙

- 삭제는 정확한 Manifest에 있는 파일만 수행한다.
- `git clean -fd`를 문서 정리에 사용하지 않는다.
- 전체 `guides` Directory를 Wildcard로 삭제하지 않는다.
- 추적 파일 삭제는 사용자 승인을 받는다.
- 미추적 파일도 다른 작업 산출물인지 확인한다.
- 삭제 후 Consumer·링크·Script를 재검사한다.

---

# 23. 문서 상태와 완료 판정 강화

## 23.1 완료

다음을 모두 만족해야 한다.

- 공식 파일명·역할·경로 준수
- 기능 전수 Coverage
- Source·Consumer 사실성
- 등록·조회·변경·실행·복구 Lifecycle
- 정상·오류·경계·동시성·부분 실패
- Permission·Masking·Reason·Approval·Audit
- 실제 API·Property·SQL·화면·명령
- EDU 활용 경로와 실무 전환
- 업무 사례와 Runbook
- 양방향 추적
- 역할별 독립 사용성 검수
- 실행 검증과 미검증 구분
- README 구조 보호
- 적용·Rollback·가비지 Gate 통과

## 23.2 부분 구현

다음 중 하나라도 해당하면 `부분 구현`이다.

- 주요 기능 일부 누락
- 등록은 있으나 변경·비활성·폐기 누락
- 정상 흐름만 존재
- 오류·부분 실패·복구 누락
- EDU는 있으나 실무 전환 누락
- Center-Cut 이름만 있고 정의·자원·실행·제어·대사 누락
- ADM Route만 있고 Field·Button·Permission·상태 변화 누락
- Property Key만 있고 Default·Consumer·재기동·Rollback 누락
- Gateway Route 예제만 있고 Publish·ACK·NACK·LKG·Rollback 누락
- BZA 기능 일부 화면만 있고 도입·설치·연계·운영 누락
- 역할별 사용성 검수 미통과

## 23.3 미검증

- Source 대조만 수행
- 특정 DB Vendor만 실행
- 단일 인스턴스만 실행
- Browser Runtime 미실행
- 장애·Process Kill·Network 분리 미실행
- Backup·Restore 미실행
- Upgrade·Rollback 미실행
- Center-Cut 대량·다중 Worker 미실행
- Gateway Partial Apply·Rollback 미실행

## 23.4 실패

- 문서 절차가 실제로 작동하지 않음
- Source·Property·Route·Permission 불일치
- 링크·이미지·명령 실패
- 복구 후 정합성 확보 실패
- README 브로셔 틀 무단 훼손
- 다른 작업자의 변경 삭제·회귀
- 민감정보 노출
- 실행하지 않은 결과를 성공으로 기록

---

# 24. AI 작업 시작용 강제 Checklist

```text
[ ] 공식 Repository가 https://github.com/freeangelsun/202412_01_CPF인지 확인했다.
[ ] 현재 Branch와 origin/master 상태를 확인했다.
[ ] 현재 HEAD, origin/master SHA, Ahead·Behind·Diverged 상태를 기록했다.
[ ] Working Tree의 수정·삭제·신규·미추적 파일을 확인했다.
[ ] 기존 Local 변경을 보호 대상으로 식별했다.
[ ] 최신 master와 기준 Commit을 확인했다.
[ ] CPF_FINAL_TARGET_REQUIREMENTS.md를 확인했다.
[ ] 관련 Architecture·Specification을 확인했다.
[ ] 실제 Source·SQL·API·Config·Frontend·Script·Test를 확인했다.
[ ] Owner Module과 실제 Consumer를 확인했다.
[ ] 현재 README를 보호 Baseline으로 기록했다.
[ ] 공식 사용자 문서는 README와 매뉴얼 8개뿐임을 확인했다.
[ ] cpf-docs/guides/README.md를 만들지 않는다.
[ ] 별도 EDU·Reference·Case·사용자용 Report를 만들지 않는다.
[ ] README 구조 변경은 사용자 승인이 필요함을 확인했다.
[ ] 대목차·중목차·세부 항목은 최소 품질선으로 적용한다.
[ ] 실제 기능이 더 있으면 목차를 추가한다.
[ ] 목표와 현재 구현을 구분한다.
[ ] 등록·조회·변경·실행·복구 Lifecycle을 검토한다.
[ ] 정상·오류·부분 실패·복구를 작성한다.
[ ] EDU 역할별·업무별 활용 경로를 작성한다.
[ ] EDU에서 실제 업무로 전환하는 방법을 작성한다.
[ ] Batch는 Job·Scheduler·Center-Cut 정의·자원·실행·제어·대사를 작성한다.
[ ] ADM 개발은 Backend·Owner Port·OpenAPI·Frontend·Permission·Approval·Audit을 연결한다.
[ ] ADM 운영은 실제 Route·Field·Column·Button·Permission을 전수 대조한다.
[ ] 플랫폼 운영은 Property·Consumer·명령·정상 결과·Rollback을 작성한다.
[ ] BZA는 도입·설치·조직·권한·결재·연계·운영을 작성한다.
[ ] Gateway는 Validation·Version·Approval·Publish·ACK·NACK·LKG·Rollback을 작성한다.
[ ] 근거 없는 홍보·완료 표현을 사용하지 않는다.
[ ] 링크·이미지·Anchor·UTF-8·공백·Repository Hygiene를 검증한다.
[ ] 적용·Rollback·가비지 방지 절차를 준비한다.
[ ] 사용자 승인 없이 Commit·Push·Branch·Tag·PR을 만들지 않는다.
[ ] 사용자 승인 없이 Merge·Rebase·Reset·Restore·Clean·Stash를 수행하지 않는다.
[ ] 작업 완료 전 git status·diff·diff --check·미추적 파일을 확인한다.
```

---

# 25. 최종 원칙

CPF 공식 문서는 기능명과 Source Path를 나열하는 Catalog가 아니다.

- 개발자는 매뉴얼만 보고 실제 기능을 생성·개발·시험·복구해야 한다.
- Batch 개발자는 매뉴얼만 보고 Job·Scheduler·Center-Cut·Runner·Worker·Agent를 등록·실행·중단·재시작·대사해야 한다.
- ADM 개발자는 매뉴얼만 보고 Backend·Frontend·Permission·Approval·Audit을 연결해야 한다.
- ADM 운영자는 매뉴얼만 보고 화면에서 조회·판단·제어·승인·복구해야 한다.
- 플랫폼 운영자는 매뉴얼만 보고 설치·배포·Upgrade·Rollback·Backup·Restore·DR을 수행해야 한다.
- BZA와 Gateway 담당자는 선택 제품의 도입부터 개발·운영·복구까지 수행해야 한다.

이 지침에 있는 항목은 최소 요구다.

실제 CPF 기능이 더 많거나 더 복잡하면 문서는 반드시 더 상세해져야 한다. 지침보다 적게 작성하고 `완료`로 판정하는 것을 금지한다.
