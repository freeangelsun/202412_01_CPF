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
            // 실패 원인이 "상속을 안 썼다"인지 "다른 ClassLoader/다른 산출물이 로드됐다"인지
            // 구분하지 못하면 Source 가 정상인데도 원인을 찾을 수 없다. 판정 근거를 함께 남긴다.
            throw new IllegalStateException(role + " must extend an abstract Domain Base Class: "
                    + businessType.getName() + describeResolution(businessType, domainBaseType));
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

    /** 판정에 사용한 실제 type/superclass/ClassLoader/코드출처를 문자열로 남긴다. */
    private static String describeResolution(Class<?> businessType, Class<?> domainBaseType) {
        StringBuilder detail = new StringBuilder();
        detail.append(" [resolvedSuperclass=").append(domainBaseType == null ? "null" : domainBaseType.getName());
        detail.append(", businessClassLoader=").append(describeLoader(businessType));
        detail.append(", businessCodeSource=").append(describeCodeSource(businessType));
        detail.append(']');
        return detail.toString();
    }

    private static String describeLoader(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return loader == null ? "bootstrap" : loader.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(loader));
    }

    private static String describeCodeSource(Class<?> type) {
        try {
            java.security.ProtectionDomain domain = type.getProtectionDomain();
            if (domain == null || domain.getCodeSource() == null || domain.getCodeSource().getLocation() == null) {
                return "unknown";
            }
            return domain.getCodeSource().getLocation().toString();
            // 진단 정보 수집이 실패해도 원래의 3단 구조 위반 보고를 가려서는 안 된다.
            // SecurityManager/ClassLoader 제약으로 코드 출처를 못 읽는 경우를 값으로 표현한다.
        } catch (RuntimeException failure) {
            return "unavailable";
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
