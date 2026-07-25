# CPF R10 이후 실제 잔여 Gap

R10은 사용자 15개 요구의 Source/Tool/Guide 기반을 크게 닫았지만 다음 항목을 완료로 숨기지 않는다.

## P0 구조/제품 Gap

1. **Generated Domain Core 내부 직접 의존**: Golden Generator에는 아직 `com.cpf.core.common.base/execution/http/logging/database/broker/filetransfer` 직접 import가 있다. Public API/SPI 경계를 기능별로 승격하고 Runtime Consumer/Aspect까지 함께 옮겨야 한다. 단순 package rename으로 처리하지 않는다.
2. **기존 `cpf-common.utils` 중복 Surface**: 기술 범용 API 정본은 R10 `cpf-core.api.util`로 만들었지만, 기존 Common Utils 실제 Consumer를 전체 compile/search한 뒤 Core API로 이관하고 업무공통 helper만 `cpf-common`에 남겨야 한다.
3. **ADM→Owner 직접 DB 접근 전수검수**: BAT Calendar는 제거했지만 BAT/REF/Generated Domain/BZA에 대한 다른 direct JdbcTemplate 접근을 전체 master compile/search로 재확인해야 한다.
4. **Calendar 외부 Provider/Cache**: JDBC canonical은 구현했으나 고객 휴일 Provider adapter/cache invalidation/다중인스턴스 변경 전파는 Runtime 요구에 맞게 추가 확인한다.
5. **Log Layout Metadata 연동**: 잘못된 fixed-length 임의 parsing은 제거했다. 실제 전문 필드 분해는 Fixed-Length Layout Registry와 연결된 경우에만 제공해야 한다.
6. **Integrated Runtime Suite 완전 자동화**: Full Runner는 주요 Gate/DB/Build/Frontend/Generator/Browser를 한 명령으로 조율하지만 local/remote/failover/broker/DR 등 모든 기존 smoke script의 mandatory parameter를 정규화해 완전 자동화하는 작업은 Runtime 검수 단계에서 마무리한다.

## 실행 검증 Gap

- Fresh/upgrade/rollback DB + V45~V50
- Generated Domain 기존 ownership/parity 및 EXS lifecycle
- Gradle/Spring/npm/Browser
- transaction/header local/remote propagation
- 파일/DB/Batch log E2E와 instance failover
- Calendar ADM 변경→Batch Scheduler/업무 Domain 일치
- Multi-instance/Lease/Fencing/UNKNOWN/Recovery
- Security/MFA/mTLS/Break-glass 실제 Provider
- Backup/Restore/DR/Release artifact

실행하지 않은 항목은 Evidence가 생길 때까지 `미검증`이다.
