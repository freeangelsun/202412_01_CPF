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
 * AOP瑜??ъ슜?섏뿬 ⑤뱺 而⑦듃濡ㅻ윭 ?붿껌?먯꽌 CpfDTO 洹쒓꺽??寃利앺빀?덈떎.
 * ?붿껌??CpfDTO 洹쒓꺽??留욎? ?딆쑝硫??덉쇅瑜?諛쒖깮?쒗궢?덈떎.
 */
@Aspect
@Component
public class CpfValidationAspect {

    private final Validator validator;

    /**
     * ?앹꽦??
     *
     * @param validator Validator ?몄뒪?댁뒪
     */
    public CpfValidationAspect(Validator validator) {
        this.validator = validator;
    }

    /**
     * ⑤뱺 而⑦듃濡ㅻ윭 ?붿껌?먯꽌 CpfDTO瑜?寃利?
     *
     * @param joinPoint AOP 議곗씤?ъ씤??
     * @return 硫붿꽌???ㅽ뻾 寃곌낵
     * @throws Throwable 寃利??ㅽ뙣 ???덉쇅 諛쒖깮
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object validateCpfRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (!(arg instanceof CpfDTO)) {
                throw new IllegalArgumentException("?붿껌? 諛섎뱶??CpfDTO ?뺤떇?댁뼱???⑸땲??");
            }

            CpfDTO<?> cpfDTO = (CpfDTO<?>) arg;

            // Header? Data ?꾨뱶 寃利?
            if (cpfDTO.getHeader() == null || cpfDTO.getData() == null) {
                throw new IllegalArgumentException("CpfDTO 援ъ“媛 ?щ컮瑜댁? ?딆뒿?덈떎. Header ?먮뒗 Data媛 ?꾨씫?섏뿀?듬땲??");
            }

            // Bean Validation ?섑뻾
            Set<ConstraintViolation<CpfDTO<?>>> violations = validator.validate(cpfDTO);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException("CpfDTO ?좏슚??寃利??ㅽ뙣", violations);
            }
        }

        return joinPoint.proceed();
    }
}

