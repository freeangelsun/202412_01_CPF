# CPF Documentation Harness Reference Basis

## 1. 우선순위

CPF 사용자 Steering, 최신 Source, Canonical Requirement가 최우선이다. 외부 작성·접근성 표준은 CPF 문서를 더 읽기 쉽고 검증 가능하게 만드는 범위에서만 반영하며, CPF Architecture/Source 계약을 바꾸는 근거로 사용하지 않는다.

## 2. 이번 Harness에 반영한 외부 근거

### Microsoft Writing Style Guide — Scannable content

- 중요한 내용을 먼저 두고, 짧은 제목·문장·문단과 일관된 패턴으로 스캔 속도를 높인다.
- 긴 문서는 내부 탐색 수단을 제공한다.
- 참고: https://learn.microsoft.com/en-us/style-guide/scannable-content/

### Microsoft Word Accessibility

- Table은 데이터에만 사용하고 가능하면 단순 구조를 사용한다. Header Row를 지정하고 Merge/Split/Nested/Blank structural cell을 피한다.
- Built-in Heading Style, Alt Text, 충분한 Contrast, 실제 Accessibility Checker를 사용한다.
- 참고: https://support.microsoft.com/en-us/accessibility/word/make-your-word-documents-accessible-to-people-with-disabilities

### Google Developer Documentation Style Guide

- Table은 여러 속성의 관계형 데이터에 적합하고, 단순 목록·절차·레이아웃에는 List/본문을 사용한다.
- Headings는 계층적으로 사용하고 본문은 좌측 정렬하며, 이미지에는 의미 있는 Alt Text를 제공한다.
- 참고: https://developers.google.com/style/tables
- 참고: https://developers.google.com/style/lists
- 참고: https://developers.google.com/style/accessibility

### GOV.UK Design System / Content Guidance

- Heading 계층과 Style을 일관되게 유지하고, Type/Spacing Scale로 일정한 Vertical Rhythm을 만든다.
- Table Header는 구조를 설명해야 하며 복잡한 Table은 분해한다.
- 참고: https://design-system.service.gov.uk/styles/headings/
- 참고: https://design-system.service.gov.uk/styles/type-scale/
- 참고: https://design-system.service.gov.uk/components/table/

### W3C WCAG 2.2

- 일반 Text 대비는 4.5:1, 큰 Text는 3:1 이상을 기준으로 한다.
- 이해에 필요한 Graphical Object/Boundary/Connector는 인접 배경과 3:1 이상의 Non-text Contrast를 확보한다.
- 참고: https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum
- 참고: https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast

## 3. CPF 적용 해석

외부 원칙을 그대로 복사하지 않는다. CPF에서는 다음으로 구체화한다.

- 문서 도입부의 독자/목적은 자연스러운 1~2문장 Lead로 표현하고 Key/Value Layout Table을 만들지 않는다.
- Harness version, Source SHA, build baseline은 Evidence/Manifest에만 두며 사용자 본문에 노출하지 않는다.
- Connector는 대상 Box 내부로 들어가지 않고 외곽 Boundary에서 종료한다.
- 원본 Figure 검사만으로 PASS하지 않고 README/DOCX/PDF에 실제 삽입된 크기에서 Crop, Boundary, Effective Text Size, Contrast를 다시 검사한다.
- 사용자 지적은 공통 Rule + Negative Fixture + Validator Assertion으로 고정해 같은 문제의 재발을 막는다.
