# CPF Documentation Final Gate Checklist v1.1

## 내용
- [ ] 불필요한 반복·장황한 배경 설명 0
- [ ] 기능명만 나열한 Section 0
- [ ] Source에 없는 API/CLI/경로 0
- [ ] README에서 CPF 차별점/실제 편의가 자연스럽게 드러남

## README
- [ ] 목차 없음
- [ ] H1 번호 1..N 연속
- [ ] Hero 직후 실제 CPF 전체 Architecture Map
- [ ] Visual 5~8개, 동일 Layout 2개 이하
- [ ] 같은 Box/Arrow Template 반복 0
- [ ] License 지정 한 문장만 존재

## 표
- [ ] 모든 표 번호+제목+목적 존재
- [ ] 일반 4열 이하(허용된 비교표만 5열)
- [ ] 장문 Cell 0(70자/4줄 초과 0)
- [ ] 코드성 값 외 중앙정렬 0
- [ ] 기계적 균등폭 0
- [ ] 짧은 API/Class 중간 자동개행 0

## 시각/레이아웃
- [ ] 본문/표 제목/표/Caption 왼쪽 Grid 일치
- [ ] 동일 Level 들여쓰기 Drift 0
- [ ] 그림 글자/Box 겹침·Crop·깨짐 0
- [ ] 빈 페이지·제목만 페이지 0
- [ ] DOCX/PDF 전페이지 실제 렌더 검수
- [ ] Contact Sheet만으로 PASS하지 않음

## 패키지
- [ ] Accessibility PASS
- [ ] PDF Preflight PASS
- [ ] Link PASS
- [ ] Unicode/NFC PASS
- [ ] ZIP 재해제 Hash PASS


## v1.2 추가 필수 Gate

- [ ] 사용자 Documentation 지적이 먼저 Harness에 반영되고 Harness Validator PASS 후 산출물을 생성했는가
- [ ] H1/H2/H3 번호와 전후 여백이 계층적으로 구분되는가
- [ ] 대메뉴 시작 전 충분한 시각적 여백이 있고 모든 블록이 같은 간격으로 다닥붙지 않는가
- [ ] 핵심 장점이 한 줄 한 메시지 + marker/bullet로 빠르게 읽히는가
- [ ] 의미 있는 그림 아래 1~2문장의 간결한 한글 설명이 있는가
- [ ] Figure 필수 Label 대비가 4.5:1 이상인가
- [ ] README에 Bootstrap/Build/Test/Runtime과 역할별 매뉴얼 진입점이 명확한가
- [ ] Gateway 선택/미선택 그림에서 내부 Domain↔Domain 호출이 Gateway를 경유하지 않는가
- [ ] 개발자 가이드가 거래/호출/API/옵션/오류/복구 선택 중심이고 저빈도 설명으로 페이지를 늘리지 않았는가
- [ ] PDF 한글 Font가 임베딩되어 있고 최소 2개 독립 렌더러에서 전페이지 한글 Glyph가 정상인가

## v1.2.5 시각 균형 추가 필수 Gate

- [ ] 모든 Figure의 Text Bounding Box가 Node/Container 내부 최소 18px 여백을 확보하는가
- [ ] Label↔Label 최소 20px, Node↔Node 최소 24px, Label↔Connector 최소 12px 여유가 있는가
- [ ] Group/Container 제목은 별도 Title Band에 있고 내부 Node/Vendor/API Label과 겹치지 않는가
- [ ] Connector가 Label을 가로지르거나 Endpoint 외 Node 내부를 관통하지 않는가
- [ ] 병렬 Label의 baseline·폰트 크기·간격·강조가 균형적인가
- [ ] Text를 맞추기 위한 과도한 font 축소가 0건인가
- [ ] 모든 페이지의 좌우·상하 정보 밀도와 whitespace가 균형적인가
- [ ] 의도 없는 한쪽 과밀·한쪽 과공백·큰 dead space가 0건인가
- [ ] 전체 문서에서 제목/본문/표/그림/Callout의 시각적 무게가 균형을 이루는가
- [ ] 전페이지와 모든 Figure를 실제 렌더 크기에서 확인했으며 Contact Sheet만으로 판정하지 않았는가

## v1.2.5 추가 Gate

- [ ] 복잡한 거래/호출/오류/복구 설명이 동일 밀도의 장문 Text Block만 연속되는 페이지 0
- [ ] Decision/Compare/Flow/State/Architecture가 필요한 곳에서 실제 이해를 빠르게 하고, 아래 한국어 설명이 1~2문장으로 보완되는가
- [ ] 실제 H1/H2가 보이지 않는 빈 TOC 페이지 0
- [ ] 마지막 1~3항목/Source 한 줄만 고립된 저밀도 페이지 0
- [ ] 사용자 Windows Repository Root 포함 절대경로 150자 초과 파일 0
- [ ] Repository에 과거 Harness 버전/날짜·R·REV·SESSION 백업/ZIP 해제본/Validator 임시 출력 0
- [ ] 현행 `documentation-harness/**` 한 세트만 존재


## v1.3.0 점진 개선·Content Rail·Visual Grammar·Link Gate

- [ ] 직전 공식 PASS 산출물을 기준선으로 잡고 영향 구간만 PATCH_FIRST로 보정했는가
- [ ] 관계없는 PASS 페이지/Visual/문장이 불필요하게 바뀐 곳 0건인가
- [ ] 변경 전/후 비교에서 이전보다 미관·가독성·링크 품질이 나빠진 회귀 0건인가
- [ ] 모든 H2/H3의 첫 내용이 제목과 가까이 묶이고 다음 부제목 전 여백보다 작게 설정되었는가
- [ ] README 포함 모든 문서에서 H2/H3 하위 본문·불릿·Callout·Figure 설명이 공통 Content Rail에 정렬되는가
- [ ] 필요한 핵심/선택/주의/복구 항목에 marker를 사용하되 장식성 bullet 남발이 없는가
- [ ] Figure 제목·이미지·설명이 하나의 블록으로 읽히고 설명 귀속이 모호한 곳 0건인가
- [ ] README 다크 배경과 주요 Figure가 밝은 Surface/명도 대비로 분명히 구분되는가
- [ ] README Visual 5개 이상일 때 서로 다른 Visual Grammar가 4개 이상이고 Rounded Rectangle+Arrow chain이 1개 이하인가
- [ ] Architecture/Flow/State/Batch/DB3/Capability 등 내용에 맞는 시각 문법을 선택했는가
- [ ] Figure Label이 Shape/Canvas safe area를 침범하는 곳 0건인가
- [ ] `[PDF]` 링크는 `.pdf`, `[DOCX]` 링크는 `.docx`를 열고 잘못 교차 연결된 링크 0건인가
- [ ] 회사 Windows에서 Python 설치 없이 PowerShell-only Harness/README 검증이 가능한가
