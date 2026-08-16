package com.cpf.messaging.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.messaging.api.CpfBrokerBridgeHandler;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfMessageListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

/** @CpfMessageListener method를 Provider-neutral Broker Bridge에 등록합니다. */
public final class CpfMessageListenerRegistrar {
    private final ApplicationContext context;
    private final CpfBrokerBridgePort bridge;

    public CpfMessageListenerRegistrar(ApplicationContext context, CpfBrokerBridgePort bridge) {
        this.context = context; this.bridge = bridge;
    }

    public int registerAll() {
        Set<String> identities = new HashSet<>();
        int count = 0;
        for (Object bean : context.getBeansOfType(Object.class).values()) {
            Class<?> type = AopUtils.getTargetClass(bean);
            for (Method method : type.getMethods()) {
                CpfMessageListener annotation = method.getAnnotation(CpfMessageListener.class);
                if (annotation == null) continue;
                validate(method, annotation);
                String destination = annotation.destination().trim();
                String group = annotation.consumerGroup().isBlank() ? type.getName() + "." + method.getName() : annotation.consumerGroup().trim();
                String identity = destination + "|" + group;
                if (!identities.add(identity)) throw new IllegalStateException("CPF_MESSAGE_LISTENER_DUPLICATE:" + identity);
                bridge.subscribe(destination, new AnnotatedHandler(bean, method, group, annotation));
                count++;
            }
        }
        return count;
    }

    private static void validate(Method method, CpfMessageListener annotation) {
        if (!Modifier.isPublic(method.getModifiers())) throw new IllegalStateException("CPF_MESSAGE_LISTENER_NOT_PUBLIC:" + method);
        if (!method.trySetAccessible()) throw new IllegalStateException("CPF_MESSAGE_LISTENER_NOT_ACCESSIBLE:" + method);
        if (annotation.destination().isBlank()) throw new IllegalStateException("CPF_MESSAGE_LISTENER_DESTINATION:" + method);
        if (method.getParameterCount() > 1 || (method.getParameterCount() == 1 && method.getParameterTypes()[0] != CpfBrokerBridgeMessage.class))
            throw new IllegalStateException("CPF_MESSAGE_LISTENER_SIGNATURE:" + method);
        if (method.getReturnType() != Void.TYPE) throw new IllegalStateException("CPF_MESSAGE_LISTENER_RETURN_TYPE:" + method);
    }

    private record AnnotatedHandler(Object bean, Method method, String consumerGroup, CpfMessageListener policy) implements CpfBrokerBridgeHandler {
        @Override public void handle(CpfBrokerBridgeMessage message) {
            if (policy.contextRequired()) CpfContexts.requireCurrent();
            if (policy.idempotencyRequired()) {
                CpfContext current = CpfContexts.requireCurrent();
                if (current.operation() == null || current.operation().idempotencyKey() == null || current.operation().idempotencyKey().isBlank())
                    throw new IllegalStateException("CPF_MESSAGE_LISTENER_IDEMPOTENCY_REQUIRED:" + method);
            }
            try {
                if (method.getParameterCount() == 0) method.invoke(bean); else method.invoke(bean, message);
            } catch (InvocationTargetException failure) {
                Throwable cause = failure.getCause();
                if (cause instanceof RuntimeException runtime) throw runtime;
                if (cause instanceof Error error) throw error;
                throw new IllegalStateException("CPF_MESSAGE_LISTENER_FAILED:" + method, cause);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("CPF_MESSAGE_LISTENER_INVOCATION:" + method, failure);
            }
        }
    }
}
