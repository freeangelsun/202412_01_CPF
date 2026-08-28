# CPF Documentation Harness Reference Basis

## 1. 적용 우선순위

CPF 사용자 Steering, 최신 Source, Canonical Requirement가 최우선이다. 외부 문서 작성·접근성 기준은 CPF 문서를 더 읽기 쉽고 검증 가능하게 만드는 범위에서만 사용하며 Architecture/API/Ownership을 바꾸는 근거로 사용하지 않는다.

## 2. 외부 근거와 Harness 반영

### Microsoft Writing Style Guide — Scannable content / Headings

- 중요한 내용을 먼저 배치하고 짧은 제목·문장·문단, 일관된 Pattern, 긴 문서 Navigation을 사용한다.
- Heading은 위쪽 여백을 더 크게 두고 아래 첫 본문과 가깝게 묶어 계층을 시각화한다.
- CPF 반영: H1/H2/H3 Vertical Rhythm, 장문 재구성, 긴 문서 Navigation, Scan pass.
- https://learn.microsoft.com/en-us/style-guide/scannable-content/
- https://learn.microsoft.com/en-us/style-guide/scannable-content/headings

### Microsoft Word Accessibility

- 표는 데이터 관계에만 사용하고 단순 구조/Header Row를 사용한다. Fixed-width Table을 피하고 Window/Contents에 맞는 유연 폭을 사용한다.
- Built-in Heading Style, Alt Text, 의미 있는 Link Text, 충분한 Contrast, Accessibility Checker를 사용한다.
- CPF 반영: Table semantic fit, content-aware width, Alt Text, Heading hierarchy, Accessibility Gate.
- https://support.microsoft.com/en-US/accessibility/word/make-your-word-documents-accessible-to-people-with-disabilities
- https://support.microsoft.com/en-US/accessibility/word/make-your-word-documents-accessible-to-everyone-with-accessibility-assistant

### GOV.UK — Publishing accessible documents

- 의미 있는 Subheading, Bullet, Numbered Step으로 문서를 나누고 Heading Level을 건너뛰지 않는다.
- Table은 실제 데이터에만 사용하며 이미지에는 Alt Text를 제공하고 Contrast/Accessibility Check를 수행한다.
- CPF 반영: 레이아웃용 Table 금지, Semantic Heading, Alt Text, Manual Accessibility Review.
- https://www.gov.uk/guidance/publishing-accessible-documents

### W3C WCAG 2.2 / WCAG2ICT

- 일반 Text 4.5:1, 큰 Text와 의미 Graphic 3:1 Contrast 기준을 적용한다.
- Text spacing 변경/확대에도 내용 손실이 없어야 한다.
- CPF 반영: Contrast Hard Gate, Font/Margin/Spacing 축소로 페이지 수를 압축하는 행위 금지, 확대/복수 Renderer 검증.
- https://www.w3.org/TR/WCAG22/
- https://www.w3.org/WAI/WCAG22/Understanding/text-spacing
- https://www.w3.org/TR/wcag2ict-22/

### Diátaxis

- Documentation 사용자의 요구를 Tutorial, How-to, Reference, Explanation으로 구분하고 그 요구를 중심으로 정보 구조를 설계한다.
- CPF 반영: README=Overview/Wayfinding, Developer/Operator=How-to 중심, Specification/Standard=Reference 중심, Architecture=Explanation+Reference.
- https://diataxis.fr/

### GitHub Repository README

- README는 Repository 사용자가 프로젝트를 이해하고 탐색하는 첫 진입점이다.
- CPF 반영: README를 다크 브로셔형 제품 간판 + 역할별 Navigation으로 사용하며 단순 Reference Sheet처럼 만들지 않는다.
- https://docs.github.com/en/repositories/creating-and-managing-repositories/best-practices-for-repositories

## 3. CPF 강제 해석

- **총 문서 용량 제한 없음:** README/DOCX/PDF의 파일 크기, 페이지, 문자, 단어, H2, Figure 총수 상한을 두지 않는다.
- **Coverage 우선:** 길이를 줄이기 위해 Source-backed 기능·장점·Reader Task를 삭제하면 Hard Fail이다. 과밀하면 Section/Paragraph/Figure/Table/Navigation을 재구성한다.
- **README 브로셔 유지:** Hero, Dark CPF-owned Surface, 시각 Story, 자연스러운 장점 표현을 유지한다.
- **AI/텍스트 대응 유지:** README의 모든 의미 Figure는 의미 있는 Alt Text와 바로 아래 1~2문장의 간략 한글 설명을 가진다.
- **표는 내용 기반 유연 폭:** 비대칭 균등폭, 고정 50:50, Header/short token wrap을 허용하지 않는다.
- **Figure Geometry:** Connector는 Node Boundary에서 끝나고 Text/Label/다른 Node를 침범하지 않는다. 실제 README/DOCX/PDF 삽입 상태를 검증한다.
- **시각검수 이중화:** 전페이지 Contact Sheet Scan pass + 각 페이지 Detail pass를 모두 수행하고 Evidence를 남긴다.
- **작성자 독립성:** Harness만 읽고도 동일 품질 절차를 수행할 수 있도록 Design Playbook, Reader Needs, README Standard, Authoring Protocol을 정본으로 유지한다.
- **False Green 금지:** Required Gate 하나라도 FAIL/BLOCKED/NOT_EXECUTED/UNKNOWN/PARTIAL/WAIVED이면 전체 PASS 불가.
