# CPF Readability & Actionability Standard

## 목적

Documentation Harness의 목적은 문서 파일을 생성하는 것이 아니라, **최신 CPF Source와 정본을 근거로 독자가 실제 업무를 끝낼 수 있는 공식 산출물을 일관되게 생성·검증하는 것**이다. 파일 존재, 키워드 존재, 표 개수, 페이지 Geometry, Renderer PASS는 필요조건일 뿐 충분조건이 아니다.

## 1. Reader-Executable 원칙

개발자·운영자·아키텍트가 문서를 열었을 때 `무엇을 하려는가 → 무엇을 선택하는가 → 무엇을 입력/설정하는가 → 실제로 무엇을 호출/실행하는가 → 정상 결과 → 실패/UNKNOWN → 복구/다음 행동 → 어떻게 검증하는가 → 어디서 상세 내용을 보는가`가 끊기지 않아야 한다. 적용 가능한 단계가 빠지면 FAIL이다.

### Selection-to-Action Hard Gate

- 비교/선택 표를 보여준 뒤 선택 결과별 첫 행동이 이어져야 한다.
- `JDBC / MyBatis / JPA`처럼 구현 전략을 선택시키면 각 전략의 Dependency/Starter, Repository/Mapper/Entity 사용, 최소 코드, DB3 확인, Test까지 연결한다.
- `Public API 빠른 선택` 표는 Reference이며 How-to 자체가 아니다. API 표만으로 장을 끝내면 FAIL이다.

### Developer Working Example Hard Gate

- 기능 장에는 실제 Consumer 또는 최소 동작 코드/명령/절차가 있어야 한다.
- Interface/Annotation 이름만 나열한 예는 Working Example이 아니다.
- Same JVM/Remote/외부 연계가 관련되면 호출 경로와 Context/System6 전달 경계를 설명한다.
- Side Effect/Timeout/UNKNOWN이 관련되면 blind retry 가능 여부와 Probe/Reconcile/Compensation 등 실제 다음 행동을 닫는다.

## 2. Visual Comfort 원칙

정보량을 줄여 빈 문서를 만드는 것이 아니라 **같은 정보를 더 읽기 좋은 구조로 재배치**한다. 페이지 수가 늘어나는 것은 허용한다.

- H1/H2/H3와 의미 Block 사이에 충분한 차등 여백을 둔다.
- 긴 Flat List 7개 이상은 Hard Fail. 3~5개 의미 그룹으로 나눈다.
- 90자 안팎의 긴 Bullet이 6개 이상 연속되면 Hard Fail.
- Table/Code/Figure/긴 List 같은 Heavy Block이 설명 없이 4개 이상 연속되면 Hard Fail.
- 목적/결과 설명 없는 Code Block 3개 이상 연속은 Hard Fail.
- Hero/Intro의 장문 중앙정렬은 금지한다. 260자를 넘는 중앙정렬 본문은 Hard Fail 후보이며 좌측 정렬/요약/그룹화를 우선한다.
- 표는 행/열 관계가 있을 때만 사용하며 선택/실패/복구 Column의 폭을 짧은 ID/상태 Column보다 충분히 확보한다.
- 페이지 수를 줄이기 위해 Font/Margin/Line spacing/Section spacing을 축소하지 않는다.

## 3. Fresh-Eyes Review

자동 Gate가 PASS한 뒤에도 다음 세 번의 독립적인 읽기 검수를 한다.

1. **Scan Pass** — 모든 페이지/README 900·1200·1440 폭을 빠르게 훑어 과밀, 평면적 계층, 긴 줄, 반복 Heavy Block을 찾는다.
2. **Detail Pass** — 표/코드/Figure/페이지 경계/마지막 페이지/복잡 장을 실제 크기로 본다.
3. **Reader Pass** — 최소 3개 대표 업무를 골라 시작 위치 → 선택 → 행동 → 정상/실패 → 결과 확인까지 문서만으로 추적한다.

Manual Review에는 page/section, 관찰내용, 결과를 Evidence로 남긴다. 모든 점수를 기계적으로 같은 값으로 채운 기록은 근거가 아니며 재검수 대상이다.

## 4. 완료 판정

다음 중 하나라도 남으면 전체 문서 완료가 아니다.

- 선택 후 행동 없음
- API Summary만 있고 Working Example/Consumer 없음
- 실패/UNKNOWN 후속 조치 없음
- Test/결과 확인 없음
- Flat List/Heavy Block 과밀
- 자동 PASS와 실제 육안 품질 불일치
- Manual Evidence 없음

발견된 사용자 지적은 해당 산출물만 고치지 않고 Rule/Profile/Validator/Negative Fixture/Final Acceptance에 동시에 반영한다.
