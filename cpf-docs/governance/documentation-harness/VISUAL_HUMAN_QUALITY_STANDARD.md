# CPF Visual Human Quality Standard — Harness 2.15.1

Visual 파일이 존재하고 해상도/Canvas 검사가 통과했다는 이유만으로 품질 PASS하지 않는다.

- 원본 100%와 200% Detail pass를 모두 수행한다. Contact Sheet만 보는 검수는 FAIL이다.
- Text/Node/Pill/Arrow/Connector가 Canvas나 다른 객체를 침범하면 1px이라도 Hard Fail이다. 특히 Connector가 중앙 Text/Label을 관통하는 경우와 좌우 Node가 잘리는 경우를 독립 Metric으로 관리한다.
- Geometry Manifest는 실제 Node/Text/Connector 좌표를 포함해야 한다. Group box만 기록한 coarse manifest는 FAIL이다.
- Visual 5개 이상이면 실제 Geometry fingerprint 기준 최소 5개 문법을 요구한다. 이름만 `map/timeline/radial`로 바꾸고 동일 카드/화살표 구조를 반복하면 FAIL이다.
- Human Review Evidence에는 현재 Asset SHA-256을 기록한다. Asset이 바뀌면 Review는 stale이며 자동 승계하지 않는다.
- README 900/1200/1440px, DOCX/PDF 삽입면을 각각 검사한다. 원본 PNG PASS를 Embedded PASS로 승계하지 않는다.
- 사용자가 실제 화면에서 결함을 발견하면 해당 Visual과 전체 품질 PASS를 즉시 재개방한다.
