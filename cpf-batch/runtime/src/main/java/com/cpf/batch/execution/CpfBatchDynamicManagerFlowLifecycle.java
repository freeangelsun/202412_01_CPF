package com.cpf.batch.execution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

/**
 * Runtime에 동적으로 materialize되는 Manager Job의 Integration Flow를 소유합니다.
 * 등록·기동·실패 rollback·cache eviction 정리를 하나의 경계에서 수행합니다.
 */
final class CpfBatchDynamicManagerFlowLifecycle implements AutoCloseable {
    private static final Object REGISTRATION_MONITOR = new Object();

    private final BeanFactory beanFactory;
    private final IntegrationFlowContext flowContext;
    private final Map<String, List<String>> flowIdsByOwner = new LinkedHashMap<>();

    CpfBatchDynamicManagerFlowLifecycle(BeanFactory beanFactory, IntegrationFlowContext flowContext) {
        this.beanFactory = beanFactory;
        this.flowContext = flowContext;
    }

    BeanFactory beanFactory() {
        return beanFactory;
    }

    <T> T materialize(String ownerId, int expectedFlowCount, Supplier<T> materializer) {
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("ownerId is required");
        if (expectedFlowCount < 0) throw new IllegalArgumentException("expectedFlowCount must not be negative");
        synchronized (REGISTRATION_MONITOR) {
            if (flowIdsByOwner.containsKey(ownerId)) {
                throw new IllegalStateException("BATCH_DYNAMIC_MANAGER_FLOW_OWNER_DUPLICATE:" + ownerId);
            }
            Set<String> before = Set.copyOf(flowContext.getRegistry().keySet());
            try {
                T result = materializer.get();
                List<String> created = createdSince(before);
                if (created.size() != expectedFlowCount) {
                    destroy(created);
                    throw new IllegalStateException("BATCH_DYNAMIC_MANAGER_FLOW_COUNT_MISMATCH:expected="
                            + expectedFlowCount + ":actual=" + created.size());
                }
                for (String flowId : created) {
                    IntegrationFlowContext.IntegrationFlowRegistration registration =
                            flowContext.getRegistrationById(flowId);
                    if (registration == null) {
                        destroy(created);
                        throw new IllegalStateException("BATCH_DYNAMIC_MANAGER_FLOW_REGISTRATION_MISSING:" + flowId);
                    }
                    registration.start();
                }
                if (!created.isEmpty()) flowIdsByOwner.put(ownerId, List.copyOf(created));
                return result;
            } catch (RuntimeException | Error failure) {
                destroy(createdSince(before));
                throw failure;
            }
        }
    }

    void release(String ownerId) {
        synchronized (REGISTRATION_MONITOR) {
            List<String> flowIds = flowIdsByOwner.remove(ownerId);
            if (flowIds != null) destroy(flowIds);
        }
    }

    int ownedFlowCount() {
        synchronized (REGISTRATION_MONITOR) {
            return flowIdsByOwner.values().stream().mapToInt(List::size).sum();
        }
    }

    @Override
    public void close() {
        synchronized (REGISTRATION_MONITOR) {
            List<String> all = flowIdsByOwner.values().stream().flatMap(List::stream).toList();
            flowIdsByOwner.clear();
            destroy(all);
        }
    }

    private List<String> createdSince(Set<String> before) {
        Set<String> created = new LinkedHashSet<>(flowContext.getRegistry().keySet());
        created.removeAll(before);
        return new ArrayList<>(created);
    }

    private void destroy(List<String> flowIds) {
        RuntimeException first = null;
        for (int index = flowIds.size() - 1; index >= 0; index--) {
            String flowId = flowIds.get(index);
            IntegrationFlowContext.IntegrationFlowRegistration registration =
                    flowContext.getRegistrationById(flowId);
            if (registration == null) continue;
            try {
                registration.destroy();
            } catch (RuntimeException failure) {
                if (first == null) first = failure;
                else first.addSuppressed(failure);
            }
        }
        if (first != null) throw first;
    }
}
