package com.cpf.web.runtime;

import com.cpf.data.api.CpfDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/** @CpfRestController 경계의 @CpfDto argument를 자동 Bean Validation 합니다. */
@Aspect
public final class CpfDtoValidationAspect {
    private final Validator validator;
    private final CpfDtoValidationProperties properties;
    public CpfDtoValidationAspect(Validator validator,CpfDtoValidationProperties properties){this.validator=validator;this.properties=properties;}

    @Around("@within(com.cpf.web.api.CpfRestController)")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        if(properties.isEnabled()){
            Set<ConstraintViolation<Object>> violations=new LinkedHashSet<>();
            for(Object arg:jp.getArgs()){
                if(arg==null)continue;
                CpfDto dto=arg.getClass().getAnnotation(CpfDto.class);
                if(dto==null)continue;
                if(dto.contractVersion().isBlank())throw new IllegalStateException("CPF_DTO_CONTRACT_VERSION_REQUIRED:"+arg.getClass().getName());
                if(dto.validationRequired())violations.addAll(validator.validate(arg));
            }
            if(!violations.isEmpty())throw new ConstraintViolationException("CPF DTO validation failed",violations);
        }
        return jp.proceed();
    }
}
