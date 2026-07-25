# CPF R11 Public Boundary Correction

기준: master `b6db56f5ee745558a59ce511ad681216004b9672` + R9/R10 overlay + R11 util consumer correction 적용 상태.

## 실제 사용자 실행에서 확인된 실패
- `cpf-account` / `cpf-member`가 `com.cpf.core.common.*` 내부 구현을 직접 import.
- REF Utility/Batch EDU 일부가 Core 내부 Runtime을 직접 사용.
- BAT Center-Cut 운영 Controller 및 sample/test가 legacy Core Center-Cut Runtime을 직접 사용.
- Core legacy `common.batch.centercut` package가 소비자 때문에 cleanup에서 보존됨.

## 보정
- ACC/MBR를 Base, Execution, Error, Database, HTTP, Logging 공개 API로 이관.
- `CpfSharedApi`, 업무 오류 API, Server Identity, Workflow, Batch Log Path 공개 facade 추가.
- `CpfHttpClient`에 표준 실행 ID 기반 GET 계약 추가.
- REF 공개 EDU에서 Core internal import 제거.
- BAT Center-Cut sample/test/controller를 `api.centercut` / `spi.centercut` 및 BAT Runtime으로 이관.
- REF Center-Cut DB adapter test는 Runner를 소유하지 않고 public SPI 계약만 검증하도록 변경.
- Core legacy Center-Cut auto-configuration과 runtime package는 zero-consumer 이후 cleanup에서 제거.
- Gate가 main + relevant test + legacy package 잔존 여부를 확인하도록 강화.

## 완료 판정
이 보정은 Source/정적 경계 보정이다. 실제 Gradle 전체 compile/test, Spring boot, DB/runtime/browser/다중 인스턴스 검증은 사용자 환경에서 통합 검증으로 실행하며 실행 전에는 성공으로 기록하지 않는다.
