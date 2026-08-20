# CPF README·매뉴얼·설계 산출물 작성 및 관리 표준

> Canonical entry: `cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md`  
> Canonical target: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`  
> 적용 범위: Root README, 공식 사용자 문서 7종(README + 02~07), 설계 산출물 5종, 공식 Architecture/Developer/Generator/EDU/DB Guide

## 1. 문서의 목적

CPF 문서는 기능을 많이 적는 자료가 아니라 **문서를 보는 사람이 자기 일을 끝내게 하는 문서 인터페이스**다. 문서 품질은 페이지 수, 표 수, 이미지 수가 아니라 독자가 Source를 역분석하거나 작성자에게 추가 설명을 요청하지 않고 판단·개발·운영할 수 있는지로 평가한다.

모든 문서는 먼저 다음 세 질문에 답한다.

1. **누가 보는가?** - Persona와 선행 지식
2. **왜 보는가?** - 이 문서를 펼치는 실제 상황
3. **어디까지 끝내야 하는가?** - 문서 하나로 달성해야 하는 종료 조건

## 2. CPF 문서의 기본 톤

공개 문서는 특정 산업이나 조직 규모를 전제로 하지 않는 **오픈형·범용 Framework 언어**를 사용한다. 품질을 형용사로 주장하지 않고 Architecture, API, Generator, 실행 예제와 운영 계약으로 보여준다.

### 2.1 기본 표현 원칙

권장:

- 업무 애플리케이션 / 업무 시스템 / 서비스 / 프로젝트
- 모듈형 Framework / Public Starter / Capability / Provider
- 일관된 실행 계약 / 상태 확인 / Reconcile / 운영 조정
- Composable / Orchestrated / Observable / Operable
- 실제 기능·선택 기준·실행 결과를 직접 설명

공개 문서에서 기술적으로 불필요하면 피하는 표현:

- 금융권 / 금융기관 전용
- 상용 / Commercial
- 기업급 / Enterprise-grade
- Production-grade / Mission Critical
- 최고 수준 / 강력한 / 완벽한
- 무중단 / 고가용성처럼 별도 자격을 암시하는 포괄적 주장

`standard-enterprise`, `full-enterprise`처럼 **실제 식별자·Preset 이름**은 정확성을 위해 그대로 표기한다. 일반 설명에서 `Enterprise`를 품질 수식어로 확장하지 않는다.

### 2.2 “복구” 용어 사용

`복구`는 금지어가 아니다. Backup/Restore, 장애 복구, XA recovery처럼 운영자가 실제로 검색하고 실행하는 정확한 기술 용어에는 사용한다.

다만 README·Hero·설계 원칙 같은 소개 문구에서 `강력한 복구`, `복구 가능한 플랫폼`처럼 품질을 포장하는 표현은 사용하지 않는다. 일반 실패 흐름은 가능하면 다음처럼 구체적으로 쓴다.

```text
실패 감지 → 상태 확인 → 재시도/재처리 → Reconcile → 업무 정상화
```

## 3. CPF 핵심 메시지 - Orchestrated MSA

CPF의 MSA 설명은 “서비스를 여러 개로 나눈다”에서 끝나지 않는다. **서비스 독립성을 유지하면서 분산 실행의 식별·문맥·상태를 연결·추적·조정한다**는 특성을 자연스럽게 드러낸다.

CPF 문서에서 사용할 기본 메시지:

> **Orchestrated MSA - 서비스 분리에서 실행 연결까지**  
> Request, Transaction, Event, Batch, Runtime의 실행 문맥과 상태를 공통 계약으로 연결한다.

문서가 설명해야 할 다섯 축:

| 축 | 설명 |
|---|---|
| Request Orchestration | Gateway → Context → Application, trust boundary, routing, timeout budget |
| Transaction Orchestration | Local TX / Saga / TCC / XA, UNKNOWN_RESULT, compensation, Reconcile |
| Event Orchestration | Outbox → Broker → Inbox, idempotency, retry, DLQ, replay |
| Batch Orchestration | Scheduler → Worker → Checkpoint, restart, reprocess, Center-Cut |
| Runtime Orchestration | Health → Drain → Runtime Control → Audit, instance/version/approval/Reconcile |

### 3.1 과장 방지 경계

`Orchestration`을 중앙 BPM/Workflow Engine과 동일시하지 않는다. CPF가 모든 업무 Step을 중앙 상태 머신으로 소유한다고 쓰지 않는다. 별도의 Workflow Engine 계약이 실제 CPF 기능으로 정의된 경우가 아니라면 다음 경계를 유지한다.

```text
서비스의 배포·데이터 Owner = 독립
공유하는 것 = transactionId / Context / attempt-lineage / deadline /
              idempotency / audit-trace / version / Reconcile 계약
