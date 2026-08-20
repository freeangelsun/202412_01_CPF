package com.cpf.common.management;

import java.time.Instant;
import java.util.Map;

/** CPF Common Product Service의 Public Management API입니다. BZA/고객 관리 앱은 Owner Table을 직접 수정하지 않습니다. */
public interface CpfCommonManagementApi {
    CpfCommonPage<Map<String,Object>> search(CpfCommonResource resource, String query, int page, int size, boolean includeDisabled, Instant effectiveAt);
    Map<String,Object> get(CpfCommonResource resource, Map<String,Object> identifiers);
    Map<String,Object> create(CpfCommonResource resource, CpfCommonMutation mutation, String actor);
    Map<String,Object> update(CpfCommonResource resource, CpfCommonMutation mutation, String actor);
    Map<String,Object> delete(CpfCommonResource resource, CpfCommonMutation mutation, String actor);
}
