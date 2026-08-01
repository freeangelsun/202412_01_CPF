# CPF 도커 개발·테스트 환경 전체 구성 완료 보고

- 기준 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- 신규 파일만 포함
- 기존 파일 수정: 0
- 삭제: 0
- Secret 원문: 0
- 특정 자동화 제품명: 0

## 제공

- 한 줄 전체 설치
- Runtime Tool Source
- 사용자·자동화 도구 공통 문서
- Manifest
- 파일별 SHA-256
- 삭제 대상 없음

## 로컬에서 수행하지 못한 항목

- 사용자 Windows PC의 실제 Docker Pull
- 통합 Runner 실제 Build
- Oracle Instant Client 실제 다운로드
- Tooling Container 실제 Create
- CPF 전체 Runtime 실행

실행 결과는 설치 Script가 사용자 PC에서 확인한다.

## 사용자 장비 실행 결과

- 통합 Toolchain Image Build: 완료
- Trivy Version 실행: 완료
- OSS Review Toolkit Version 실행: 완료
- OpenTelemetry Collector Version 실행: 완료
- Tooling Container 생성: 완료
- 필수 Image: 13/13
- Legacy Runner Image 보존: 3/3
- Container: 7/7 Created/Stopped
- Running: 0
- Volume: 5/5
- CPF 업무 Schema·Data·Seed: 생성하지 않음

실행 중 확인된 Oracle 경로, ZIP 덮어쓰기, Docker Pull 출력 반환값 결함을 V3에 반영했다.


## 연동 및 사용 가이드 보강

`cpf-docs/guides/docker/CPF_도커_연동및사용가이드.md`에 다음 설명을 추가했다.

- Host 실행 기반별 역할
- DB·Redis·Kafka·Toxiproxy·OpenTelemetry Collector 용도
- 통합 Toolchain에 포함된 프로그램별 용도
- Trivy와 OSS Review Toolkit 용도
- CPF 공식 Module·Starter와 Docker 구성요소 관계
- 작업 유형별 필요한 Service와 불필요한 Service
- Output·Cache·Image Lock 위치

## 파일명 정책 정리

장기 유지 문서의 파일명과 경로에서 날짜 접두사를 제거했다.

- 날짜와 실행 시각은 문서 본문과 실행 결과에만 기록
- 문서 개정 시 동일한 정본 파일을 갱신
- 날짜별 중복 문서 생성 방지
- Evidence도 장기 유지 경로로 통일

## Docker 가이드 메뉴 구조

Docker 사용자 가이드를 다음 정본 디렉터리로 통합했다.

```text
cpf-docs/guides/docker/
```

`README.md`를 메뉴로 두고 안내, 전체 구축, 연동 및 사용, 문제 해결 및 초기화 문서를 같은 디렉터리에 배치했다. 기존 `cpf-docs/guides/` 바로 아래의 Docker 가이드 파일은 삭제 대상 Manifest에 기록했다.
