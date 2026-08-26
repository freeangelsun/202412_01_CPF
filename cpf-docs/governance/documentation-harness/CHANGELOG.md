# CPF Documentation Harness Changelog

## 1.3.0 - 2026-08-26

- README 포함 모든 문서의 H2/H3와 첫 내용 사이 간격을 축소하고, 하위 본문·불릿·Callout·Figure 설명을 공통 Content Rail로 들여쓰도록 고정.
- 짧은 핵심/선택/주의/복구는 필요할 때 bullet/number marker로 구조화하되 장식성 남발은 금지.
- Figure와 설명을 하나의 시각 블록으로 묶어 설명 귀속이 명확하도록 하고, 설명이 다음 섹션에 더 가까워지는 배치를 Hard Fail로 정의.
- README 다크 배경에서는 주요 Visual을 밝은 neutral/light-tint canvas 또는 bright focal surface로 분리하고 dark-on-dark Visual을 금지.
- Visual을 내용별 문법으로 다양화: Layer/Zone, Lane, Route/Timeline, State Ring, Split Boundary, Vendor Band, Mosaic/Cluster, Nested Zone 등을 사용하고 동일 Rounded Rectangle+Arrow 반복을 금지.
- Figure Canvas safe margin 48px, Node padding 24px(권장 32px), Label gap 24px, Node gap 28px, Label-Connector 16px로 강화.
- README 문서 링크에서 `[PDF]`는 `.pdf`, `[DOCX]`는 `.docx`를 직접 가리키도록 강제하고 형식/Target 불일치를 Hard Fail로 추가.
- 산출물 작업 기본을 `PATCH_FIRST` 점진 개선으로 변경. 직전 PASS 영역/Visual은 보존하고 새 Finding 영향 범위만 보정하며 변경 전/후 회귀 비교를 필수화.
- 회사 Windows 환경에서 Python을 필수 Runtime으로 요구하지 않도록 `validate_readme.ps1`을 추가하고 PowerShell-only 검증 경로를 정본으로 명시.

## 1.2.5 - 2026-08-26

- 복잡한 Text 연속 배치 금지와 Visual+짧은 한국어 설명 조합을 공통 Gate로 추가.
- 사용자 Windows Root 포함 절대경로 150자 이하 Gate 추가.
- 현행 Harness 한 세트만 유지하고 과거 Harness/세션/백업/날짜·R·REV suffix/해제본/임시 Validator 출력은 삭제 대상으로 지정.
- 빈 TOC/고립 저밀도 페이지 Gate 추가.

## 1.2.5 — 2026-08-26

- 사용자 지적: Figure 내부 글자 깨짐/겹침과 문서 전체 시각적 균형을 Harness 공통 Gate로 승격.
- Node 내부 여백 18px(권장 24px), Label 간 20px, Node 간 24px, Label-Connector 12px 최소 기준 추가.
- Group Title 전용 Band와 Child Label 분리, 병렬 Label baseline/간격 일관성, Connector-Text 충돌 금지 추가.
- 한쪽 과밀/한쪽 과공백/큰 dead space를 페이지 및 Figure Visual Balance 실패로 정의.
- 모든 페이지와 모든 Figure의 실제 Render 기반 Balance 검수 및 Validator Meta-Gate 강화.

## 1.2.0 — 2026-08-26

사용자 Documentation 피드백을 Harness-First 지속 개선 규칙으로 통합했다.

- H1/H2/H3 계층 번호와 차등 여백 강화, 대메뉴 시작 전 시각적 분리 필수화
- 문단·표·그림·Callout을 기계적으로 같은 간격으로 붙이는 레이아웃 금지
- README 핵심 장점을 한 줄 한 메시지 + marker/bullet로 표시
- README에 호출 오케스트레이션, Bootstrap/Build/Test/Runtime, 역할별 매뉴얼 진입점 강화
- Gateway 선택/미선택 비교 Visual 필수화 및 내부 Domain↔Domain Gateway 경유 금지
- 의미 있는 Visual 아래 1~2문장 간결한 한국어 설명 필수화
- Figure 필수 Label 4.5:1 대비 Gate 추가
- 개발자 가이드를 사용빈도 높은 거래 패턴·API·옵션·오류·복구 선택 중심으로 압축하고 Page Budget 축소
- PDF 한글 Font embedding + 최소 2개 독립 렌더러 Glyph 검증 필수화
- 사용자 Documentation 지적은 산출물 임시 수정이 아니라 Harness 보완 후 적용하도록 Workflow 고정

## 1.1.3 — 2026-08-25

Windows PowerShell 5.1의 문자열 비교 False FAIL을 제거했다.

- License 검증에서 한글 문자열 직접 비교를 제거하고 UTF-8 SHA-256 비교로 통일
- `writing-style.json`, `document-output-rules.json`, `profiles/README.json`의 License 문구를 각각 독립 Hash 검증
- README License H2 문구도 동일 Hash로 검증
- 실패 시 expected/actual License SHA-256을 출력해 원인을 즉시 확인 가능
- Package/Lock/semantic validator를 v1.1.3 기준으로 재생성

## 1.1.2 — 2026-08-25

사용자 명시 요청에 따른 전면 시각·내용 품질 보강.

- README 목차 제거, 번호형 H1 강제
- License 사용자 문구를 한 문장으로 고정
- README Visual 14~20개 강제 폐기 → 잘 만든 5~8개, Hero + 실제 CPF Architecture Map 필수
- 동일 Box/Arrow 반복 도식 금지, Visual Type 다양성 Gate 추가
- 표 제목/목적 필수, 일반 4열, Cell 장문/과다 자동개행 금지
- 코드성 값 외 Table Body 중앙정렬 금지 강화
- Alignment Grid/Indentation Scale/Paragraph Rhythm 수치화
- 전체 문서 Content Density/Page Budget 도입, 장문/반복 설명 금지
- 11개 문서 Profile을 기능별 책 구조에서 업무/판단 중심 압축 구조로 재설계
- 개발자 가이드의 긴 5열 설명표를 Compact 4열 선택표로 변경
- Visual QA에 들여쓰기/표 제목/반복 도식/장문 Cell 검사 추가
- 사용자 요청 없이는 Harness 수정 불가 정책 유지

## 1.0.0

초기 Documentation Harness.

## 1.1.2
- Windows 설치 검증을 Python 의존 방식에서 PowerShell 5.1+ 자체 검증 방식으로 전환했다.
- Validator 실패 시 정확한 실패 항목과 Lock mismatch의 expected/actual SHA-256을 출력한다.
- Python validator는 보조 검증용으로 유지하되 Python 3.6+에서 파싱되도록 호환성을 높였다.
