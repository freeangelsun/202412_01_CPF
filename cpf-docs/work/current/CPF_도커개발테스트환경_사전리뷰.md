# CPF 도커 개발·테스트 환경 전체 구성 사전 리뷰

- 기준 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- 작업 범위: 신규 환경 Tool과 장기 유지 문서
- 기존 README 수정: 없음
- 기존 가이드 수정: 없음
- 기존 Docker Base 자산 삭제: 없음
- Git 쓰기 작업: 없음

## 해결 대상

1. 현재 11개 Image로 부족한 Python·Git·DB Client 보강
2. Toxiproxy
3. OpenTelemetry Collector
4. Trivy
5. OSS Review Toolkit
6. 다른 PC에서 한 줄로 전체 구성
7. 설치 후 Created/Stopped 상태
8. CPF 업무 초기 데이터 미생성
9. Source·Secret·Image·Volume 보호
