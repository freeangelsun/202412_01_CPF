package com.cpf.admin.opr.parameter;

import com.cpf.gateway.api.CpfGatewayRegistryPort;
import com.cpf.common.parameter.api.CpfParameterReferenceCatalogPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** CPF Registry와 설치 Capability를 Reference Picker 검색 모델로 변환합니다. */
@Component
public final class AdmParameterReferenceCatalogAdapter implements CpfParameterReferenceCatalogPort {
    private final ObjectProvider<CpfServiceRegistryQueryPort> services;
    private final ObjectProvider<CpfGatewayRegistryPort> gateway;
    private final AdmParameterReferenceProperties configured;

    public AdmParameterReferenceCatalogAdapter(
            ObjectProvider<CpfServiceRegistryQueryPort> services,
            ObjectProvider<CpfGatewayRegistryPort> gateway,
            AdmParameterReferenceProperties configured) {
        this.services=services;this.gateway=gateway;this.configured=configured;
    }

    @Override
    public CatalogPage search(ReferenceQuery query) {
        String type=required(query.referenceType()).toUpperCase(Locale.ROOT);
        int offset=Math.max(0,query.offset());int limit=Math.max(1,Math.min(query.limit(),100));
        List<ReferenceItem> all=switch(type) {
            case "SERVICE","SERVICE_REFERENCE" -> services(query.query());
            case "ENDPOINT","ENDPOINT_REFERENCE","SERVICE_ENDPOINT" -> endpoints(query.parentId(),query.query());
            case "INSTANCE","INSTANCE_REFERENCE","SERVICE_INSTANCE" -> instances(query.parentId(),query.query());
            case "GATEWAY_GROUP","SERVER_GROUP" -> groups(query.parentId(),query.query());
            case "SECRET","SECRET_REFERENCE" -> secretReferences(query.query());
            case "PATH","PATH_ALIAS" -> pathAliases(query.query());
            case "FILE","FILE_REFERENCE" -> fileReferences(query.parentId(),query.query());
            case "CODE","CODE_REFERENCE" -> unavailable(type,"Code Group ID가 없는 자유 Code Reference는 허용하지 않습니다.");
            default -> throw new IllegalArgumentException("Unsupported parameter reference type: "+type);
        };
        if(isUnavailable(all))return new CatalogPage(type,false,false,all.getFirst().disabledReason(),offset,limit,false,List.of());
        int from=Math.min(offset,all.size());int to=Math.min(from+limit,all.size());
        return new CatalogPage(type,true,true,null,offset,limit,to<all.size(),List.copyOf(all.subList(from,to)));
    }

    private List<ReferenceItem> secretReferences(String q) {
        if(configured.getSecrets().isEmpty())return unavailable("SECRET_REFERENCE","승인된 Secret Metadata Catalog가 구성되지 않았습니다.");
        return configured.getSecrets().entrySet().stream()
                .filter(e->matches(e.getKey(),Objects.toString(e.getValue().getLabel(),e.getKey()),q))
                .map(e->{var v=e.getValue();String provider=Objects.toString(v.getProviderId(),"").trim();String key=Objects.toString(v.getKey(),"").trim();
                    boolean valid=!provider.isBlank()&&!key.isBlank()&&v.isEnabled();
                    return new ReferenceItem(e.getKey(),Objects.toString(v.getLabel(),e.getKey()),"SECRET_REFERENCE",provider,valid,
                            valid?null:"비활성 또는 Provider/Key Metadata 누락",
                            Map.of("providerId",provider,"scope",Objects.toString(v.getScope(),"default"),"valueExposed",false));}).toList();
    }
    private List<ReferenceItem> pathAliases(String q) {
        if(configured.getPaths().isEmpty())return unavailable("PATH_ALIAS","승인된 Path Alias Catalog가 구성되지 않았습니다.");
        return configured.getPaths().entrySet().stream()
                .filter(e->matches(e.getKey(),Objects.toString(e.getValue().getLabel(),e.getKey()),q))
                .map(e->{var v=e.getValue();return new ReferenceItem(e.getKey(),Objects.toString(v.getLabel(),e.getKey()),
                        "PATH_ALIAS",null,v.isEnabled(),v.isEnabled()?null:"비활성 Path Alias",
                        Map.of("provider",Objects.toString(v.getProvider(),"LOCAL"),"remote",v.isRemote(),
                                "sharedDurable",v.isSharedDurable()));}).toList();
    }
    private List<ReferenceItem> fileReferences(String parentId,String q) {
        if(configured.getFiles().isEmpty())return unavailable("FILE_REFERENCE","승인된 File Reference Catalog가 구성되지 않았습니다.");
        String parent=clean(parentId);
        return configured.getFiles().entrySet().stream()
                .filter(e->parent==null||parent.equals(e.getValue().getPathAlias()))
                .filter(e->matches(e.getKey(),Objects.toString(e.getValue().getLabel(),e.getKey()),q))
                .map(e->{var v=e.getValue();String alias=Objects.toString(v.getPathAlias(),"").trim();
                    boolean valid=v.isEnabled()&&!alias.isBlank()&&configured.getPaths().containsKey(alias);
                    return new ReferenceItem(e.getKey(),Objects.toString(v.getLabel(),e.getKey()),"FILE_REFERENCE",alias,valid,
                            valid?null:"비활성 또는 Path Alias 누락",
                            Map.of("pathAlias",alias,"relativePath",Objects.toString(v.getRelativePath(),"")));}).toList();
    }

