# CPF Generator Guide

## 1. 목적

신규 업무 Domain을 표준 구조로 만들고, 생성·검증·삭제·재생성까지 재현한다.

## 2. Input

- DomainName
- SystemCode
- BasePackage
- DB Vendor
- Capabilities

SystemCode와 DomainName을 혼용하지 않는다.

## 3. Capability

- database
- batch
- external
- messaging
- file
- ui
- security
- audit
- local-call
- remote-call

미선택 기능은 Dependency·Source·Config·Directory까지 없어야 한다.

## 4. Collision

- Reserved
- Module
- Package
- SystemCode
- Config Prefix
- Route
- Port
- Schema
- Table
- Queue
- Topic
- Cache
- Menu
- Manifest

## 5. Output

- Module
- Package
- Build
- Application
- Public Contract
- Service
- Repository
- SQL
- Test
- OpenAPI
- EDU
- Deploy
- Manifest

## 6. Lifecycle

```text
Dry Run
→ Create
→ Verify
→ DB Init
→ Build
→ Runtime
→ Safe Delete
→ Residual
→ Regenerate
→ Parity
```

## 7. Safe Delete

- Manifest Owner
- User File
- Consumer
- DB Object
- Route
- Menu
- Queue·Topic
- Dry Run
- Expected SystemCode

## 8. Reference Domains

MBR·ACC·EXS는 최소 검증 구조다.

- 과도한 업무 Table 금지
- 실제 Consumer 없는 Abstraction 금지
- Generic Capability를 EXS에 두지 않음

## 9. Official API Adoption

Generator Template은 다음을 사용한다.

- Standard Context
- Standard Error
- List/Page/Slice/Cursor
- Cpf Web Client
- Cpf Fixed-Length
- Cpf Validation
- Cpf Dates/Strings
- Audit/Masking

## 10. Evidence

- Input
- Tool Version
- File Count
- Hash
- Build
- DB
- Runtime
- Delete
- Residual
- Regeneration
- Semantic Parity
