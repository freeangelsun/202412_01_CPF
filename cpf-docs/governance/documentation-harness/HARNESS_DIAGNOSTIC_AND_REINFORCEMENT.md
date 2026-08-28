# Documentation Harness 2.10.0 구조 결함 진단 및 보강

## 진단 결론

2.9.0은 Package/Manifest/Geometry/Contrast/Renderer 계층은 강했지만, **Reader Task의 실사용 완결성을 자동으로 강제하는 계층이 약해 False Green이 발생할 수 있었다.** 특히 `validate_reader_task_coverage.py`는 allRequiredConcepts 키워드 존재만 검사했으므로 선택표/API 표가 있어도 실제 적용·Consumer·Working Example·실패/복구·검증이 빠진 문서를 통과시킬 수 있었다.

사용자 대표 Finding은 다음 세 유형으로 묶인다.

- JDBC·MyBatis·JPA: 선택 기준은 있으나 선택 후 실제 적용 경로가 없음 → Selection-to-Action 결함
- Domain 호출과 외부 연계: API/옵션/Topology/System6 표는 있으나 실제 Consumer 호출 예와 UNKNOWN→Reconcile 흐름이 없음 → Developer Actionability 결함
- README: 긴 Hero 문단, 긴 Flat Navigation, Code Block/텍스트 적층 → Visual Comfort / Density / Hierarchy 결함

## 2.10.0 보강

- Readability & Actionability를 정식 completion gate로 추가
- Selection-to-Action / Developer Working Example / Visual Comfort를 독립 gate로 추가
- Developer chapter의 table-only/API-summary-only PASS 금지
- JDBC/MyBatis/JPA와 Domain Invocation의 대표 false-green을 자동 진단하는 validator 추가
- README/DOCX의 Flat List, 장문 Bullet, 연속 Code/Heavy Block, 장문 중앙정렬 Hero를 탐지
- 전 문서 line spacing/paragraph/heading/semantic transition/table padding을 여유 있게 강화
- Manual Review에 실제 Reader Task trace와 Dimension별 Evidence를 요구
- 동일 점수 반복을 실제 관찰 근거 없이 승인하지 않도록 Final Acceptance를 강화

2.10.0은 기존 2.9.0의 좋은 Package/Geometry/Contrast/Renderer Gate를 폐기하지 않고 그 위에 실사용 가능성과 시각적 호흡 계층을 추가한 증분 보강판이다.
