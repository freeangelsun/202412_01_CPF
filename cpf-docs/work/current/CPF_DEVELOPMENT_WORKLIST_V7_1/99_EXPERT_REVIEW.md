# CPF V7 상세 개발 작업 목록 전문가 검수 보고

## 1. 관리자 결론

V6는 Canonical 169개를 빠짐없이 보존했지만, 실제 개발 단위가 Canonical 카드에 과도하게 묶여 있었다. V7은 정본 ID를 유지하면서 실제 작업을 독립 Work Package로 분리했다.

- Canonical Requirement: **169개 유지** — 영속 정본 ID이므로 임의 감소 금지
- 실제 실행 Work Package: **775개**
- Baseline 안정화: **28개** — V6 15개에서 확대
- Requirement Gap 검토: **24개** — V6 16개에서 재판정·확대
- 공통 Engineering Gate: **21개**
- 외부 표준·기술 Profile: **17개**
- 개발 세션 수: 고정하지 않음
- 모든 파일: Repository Root Overlay, 파일당 10MB 미만

Canonical 수가 유지된 것은 최적화를 하지 않아서가 아니라 Requirement ID 연속성과 30,558 파생 Requirement·Scenario 추적을 보존하기 위해서다. **실제 개발량과 검수 세분화는 775개 Work Package로 확대했다.**

## 2. V6 대비 정량 비교

| 구분 | V6 | V7 | 변화 |
|---|---:|---:|---:|
| Canonical Requirement | 169 | 169 | 정본 유지 |
| 실제 실행 Work Package | 169개 Canonical 카드 중심 | **775** | 실질 세분화 |
| Baseline 안정화 | 15 | **28** | +13 |
| Gap 후보 | 16 | **24** | +8, 승격/병합/보류 판정 |
| 공통 Engineering Gate | 분산 문구 | **21** | 독립 필수 Gate |
| 표준 Crosswalk | 약 10개 참조 | **17** | 현재 버전·적용정책 추가 |
| 전체 비압축 파일 용량 | 1,540,211B | **4,509,936B** | +192.8% |
| Markdown 줄 수 | 21,682 | **55,628** | +156.6% |
| 체크 항목 | 4,368 | **4,842** | +10.9% |

## 3. Work Package 유형

| 유형 | 수 |
|---|---:|
| `AUTOMATION_CONTINUITY` | 6 |
| `CANONICAL_MODEL` | 6 |
| `COMPATIBILITY_SECURITY` | 4 |
| `CONTRACT_OWNERSHIP` | 142 |
| `DATA_MIGRATION` | 48 |
| `EVIDENCE_VALIDATION` | 7 |
| `FAILURE_PARITY` | 6 |
| `FAILURE_RECOVERY` | 113 |
| `GATE_AGGREGATION` | 1 |
| `GATE_ENGINE` | 10 |
| `GENERATION_COMPATIBILITY` | 38 |
| `IMPLEMENTATION_CONSUMER` | 142 |
| `INTEGRATION_ENFORCEMENT` | 10 |
| `NEGATIVE_FIXTURES` | 10 |
| `OPERATIONS_SECURITY` | 70 |
| `POLICY_BOUNDARY` | 4 |
| `PROTOTYPE_EVIDENCE` | 4 |
| `REFERENCE_RUNTIME` | 6 |
| `REGENERATION_EVIDENCE` | 6 |
| `VERIFICATION_EVIDENCE` | 142 |

## 4. 영역별 실행 Work Package

| 파일 | Work Package 수 |
|---|---:|
| `100_RELEASE_PRODUCT_GOVERNANCE.md` | 52 |
| `10_ARCHITECTURE_MODULE_BOUNDARY.md` | 37 |
| `11_CORE_CALL_CONTEXT_STATE_RESILIENCE.md` | 74 |
| `12_CORE_OBSERVABILITY_FILE_MESSAGE.md` | 45 |
| `20_COMMON_DATA_DB.md` | 93 |
| `30_GATEWAY_EXTERNAL_INTEGRATION.md` | 61 |
| `31_EVENT_MESSAGING_SAGA.md` | 49 |
| `40_BATCH_AGENT_WORKER.md` | 37 |
| `41_CENTER_CUT.md` | 35 |
| `50_ADMIN_PLATFORM.md` | 77 |
| `51_BIZ_ADMIN.md` | 18 |
| `60_SECURITY_PRIVACY_CRYPTO.md` | 46 |
| `70_OPERATIONS_SRE_RUNTIME_CONTROL.md` | 69 |
| `80_DEVEX_GENERATOR_SAMPLE.md` | 33 |
| `90_API_QUALITY_TESTING.md` | 49 |

## 5. 전문가가 추가로 보강한 핵심

1. **정본과 실행 작업 분리**  
   169개는 추적 Anchor로 유지하고 계약·구현·Consumer·복구·운영·DB·생성·검증을 별도 Work Package로 분리했다.

2. **현재 Baseline 자체 안정화 확대**  
   QA 직접 Patch뿐 아니라 Java 25, Gradle shadow upgrade, Spring stack matrix, OpenAPI/AsyncAPI, SLSA/CycloneDX, OTel stability, CI, drift와 secret/hygiene를 추가했다.

