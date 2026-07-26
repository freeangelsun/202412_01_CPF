# CPF 기본 Metadata / Code / Message Guide

## 1. 원칙
업무 개발자가 프로젝트마다 같은 상태 코드와 오류 메시지를 다시 정의하지 않도록 CPF가 반복성이 높은 **기술/운영 메타데이터**를 기본 제공한다. 고객 업무 고유 코드는 CPF Product Seed에 넣지 않는다.

Vendor-neutral 정본은 `cpf-tools/db/metadata/default-metadata-catalog.json`이다. 현재 구현 DB인 MariaDB의 SQL projection은 `cpf-tools/db/vendor/mariadb/source/50_framework_seed_data.sql`이다. 미지원 Vendor를 MariaDB SQL 복사로 지원 완료 처리하지 않는다.

## 2. R14 기본 그룹
- HTTP_METHOD
- EXECUTION_STATUS
- ASYNC_STATUS
- RETRY_STATUS
- IDEMPOTENCY_STATUS
- HEALTH_STATUS
- CIRCUIT_STATUS
- FILE_SCAN_STATUS
- DATA_CLASSIFICATION
- APPROVAL_STATUS
- ERROR_CATEGORY
- RETENTION_ACTION

기본값은 제품 동작에 필요한 범용 상태에 한정한다. 신규 상태를 추가할 때 Source/API/Test/ADM 표시/Generator 영향을 함께 검토한다.

## 3. 공통 메시지/응답코드
R14 Seed에는 timeout, target-down, UNKNOWN_RESULT, optimistic lock, idempotency duplicate, attachment scan pending/quarantine 등 프레임워크 공통 상황의 메시지와 HTTP 응답 매핑을 추가했다.

### 사용 규칙
- 내부 예외 문자열을 그대로 외부 메시지로 노출하지 않는다.
- ResponseCode는 HTTP 상태와 CPF 오류코드를 함께 관리한다.
- `UNKNOWN_RESULT`는 일반 실패로 치환하지 않는다.
- 관리자 화면은 코드/메시지를 수정할 수 있어도 Secret 또는 내부 stack/SQL을 노출하지 않는다.

## 4. DB 초기화와 Generator
MariaDB Product Seed는 canonical source의 50/52/56/60 파일을 순서대로 결합한다. Generator는 catalog의 필수 group/message/response code를 검증하고, 생성 Domain이 별도 중복 기술 코드를 만들지 않도록 한다.

지원 DB가 추가될 때는 해당 Vendor용 SQL을 독립 검증한 뒤 catalog와 의미 parity를 확인한다.
