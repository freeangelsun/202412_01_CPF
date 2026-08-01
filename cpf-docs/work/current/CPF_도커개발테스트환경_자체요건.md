# CPF 도커 개발·테스트 환경 자체 요건

## 포함

- 전체 Toolchain Runner
- 공식 DB 3종 Client
- 장애 조건 Tool
- Telemetry 수집기
- 취약점·Secret·SBOM Tool
- OSS License·Policy Tool
- 한 줄 설치
- 다른 PC 재구축
- 일상 사용
- 문제 해결
- 안전한 데이터 초기화

## 남은 환경 의존

- 인터넷 차단 장비의 Offline Image Bundle
- 회사 Proxy·사설 인증서
- Oracle Registry 조직정책
- 실제 CPU·Memory·Disk 여유
- 외부 Nexus 배포가 필요한 별도 환경

위 항목은 이번 설치 Script가 임의 우회하지 않는다.
