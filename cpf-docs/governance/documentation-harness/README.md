# CPF Documentation Harness v1.1.3

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
