# CPF External·Fixed-Length·File Guide

## 1. 목적

범용 기술 Contract와 기관별 대외연계 구현의 Ownership, 전문·파일 처리, 실패·결과 불명·복구 기준을 정의한다.

## 2. Module 경계

```text
cpf-core
→ generic fixed-length/file/message contract and SPI

cpf-external
→ institution endpoint, layout mapping, authentication, adapter and recovery
```

`cpf-common` 또는 `cmnDB`는 범용 전문 Engine을 소유하지 않는다.

## 3. Core Fixed-Length Engine

- Layout, Version, Field와 Repeated Group
- Header/Body/Trailer
- byte length와 character encoding
- padding, alignment, required와 type validation
- number, decimal, amount, date/time converter
- parser/writer와 streaming
- field name, byte offset, original position을 포함한 error
- masking과 secure diagnostic
- codec/converter/validator SPI
- property-driven safe default

문자 길이와 byte 길이를 혼동하지 않는다. Multi-byte encoding 경계와 잘린 문자, overflow, underflow를 테스트한다.

## 4. Dynamic Layout

DB 기반 Layout 관리가 실제 제품 요구라면 Core 소유 Schema와 API를 사용한다. ADM은 관리·승인·Audit UI를 제공하지만 Table Owner가 아니다.

필수:

- layoutId/version/effective range
- immutable published version
- draft/review/approve/publish/retire
- backward compatibility
- test sample과 checksum
- permission와 audit

## 5. Institution Adapter

- institution/endpoint/profile
- protocol, timeout와 retry
- mTLS/OAuth2/JWT/API key
- request/response mapping
- institution error mapping
- idempotency key와 correlation
- circuit breaker와 rate limit
- unknown result query와 reconciliation

기관별 규칙을 Core에 Hard Coding하지 않는다.

## 6. File Transfer

- path alias와 allowlist
- file naming과 sequence
- temp/write/atomic rename
- size, checksum와 completion marker
- chunk/resume
- compression/encryption
- SFTP connection/profile
- ack/nack와 duplicate detection
- quarantine와 malware scan hook
- retention, archive와 purge

절대 경로와 Credential은 Source·Seed·Evidence에 남기지 않는다.

## 7. Attachment API

- metadata와 binary 분리
- content type와 size limit
- checksum
- upload/download permission
- expiry와 one-time URL 후보
- range download
- masking/watermark/audit

## 8. 결과 불명과 Reconciliation

전송 timeout을 확정 실패로 취급하지 않는다.

```text
send attempt
→ response confirmed / pre-send failure / unknown
→ query actual result
→ reconcile
→ retry or compensate with policy
```

원본 request hash, external correlation id, attempt, response와 reconciliation 결과를 연결한다.

## 9. 운영

ADM은 기관, Endpoint, Circuit, Failure, Unknown, File queue, Retry, Reconciliation과 Certificate 상태를 조회한다. 차단, 재시도, 재조정과 Manual Recovery는 권한·승인·사유·Audit를 요구한다.

## 10. Test와 Evidence

- encoding/byte boundary/golden message
- invalid layout/type/length
- large streaming
- endpoint timeout/connection reset
- duplicate/idempotency
- mTLS/certificate rotation
- file resume/checksum/atomicity
- unknown/reconciliation/compensation
- masking and secret review