    private List<ReferenceItem> services(String q) {
        CpfServiceRegistryQueryPort port=services.getIfAvailable();if(port==null)return unavailable("SERVICE_REFERENCE","Service Registry가 설치되지 않았습니다.");
        return port.services(clean(q),"Y",1000).stream().map(v->new ReferenceItem(v.serviceId(),v.serviceName(),"SERVICE_REFERENCE",null,v.enabled(),v.enabled()?null:"비활성 Service",Map.of("serviceType",v.serviceType(),"version",v.version()))).toList();
    }
    private List<ReferenceItem> endpoints(String serviceId,String q) {
        CpfServiceRegistryQueryPort port=services.getIfAvailable();if(port==null)return unavailable("ENDPOINT_REFERENCE","Service Registry가 설치되지 않았습니다.");
        if(clean(serviceId)==null)return unavailable("ENDPOINT_REFERENCE","먼저 Service를 선택해야 합니다.");
        return port.endpoints(serviceId,clean(q),"Y",1000).stream().map(v->new ReferenceItem(v.endpointCode(),v.endpointName(),"ENDPOINT_REFERENCE",v.serviceId(),v.enabled(),v.enabled()?null:"비활성 Endpoint",Map.of("endpointType",v.endpointType(),"baseUrl",v.baseUrl(),"version",v.version()))).toList();
    }
    private List<ReferenceItem> instances(String parentId,String q) {
        CpfServiceRegistryQueryPort port=services.getIfAvailable();if(port==null)return unavailable("INSTANCE_REFERENCE","Service Registry가 설치되지 않았습니다.");
        String[] parent=Objects.toString(parentId,"").split("/",2);if(parent.length!=2)return unavailable("INSTANCE_REFERENCE","Service/Endpoint를 먼저 선택해야 합니다.");
        return port.instances(parent[0],parent[1],null,1000).stream().filter(v->matches(v.instanceId(),v.instanceName(),q)).map(v->new ReferenceItem(v.instanceId(),v.instanceName(),"INSTANCE_REFERENCE",parentId,v.active()&&!v.draining(),v.draining()?"Drain 상태":!v.active()?"비활성 Instance":null,Map.of("status",v.status(),"environment",v.environmentCode(),"version",v.version()))).toList();
    }
    private List<ReferenceItem> groups(String serviceId,String q) {
        CpfGatewayRegistryPort port=gateway.getIfAvailable();if(port==null)return unavailable("GATEWAY_GROUP","Gateway Control Plane이 설치되지 않았습니다.");
        return port.findServerGroups(null,clean(serviceId),null,1000).stream().filter(v->matches(v.serverGroupId(),v.groupName(),q)).map(v->new ReferenceItem(v.serverGroupId(),v.groupName(),"GATEWAY_GROUP",v.serviceId(),!"RETIRED".equals(v.status()),"RETIRED".equals(v.status())?"폐기된 Group":null,Map.of("environment",v.environmentCode(),"status",v.status(),"version",v.version()))).toList();
    }
    private static boolean matches(String a,String b,String q){String n=clean(q);return n==null||a.toLowerCase(java.util.Locale.ROOT).contains(n.toLowerCase(java.util.Locale.ROOT))||b.toLowerCase(java.util.Locale.ROOT).contains(n.toLowerCase(java.util.Locale.ROOT));}
    private static List<ReferenceItem> unavailable(String type,String reason){return List.of(new ReferenceItem("","",type,null,false,reason,Map.of("capability","UNAVAILABLE")));}
    private static boolean isUnavailable(List<ReferenceItem> values){return values.size()==1&&values.getFirst().id().isBlank()&&!values.getFirst().enabled();}
    private static String clean(String value){return value==null||value.isBlank()?null:value.trim();}
    private static String required(String value){if(value==null||value.isBlank())throw new IllegalArgumentException("referenceType is required");return value.trim();}
}
