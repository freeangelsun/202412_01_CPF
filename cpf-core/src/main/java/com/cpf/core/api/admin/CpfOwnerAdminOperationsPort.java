package com.cpf.core.api.admin;

import java.util.Map;

/**
 * 업무 Owner가 ADM Control Plane에 제공하는 표준 운영 Port입니다.
 *
 * <p>Owner DB 접근과 transaction은 Owner Module에 남기고 ADM은 이 Port만 소비합니다.
 * Generated Domain도 동일 계약을 구현하면 ADM과 같은 JVM/분리 WAS topology 양쪽에 합류할 수 있습니다.</p>
 */
public interface CpfOwnerAdminOperationsPort {
    /** 3자리 Owner SystemCode를 반환합니다. */
    String ownerSystemCode();

    /** Owner 자원 조회를 수행합니다. */
    Map<String, Object> query(CpfOwnerAdminQuery query);

    /** Owner 자원 변경 명령을 수행합니다. */
    Map<String, Object> command(CpfOwnerAdminCommand command);
}
