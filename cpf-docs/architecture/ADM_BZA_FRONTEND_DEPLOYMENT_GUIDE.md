# CPF ADM·BZA Frontend·Deployment Guide

## 1. Product Boundary

### ADM

Platform Operations.

### BZA

Customer Business Operations.

## 2. Source

```text
src/features/<feature>/
├─ pages
├─ components
├─ dialogs
├─ api
├─ model
├─ composables
└─ routes
```

Giant `App.vue`·State File을 분해한다.

## 3. Build

- npm ci
- lint
- typecheck
- unit
- production build
- artifact checksum

ADM/BZA 별도 Artifact·Version.

## 4. Deploy

### UI

- Nginx/Apache
- Static
- Base Path
- SPA Fallback
- Cache
- Compression
- CSP

### API

- JAR/WAR
- WAS
- Independent Deploy

## 5. Security

- Server API Permission
- Direct URL Block
- Cookie Secure/SameSite
- CSRF
- Token Refresh
- Session Expiry
- XSS
- CSP
- File Permission
- Masking

## 6. Contract

```text
Vue Action
→ API
→ Service
→ DB/Runtime
→ Result/Error
→ UI
```

Mock-only 완료 금지.

## 7. Browser

- Login
- Role
- Menu
- Direct URL
- Search
- Detail
- Approval
- Batch
- Center-Cut
- Agent
- Reprocess
- Unknown
- Sequence Sample
- Download
- Session
- Deep Link
- Rollback

## 8. Compatibility

- UI/API Version Matrix
- Rolling
- UI-only Redeploy
- API-only Redeploy
- Independent Rollback
