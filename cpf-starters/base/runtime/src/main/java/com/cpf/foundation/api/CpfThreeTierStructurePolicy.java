package com.cpf.foundation.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * CPF Framework Base -> Domain Base -> Business 구현의 정확한 3단 상속구조를 검증합니다.
 *
 * <p>Business가 Framework Base를 직접 상속하는 2단 구조, 중간 계층을 더 끼운 4단 이상 구조,
 * concrete Domain Base, 기능이 전혀 없는 형식적 Domain Base를 시작 단계에서 차단합니다.</p>
 */
public final class CpfThreeTierStructurePolicy {
    private CpfThreeTierStructurePolicy() { }

    public static void verifyBusinessType(Class<?> businessType, Class<?> frameworkBaseType, String roleName) {
        if (businessType == null || frameworkBaseType == null) {
            throw new IllegalArgumentException("businessType/frameworkBaseType must not be null");
        }
        String role = roleName == null || roleName.isBlank() ? "CPF managed type" : roleName.trim();
        if (!Modifier.isAbstract(frameworkBaseType.getModifiers())) {
            throw new IllegalStateException("CPF Framework Base must be abstract: " + frameworkBaseType.getName());
        }
        if (businessType.isInterface() || Modifier.isAbstract(businessType.getModifiers())) {
            throw new IllegalStateException(role + " Business type must be concrete: " + businessType.getName());
        }

        Class<?> domainBaseType = businessType.getSuperclass();
        if (domainBaseType == null || domainBaseType == Object.class) {
            throw new IllegalStateException(role + " must extend an abstract Domain Base Class: " + businessType.getName());
        }
        if (domainBaseType == frameworkBaseType) {
            throw new IllegalStateException(role + " must not directly extend Framework Base (2-tier is forbidden): " + businessType.getName());
        }
        if (!Modifier.isAbstract(domainBaseType.getModifiers())) {
            throw new IllegalStateException(role + " Domain Base must be abstract: " + domainBaseType.getName());
        }
        if (domainBaseType.getSuperclass() != frameworkBaseType) {
            throw new IllegalStateException(role + " must use exactly Framework Base -> Domain Base -> Business (4-tier/alternate base is forbidden): "
                    + businessType.getName());
        }
        if (!hasMeaningfulDomainContract(domainBaseType)) {
            throw new IllegalStateException(role + " Domain Base must declare real common behavior/hook/state: " + domainBaseType.getName());
        }
    }

    private static boolean hasMeaningfulDomainContract(Class<?> domainBaseType) {
        for (Method method : domainBaseType.getDeclaredMethods()) {
            if (!method.isSynthetic() && !method.isBridge()) {
                return true;
            }
        }
        for (Field field : domainBaseType.getDeclaredFields()) {
            if (!field.isSynthetic()) {
                return true;
            }
        }
        return false;
    }
}
