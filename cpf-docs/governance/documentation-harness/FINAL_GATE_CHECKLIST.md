# CPF Documentation Final Gate Checklist

## Source / Harness
- [ ] HARNESS_LOCK PASS
- [ ] 최신 Source SHA-256 기록
- [ ] 최상위 Target Requirements 확인
- [ ] Source/API/CLI/DB/OpenAPI/Consumer Inventory 완료
- [ ] Harness Drift 없음 또는 사용자 승인 변경 반영

## Structure / Content
- [ ] 공식 12개 산출물 Scope 일치
- [ ] 모든 문서 목차 존재
- [ ] Profile H1/H2 이름/순서 100% 일치
- [ ] Product Coverage 미매핑 0
- [ ] Source에 없는 API/CLI/Path/DB Object 0
- [ ] 장점 전용 Label/과장 문구 0
- [ ] README 핵심 Visual 수량/위치/설명 규격 충족
- [ ] 개발자 장 기능 선택표 존재
- [ ] 운영 장 판단→조치→정상화 형식 충족
- [ ] Specification 식별자 Source exact match

## Table / Figure
- [ ] 코드성 짧은 값 외 표 본문 가운데 정렬 0
- [ ] 기계적 균등폭 표 0
- [ ] tblGrid / Cell Width 불일치 0
- [ ] 짧은 Token 과도 자동개행 0
- [ ] Header 반복 / 행 분할 규칙 충족
- [ ] 이미지 Text/Box Overflow 0
- [ ] 이미지 글자 겹침 0
- [ ] Crop 0
- [ ] Broken Glyph 0
- [ ] Informative Image Alt Text 100%
- [ ] 핵심 이미지 아래 2~4문장 설명 100%

## Visual / Accessibility / PDF
- [ ] README 1440/390 Light/Dark 실제 Render 확인
- [ ] DOCX 11개 PDF 렌더
- [ ] PDF 11개 전페이지 이미지 렌더
- [ ] 모든 페이지 Quick Scan
- [ ] 고위험 페이지 원본 크기 확인
- [ ] 빈 페이지 0
- [ ] 제목 단독 페이지 0
- [ ] 저밀도 꼬리 페이지 검토 완료
- [ ] Contrast 기준 충족
- [ ] Accessibility Blocking 0
- [ ] PDF Preflight Warning/Error 0
- [ ] Font substitution 0

## Package
- [ ] UTF-8 / NFC 경로 확인
- [ ] Broken local link 0
- [ ] Broken image link 0
- [ ] Product/Evidence 분리
- [ ] ZIP 재해제 구조 동일
- [ ] 파일별 SHA-256 일치
- [ ] 최종 판정: Semantic + Source + Cross-document + Visual + Accessibility + PDF + Link + NFC + Package 모두 PASS
