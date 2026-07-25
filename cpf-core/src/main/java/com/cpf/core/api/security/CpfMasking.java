package com.cpf.core.api.security;

import com.cpf.core.common.logging.SensitiveDataMasker;

/** 로그/화면에 민감정보를 안전하게 표시하기 위한 공개 masking utility입니다. */
public final class CpfMasking {
    private CpfMasking() { }
    public static String mask(String value) { return SensitiveDataMasker.mask(value); }
    public static String mask(String value, int maxLength) { return SensitiveDataMasker.mask(value, maxLength); }
    public static String truncate(String value, int maxLength) { return SensitiveDataMasker.truncate(value, maxLength); }
    public static String name(String value) {
        if (value == null || value.isBlank()) return value;
        String v=value.trim(); if(v.length()==1) return "*"; if(v.length()==2) return v.substring(0,1)+"*";
        return v.substring(0,1)+"*".repeat(Math.max(1,v.length()-2))+v.substring(v.length()-1);
    }
    public static String email(String value) {
        if (value == null || value.isBlank()) return value;
        int at=value.indexOf('@'); if(at<=0) return mask(value);
        String local=value.substring(0,at); String domain=value.substring(at);
        return (local.length() <= 2 ? local.substring(0,1)+"*" : local.substring(0,2)+"***") + domain;
    }
    public static String mobile(String value) {
        if (value == null || value.isBlank()) return value;
        String d=value.replaceAll("[^0-9]", "");
        if(d.length()<7) return mask(value);
        return d.substring(0,3)+"****"+d.substring(d.length()-4);
    }
    /** 기존 MaskingUtils 이름과 동일한 공개 alias입니다. */
    public static String maskSensitive(String value) { return mask(value); }
    public static String maskName(String value) { return name(value); }
    public static String maskEmail(String value) { return email(value); }
    public static String maskMobile(String value) { return mobile(value); }
}

