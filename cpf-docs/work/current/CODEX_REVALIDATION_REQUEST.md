# CPF Codex Revalidation Request — Current

## 기준

- Baseline marker: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
- Result source SHA-256: `629bc777b66c46a8d79cce28e5f5a5a694655d3b145a90cfd50cf2b232c7b6a9`
- Developer static/source status: `완료`
- Runtime verification: `RUNTIME_REVERIFY_REQUIRED`

## 독립 재검수 우선순위

1. Canonical six가 internal typed Domain transport에서만 자동 wire propagation되는지, external client/generic HTTP에서는 0개인지.
2. ingress trust가 Header self-assertion을 신뢰하지 않고 verified caller/peer mapping을 쓰는지.
3. System/Target/Operation mismatch가 Controller 전에 409, forged caller가 403, malformed/missing이 400인지.
4. `CpfHttpHeaders`와 `CpfContexts` Public API에 duplicate/dead surface 또는 protected setter가 없는지.
5. `instanceId`가 explicit `CPF_RUNTIME_INSTANCE_ID` else runtime hostname으로 startup-stable한지.
6. DB/Java/ADM 같은 의미 이름이 일치하고 V119/V120/refDB V95 upgrade/rollback과 generated current가 일치하는지.
7. OpenAPI operationId = handler/domain/ADM operation identity가 유지되는지.
8. EDU/member/external이 Internal/raw ThreadLocal/MDC/Servlet/manual canonical Header 조립 없이 Golden Path를 실제 소비하는지.
9. Delete Manifest 75개가 모두 dead/duplicate/stale이며 보호 경로 삭제가 0인지.
10. 과거 Evidence를 현재 SHA PASS로 승계하지 않았는지.

Runtime은 전체 FullLocal 반복보다 이번 변경 영향 최소 세트부터 독립 재검수한다.
