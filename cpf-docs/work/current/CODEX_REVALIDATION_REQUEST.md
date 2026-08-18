# CPF Codex Revalidation Request — Final Development Package

현재 Source를 독립적으로 검수한다. 과거 PASS를 승계하지 않는다.

우선 확인:
- Channel Header/Context 최신 정본: 외부 5 Header + Receiver Current 자동설정
- Generated Domain systemCode 값 자체를 Channel Identity로 사용, Mapping 없음
- Channel Policy key = operationId + callerChannel
- Operation Catalog/Policy DB3/ADM/Bootstrap/Discovery/Multi-WAS 경계
- Transaction completion hooks, REST/Async/File/Cache/Testkit Public DX
- EDU 20+15 실개발 package/class/Test 구조와 Public API consumer
- DB3 V121~V127, Frontend generated/compatibility client, stale contract zero
- 실행하지 못하는 Java25/live runtime은 미검증으로 판정

Codex는 Codex 소유 Evidence/컬럼만 수정하고 개발 GPT/QA 소유 컬럼을 임의 수정하지 않는다.
