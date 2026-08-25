# CPF Documentation Harness Changelog

## 1.2.1 — 2026-08-26

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
