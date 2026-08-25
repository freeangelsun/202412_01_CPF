# CPF Documentation Harness Changelog

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