```

즉, `Orchestrated MSA`는 **분산 실행의 연결·추적·조정 특성**을 설명하는 CPF Architecture 용어다.

## 4. 문서별 독자와 종료 조건

| 문서 | 주 독자 | 문서로 끝내야 하는 일 |
|---|---|---|
| README | 처음 보는 사용자 / 의사결정자 / 개발자 | CPF가 무엇인지, 왜 쓰는지, 어디서 시작하는지 10분 안에 이해 |
| 00 프레임워크 안내 | Architect / Tech Lead | Architecture, Ownership, Starter, DB, Topology, 도입 구조 결정 |
| 01 개발자 매뉴얼 | 업무 개발자 | Generator → Online/Batch → DB/Transaction/Integration → Test → 운영 인계 |
| 02 배치개발 매뉴얼 | Batch 개발·운영자 | Job/Step/Worker/Scheduler/Restart/Reprocess/Reconcile 수행 |
| 03 ADM 개발자 매뉴얼 | ADM 개발자 | Owner API → Backend → OpenAPI → Generated Client → 화면 연결 |
| 04 ADM 운영자 매뉴얼 | 운영자 | 검색 → 판단 → 권한/승인 → 조치 → Reconcile → 정상화 |
| 05 플랫폼 운영 매뉴얼 | 플랫폼 운영·배포 담당자 | 설치·배포·관측·Drain·Backup/Restore·Upgrade/Rollback·정상화 수행 |
| 06 빠른 시작·초기 설정 가이드 | 신규 개발자 | 환경 확인 → Generator → Starter → 첫 Online 거래 → 완료 확인까지 첫 개발 수행 |
| 07 Starter·공통 기능 활용 가이드 | 업무 개발자 | Starter/Provider를 고르고 코드·파라미터·메시지·Context·Cache·Lock·Persistence를 상황에 맞게 사용 |
| 08 Online 거래·Transaction·MSA 호출 예제 가이드 | Online 업무 개발자 | 조회/변경 거래, 공통 응답, Local TX, 원격 호출, UNKNOWN_RESULT, 비동기 Context를 예제로 적용 |
| 09 Generator·업무 Domain 생성 가이드 | 프로젝트 구성·업무 개발자 | 정의 → 검증 → 생성 → diff → 재생성 Lifecycle과 Generated 구조 적용 |
| 10 설정·Profile·환경 가이드 | 개발·배포·운영 담당자 | Profile, 환경 Override, Secret, Runtime 변경과 Drift를 안전하게 적용 |
| 11 Data·DB·Migration 가이드 | Backend 개발자·DBA | Persistence, Lock, Canonical DB, Oracle/PostgreSQL/MariaDB, Migration/Seed/Rollback 적용 |
| 12 Messaging·외부 연계 가이드 | 연계·업무 개발자 | HTTP/Event/Broker/File 연계와 Retry/DLQ/Replay/부분 실패 처리 |
| 13 Security·권한·Audit 가이드 | 개발·보안 담당자 | Trust Boundary, 인증/권한, Secret, 위험 조치, Audit 적용 |
| 14 CLI·Tool·검증 가이드 | 개발·CI 담당자 | CLI, Gate, Test, Package/Hash 검증 절차 수행 |
| 15 Education·예제 찾기 가이드 | 신규 개발자·교육 담당자 | 실행 가능한 Education Source/Test를 빠르게 찾아 업무 코드에 적용 |
| 16 장애 대응·Reconcile 가이드 | 개발·운영 담당자 | UNKNOWN/PARTIAL 판단, Blind Retry 방지, Reconcile/Replay/종결 수행 |
| 90 BZA 매뉴얼 | 업무 관리자 | 조직·사용자·권한·결재·세션·감사 운영 |
| 91 Gateway 매뉴얼 | Gateway/보안 담당자 | Route·Trust·Publish·LKG·Scale-out·Drift 운영 |
| Architecture 설계서 | Architect / Reviewer | Owner·dependency·topology·orchestration 경계 검토 |
| 기술사양서 | 구현 Reviewer | Public Contract, protocol, lifecycle, state/failure semantics 검토 |
| 기술표준서 | 개발자 / Reviewer | 구현 시 지켜야 할 규칙과 Good/Bad 판단 |
| DB 표준서 | DBA / Data Architect | Canonical DB, Vendor3, Migration, ownership, transaction 규칙 적용 |
| 산출물목록 | 인수자 / 관리자 | 공식 산출물 위치·용도·Owner·사용 순서 파악 |

## 4.1 사용자 문서 체계 운영 원칙

공식 사용자 문서 7종은 역할이 다르다. README는 전체 진입점이고 02~06은 역할별 개발·운영 흐름, 07은 정확한 기술 계약을 담당한다. 새 기능을 이유로 공식 Guide를 계속 추가하지 않고 기존 Owner 문서에 흡수하며, 다른 문서가 있다는 이유로 Owner 문서의 핵심 설명을 비우지 않는다.

```text
핵심 매뉴얼: 00~05, 90, 91
실무 가이드: 06~16
설계 산출물: 산출물목록, 아키텍처설계서, 기술사양서, 기술표준서, 데이터베이스표준서
```

모든 문서 첫 부분에는 독자·사용 상황·빠른 탐색 경로가 보여야 하며, 상세한 배경 설명은 핵심 매뉴얼, 반복해서 찾는 선택표·레시피·체크리스트는 실무 가이드에 우선 배치한다.

## 5. README 작성 기준

README는 Repository 첫 화면이자 CPF 소개·Architecture 진입점이다. 링크 모음이나 개발 이력으로 만들지 않는다.

권장 정보 흐름:

```text
Hero
→ 30초 만에 보는 CPF
→ 전체 Architecture
→ Orchestrated MSA
→ Capability / Ownership Map
→ Starter / Provider
→ Generated Domain Journey
→ Data Architecture
→ Runtime / Transaction / Operations
→ 역할별 Guide Map
```

Hero는 CPF가 무엇을 하는지 한 문장으로 말한다. `Composable · Orchestrated · Observable · Operable` 같은 간결한 CPF 표현을 사용할 수 있으나, 그 아래 본문에서 실제 기능으로 증명한다.

전체 Architecture 이미지는 최소 다음을 한 화면에서 연결한다.

- Consumer / Generated Domain / First-party Application
- Public Starter / Profile / Provider
- Common Service / Capability Owner
- cpf-core Kernel
- ADM / BZA / Gateway / Batch / Education
- cpfDB / bzaDB / Customer Business DB
- Canonical DB → Oracle/PostgreSQL/MariaDB
- Tooling / Deploy

## 6. 내용 작성 순서

기능 설명은 가능한 범위에서 다음 순서를 따른다.

```text
목적 / 언제 쓰는가
→ 선택·비선택 기준
→ Owner / Public API / Consumer
→ 실제 Source / Config / SQL / HTTP / Command
→ 정상 결과와 상태 변화
→ 오류 / Timeout / Partial / UNKNOWN
→ Retry / Restart / Reprocess / Reconcile / Rollback
→ Permission / Reason / Approval / Audit
→ Test / 운영 확인 / 종료 조건
```

코드나 명령이 필요한 기능은 장식용 3~4줄 예제로 끝내지 않는다. 사용자가 실제로 시작하고 결과를 판정할 수 있는 최소 완결 단위를 제공한다.

## 7. 사용자 문서의 구현 상태 표현

README·공식 매뉴얼·설계 산출물은 **완성된 CPF 사용 문서**로 작성한다. 다음 개발 진행 표현을 공식 문서 본문에 넣지 않는다.

```text
부분 구현 / 미구현 / 구현 예정 / 다음 개발 / 검수 필요 / 재확인 필요 / 미검증
세션 번호 / 개발 회차 / currentization SHA / 임시 migration 상태
```

개발·QA Evidence와 CPF 문서를 혼합하지 않는다. CPF 문서는 최종 Target 계약과 사용법을 설명하고, 진행 상태는 별도 Work/Evidence가 관리한다.

## 8. 정확성과 Source 사용

문서를 현행화할 때는 최신 Canonical Index → Requirement/Architecture → 실제 Source/Consumer/Config/SQL/Test 순으로 확인한다. Source를 참고하되 사용자 문서에는 구현 이력이나 임시 위치를 그대로 노출하지 않는다.

정확한 식별자가 필요한 항목은 그대로 쓴다.

- Public Artifact / Starter / Profile
- Class / API / Property / Route / Permission
- `cpf-domain.yaml` / lock / CLI Grammar
- DB Role / Prefix / Migration / Vendor
- Status / Error / UNKNOWN_RESULT / Reconcile contract

Internal-only Artifact는 사용자 예시의 직접 dependency로 제시하지 않는다.

## 8.1 개발/QA 정보의 산출물 반영 원칙

산출물 작업 중 전달되는 최신 개발요건은 사용자 문서에서 **최종 구현 완료 상태의 사용 계약**으로 정리한다. QA는 사용자 문서에 미완료 상태를 노출하기 위한 절차가 아니라, Source를 깊게 확인하여 실제 사용자가 필요한 정보를 찾는 Source Truth 수집 절차다.

반드시 함께 대조한다.

- Public Class / Annotation / Method / Property의 최신 이름
- Generated Domain과 실제 Consumer가 사용하는 Golden Path
- Starter/Profile/Provider 선택 기준과 Internal 경계
- 정상·오류·UNKNOWN·Reconcile 흐름
- Same JVM/Remote/Async/Batch의 Context/Correlation
- ADM/BZA/Gateway에서 실제로 조회·제어하는 방식
- 개인정보·권한·감사·Masking·Retention
- 예제 코드와 표/도식의 API 명칭

기능 변경을 반영할 때 기존 문서의 상세 설명을 임의 축약하지 않는다. 바뀐 API 이름 하나라도 README/가이드/Specification/설계 산출물/예제에서 서로 다르게 남지 않도록 전수 검색한다.

## 9. 시각 디자인과 다이어그램 기준

시각 자료는 장식이 아니라 이해를 줄이는 도구다.

필수 Gate:

- 글자 겹침 0
- 선/화살표가 텍스트를 통과하는 경우 0
- 잘린 텍스트·박스·그림 0
- 화살표 방향이 의미와 일치
- 계층과 Owner가 색·정렬로 일관됨
- Desktop/Mobile에서 별도 레이아웃이 필요한 그림은 별도 제작
- 작은 글씨를 억지로 한 화면에 압축하지 않음
- 이미지의 제목·캡션·본문 용어가 동일

Orchestration 그림은 다섯 축을 **무조건 순차 실행되는 Workflow Step처럼 연결하지 않는다.** 공통 실행 계약을 공유하는 조정 축이라는 의미가 보여야 한다.

## 10. DOCX/PDF 기준

DOCX와 PDF는 같은 내용과 순서를 유지한다. 최종 인도 전 전 페이지 렌더 QA를 수행한다.

- Heading 계층과 TOC 일관성
- 표 Header 반복 및 행 분할 품질
- 코드/SQL/Terminal block clipping 0
- 이미지 원본 비율 유지
- 한글/영문/모노스페이스 글꼴 깨짐 0
- Comment / Tracked Change 0
- 이미지 alt text와 표 Header 접근성
- PDF blank/sparse/clipping/암호화 이상 0

## 11. 문서 현행화와 Garbage 관리

공식 문서 영역에는 **현재 사용하는 파일만 남긴다.** History는 Git이 보존한다.

금지:

- `*_R1`, `*_REV`, `*_FINAL2`, `*_SESSION`, `*_YYYYMMDD` 같은 복제본을 공식 폴더에 누적
- 같은 목적의 README/Guide를 여러 경로에 중복 유지
- 이전 이미지와 새 이미지를 모두 남겨 어떤 것이 Current인지 알 수 없게 함
- 임시 렌더 PNG, contact sheet, 변환 중간 PDF를 전달 ZIP에 포함

삭제가 필요한 경우:

1. Canonical/보호 경로인지 확인한다.
2. 대체 Current 파일이 존재하는지 확인한다.
3. Root 상대경로 exact Delete Manifest를 만든다.
4. wildcard/`git clean` 없이 exact path만 삭제한다.
5. 삭제 후 링크·README·DOCX/PDF 참조를 다시 검증한다.

보호 경로 삭제는 별도 승인 없이 수행하지 않는다.

## 12. 제출 Gate

완료 ZIP은 다음을 모두 만족할 때만 만든다.

- 최신 Canonical/Architecture와 내용 정합
- 공개 문서 Heavy positioning 표현 정책 준수
- Orchestrated MSA 표현이 Architecture와 실제 기능 경계를 넘지 않음
- 독자별 업무 종료 조건 충족
- README local link/image missing 0
- DOCX/PDF 렌더 이상 0
- 접근성 주요 오류 0
- stale/duplicate/garbage Current file 0 또는 exact Delete Manifest 제공
- Repository Root 상대경로, wrapper folder 없음
- Manifest와 SHA-256 재생성

Commit/Push/Branch/Tag/Reset/Restore/Stash/Clean은 사용자 승인 없이 수행하지 않는다.

## 13. 부담을 줄이는 용어와 레이아웃 보존 규칙

공개 문서에서는 추상적인 `제품`을 기본 주어로 사용하지 않는다. 문장이 가리키는 실제 대상을 `CPF`, `Framework`, `Capability`, `Service`, `Contract`, `구성`, `기능`, `적용 범위`, `구현 상태`처럼 구체적으로 쓴다. `상용급`, `기업급`, `Mission Critical`, `Production-grade`, `강력한`, `완벽한` 같은 자기평가 표현은 기술적 근거가 필요한 경우가 아니면 사용하지 않는다. Production profile/environment, XA Recovery, Backup/Restore, DR처럼 실제 기술 의미를 가진 용어는 유지한다.

DOCX/PDF 현행화는 기존 완성도를 파괴하지 않는다. 기존 문단·표·이미지·스타일·Header/Footer·Section·링크를 우선 보존하고, 필요한 내용을 해당 위치 또는 별도 보강 절에 추가한다. 최종 전달 전 이전 공식 산출물과 파일 크기, PDF 페이지, 본문량, 표/그림/Section 수를 비교해 **내용 보강에 따른 증가**와 **레이아웃 오류에 따른 비정상 증가**를 구분한다.

Source와 Target이 다르면 반드시 `CURRENT / TARGET / REFERENCE`를 표시한다. 특히 Generated Domain metadata 위치, CLI launcher 같은 전환 중 Surface를 Target인데 현재처럼 안내하지 않는다.



## 페이지 구성·여백·밀도 편집 표준

- **빈 공간 최소화는 문단 간격 제거를 뜻하지 않는다.** 본문 문단 사이에는 읽기 흐름이 보이는 적정 간격을 유지하고, 중간 절은 이전 내용과 약 1~2줄 수준의 시각적 간격으로 구분한다.
- **대메뉴/대장급 장은 새 페이지에서 시작**하는 것을 기본으로 한다. 다만 모든 Heading 1/2에 기계적으로 Page Break를 넣지 않는다. 동일 주제의 중간 절은 앞 장의 남은 공간과 정보 흐름을 고려해 자연스럽게 이어간다.
- 표·그림·코드 때문에 페이지 하단에 과도한 공백이 생기면 표 크기, 행 분할, 그림 크기, 캡션 위치, 앞뒤 설명의 배치를 조정한다. **문단 간격을 0으로 만들거나 글자를 과도하게 축소하는 방식으로 해결하지 않는다.**
- 제목만 한 페이지에 고립되거나, 표 한 행/불릿 한 줄만 다음 페이지에 남는 현상을 금지한다. Heading은 다음 본문과 함께 유지하고, 표 행은 가능한 한 페이지 중간에서 분리하지 않는다.
- 표지와 목차를 제외한 본문은 한 페이지의 정보 밀도, 제목/본문/표/그림의 비율, 장 시작/종료 위치, widow/orphan, 반쪽 페이지를 종합해 균형 있게 편집한다.
- 문서 전체에서 동일한 본문 행간·문단 후 간격·Heading 계층 간격을 사용하되, 표 내부는 본문보다 소폭 조밀하게 구성할 수 있다.
- 최종 전달 전 DOCX를 전 페이지 PNG로 렌더하여 **잘림·겹침뿐 아니라 이유 없는 저밀도 페이지, 과도한 공백, 제목 고립, 표/그림 배치 균형**까지 육안 검수한다.
