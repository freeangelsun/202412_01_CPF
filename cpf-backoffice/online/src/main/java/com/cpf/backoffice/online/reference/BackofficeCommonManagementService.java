package com.cpf.backoffice.online.reference;


import com.cpf.foundation.annotation.CpfService;
import com.cpf.common.management.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/** MBW는 Common owner table을 직접 접근하지 않고 Public Management API만 소비합니다. */
@CpfService
public final class BackofficeCommonManagementService extends com.cpf.backoffice.online.base.BackofficeBaseService {
    private static final Set<CpfCommonResource> GENERIC = Set.of(
            CpfCommonResource.CODE, CpfCommonResource.PARAMETER, CpfCommonResource.CALENDAR, CpfCommonResource.TEMPLATE);
    private final CpfCommonManagementApi common;
    public BackofficeCommonManagementService(CpfCommonManagementApi common) { this.common=common; }

    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfCommonPage<Map<String,Object>> search(CpfCommonResource resource,String query,Integer page,Integer size,
                                                     Boolean includeDisabled,Instant effectiveAt){
        requireGeneric(resource);
        return common.search(resource,query,page==null?0:page,size==null?50:size,Boolean.TRUE.equals(includeDisabled),effectiveAt);
    }
    public Map<String,Object> get(CpfCommonResource resource,Map<String,Object> identifiers){ requireGeneric(resource); return common.get(resource,identifiers); }
    public Map<String,Object> create(CpfCommonResource resource,CpfCommonMutation mutation,String actor){ requireGeneric(resource); return common.create(resource,mutation,actor); }
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String,Object> update(CpfCommonResource resource,CpfCommonMutation mutation,String actor){ requireGeneric(resource); return common.update(resource,mutation,actor); }
    public Map<String,Object> delete(CpfCommonResource resource,CpfCommonMutation mutation,String actor){ requireGeneric(resource); return common.delete(resource,mutation,actor); }

    private static void requireGeneric(CpfCommonResource resource) {
        if(resource==null || !GENERIC.contains(resource)) throw new IllegalArgumentException("Use typed Error/Message catalog API for this Common resource");
    }
}
