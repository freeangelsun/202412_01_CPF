# CPF QA32 Codex 독립 검수 준비

Base `d31bd127aa12bb9368933216642a5a9d25bd0bfd`에 대한 Root-relative Overlay다. Spring Batch Primary Engine 방향 정정을 최우선으로 검수한다.

## 정적 검수

- `python cpf-tools/scripts/verify-cpf-qa32-primary-engines.py --root .`
- `python cpf-tools/scripts/verify-cpf-qa32-repository-security.py --root .`
- `python cpf-tools/scripts/verify-cpf-supply-chain.py --root .`
- `python cpf-tools/scripts/verify-cpf-qa32-generator.py --root .`

## Runtime 검수

- `pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa32-runtime.ps1 -Root .`
- `python cpf-tools/scripts/verify-cpf-qa32-completion.py --root . --release`

Dependency만 추가한 Dual Path, 자체 Batch lifecycle 잔존, Gateway 수동 Proxy 잔존, 브라우저 Token, Hash Router, Unknown License, stale SHA Evidence가 발견되면 실패 처리한다. README/Guide 제외 범위는 검수 대상에서 제외한다.
