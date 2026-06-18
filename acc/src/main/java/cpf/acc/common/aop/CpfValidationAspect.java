package cpf.acc.common.aop;

import cpf.cmn.dto.CpfDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * ACC Controller 요청 DTO를 검증하는 AOP입니다.
 * 단순 query parameter, path variable, servlet 객체는 검증 대상에서 제외하고 CPF 표준 DTO만 검증합니다.
 */
@Aspect
@Component
public class CpfValidationAspect {

    private final Validator validator;

    public CpfValidationAspect(Validator validator) {
        this.validator = validator;
    }

    /**
     * Controller 인자 중 {@link CpfDTO}만 헤더와 데이터, Bean Validation 기준으로 검증합니다.
     *
     * @param joinPoint Controller 호출 지점
     * @return Controller 실행 결과
     * @throws Throwable Controller 실행 중 발생한 예외
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object validateCpfRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof CpfDTO<?> cpfDTO) {
                validateCpfDto(cpfDTO);
            }
        }
        return joinPoint.proceed();
    }

    private void validateCpfDto(CpfDTO<?> cpfDTO) {
        if (cpfDTO.getHeader() == null || cpfDTO.getData() == null) {
            throw new IllegalArgumentException("CPF 표준 요청은 header와 data가 필요합니다.");
        }

        Set<ConstraintViolation<CpfDTO<?>>> violations = validator.validate(cpfDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("CPF 표준 요청 검증에 실패했습니다.", violations);
        }
    }
}
