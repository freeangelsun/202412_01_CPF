package com.cpf.foundation.validation;
import java.util.Collection;
public final class CpfValidation {
    private CpfValidation() {}
    public static String requireText(String value,String name) { if(value==null||value.isBlank()) throw new IllegalArgumentException(name+" is required"); return value.trim(); }
    public static <T extends Collection<?>> T requireNotEmpty(T value,String name) { if(value==null||value.isEmpty()) throw new IllegalArgumentException(name+" must not be empty"); return value; }
    public static void require(boolean condition,String message) { if(!condition) throw new IllegalArgumentException(message); }
}
