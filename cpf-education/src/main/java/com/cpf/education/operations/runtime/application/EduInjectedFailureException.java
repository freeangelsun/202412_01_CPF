package com.cpf.education.operations.runtime.application;
import com.cpf.education.operations.runtime.model.EduFailurePoint;
/** EduInjectedFailureException 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EduInjectedFailureException extends RuntimeException {
    private final EduFailurePoint point;
    public EduInjectedFailureException(EduFailurePoint point){super("Injected failure: "+point);this.point=point;}
    public EduFailurePoint point(){return point;}
}
