package cpf.pfw.common.execution;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 애플리케이션 시작 시 표준 실행 annotation을 전수 탐색해 catalog에 등록합니다.
 */
public final class CpfExecutionCatalogScanner implements SmartInitializingSingleton {
    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final CpfExecutionCatalogPort catalogPort;

    public CpfExecutionCatalogScanner(
            ApplicationContext applicationContext,
            Environment environment,
            CpfExecutionCatalogPort catalogPort) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.catalogPort = catalogPort;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<CpfExecutionDefinition> definitions = new ArrayList<>();
        String sourceModule = environment.getProperty("cpf.framework.module-id", "UNKNOWN").toUpperCase();
        String sourceVersion = environment.getProperty("cpf.framework.source-version", "local");
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> beanType;
            try {
                beanType = AopUtils.getTargetClass(applicationContext.getBean(beanName));
            } catch (RuntimeException ex) {
                continue;
            }
            if (beanType == null || beanType.getName().startsWith("org.springframework")) {
                continue;
            }
            ReflectionUtils.doWithMethods(beanType, method -> collect(definitions, sourceModule, sourceVersion, beanType, method));
        }
        catalogPort.upsertAll(rejectDuplicateIds(definitions));
    }

    private List<CpfExecutionDefinition> rejectDuplicateIds(List<CpfExecutionDefinition> definitions) {
        Map<String, CpfExecutionDefinition> unique = new LinkedHashMap<>();
        for (CpfExecutionDefinition definition : definitions) {
            CpfExecutionDefinition existing = unique.putIfAbsent(definition.standardExecutionId(), definition);
            if (existing != null
                    && (!existing.sourceClass().equals(definition.sourceClass())
                    || !existing.sourceMethod().equals(definition.sourceMethod()))) {
                throw new IllegalStateException("표준 실행 ID가 서로 다른 source에 중복 선언되었습니다. id="
                        + definition.standardExecutionId()
                        + ", first=" + existing.sourceClass() + "#" + existing.sourceMethod()
                        + ", second=" + definition.sourceClass() + "#" + definition.sourceMethod());
            }
        }
        return List.copyOf(unique.values());
    }

    private void collect(
            List<CpfExecutionDefinition> definitions,
            String sourceModule,
            String sourceVersion,
            Class<?> beanType,
            Method method) {
        CpfOnlineTransaction online = AnnotatedElementUtils.findMergedAnnotation(method, CpfOnlineTransaction.class);
        if (online == null) {
            online = AnnotatedElementUtils.findMergedAnnotation(beanType, CpfOnlineTransaction.class);
        }
        if (online != null) {
            definitions.add(definition(
                    online.id(), online.name(), CpfExecutionType.ONLINE, online.ownerDomain(),
                    sourceModule, sourceVersion, beanType, method));
        }
        CpfBatchJob batch = AnnotatedElementUtils.findMergedAnnotation(method, CpfBatchJob.class);
        if (batch == null) {
            batch = AnnotatedElementUtils.findMergedAnnotation(beanType, CpfBatchJob.class);
        }
        if (batch != null) {
            definitions.add(definition(
                    batch.id(), batch.name(), CpfExecutionType.BATCH, batch.ownerDomain(),
                    sourceModule, sourceVersion, beanType, method));
        }
    }

    private CpfExecutionDefinition definition(
            String id,
            String name,
            CpfExecutionType type,
            String ownerDomain,
            String sourceModule,
            String sourceVersion,
            Class<?> beanType,
            Method method) {
        CpfStandardExecutionId parsed = CpfStandardExecutionId.parse(id);
        String resolvedOwner = ownerDomain == null || ownerDomain.isBlank() ? parsed.domain() : ownerDomain;
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        RequestMapping typeMapping = AnnotatedElementUtils.findMergedAnnotation(beanType, RequestMapping.class);
        String endpoint = combinePaths(firstPath(typeMapping), firstPath(mapping));
        Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
        String operationId = operation == null ? "" : operation.operationId();
        return new CpfExecutionDefinition(
                parsed.value(), name, type, resolvedOwner, sourceModule,
                beanType.getName(), method.getName(), endpoint, operationId, sourceVersion, Instant.now());
    }

    private String firstPath(RequestMapping mapping) {
        if (mapping == null) {
            return "";
        }
        String[] paths = mapping.path().length == 0 ? mapping.value() : mapping.path();
        return paths.length == 0 ? "" : paths[0];
    }

    private String combinePaths(String typePath, String methodPath) {
        String combined = ("/" + typePath + "/" + methodPath).replaceAll("/{2,}", "/");
        return combined.length() > 1 && combined.endsWith("/")
                ? combined.substring(0, combined.length() - 1)
                : combined;
    }
}
