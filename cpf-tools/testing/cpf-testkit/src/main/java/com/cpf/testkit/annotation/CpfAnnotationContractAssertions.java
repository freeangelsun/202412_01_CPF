package com.cpf.testkit.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/** CPF Developer Annotation Golden Path 검증에 공통으로 사용하는 Testkit assertion입니다. */
public final class CpfAnnotationContractAssertions {
    private CpfAnnotationContractAssertions() { }

    public static <A extends Annotation> A requireAnnotation(AnnotatedElement element, Class<A> type) {
        if (element == null) throw new IllegalArgumentException("element is required");
        if (type == null) throw new IllegalArgumentException("annotation type is required");
        A annotation = element.getAnnotation(type);
        if (annotation == null) throw new AssertionError(type.getName() + " is required on " + element);
        return annotation;
    }

    public static void requireRuntimeRetention(Class<? extends Annotation> type) {
        java.lang.annotation.Retention retention = type.getAnnotation(java.lang.annotation.Retention.class);
        if (retention == null || retention.value() != java.lang.annotation.RetentionPolicy.RUNTIME) {
            throw new AssertionError(type.getName() + " must use RUNTIME retention");
        }
    }
}
