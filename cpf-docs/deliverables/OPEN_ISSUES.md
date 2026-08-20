# CPF DEV20 OPEN ISSUES

## Source blocker

- **0건**. DEV20에서 재현 가능한 Source/정적/독립 Gate 실패는 모두 수정 후 재검증했다.

## 환경 의존 미검증

다음은 제품 결함으로 확인된 항목이 아니라 현재 실행환경에서 직접 증명할 수 없는 최종 Runtime 항목이다.

- Java 25 전체 Gradle clean build/test/publication
- Oracle live DB lifecycle 및 PostgreSQL/MariaDB 최종 live lifecycle
- Multi-WAS / same-host multi-process instance identity
- Process kill/restart/redeploy와 UNKNOWN/reconcile
- Browser → Backoffice Web → Gateway → Backoffice → Business Domain E2E
- 최종 ADM runtime discovery

실행 명령과 PASS 기준은 `TEST_AND_EVIDENCE.md` 및 최종 응답의 로컬 통합검증 명령을 따른다.
