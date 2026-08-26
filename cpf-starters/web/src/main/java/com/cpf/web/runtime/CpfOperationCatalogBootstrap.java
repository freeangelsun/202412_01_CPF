package com.cpf.web.runtime;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import com.cpf.web.context.CpfRuntimeIdentity;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** WAS READY 직전에 실제 Handler를 스캔하여 Operation Catalog를 자동 현행화합니다. */
public final class CpfOperationCatalogBootstrap implements ApplicationListener<ApplicationReadyEvent> {
    private final RequestMappingHandlerMapping mappings;
    private final CpfRuntimeIdentity runtime;
    private final Environment environment;
    private final List<CpfOperationCatalogRegistry> registries;
    private final CpfBusinessOperationManifestVerifier manifestVerifier;

    public CpfOperationCatalogBootstrap(RequestMappingHandlerMapping mappings, CpfRuntimeIdentity runtime, Environment environment,
            List<CpfOperationCatalogRegistry> registries) {
        this(mappings, runtime, environment, registries, new CpfBusinessOperationManifestVerifier());
    }

    CpfOperationCatalogBootstrap(RequestMappingHandlerMapping mappings, CpfRuntimeIdentity runtime, Environment environment,
            List<CpfOperationCatalogRegistry> registries, CpfBusinessOperationManifestVerifier manifestVerifier) {
        this.mappings=mappings; this.runtime=runtime; this.environment=environment;
        this.registries=List.copyOf(registries); this.manifestVerifier=manifestVerifier;
    }

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
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
        String domain=first("cpf.domain","cpf.generated-domain.domain","cpf.framework.domain");
        if(domain==null) domain=runtime.systemCode();
        registries.getFirst().synchronize(new CpfOperationCatalogRegistry.SyncRequest(
                runtime.systemCode(), domain, runtime.application(), runtime.instance(),
                first("cpf.runtime.artifact-version", "info.build.version"),
                first("cpf.runtime.artifact-commit", "git.commit.id"), operations));
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
            String domain=first("cpf.domain","cpf.generated-domain.domain","cpf.framework.domain"); if(domain==null) domain=runtime.systemCode();
            String name=required(tx.name(),"name");
            String description=required(tx.description(),"description");
            String fp=fingerprint(operationId+"|"+name+"|"+description+"|"+httpMethod+"|"+path+"|"+handler.getBeanType().getName()+"|"+method.getName());
            out.add(new CpfOperationCatalogRegistry.Operation(operationId,name,description,
                    runtime.systemCode(),domain,runtime.application(),httpMethod,path,handler.getBeanType().getName(),method.getName(),fp));
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
