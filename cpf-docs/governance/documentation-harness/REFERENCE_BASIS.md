# 외부 문서 제작 기준 참고와 CPF 적용 판단

외부 기준은 CPF 하네스보다 우선하지 않는다. CPF Source/정본/사용자 Steering과 일치하면서 산출물 품질을 실제로 높이는 규칙만 채택한다.

## 채택한 기준

1. **Microsoft Word 접근성**
   - 긴 표에서 Header Row를 반복한다.
   - 데이터 표는 단순 구조를 사용하고 Merge/Split/Nested Table을 피한다.
   - 접근성 검사와 실제 시각 검사를 함께 수행한다.
   - CPF 적용: `design-tokens.json`의 표 Header 반복, 단순 표, 행 분할 금지, Visual QA에 반영.

2. **WCAG 2.2 Contrast**
   - 일반 텍스트 4.5:1, 큰 텍스트 3:1의 최소 대비 기준을 사용한다.
   - CPF 적용: README Visual, DOCX/PDF 상태/Callout/Diagram의 텍스트와 의미 있는 Graphic 대비에 반영.

3. **GitHub README**
   - README는 프로젝트가 왜 유용한지, 무엇을 할 수 있는지, 어떻게 시작하는지 알려주는 첫 진입점이다.
   - Repository 내부 Link/Image는 상대 경로를 사용하고 이미지에는 의미 있는 Alt Text를 사용한다.
   - CPF 적용: README Brochure + Quick Start + 상대 링크/Alt Text에 반영.

4. **Google Developer Documentation Style**
   - Actor가 중요한 경우 능동형을 우선한다.
   - 짧고 직접적인 문장, 설명적인 Heading, 좌측 정렬, 병렬 구조의 목록을 사용한다.
   - CPF 적용: `writing-style.json`의 문장/Heading/List/Alignment 규칙에 반영.

## 그대로 채택하지 않은 기준

- 일반적인 “README는 짧아야 한다” 규칙은 CPF에 그대로 적용하지 않는다. CPF README는 제품 브로셔와 실제 시작점 역할을 함께 하므로 핵심 기능/장점을 충분히 보여준다.
- Word의 “고정 폭 표를 피하라”는 일반 권고를 DOCX/PDF에 그대로 적용하지 않는다. CPF는 인쇄/렌더 결과의 열 폭이 흔들리지 않도록 의미 기반 비율과 `tblGrid`를 고정하되, 표 자체를 단순화하고 과도한 열 수를 제한한다.
- 영어 문서의 문법/철자 규칙은 한국어 CPF 공식 문서에 적용하지 않는다.
- 외부 Template의 색상/브랜드/목차를 가져오지 않는다. CPF 자체 Design Token과 Document Profile만 사용한다.