3. **외부 표준 최신화**  
   OpenAPI 3.2.0, AsyncAPI 3.1.0, SLSA 1.2 Approved, CycloneDX 1.7, OWASP ASVS 5.0.0, OTel SemConv 1.43.0, WCAG 2.2와 NIST SP 1326을 반영했다. 최신 버전을 무조건 강제하지 않고 CPF 지원 Profile과 tooling compatibility를 먼저 검증한다.

4. **누락 기능 적극 판정**  
   Cache, Feature Flag, Session/BFF, Crypto Agility, Vulnerability Response, Threat Model, Performance/Soak, Webhook, AsyncAPI Schema, Data Encryption, Data Quality, Time, Notification, Support Bundle, Supplier Due Diligence와 Upgrade Assistant를 신규 Canonical 승격 우선 후보로 분류했다.

5. **범위 과잉 방지**  
   AI Security는 AI 기능이 제품 범위에 들어오기 전까지 보류했다. Kubernetes/Cloud/Mobile/Thick-client 전용 기능도 공식 지원 범위가 아니므로 무단 추가하지 않는다.

6. **DB·Query 전수 영향**  
   적용 Work Package에는 `GATE-05-DB-QUERY`가 연결되며 Generator→초기화/Seed→Migration/Rollback→3 Vendor→Consumer/API/Frontend/Batch→Evidence를 전수 확인한다.

7. **표준과 제안 분리**  
   필수 결과와 Gate는 강제다. Class/Package/Library/알고리즘/Test 도구는 비강제 제안이며 동등 이상의 표준 준수 대안이 허용된다.

## 6. AI 개발 할당 방식

- `WORK_ITEM_INDEX.csv`를 기준으로 P0→P1→P2와 dependency 순서로 배정하고, 각 행의 `markdown_file`과 `ledger_part`만 읽고 갱신한다.
- 같은 State Owner와 호출 경로를 공유하는 Work Package는 한 Slice로 묶을 수 있다.
- 묶음 개발 후에도 각 Work Item, CPF-FR, CPF-SC와 Gate 판정은 개별로 남긴다.
- 한 세션에 과도한 범위를 넣지 않고 3~15개 Work Package를 기본 Slice로 사용한다.
- Context 제한 시 마지막 Work Item과 다음 Work Item을 정확히 남기며 완료로 표시하지 않는다.

## 7. 최종 판정

V7은 V6의 내용을 단순 유지하거나 문구만 늘린 버전이 아니다. Canonical 추적성을 보존하면서 실제 개발 구조를 775개 실행 Work Package, 21개 공통 Gate, 28개 baseline 안정화와 24개 누락 기능 판정으로 재설계했다.


## 8. Repository Root Overlay 검산

ZIP은 Repository Root에서 바로 해제한다.

```text
cpf-docs/
└─ work/
   └─ current/
      └─ CPF_DEVELOPMENT_WORKLIST_V7/
```

- 별도 상위 Wrapper 폴더 없음
- ZIP 내부 모든 파일이 위 Repository 상대경로 아래 위치
- 파일 수: 26개
- 파일당 10MB 초과: 0개
- 최대 파일: `WORK_ITEM_INDEX.csv` (276,171B)


## 9. AI 처리 크기 최적화 V7.1

V7에는 1.1MB 단일 `WORK_ITEM_LEDGER.csv`가 있었지만 V7.1에서 제거했다. 현재 V7.1은 데이터를 줄이지 않고 다음처럼 변경된 상태다.

- 넓은 Work Item Ledger 775행을 Domain/Part별 `ledgers/*.csv`로 무손실 분할
- `WORK_ITEM_INDEX.csv`를 추가해 Work Item→상세 Markdown→Ledger Part를 즉시 탐색
- 250KB를 넘는 상세 Markdown을 약 210KB 목표로 분할
- 기존 대형 영역 파일 경로는 작은 Navigation Index로 유지
- Canonical 169개, Work Package 775개, 모든 필수 결과·제안·Gate·Scenario·상태 컬럼 유지

AI는 한 번에 3~15개 Work Item만 선택해 해당 Part를 읽는다. Git 사용자는 영역 Index와 `WORK_ITEM_INDEX.csv`로 빠르게 이동한다.


## 10. 04-05 Push 검증

- 확인 Commit: `f3814ccfb80a39be80772521826b671d692955e7` (`04-05`)
- 이전 Commit: `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` (`04-04`)
- Compare 상태: `ahead`, ahead 1, behind 0
- 추가 파일: 53개
- 배치 경로: `cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1/`
- Canonical: 169
- Work Package/Index/Split Ledger/Detailed Card: 각각 775
- 최대 파일: 276,171B
- GitHub Combined Status: 연결 Status 없음
- GitHub Workflow Run: 확인된 Run 없음

따라서 Push와 경로는 확인됐지만 Build·Runtime·QA 검증은 아직 수행되지 않은 상태다.

## 11. Push 후 문서 정합성 보정

V7.1에서 제거된 `WORK_ITEM_LEDGER.csv`를 참조하던 `01_COMMON_ENGINEERING_GATES.md`와 본 문서의 잔여 표현을 다음으로 정정했다.

```text
WORK_ITEM_INDEX.csv
+ 각 행의 markdown_file
+ 각 행의 ledger_part
+ GATE_APPLICABILITY_MATRIX.csv
```

이 보정은 Work Item 775개, Canonical 169개 또는 Ledger 컬럼을 변경하지 않는다.
