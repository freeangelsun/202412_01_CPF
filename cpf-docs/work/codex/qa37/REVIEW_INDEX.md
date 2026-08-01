# QA37 Codex Review Index

Codex 검수는 QA37 개발 완료·사용자 Commit·Push 후에만 수행한다.

## 검수 순서

1. latest exact SHA·Clean Tree
2. Root Build·Included Build Source
3. EDU 32 Source Closure
4. Manual EDU 135 Coverage
5. Java 25·Frontend
6. 3DB·Kafka·Redis·Fault
7. Browser
8. Supply-chain
9. Evidence·최종 판정

현재 `23a16f35a5633ce1317920468a69fef00c1a6a41`는 P0 Build Source 결함이 있으므로 Codex 최종 검수 대상이 아니다.
