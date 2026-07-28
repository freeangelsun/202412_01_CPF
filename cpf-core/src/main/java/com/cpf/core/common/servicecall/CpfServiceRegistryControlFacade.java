package com.cpf.core.common.servicecall;

import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import java.util.Map;
import java.util.Objects;

/** Service Registry Owner 내부 command adapter입니다. */
public final class CpfServiceRegistryControlFacade implements CpfServiceRegistryControlPort {
    private final CpfServiceRegistryRepository repository;
    public CpfServiceRegistryControlFacade(CpfServiceRegistryRepository repository){this.repository=Objects.requireNonNull(repository);}
    @Override public Map<String,Object> saveService(ServiceDefinition c){return repository.saveService(c);}
    @Override public Map<String,Object> saveEndpoint(EndpointDefinition c){return repository.saveEndpoint(c);}
    @Override public Map<String,Object> saveInstance(InstanceDefinition c){return repository.saveInstance(c);}
    @Override public void deleteService(String id,DeleteCommand c){repository.deleteService(id,c);}
    @Override public void deleteEndpoint(String id,DeleteCommand c){repository.deleteEndpoint(id,c);}
    @Override public void deleteInstance(String id,DeleteCommand c){repository.deleteInstance(id,c);}
    @Override public Map<String,Object> changeInstanceState(String serviceId,String endpointCode,String instanceId,InstanceCommand command,String reason,String requestedBy){
        return repository.changeInstanceState(serviceId,endpointCode,instanceId,command,reason,requestedBy);
    }
}
