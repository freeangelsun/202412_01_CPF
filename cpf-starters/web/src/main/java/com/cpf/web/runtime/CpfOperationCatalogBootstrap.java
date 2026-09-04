package com.cpf.web.runtime;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.context.CpfOperationOwnerResolver;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 실제 Handler를 스캔하여 Operation Catalog를 자동 현행화합니다.
 *
 * <p>동기화는 Web Server가 Listen을 시작하기 전(singleton 초기화 완료 시점)에 끝나야 한다.
 * ApplicationReadyEvent는 Tomcat이 이미 포트를 연 뒤에 발행되므로, 그 시점에 동기화하면
 * 기동 직후 도착한 첫 업무 거래가 아직 비어 있는 Catalog 때문에 OPERATION_NOT_REGISTERED로
 * 거절된다(운영 롤링 배포에서도 동일하게 재현된다). 따라서 {@link SmartInitializingSingleton}
 * 시점에 동기화하고, 실패하면 기동 자체를 fail-closed로 중단한다.</p>
 */
public final class CpfOperationCatalogBootstrap implements SmartInitializingSingleton {
    private final RequestMappingHandlerMapping mappings;
    private final CpfRuntimeIdentity runtime;
    private final Environment environment;
    private final List<CpfOperationCatalogRegistry> registries;
    private final CpfBusinessOperationManifestVerifier manifestVerifier;
    private final CpfOperationOwnerResolver operationOwners;

    public CpfOperationCatalogBootstrap(RequestMappingHandlerMapping mappings, CpfRuntimeIdentity runtime, Environment environment,
            List<CpfOperationCatalogRegistry> registries) {
        this(mappings, runtime, environment, registries, new CpfBusinessOperationManifestVerifier(),
                new CpfClasspathOperationOwnerResolver());
    }

    CpfOperationCatalogBootstrap(RequestMappingHandlerMapping mappings, CpfRuntimeIdentity runtime, Environment environment,
            List<CpfOperationCatalogRegistry> registries, CpfBusinessOperationManifestVerifier manifestVerifier) {
        this(mappings, runtime, environment, registries, manifestVerifier, new CpfClasspathOperationOwnerResolver());
    }

    CpfOperationCatalogBootstrap(RequestMappingHandlerMapping mappings, CpfRuntimeIdentity runtime, Environment environment,
            List<CpfOperationCatalogRegistry> registries, CpfBusinessOperationManifestVerifier manifestVerifier,
            CpfOperationOwnerResolver operationOwners) {
        this.mappings=mappings; this.runtime=runtime; this.environment=environment;
        this.registries=List.copyOf(registries); this.manifestVerifier=manifestVerifier;
        this.operationOwners=operationOwners;
    }

    @Override public void afterSingletonsInstantiated() {
        List<CpfOperationCatalogRegistry.Operation> operations=detect();
        boolean generatedBusinessDomain = first("cpf.generated-domain.name") != null;
        if (operations.isEmpty() && !generatedBusinessDomain) return;
        boolean manifestRequired = environment.getProperty(
                "cpf.operation-catalog.manifest-required", Boolean.class, generatedBusinessDomain);
        manifestVerifier.verify(operations, manifestRequired);
        // 빈 Generated Domain도 manifest 정합성은 검증하지만, 동기화할 업무 Operation이 없으면
        // 외부 Catalog Registry를 요구하지 않습니다. 실제 Operation이 하나라도 있으면 아래 fail-closed 계약을 유지합니다.
        if (operations.isEmpty()) return;
        boolean required=environment.getProperty("cpf.operation-catalog.required",Boolean.class,true);
        if (registries.size()!=1) {
            if(required) throw new IllegalStateException("CPF_OPERATION_CATALOG_REGISTRY_UNAVAILABLE:"+registries.size());
            return;
        }
        // 1-WAS topology는 System이 아니지만 그 안의 업무 Operation은 각 owner System으로
        // 분리 등록한다. Topology의 null SystemCode를 이유로 Catalog 동기화를 생략하면 안 된다.
        Map<String, List<CpfOperationCatalogRegistry.Operation>> bySystem = new LinkedHashMap<>();
        for (CpfOperationCatalogRegistry.Operation operation : operations) {
            bySystem.computeIfAbsent(operation.systemCode(), ignored -> new ArrayList<>()).add(operation);
        }
        for (Map.Entry<String, List<CpfOperationCatalogRegistry.Operation>> entry : bySystem.entrySet()) {
            List<CpfOperationCatalogRegistry.Operation> owned = List.copyOf(entry.getValue());
            CpfOperationCatalogRegistry.Operation first = owned.getFirst();
            registries.getFirst().synchronize(new CpfOperationCatalogRegistry.SyncRequest(
                    entry.getKey(), first.domainCode(), first.application(), runtime.instance(),
                    first("cpf.runtime.artifact-version", "info.build.version"),
                    first("cpf.runtime.artifact-commit", "git.commit.id"), owned));
        }
    }

