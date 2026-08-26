# CPF Documentation Harness v1.3.0

CPF 공식 README 및 11개 DOCX/PDF를 생성·현행화할 때 사용하는 고정 하네스입니다.

- 변경 권한: **사용자의 명시적 요청만**
- 작성 원칙: 필요한 내용만 짧고 정확하게, 표/그림은 이해 시간을 줄일 때만 사용
- README: 목차 없음, 번호형 H1, Hero 다음 실제 CPF Architecture Map 필수
- DOCX/PDF: 목차 필수, 자동 H1/H2 번호, 좁은 여백, 공통 Alignment Grid
- 표: 제목+목적 필수, 일반 4열 이하, 장문 Cell 금지, 코드성 값 외 본문 중앙정렬 금지, 균등폭 금지
- 그림: 같은 박스-화살표 반복 금지, 시각화 역할 다양성 필수
- 완료: 내용·시각·접근성·PDF·링크·Package 검증을 모두 통과해야 함

실행 순서와 세부 규칙은 `CPF_DOCUMENTATION_HARNESS.md`, `design-tokens.json`, `content-density.json`, `visual-system.json`, `profiles/*.json`을 따릅니다.

## 설치 검증

Windows에서는 `validators/validate_harness.ps1`을 정본 검증기로 사용한다. Python 설치/버전에 의존하지 않는다. Linux/macOS 또는 독립 교차검증에는 `validators/validate_harness.py`를 사용할 수 있다.


## v1.2 핵심 변경

- Documentation 작업은 Harness 보완 → Validator PASS → 산출물 생성 순서를 고정합니다.
- H1/H2/H3 번호와 차등 여백으로 대·중·소 메뉴를 분명히 구분합니다.
- README에 호출 오케스트레이션, Gateway 선택/미선택, Bootstrap/Build/Test/Runtime, 역할별 매뉴얼 진입점을 강화합니다.
- 내부 Domain 간 호출은 Gateway를 경유하지 않습니다.
- 개발자 가이드는 거래 패턴/API/옵션/오류/복구의 선택 중심으로 압축합니다.
- PDF 한글 Font 임베딩과 복수 렌더러 Glyph 검증을 필수화합니다.

## v1.2.5 핵심 추가

- Figure 내부 Text/Box/Connector 충돌과 Group Title/Child Label 겹침을 정량 Gate로 검수합니다.
- Node 내부 여백, Label/Node/Connector 최소 간격, 병렬 Label baseline을 검수합니다.
- README/DOCX/PDF 모든 페이지의 좌우·상하 정보 밀도와 whitespace 균형을 최종 Render에서 확인합니다.
- 한쪽 과밀·반대쪽 과공백·큰 dead space·과도한 font 축소를 최종본으로 허용하지 않습니다.

## 현행본 유지

이 디렉터리 한 세트만 Harness 정본이다. 과거 Harness 버전/ZIP 해제본/세션 백업은 Repository에 보존하지 않는다.

## v1.3.0 핵심 추가

- 산출물은 직전 공식 PASS본을 기준으로 `PATCH_FIRST` 점진 개선하며 관계없는 좋은 요소를 전면 재생성하지 않습니다.
- README 포함 모든 문서의 H2/H3 하위 내용은 공통 Content Rail로 들여쓰고, 제목-첫 내용 간격은 compact하게 유지합니다.
- 핵심/선택/주의/복구는 필요할 때 marker를 사용해 읽기 진입점을 만듭니다.
- Figure와 설명을 같은 시각 블록으로 묶어 어느 그림 설명인지 모호하지 않게 합니다.
- README 다크 배경 위 주요 Visual은 밝은 Surface로 분리하고, 내용별 Visual Grammar를 사용해 동일 네모+화살표 반복을 금지합니다.
- `[PDF]`와 `[DOCX]` 링크 Target을 실제 형식과 일치시키며 PowerShell-only README Validator를 제공합니다.
