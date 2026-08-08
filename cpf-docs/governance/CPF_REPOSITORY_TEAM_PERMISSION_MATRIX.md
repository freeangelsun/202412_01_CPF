# CPF Repository Team / Permission Matrix

- 중앙 정책 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)

| Role | Source | Merge | Release/Artifact | Production Deploy | Approval |
|---|---|---|---|---|---|
| Platform Core Maintainer | Core/Common read-write | Core PR approve | no production promotion | no | architecture owner |
| Batch Platform Maintainer | BAT read-write | BAT PR approve | BAT artifact publish candidate | no | BAT owner |
| Domain Maintainer | own Domain repo read-write | own Domain approve | own Job Pack candidate | no | no platform override |
| Security Reviewer | read | security-sensitive approval | signature/security gate | no | security dual-control |
| Release Manager | read | no source bypass | promote signed artifacts | no | release approval |
| Deployment Operator | read artifact/manifest | no source merge | read promoted artifacts | execute approved plan | cannot self-approve |
| Deployment Approver | read | no source merge | read | approve/reject | cannot execute own request |
| Auditor | read | no | read Evidence | no | immutable read-only |
| Break-glass | temporary scoped | no | no | emergency scoped | TTL + reason + post-review |

Actual GitHub team names are installation-specific. `templates/CODEOWNERS.template` intentionally contains placeholders; do not commit invented team names. `verify-github-governance.ps1` verifies the real branch-protection state and saves Evidence.
