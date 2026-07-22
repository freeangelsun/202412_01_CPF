# CPF External·Fixed-Length·File Guide

## 1. Ownership

### Core

Generic Contract·Engine·SPI.

### External

Institution-specific Definition·Adapter.

### Admin

Operations UI.

## 2. Fixed-Length

- Layout
- Field
- Group
- Header/Body/Trailer
- Byte Length
- Encoding
- Padding
- Numeric/Date
- Validation
- Masking
- Streaming
- Version
- Error Position

## 3. External Call

- Institution
- Endpoint
- Auth
- Timeout
- Retry
- Circuit Breaker
- Idempotency
- Unknown
- Result Query
- Reconciliation
- Audit

## 4. File

- Path Alias
- Protocol
- SFTP
- Upload/Download
- Chunk
- Resume
- Checksum
- Compression
- Encryption
- Virus
- Zip Bomb
- Retention
- Archive
- Duplicate

## 5. Attachment

- Metadata
- Content Type
- Size
- Count
- PII
- Access
- Audit
- Expiry

## 6. Messaging

- Envelope
- Correlation
- Outbox
- Inbox
- DLQ
- Retry
- Replay
- Poison
- Ordering
- Idempotency

## 7. Unknown Result

- Timeout after Send
- Status Query
- Success/Failure/Unknown
- Approval
- Compensation
- No Blind Replay

## 8. Sample

REF/EXS Sample은 실제 Core API를 사용한다. 직접 Parser·Raw SFTP·Custom Retry를 만들지 않는다.
