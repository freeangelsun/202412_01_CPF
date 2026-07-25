package com.cpf.core.common.servicecall;

import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import java.util.Map;
import java.util.Objects;

/** Service Registry Owner 내부 command adapter입니다. */
public final class CpfServiceRegistryControlFacade implements CpfServiceRegistryControlPort {
    private final CpfServiceRegistryRepository repository;
    public CpfServiceRegistryControlFacade(CpfServiceRegistryRepository repository){this.repository=Objects.requireNonNull(repository);}
    @Override public Map<String,Object> changeInstanceState(String serviceId,String endpointCode,String instanceId,InstanceCommand command,String reason,String requestedBy){
        return repository.changeInstanceState(serviceId,endpointCode,instanceId,command,reason,requestedBy);
    }
}
