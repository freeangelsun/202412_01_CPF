# DOCUMENT SOURCE FACT CHECK

기준 Source SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

## 사용자 문서 Source 정합성

- Public Profile/Starter/Provider 선택은 Canonical Starter Catalog와 실제 Generated Domain build.gradle 소비 형태를 대조했다.
- `CpfBaseController`, `CpfBaseService`, `CpfBaseDao`, Persistence ports, `@CpfTx`, `CpfDomainClient`, Batch control ports, Gateway route/auth contracts를 실제 Source와 대조했다.
- `@CpfTx` propagation은 REQUIRED, REQUIRES_NEW, SUPPORTS, MANDATORY, NOT_SUPPORTED, NEVER, NESTED를 포함한다.
- Batch의 동일 단순명 `CpfBatchJob` 2종은 FQCN과 Consumer 기준으로 03/07에서 구분했다.
- ADM/Batch/Gateway 운영 API는 실제 Controller route를 근거로 사용 흐름을 작성했다.
- 공식 DB Vendor는 Oracle/PostgreSQL/MariaDB만 사용자 문서에 노출한다.
- Generated Domain과 Batch Capability의 설계/구현 경계 차이는 사용자 문서에서 내부 개발 ID를 노출하지 않고, 별도 개발 검토 요청서에 Source 근거를 보존했다.

## 별도 개발 검토

- `cpf-docs/deliverables/CPF_DOCUMENTATION_TO_DEVELOPMENT_REVIEW.md`
- `cpf-docs/deliverables/CPF_DEVELOPER_USABILITY_REVIEW.md`

문서 검수 중 발견한 Source/UX 이슈를 문서에서 숨기거나 가상 API로 보정하지 않았다.