    private List<CpfOperationCatalogRegistry.Operation> detect(){
        List<CpfOperationCatalogRegistry.Operation> out=new ArrayList<>();
        for(var entry:mappings.getHandlerMethods().entrySet()){
            HandlerMethod handler=entry.getValue();
            var method=handler.getMethod();
            CpfOnlineTransaction tx=AnnotatedElementUtils.findMergedAnnotation(method,CpfOnlineTransaction.class);
            if(tx==null) tx=AnnotatedElementUtils.findMergedAnnotation(handler.getBeanType(),CpfOnlineTransaction.class);
            if(tx==null) continue;
            String operationId=required(tx.operationId(),"operationId");
            Operation open=AnnotatedElementUtils.findMergedAnnotation(method,Operation.class);
            if(open!=null && open.operationId()!=null && !open.operationId().isBlank() && !operationId.equals(open.operationId().trim()))
                throw new IllegalStateException("CPF_OPERATION_ID_OPENAPI_MISMATCH:"+operationId+":"+open.operationId().trim());
            String httpMethod=entry.getKey().getMethodsCondition().getMethods().stream().map(value -> value.name()).sorted().findFirst().orElse("ANY");
            String path=entry.getKey().getPatternValues().stream().sorted().findFirst().orElse("/");
            CpfOperationOwnerResolver.CpfOperationOwner owner = operationOwners == null
                    ? null : operationOwners.resolve(handler, operationId);
            String owningSystem = owner == null ? runtime.systemCode() : owner.systemCode();
            if (owningSystem == null || owningSystem.isBlank()) {
                throw new IllegalStateException("CPF_OPERATION_OWNER_UNRESOLVED:" + operationId + ":"
                        + handler.getBeanType().getName());
            }
            String domain = owner == null ? first("cpf.domain","cpf.generated-domain.domain","cpf.framework.domain") : owner.domainCode();
            if(domain==null) domain=owningSystem;
            String application = owner == null || owner.application() == null ? runtime.application() : owner.application();
            String name=required(tx.name(),"name");
            String description=required(tx.description(),"description");
            String fp=fingerprint(operationId+"|"+name+"|"+description+"|"+httpMethod+"|"+path+"|"+handler.getBeanType().getName()+"|"+method.getName());
            out.add(new CpfOperationCatalogRegistry.Operation(operationId,name,description,
                    owningSystem,domain,application,httpMethod,path,handler.getBeanType().getName(),method.getName(),fp));
        }
        out.sort(Comparator.comparing(value -> value.operationId()));
        for(int i=1;i<out.size();i++) if(out.get(i-1).operationId().equals(out.get(i).operationId()))
            throw new IllegalStateException("CPF_DUPLICATE_OPERATION_ID:"+out.get(i).operationId());
        return List.copyOf(out);
    }
    private String first(String... keys){for(String k:keys){String v=environment.getProperty(k);if(v!=null&&!v.isBlank())return v.trim();}return null;}
    private static String required(String v,String field){if(v==null||v.isBlank())throw new IllegalStateException("CPF_ONLINE_TRANSACTION_"+field.toUpperCase(java.util.Locale.ROOT)+"_REQUIRED");return v.trim();}
    private static String fingerprint(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
