# Rendered Page Composition Standard

자동 OOXML 검사는 최종 판정이 아니다. PDF 렌더의 실제 Text/Image/Vector 점유영역을 측정하고 모든 페이지를 육안 확인한다.

- 마지막 페이지가 실질 내용 없이 상단에서 끝나면 FAIL한다. 단, 충분한 완료/검수 내용이 있는 45~55% 점유 페이지는 Fresh-Eyes Review로 판정한다.
- H1이 페이지 하단에서 제목+소개만 남지 않게 한다.
- 긴 문단·표·코드·목록을 같은 무게로 연속 적층하지 않는다.
- 페이지 수를 줄이기 위해 Font/Margin/Line spacing을 축소하지 않는다.
