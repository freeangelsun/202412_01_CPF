package com.cpf.core.api.database;

/**
 * Vendor별 Runtime SQL을 조회하는 topology-independent Public API입니다.
 *
 * <p><b>책임:</b> Consumer가 statement key만 알고 SQL을 조회하도록 하며 실제 Resource 위치와
 * Vendor 선택은 cpf-core 내부 Provider에 숨깁니다. Public API에는 Spring 등의 구현 프레임워크 타입을
 * 노출하지 않습니다.</p>
 * <p><b>사용 조건:</b> 모듈은 {@link CpfVendorSqlCatalogProvider#forModule(String)}으로 자기 Owner Module의
 * Catalog를 획득한 뒤 {@link #required(String)}을 사용합니다.</p>
 * <p><b>실패 조건:</b> 지원하지 않는 Vendor, 존재하지 않는 statement key, Resource 누락/중복은 정상 SQL로
 * 대체하지 않고 즉시 실패해야 합니다. 다른 Vendor SQL fallback은 허용하지 않습니다.</p>
 * <p><b>Thread Safety:</b> 구현은 생성 이후 불변 Resource 해석 정보만 보유해야 하며 다중 Thread에서 공유할 수
 * 있어야 합니다.</p>
 * <p><b>Runtime Resource:</b> 공식 Vendor(MariaDB/PostgreSQL/Oracle)의 외부 Query Pack이 정본이며,
 * Source↔Resource 양방향 Contract Gate가 key/parameter/result alias를 검증합니다.</p>
 * <p><b>다중 인스턴스:</b> Catalog 선택은 인스턴스 로컬 캐시 상태가 아니라 동일 배포 Artifact/Config에 의해
 * 결정되어 모든 인스턴스가 같은 Vendor 계약을 사용해야 합니다.</p>
 * <p><b>보안/복구:</b> SQL이나 설정에 credential을 포함하지 않습니다. Resource Drift가 발견되면 자동 fallback하지
 * 않고 배포/Upgrade를 중단해 정본 Pack을 복구한 뒤 재시도합니다.</p>
 */
public interface CpfVendorSqlCatalog {
    /** 필수 statement SQL을 반환하며 누락 시 fail-closed 합니다. */
    String required(String statementKey);
    /** 진단/Evidence용 실제 Resource 경로를 반환합니다. */
    String resourcePath(String statementKey);
    /** 이 Catalog가 해석하는 DB Vendor를 반환합니다. */
    CpfDatabaseVendor vendor();
}
