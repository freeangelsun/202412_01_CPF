package com.cpf.education.common.foundation;
import com.cpf.foundation.validation.CpfValidation;
import java.util.Map;

/** Shows CPF policy utility plus deliberate native-JDK escape rather than generic wrapper proliferation. */
/** CpfFoundationUtilityEducation 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class CpfFoundationUtilityEducation {
    public String requiredBusinessKey(String value){return CpfValidation.requireText(value,"businessKey");}
    public Map<String,String> nativeImmutableCopy(Map<String,String> source){return Map.copyOf(source);}
}
