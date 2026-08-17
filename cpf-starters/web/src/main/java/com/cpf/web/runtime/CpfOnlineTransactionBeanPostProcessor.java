package com.cpf.web.runtime;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/** @CpfOnlineTransaction 선언 오류와 중복 operationId를 Application startup에서 차단합니다. */
public final class CpfOnlineTransactionBeanPostProcessor implements BeanPostProcessor {
    private final Map<String, String> operationIds = new ConcurrentHashMap<>();
    @Override public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> type = ClassUtils.getUserClass(bean);
        CpfOnlineTransaction classRule = AnnotatedElementUtils.findMergedAnnotation(type, CpfOnlineTransaction.class);
        if (classRule != null) register(classRule, type.getName());
        ReflectionUtils.doWithMethods(type, method -> inspect(type, method));
        return bean;
    }
    private void inspect(Class<?> type, Method method) {
        CpfOnlineTransaction rule = AnnotatedElementUtils.findMergedAnnotation(method, CpfOnlineTransaction.class);
        if (rule != null) register(rule, type.getName() + "#" + method.getName());
    }
    private void register(CpfOnlineTransaction rule, String source) {
        CpfOnlineTransactionMetadataValidator.validate(rule, source);
        String previous = operationIds.putIfAbsent(rule.operationId(), source);
        if (previous != null && !previous.equals(source)) {
            throw new IllegalStateException("CPF_OPERATION_ID_DUPLICATE:" + rule.operationId() + ":" + previous + ":" + source);
        }
    }
}
