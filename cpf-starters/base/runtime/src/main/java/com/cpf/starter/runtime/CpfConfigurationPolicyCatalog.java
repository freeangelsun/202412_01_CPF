package com.cpf.starter.runtime;

import com.cpf.core.api.config.CpfConfigCatalog;
import com.cpf.core.api.config.CpfConfigDescriptor;
import com.cpf.core.api.config.CpfConfigPolicy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * 실제 등록된 Bean의 {@link CpfConfigPolicy}를 수집하여 Runtime 설정 정책 Catalog를 제공합니다.
 *
 * <p>설정 정책 계약의 정본은 {@code cpf-core} Public API이며, 동일 prefix에 서로 다른 정책이
 * 등록되면 기동 단계에서 즉시 실패하여 환경별 설정 drift를 숨기지 않습니다.</p>
 */
public final class CpfConfigurationPolicyCatalog implements CpfConfigCatalog, SmartInitializingSingleton {

    private final ApplicationContext context;
    private volatile Map<String, CpfConfigDescriptor> descriptors = Map.of();

    public CpfConfigurationPolicyCatalog(ApplicationContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, CpfConfigDescriptor> found = new TreeMap<>();
        for (String name : context.getBeanDefinitionNames()) {
            Class<?> type = context.getType(name);
            if (type == null) {
                continue;
            }
            CpfConfigPolicy policy = AnnotatedElementUtils.findMergedAnnotation(type, CpfConfigPolicy.class);
            if (policy == null) {
                continue;
            }
            CpfConfigDescriptor descriptor = new CpfConfigDescriptor(
                    policy.prefix(),
                    policy.mutability(),
                    policy.secretSeparated(),
                    type.getName());
            CpfConfigDescriptor previous = found.putIfAbsent(policy.prefix(), descriptor);
            if (previous != null && !previous.equals(descriptor)) {
                throw new IllegalStateException(
                        "Conflicting CPF config policy for prefix " + policy.prefix());
            }
        }
        descriptors = Map.copyOf(found);
    }

    @Override
    public List<CpfConfigDescriptor> descriptors() {
        return descriptors.values().stream().toList();
    }

    @Override
    public Optional<CpfConfigDescriptor> find(String prefix) {
        return Optional.ofNullable(descriptors.get(prefix));
    }
}
